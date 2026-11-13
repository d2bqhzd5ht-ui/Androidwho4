package cn.stu.edu.sicnu.tay.cardgame04

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        applySystemBarInsets(findViewById(R.id.root), findViewById(R.id.appBar))

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val cbTravel = findViewById<CheckBox>(R.id.cbTravel)
        val cbReading = findViewById<CheckBox>(R.id.cbReading)
        val cbGame = findViewById<CheckBox>(R.id.cbGame)
        val cbShopping = findViewById<CheckBox>(R.id.cbShopping)

        // 回显数据
        intent.getStringExtra("name")?.let { etName.setText(it) }
        intent.getStringExtra("phone")?.let { etPhone.setText(it) }
        intent.getStringExtra("email")?.let { etEmail.setText(it) }
        if (intent.getStringExtra("gender") == "男") {
            rgGender.check(R.id.rbMale)
        } else {
            rgGender.check(R.id.rbFemale)
        }

        val hobbiesStr = intent.getStringExtra("hobbies") ?: ""
        cbTravel.isChecked = hobbiesStr.contains("旅游")
        cbReading.isChecked = hobbiesStr.contains("阅读")
        cbGame.isChecked = hobbiesStr.contains("游戏")
        cbShopping.isChecked = hobbiesStr.contains("购物")

        // 保存
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("name", etName.text.toString())
                putExtra("phone", etPhone.text.toString())
                putExtra("email", etEmail.text.toString())
                putExtra("gender", if (rgGender.checkedRadioButtonId == R.id.rbMale) "男" else "女")

                val list = mutableListOf<String>()
                if (cbTravel.isChecked) list.add("旅游")
                if (cbReading.isChecked) list.add("阅读")
                if (cbGame.isChecked) list.add("游戏")
                if (cbShopping.isChecked) list.add("购物")
                putExtra("hobbies", list.joinToString(", "))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // 取消
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }
    }
}