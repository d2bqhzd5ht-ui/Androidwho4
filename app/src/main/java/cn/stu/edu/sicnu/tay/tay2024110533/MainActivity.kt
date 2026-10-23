package cn.stu.edu.sicnu.tay.tay2024110533

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedData = result.data?.getStringExtra("return_data")
            Toast.makeText(this, "收到返回结果: $returnedData", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 启动 Activity1 (正向传参)
        findViewById<Button>(R.id.btnStartActivity1).setOnClickListener {
            val intent = Intent(this, Activity1::class.java)
            intent.putExtra("message_key", "这是MainActivity传过去的参数！")
            startActivity(intent)
        }

        // 2. 启动 Activity2 (等待返回结果)
        findViewById<Button>(R.id.btnStartActivity2).setOnClickListener {
            val intent = Intent(this, Activity2::class.java)
            resultLauncher.launch(intent)
        }

        // 3. 启动 Activity3 (调用相机)
        findViewById<Button>(R.id.btnStartActivity3).setOnClickListener {
            startActivity(Intent(this, Activity3::class.java))
        }

        // 4. 启动 Activity4 (秒表)
        findViewById<Button>(R.id.btnStartActivity4).setOnClickListener {
            startActivity(Intent(this, Activity4::class.java))
        }
    }
}