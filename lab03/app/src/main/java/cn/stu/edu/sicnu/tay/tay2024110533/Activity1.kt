package cn.stu.edu.sicnu.tay.tay2024110533

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Activity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)

        val receivedMsg = intent.getStringExtra("message_key")
        findViewById<TextView>(R.id.tvReceiveMsg).text = receivedMsg
    }
}