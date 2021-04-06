package coding.universe.showmoretextview

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat


class ShowMoreTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    enum class TrimMode {
        TRIM_LINES,
        TRIM_LENGTH
    }

    private var textString: CharSequence? = null
    private var maxLinesVisible = DEFAULT_MAX_LINE
    private var collapsedText: CharSequence? = null
    private var expandedText: CharSequence? = null
    private var endCharacterIndex = 0
    private val colorClickableText = 0
    private var showFullExpandedText = false
    private var readMore = true
    private val ELLIPSIZE = "..."
    private var showMoreSpanColor : Int = 0
    private var showLessSpanColor : Int = 0

    private val viewMoreSpan by lazy { ShowMoreClickableSpan() }


    companion object {
        const val DEFAULT_INDEX = -1
        const val DEFAULT_MAX_LINE = -1
    }


    private fun setText() {
        super.setText(getDisplayableText())
        movementMethod = LinkMovementMethod.getInstance();
        highlightColor = Color.TRANSPARENT;
    }

    private fun getDisplayableText(): CharSequence? {
        return getTrimmedText(textString)
    }

    /**
     *  get Collapsed text if readMore is true
     *  get Expanded text if readMore is false
     */
    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        return if (readMore) {
            getCollapsedText(text)
        } else {
            getExpandedText(text)
        }
    }


    /**
     * return a char sequence of text string upto maxLinesVisible
     *
     * if lastIndexOfText = -1 then return whole text without appending Show More
     */
    private fun getCollapsedText(text: CharSequence?): CharSequence {

        if (endCharacterIndex == DEFAULT_INDEX) {
            return text ?: ""
        }else {
            val trimEndIndex: Int = endCharacterIndex - (ELLIPSIZE.length + (collapsedText?.length ?: 0) + 1)


            val stringBuilder = SpannableStringBuilder(text, 0, trimEndIndex)
                .append(ELLIPSIZE)
                .append(collapsedText ?: "")

//            val fcs = ForegroundColorSpan(showMoreSpanColor)
//            Log.d(TAG, "getCollapsedText: color -> $fcs")
//
//            stringBuilder.setSpan(
//                fcs,
//                stringBuilder.length - (collapsedText?.length ?: 0),
//                stringBuilder.length,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )

            return addClickableSpan(stringBuilder, collapsedText ?: "")
        }

    }

    private fun getExpandedText(text: CharSequence?): CharSequence? {
        if (!readMore) {
            val stringBuilder = SpannableStringBuilder(text, 0, text?.length ?: 0)
                .append(ELLIPSIZE)
                .append(expandedText ?: "")

//            val fcs = ForegroundColorSpan(showLessSpanColor)
//
//            stringBuilder.setSpan(
//                fcs,
//                stringBuilder.length - (expandedText?.length ?: 0),
//                stringBuilder.length,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
            return addClickableSpan(stringBuilder, expandedText ?: "")
        }

        return text
    }

    private fun addClickableSpan(s: SpannableStringBuilder, trimText: CharSequence): CharSequence {
        s.setSpan(
            viewMoreSpan,
            s.length - trimText.length,
            s.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return s
    }

    inner class ShowMoreClickableSpan : ClickableSpan() {
        override fun onClick(widget: View) {
            readMore = !readMore
            setText()
        }

        override fun updateDrawState(ds: TextPaint) {
            if (readMore){
                ds.color = showMoreSpanColor
            }else{
                ds.color = showLessSpanColor
            }
        }
    }

    private fun initializingViewTreeObserver() {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
                calculateLastIndex()
                setText()
            }
        })
    }

    private fun calculateLastIndex() {
        try {
            endCharacterIndex = when (maxLinesVisible) {
                0 -> {
                    layout.getLineEnd(0)
                }
                in 1 until lineCount -> {
                    showFullExpandedText = false
                    layout.getLineEnd(maxLinesVisible - 1)
                }
                else -> {
                    showFullExpandedText = true
                    DEFAULT_INDEX
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ShowMoreTextView)

        collapsedText =
            typedArray.getString(
                R.styleable.ShowMoreTextView_collapsedText
            )

        expandedText =
            typedArray.getString(
                R.styleable.ShowMoreTextView_expandedText
            )

        /*
            Set default string if collapsed text is null i.e. "Show more"
         */
        if (collapsedText == null) {
            collapsedText = resources.getString(R.string.show_more)
        }

        /*
            Set default string if expanded text is null i.e. "Show less"
         */
        if (expandedText == null) {
            expandedText = resources.getString(R.string.show_less)
        }

        maxLinesVisible = typedArray.getInt(
            R.styleable.ShowMoreTextView_maxLinesVisible,
            DEFAULT_MAX_LINE
        )

        showMoreSpanColor = typedArray.getColor(
            R.styleable.ShowMoreTextView_showMoreSpanColor,
            ContextCompat.getColor(context, R.color.default_show_more)
        )

        showLessSpanColor = typedArray.getColor(
            R.styleable.ShowMoreTextView_showLessSpanColor,
            ContextCompat.getColor(context, R.color.default_show_less)
        )


        textString = text
        
        initializingViewTreeObserver()
        typedArray.recycle()
    }

}