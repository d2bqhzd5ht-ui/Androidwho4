package cn.stu.edu.sicnu.tay.uicodeandmvc

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.stu.edu.sicnu.tay.uicodeandmvc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // 声明 ViewBinding 变量
    private lateinit var binding: ActivityMainBinding
    private var textViewCount = 0 // 记录TextView的数量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化 ViewBinding 并设置布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 区1功能：点击按钮动态添加 TextView
        binding.btnAddText.setOnClickListener {
            textViewCount++
            val textView = TextView(this).apply {
                // 使用字符串资源格式化（符合无硬编码字符串要求）
                text = getString(R.string.text_view_format, textViewCount)
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            // 添加到布局中
            binding.llContainer.addView(textView)

            // 让 ScrollView 自动滚动到最新添加的位置
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
        // 区2功能：MVC 模式查询（更新后的代码）
        binding.btnQuery.setOnClickListener {
            val input = binding.etQuery.text.toString().trim()
            if (input.isNotEmpty()) {
                // 调用 Model 层获取资源 ID (MVC 的 C 调用 M)
                val resId = ProgramAdviserModel.getAdviceResId(this, input)

                // 根据资源 ID 获取字符串，并更新 View (MVC 的 C 更新 V)
                binding.tvResult.text = if (resId == R.string.advice_unknown) {
                    // 如果是未知语言，需要把输入的内容作为参数传进去
                    getString(resId, input)
                } else {
                    getString(resId)
                }
            } else {
                binding.tvResult.text = getString(R.string.tv_result_default)
            }
        }
    }
}