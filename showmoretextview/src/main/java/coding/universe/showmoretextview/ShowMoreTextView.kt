package coding.universe.showmoretextview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import java.lang.Exception


class ShowMoreTextView(context: Context, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatTextView(context, attrs){

    private val textString: CharSequence? = null
    private val bufferType: BufferType? = null
    private val readMore = true
    private var endCharacterIndex = 0
    private var maxLinesVisible = DEFAULT_MAX_LINE
    private var trimCollapsedText: CharSequence? = null
    private var trimExpandedText: CharSequence? = null
    private var lastIndexOfText = 0
    private val colorClickableText = 0
    private val showTrimExpandedText = false

    companion object{
        const val DEFAULT_INDEX = -1
        const val DEFAULT_MAX_LINE = -1
    }


    private fun setText(){
        
    }

    private fun initializingViewTreeObserver(){
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
                calculateLastIndex()
                setText()
            }
        })
    }

    private fun calculateLastIndex(){
        try {
            lastIndexOfText = if (maxLinesVisible == 0){
                layout.getLineEnd(0)
            }else if(maxLinesVisible > 0 && maxLinesVisible > lineCount){
                layout.getLineEnd(maxLinesVisible-1)
            }else{
                DEFAULT_INDEX
            }
        }catch (e : Exception){
            throw e
        }
    }


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowMoreTextView)

        trimCollapsedText = resources.getString(typedArray.getResourceId(R.styleable.ShowMoreTextView_trimCollapsedText, R.string.show_more))
        trimExpandedText = resources.getString(typedArray.getResourceId(R.styleable.ShowMoreTextView_trimExpandedText, R.string.show_less))
        maxLinesVisible = typedArray.getResourceId(R.styleable.ShowMoreTextView_maxLinesVisible, DEFAULT_MAX_LINE)

        initializingViewTreeObserver()

        typedArray.recycle()
    }
}