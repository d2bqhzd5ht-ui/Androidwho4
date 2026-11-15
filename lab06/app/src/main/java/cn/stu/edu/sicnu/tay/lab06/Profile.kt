package cn.stu.edu.sicnu.tay.lab06

import java.io.Serializable

/**
 * 个人信息数据模型。
 *
 * 从 Serializable 派生后，整个对象可以用 ObjectOutputStream 写入
 * openFileOutput 得到的文件，再用 ObjectInputStream 从 openFileInput 读回。
 */
data class Profile(
    var name: String = "田爱源",
    var studentId: String = "2024110533",
    var gender: String = "男",
    var phone: String = "13800000000",
    var email: String = "tay@example.com",
    var hobbies: String = "旅游, 游戏",
    var highScoreTwo: Int = 0,
    var highScoreThree: Int = 0,
    var lastSaved: String = ""
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
