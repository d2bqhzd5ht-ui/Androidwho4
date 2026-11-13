package cn.stu.edu.sicnu.tay.cardgame04

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvHobbies: TextView
    private lateinit var tvHighScore: TextView

    // 默认的玩家信息
    private var name = "张三"
    private var gender = "男"
    private var phone = "13800000000"
    private var email = "zhangsan@example.com"
    private var hobbies = "旅游, 游戏"
    private var highScore = 0

    // 处理修改信息界面的返回结果
    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            name = data?.getStringExtra("name") ?: name
            gender = data?.getStringExtra("gender") ?: gender
            phone = data?.getStringExtra("phone") ?: phone
            email = data?.getStringExtra("email") ?: email
            hobbies = data?.getStringExtra("hobbies") ?: hobbies
            refreshUI()
        }
    }

    // 处理游戏界面的返回结果
    private val gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val score = result.data?.getIntExtra("score", 0) ?: 0
            if (score > highScore) {
                highScore = score
                refreshUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets(findViewById(R.id.root), findViewById(R.id.appBar))

        tvName = findViewById(R.id.tvName)
        tvGender = findViewById(R.id.tvGender)
        tvPhone = findViewById(R.id.tvPhone)
        tvEmail = findViewById(R.id.tvEmail)
        tvHobbies = findViewById(R.id.tvHobbies)
        tvHighScore = findViewById(R.id.tvHighScore)

        refreshUI()

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java).apply {
                putExtra("name", name)
                putExtra("gender", gender)
                putExtra("phone", phone)
                putExtra("email", email)
                putExtra("hobbies", hobbies)
            }
            editProfileLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btnPlayGame).setOnClickListener {
            gameLauncher.launch(Intent(this, GameActivity::class.java))
        }
    }

    private fun refreshUI() {
        tvName.text = getString(R.string.name_label, name)
        tvGender.text = getString(R.string.gender_label, gender)
        tvPhone.text = getString(R.string.phone_label, phone)
        tvEmail.text = getString(R.string.email_label, email)
        tvHobbies.text = getString(R.string.hobbies_label, hobbies)
        tvHighScore.text = getString(R.string.high_score_label, highScore)
    }
}