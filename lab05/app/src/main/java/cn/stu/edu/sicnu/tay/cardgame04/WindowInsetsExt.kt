package cn.stu.edu.sicnu.tay.cardgame04

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

/**
 * 让页面内容避开状态栏 / 导航栏 / 输入法，避免顶部内容被系统栏遮挡。
 * [appBar] 会额外获得状态栏高度的顶部内边距，使顶部色块延伸到状态栏下方。
 */
fun Activity.applySystemBarInsets(root: View, appBar: View) {
    WindowInsetsControllerCompat(window, root).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = true
    }

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

        appBar.updatePadding(top = bars.top)
        view.updatePadding(
            left = bars.left,
            right = bars.right,
            bottom = maxOf(bars.bottom, imeBottom)
        )
        insets
    }
}
