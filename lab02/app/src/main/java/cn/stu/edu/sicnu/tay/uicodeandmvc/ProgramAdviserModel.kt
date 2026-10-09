package cn.stu.edu.sicnu.tay.uicodeandmvc

object ProgramAdviserModel {
    // 这里存放资源 ID (R.string.xxx)
    private val adviceMap = mapOf(
        "kotlin" to R.string.advice_kotlin,
        "java" to R.string.advice_java,
        "android" to R.string.advice_android
    )

    // 返回值确实是 Int
    fun getAdviceResId(language: String): Int {
        // 找不到就返回未知的资源 ID
        return adviceMap[language.lowercase()] ?: R.string.advice_unknown
    }
}