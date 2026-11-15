package cn.stu.edu.sicnu.tay.lab06

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 第 1 个 Fragment：个人信息。
 *
 * 个人信息保存在 Profile 对象中，Profile 从 Serializable 派生。
 * 每次修改后调用 ProfileStore.save()，用 openFileOutput + ObjectOutputStream
 * 写入文件；Fragment 创建时用 openFileInput + ObjectInputStream 读回，
 * 因此退出程序再打开仍然可以恢复。
 */
class PersonalInfoFragment : Fragment(R.layout.fragment_profile) {

    private var profile = Profile()

    private lateinit var tvName: TextView
    private lateinit var tvStudentId: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvHobbies: TextView
    private lateinit var tvHighScoreTwo: TextView
    private lateinit var tvHighScoreThree: TextView
    private lateinit var tvLastSaved: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvName)
        tvStudentId = view.findViewById(R.id.tvStudentId)
        tvGender = view.findViewById(R.id.tvGender)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvHobbies = view.findViewById(R.id.tvHobbies)
        tvHighScoreTwo = view.findViewById(R.id.tvHighScoreTwo)
        tvHighScoreThree = view.findViewById(R.id.tvHighScoreThree)
        tvLastSaved = view.findViewById(R.id.tvLastSaved)

        profile = ProfileStore.load(requireContext())
        refreshUI()

        view.findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            showEditDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从游戏 Fragment 返回时重新读取，及时显示最新的最高分
        profile = ProfileStore.load(requireContext())
        refreshUI()
    }

    private fun refreshUI() {
        tvName.text = getString(R.string.name_label, profile.name)
        tvStudentId.text = getString(R.string.student_id_label, profile.studentId)
        tvGender.text = getString(R.string.gender_label, profile.gender)
        tvPhone.text = getString(R.string.phone_label, profile.phone)
        tvEmail.text = getString(R.string.email_label, profile.email)
        tvHobbies.text = getString(R.string.hobbies_label, profile.hobbies)
        tvHighScoreTwo.text = getString(R.string.high_score_two_label, profile.highScoreTwo)
        tvHighScoreThree.text = getString(R.string.high_score_three_label, profile.highScoreThree)
        tvLastSaved.text = if (profile.lastSaved.isEmpty()) {
            getString(R.string.last_saved_none)
        } else {
            getString(R.string.last_saved_label, profile.lastSaved)
        }
    }

    /**
     * 弹出修改个人信息的对话框，保存时把整个 Profile 序列化写入文件。
     */
    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etStudentId = dialogView.findViewById<EditText>(R.id.etStudentId)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val rgGender = dialogView.findViewById<RadioGroup>(R.id.rgGender)
        val cbTravel = dialogView.findViewById<CheckBox>(R.id.cbTravel)
        val cbReading = dialogView.findViewById<CheckBox>(R.id.cbReading)
        val cbGame = dialogView.findViewById<CheckBox>(R.id.cbGame)
        val cbShopping = dialogView.findViewById<CheckBox>(R.id.cbShopping)

        // 回显当前信息
        etName.setText(profile.name)
        etStudentId.setText(profile.studentId)
        etPhone.setText(profile.phone)
        etEmail.setText(profile.email)
        if (profile.gender == getString(R.string.female)) {
            rgGender.check(R.id.rbFemale)
        } else {
            rgGender.check(R.id.rbMale)
        }
        cbTravel.isChecked = profile.hobbies.contains(getString(R.string.travel))
        cbReading.isChecked = profile.hobbies.contains(getString(R.string.reading))
        cbGame.isChecked = profile.hobbies.contains(getString(R.string.game))
        cbShopping.isChecked = profile.hobbies.contains(getString(R.string.shopping))

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_profile)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                profile.name = etName.text.toString().ifBlank { profile.name }
                profile.studentId = etStudentId.text.toString().ifBlank { profile.studentId }
                profile.phone = etPhone.text.toString()
                profile.email = etEmail.text.toString()
                profile.gender = if (rgGender.checkedRadioButtonId == R.id.rbFemale) {
                    getString(R.string.female)
                } else {
                    getString(R.string.male)
                }

                val hobbies = mutableListOf<String>()
                if (cbTravel.isChecked) hobbies.add(getString(R.string.travel))
                if (cbReading.isChecked) hobbies.add(getString(R.string.reading))
                if (cbGame.isChecked) hobbies.add(getString(R.string.game))
                if (cbShopping.isChecked) hobbies.add(getString(R.string.shopping))
                profile.hobbies = hobbies.joinToString(", ")

                profile.lastSaved = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())

                if (ProfileStore.save(requireContext(), profile)) {
                    Toast.makeText(requireContext(), R.string.saved_toast, Toast.LENGTH_SHORT).show()
                }
                refreshUI()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
