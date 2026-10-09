package cn.stu.edu.sicnu.tay.uicodeandmvc

object ProgramAdviserModel {
    // 这里改成存放资源 ID
    private val adviceMap = mapOf(
        "kotlin" to R.string.advice_kotlin,
        "java" to R.string.advice_java,
        "android" to R.string.advice_android
    )

    // 返回值改成 Int
    fun getAdviceResId(language: String): Int {
        return adviceMap[language.lowercase()] ?: R.string.advice_unknown
    }
}