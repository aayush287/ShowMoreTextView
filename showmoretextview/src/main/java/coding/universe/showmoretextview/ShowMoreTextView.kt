package coding.universe.showmoretextview

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import kotlin.math.E


class ShowMoreTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    enum class TrimMode {
        TRIM_LINES,
        TRIM_LENGTH
    }

    private var textString: CharSequence? = null
    private val readMore = true
    private var endCharacterIndex = 0
    private var maxLinesVisible = DEFAULT_MAX_LINE
    private var collapsedText: CharSequence? = null
    private var expandedText: CharSequence? = null
    private var lastIndexOfText = 0
    private val colorClickableText = 0
    private val showTrimExpandedText = false

    private var bufferType: BufferType? = null

    private val ELLIPSIZE = "..."

    companion object {
        const val DEFAULT_INDEX = -1
        const val DEFAULT_MAX_LINE = -1
    }


    private fun setText() {
        super.setText(getDisplayableText())
        invalidate()
        requestLayout()
    }

    private fun getDisplayableText(): CharSequence? {
        return getTrimmedText(text)
    }

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        return getCollapsedText(text)
    }

    private fun getCollapsedText(text: CharSequence?): CharSequence? {
        Log.d(TAG, "getCollapsedText: $ELLIPSIZE")
        Log.d(TAG, "getCollapsedText: $collapsedText")
        Log.d(TAG, "getCollapsedText: lastIndex -> $lastIndexOfText")
        Log.d(TAG, "getCollapsedText: text -> $text")
        if (text != null) {
            Log.d(TAG, "getCollapsedText: text size -> ${text.length}")
        }
        val trimEndIndex: Int = if (lastIndexOfText == DEFAULT_INDEX) {
            text?.length ?: 0
        } else {
            lastIndexOfText - (ELLIPSIZE.length +
                    (collapsedText?.length ?: 0) + 1)
        }


        Log.d(TAG, "getCollapsedText: trimIndex -> $trimEndIndex")

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

        Log.d(TAG, "getCollapsedText: string builder -> $stringBuilder")

        return stringBuilder
    }

//    override fun setText(text: CharSequence?, type: BufferType?) {
//        this.textString = text
//        bufferType = type
//    }

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
        Log.d(TAG, "calculateLastIndex: lineCount -> $lineCount and maxLines -> $maxLinesVisible")
        try {
            lastIndexOfText = if (maxLinesVisible == 0) {
                layout.getLineEnd(0)
            } else if (maxLinesVisible > 0 && maxLinesVisible > lineCount) {
                layout.getLineEnd(maxLinesVisible - 1)
            } else {
                DEFAULT_INDEX
            }
        } catch (e: Exception) {
            throw e
        }
    }

    init {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ShowMoreTextView, 0, 0)
        collapsedText = resources.getString(
            typedArray.getResourceId(
                R.styleable.ShowMoreTextView_collapsedText,
                R.string.show_more
            )
        )
        expandedText = resources.getString(
            typedArray.getResourceId(
                R.styleable.ShowMoreTextView_expandedText,
                R.string.show_less
            )
        )
        Log.d(TAG, "get Collapsed text : $collapsedText")
        maxLinesVisible = typedArray.getResourceId(
            R.styleable.ShowMoreTextView_maxLinesVisible,
            DEFAULT_MAX_LINE
        )
        initializingViewTreeObserver()
        typedArray.recycle()
    }

}