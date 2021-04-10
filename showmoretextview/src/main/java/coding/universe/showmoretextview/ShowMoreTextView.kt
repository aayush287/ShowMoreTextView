package coding.universe.showmoretextview

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import coding.universe.showmoretextviewsamples.R


class ShowMoreTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    enum class TrimMode {
        TRIM_LINES, TRIM_LENGTH, NONE
    }

    private val ELLIPSIZE = "..."


    private var textString: CharSequence
    private var maxLinesVisible = DEFAULT_MAX_LINE
    private var maxLengthVisible = DEFAULT_MAX_LENGTH
    private var collapsedText: CharSequence?
    private var expandedText: CharSequence?
    private var endCharacterIndex = 0
    private var readMore = true
    private var showMoreSpanColor: Int = 0
    private var showLessSpanColor: Int = 0
    private var trimMode: TrimMode
    private var totalLines: Int = 0

    private val viewMoreSpan by lazy { ShowMoreClickableSpan() }


    companion object {
        const val DEFAULT_INDEX = -1
        const val DEFAULT_MAX_LINE = -1
        const val DEFAULT_MAX_LENGTH = -1
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

        maxLengthVisible = typedArray.getInt(
            R.styleable.ShowMoreTextView_maxLengthVisible,
            DEFAULT_MAX_LENGTH
        )

        showMoreSpanColor = typedArray.getColor(
            R.styleable.ShowMoreTextView_showMoreSpanColor,
            ContextCompat.getColor(context, R.color.default_show_more)
        )

        showLessSpanColor = typedArray.getColor(
            R.styleable.ShowMoreTextView_showLessSpanColor,
            ContextCompat.getColor(context, R.color.default_show_less)
        )

        /**
        Setting trim mode on the basis of attribute present
        e.g. If maxLineVisible != DEFAULT_MAX_LINE then trimMode will be "TRIM_LINES"
        else "TRIM_LENGTH"
         **/

        trimMode = if (maxLinesVisible != DEFAULT_MAX_LINE) {
            TrimMode.TRIM_LINES
        } else if (maxLengthVisible != DEFAULT_MAX_LENGTH) {
            TrimMode.TRIM_LENGTH
        } else {
            TrimMode.NONE
        }


        textString = text

        initializingViewTreeObserver()
        typedArray.recycle()
    }


    private fun setText() {
        super.setText(getTrimmedText(textString))
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = Color.TRANSPARENT
        invalidate()
        requestLayout()
    }


    /**
     *  get Collapsed text if readMore is true
     *  get Expanded text if readMore is false
     */
    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        if (trimMode == TrimMode.TRIM_LENGTH && text != null && text.length > maxLengthVisible) {
            return if (readMore) {
                getCollapsedText(text)
            } else {
                getExpandedText(text)
            }
        } else if (trimMode == TrimMode.TRIM_LINES && text != null && totalLines > maxLinesVisible) {
            return if (readMore) {
                getCollapsedText(text)
            } else {
                getExpandedText(text)
            }
        }
        return text
    }


    /**
     * return a char sequence of text string upto maxLinesVisible
     *
     * if lastIndexOfText = -1 then return whole text without appending Show More
     */
    private fun getCollapsedText(text: CharSequence?): CharSequence {

        if (trimMode == TrimMode.TRIM_LINES) {
            val trimEndIndex: Int =
                endCharacterIndex - (ELLIPSIZE.length + (collapsedText?.length ?: 0) + 1)
            val stringBuilder = SpannableStringBuilder(text, 0, trimEndIndex)
                .append(ELLIPSIZE)
                .append(collapsedText ?: "")

            return addClickableSpan(stringBuilder, collapsedText ?: "")
        } else {
            val trimEndIndex: Int = if (maxLengthVisible > 0) maxLengthVisible else text!!.length
            val stringBuilder = SpannableStringBuilder(text, 0, trimEndIndex)
                .append(ELLIPSIZE)
                .append(collapsedText ?: "")

            return addClickableSpan(stringBuilder, collapsedText ?: "")
        }
    }

    private fun getExpandedText(text: CharSequence?): CharSequence? {
        if (!readMore) {
            val stringBuilder = SpannableStringBuilder(text, 0, text?.length ?: 0)
                .append(ELLIPSIZE)
                .append(expandedText ?: "")
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
            if (readMore) {
                ds.color = showMoreSpanColor
            } else {
                ds.color = showLessSpanColor
            }
        }
    }

    private fun initializingViewTreeObserver() {
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
                totalLines = lineCount
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
                    layout.getLineEnd(maxLinesVisible - 1)
                }
                else -> {
                    DEFAULT_INDEX
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun setMaxLengthVisible(maxLengthVisible: Int) {
        this.maxLengthVisible = maxLengthVisible
        trimMode = TrimMode.TRIM_LENGTH
        setText()
    }


    fun setCollapsedText(collapsedText: CharSequence) {
        this.collapsedText = collapsedText
        setText()
    }

    fun setExpandedText(expandedText: CharSequence) {
        this.expandedText = expandedText
        setText()
    }


    fun setMaxLinesVisible(maxLinesVisible: Int) {
        this.maxLinesVisible = maxLinesVisible
        calculateLastIndex()
        trimMode = TrimMode.TRIM_LINES
        setText()
    }

}