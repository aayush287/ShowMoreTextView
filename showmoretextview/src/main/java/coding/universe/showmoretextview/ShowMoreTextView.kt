package coding.universe.showmoretextview

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener


class ShowMoreTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    enum class TrimMode {
        TRIM_LINES,
        TRIM_LENGTH
    }

    private var textString: CharSequence? = null
    private var endCharacterIndex = 0
    private var maxLinesVisible = DEFAULT_MAX_LINE
    private var collapsedText: CharSequence? = null
    private var expandedText: CharSequence? = null
    private var lastIndexOfText = 0
    private val colorClickableText = 0
    private var showFullExpandedText = false
    private val viewMoreSpan by lazy { ShowMoreClickableSpan() }

    private var readMore = true

    private var bufferType: BufferType? = null

    private val ELLIPSIZE = "..."

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

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        return if (readMore) {
            getCollapsedText(text)
        } else {
            getExpandedText(text)
        }
    }


    private fun getCollapsedText(text: CharSequence?): CharSequence {

        val trimEndIndex: Int = if (lastIndexOfText == DEFAULT_INDEX) {
            text?.length ?: 0
        } else {
            lastIndexOfText - (ELLIPSIZE.length +
                    (collapsedText?.length ?: 0) + 1)
        }

        val stringBuilder = SpannableStringBuilder(text, 0, trimEndIndex)
            .append(ELLIPSIZE)
            .append(collapsedText ?: "")

        val fcs = ForegroundColorSpan(Color.parseColor("#4287f5"))

        stringBuilder.setSpan(
            fcs,
            stringBuilder.length - (collapsedText?.length ?: 0),
            stringBuilder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return addClickableSpan(stringBuilder, collapsedText ?: "")
    }

    private fun getExpandedText(text: CharSequence?): CharSequence? {
        if (!readMore) {
            val stringBuilder = SpannableStringBuilder(text, 0, text?.length ?: 0)
                .append(ELLIPSIZE)
                .append(expandedText ?: "")

            val fcs = ForegroundColorSpan(Color.parseColor("#4287f5"))

            stringBuilder.setSpan(
                fcs,
                stringBuilder.length - (expandedText?.length ?: 0),
                stringBuilder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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
            lastIndexOfText = when (maxLinesVisible) {
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

        textString = text
        
        initializingViewTreeObserver()
        typedArray.recycle()
    }

}