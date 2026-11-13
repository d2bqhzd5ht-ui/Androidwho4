package cn.stu.edu.sicnu.tay.cardgame04 // 注意这里加了 stu

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun clickEditProfile_opensEditScreen() {
        // 在主界面点击“修改个人信息”按钮
        onView(withId(R.id.btnEditProfile)).perform(click())

        // 验证跳转后的“姓名输入框”是否显示
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
    }
}