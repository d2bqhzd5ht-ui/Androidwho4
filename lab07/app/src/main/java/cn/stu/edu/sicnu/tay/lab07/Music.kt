package cn.stu.edu.sicnu.tay.lab07

/**
 * 音乐数据类
 * 包含音乐的基本信息：ID、标题、歌手、专辑、时长、路径
 */
data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // 毫秒
    val path: String
) {
    /**
     * 格式化时长为 mm:ss
     */
    fun formatDuration(): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    /**
     * 格式化当前位置为 mm:ss
     */
    companion object {
        fun formatPosition(positionMs: Long): String {
            val totalSeconds = positionMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
