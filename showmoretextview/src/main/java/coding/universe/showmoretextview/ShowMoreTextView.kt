package coding.universe.showmoretextview

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log


class ShowMoreTextView(context: Context, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatTextView(context, attrs){

    private val textString: CharSequence? = null
    private val bufferType: BufferType? = null
    private val readMore = true
    private var trimMaxLine = 0
    private var trimCollapsedText: CharSequence? = null
    private var trimExpandedText: CharSequence? = null
    private val colorClickableText = 0
    private val showTrimExpandedText = false

    override fun onDraw(canvas: Canvas?) {

        if (lineCount > trimMaxLine){
            maxLines = trimMaxLine
            ellipsize = TextUtils.TruncateAt.END
            Log.d(TAG, "onDraw: $maxLines")
        }

        super.onDraw(canvas)

    }


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowMoreTextView)

        trimCollapsedText = resources.getString(typedArray.getResourceId(R.styleable.ShowMoreTextView_trimCollapsedText, R.string.show_more))
        trimExpandedText = resources.getString(typedArray.getResourceId(R.styleable.ShowMoreTextView_trimExpandedText, R.string.show_less))
        trimMaxLine = typedArray.getResourceId(R.styleable.ShowMoreTextView_trimMaxLine, 12)


        typedArray.recycle()
    }
}