package coding.universe.showmoretextview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ShowMoreTextView>(R.id.textView).apply {
            setCollapsedText("Show")
            setExpandedText("Less")
            setMaxLinesVisible(2)
        }
    }
}