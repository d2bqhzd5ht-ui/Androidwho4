package cn.stu.edu.sicnu.tay.lab06

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 实验六：多 Fragment 应用的主界面。
 *
 * MainActivity 只负责承载 Fragment：底部导航栏在三个 Fragment 之间切换，
 * 个人信息、二张牌游戏、三张牌游戏都被拆分成独立的 Fragment。
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_TAB = "selected_tab"
    }

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets(findViewById(R.id.root), findViewById(R.id.appBar))

        bottomNav = findViewById(R.id.bottomNav)

        val tabId = savedInstanceState?.getInt(KEY_TAB) ?: R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        bottomNav.selectedItemId = tabId

        // 首次启动时手动显示默认 Fragment（系统恢复时 FragmentManager 已经恢复，无需重复添加）
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            showTab(tabId)
        }
    }

    /**
     * 根据底部导航栏的菜单项切换到对应的 Fragment。
     */
    private fun showTab(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_two -> TwoCardGameFragment()
            R.id.nav_three -> ThreeCardGameFragment()
            else -> PersonalInfoFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, bottomNav.selectedItemId)
    }
}
