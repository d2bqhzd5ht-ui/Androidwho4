package cn.stu.edu.sicnu.tay.uicodeandmvc

import android.content.Context

object ProgramAdviserModel {
    // 返回值确实是 Int
    fun getAdviceResId(context: Context, language: String): Int {
        val adviceMap = mapOf(
            context.getString(R.string.lang_kotlin) to R.string.advice_kotlin,
            context.getString(R.string.lang_java) to R.string.advice_java,
            context.getString(R.string.lang_android) to R.string.advice_android
        )
        // 找不到就返回未知的资源 ID
        return adviceMap[language.lowercase()] ?: R.string.advice_unknown
    }
}