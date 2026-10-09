package cn.stu.edu.sicnu.tay.first_helloword

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // 定义当前语言状态：0=中文，1=英文，2=法文
    private var currentLanguage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 创建根布局（垂直排列，居中）
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.WHITE)

        // 2. 创建显示国旗的 ImageView
        val imageView = ImageView(this)
        // 设置默认显示中国国旗
        imageView.setImageResource(R.drawable.flag_cn)
        // 设置图片大小（单位：像素）
        val imageParams = LinearLayout.LayoutParams(300, 200)
        imageParams.setMargins(0, 0, 0, 50)
        imageView.layoutParams = imageParams
        layout.addView(imageView)

        // 3. 创建显示文字的 TextView
        val textView = TextView(this)
        textView.text = "你好，世界！"
        textView.textSize = 40f
        textView.gravity = Gravity.CENTER
        val textParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textParams.setMargins(0, 0, 0, 100)
        textView.layoutParams = textParams
        layout.addView(textView)

        // 4. 创建三个切换按钮
        // 按钮1：中文
        val btnCn = Button(this)
        btnCn.text = "中文"
        btnCn.setOnClickListener {
            currentLanguage = 0
            updateUI(textView, imageView)
        }
        layout.addView(btnCn)

        // 按钮2：英文
        val btnEn = Button(this)
        btnEn.text = "English"
        btnEn.setOnClickListener {
            currentLanguage = 1
            updateUI(textView, imageView)
        }
        layout.addView(btnEn)

        // 按钮3：第三种语言（这里用法语举例）
        val btnFr = Button(this)
        btnFr.text = "Français"
        btnFr.setOnClickListener {
            currentLanguage = 2
            updateUI(textView, imageView)
        }
        layout.addView(btnFr)

        // 5. 把布局放到界面上
        setContentView(layout)
    }

    // 更新界面显示的函数
    private fun updateUI(textView: TextView, imageView: ImageView) {
        when (currentLanguage) {
            0 -> {
                textView.text = "你好，世界！"
                imageView.setImageResource(R.drawable.flag_cn)
            }
            1 -> {
                textView.text = "Hello, World!"
                imageView.setImageResource(R.drawable.flag_us)
            }
            2 -> {
                textView.text = "Bonjour le monde!"
                imageView.setImageResource(R.drawable.flag_fr)
            }
        }
    }
}