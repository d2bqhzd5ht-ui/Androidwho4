package cn.stu.edu.sicnu.tay.tay2024110533

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class Activity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        val etInput = findViewById<EditText>(R.id.etInput)

        findViewById<Button>(R.id.btnConfirmReturn).setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("return_data", etInput.text.toString())
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }
}