package cn.stu.edu.sicnu.tay.lab07

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * 音乐播放服务
 * 所有音乐播放相关功能在Service中实现
 * Activity通过bindService获取服务Binder来交互
 */
class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1001

        // 状态常量
        const val STATE_IDLE = 0       // 空闲
        const val STATE_PLAYING = 1    // 播放中
        const val STATE_PAUSED = 2     // 暂停
    }

    private var mediaPlayer: MediaPlayer? = null
    private var musicList: List<Music> = emptyList()
    private var currentIndex: Int = -1
    private var state: Int = STATE_IDLE
    private val binder = MusicBinder()
    private val listeners = mutableListOf<OnPlayStateChangeListener>()

    // 用于多线程更新进度
    private var progressThread: Thread? = null
    @Volatile
    private var isUpdatingProgress = false

    /**
     * 监听器接口：监听播放状态和进度变化
     */
    interface OnPlayStateChangeListener {
        fun onPlayStateChanged(state: Int, music: Music?)
        fun onProgressChanged(currentPosition: Long, duration: Long)
    }

    /**
     * Binder类：Activity通过它与Service交互
     */
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdate()
        releaseMediaPlayer()
    }

    /**
     * 设置音乐列表
     */
    fun setMusicList(list: List<Music>) {
        this.musicList = list
        if (list.isNotEmpty() && currentIndex == -1) {
            currentIndex = 0
        }
    }

    /**
     * 获取音乐列表
     */
    fun getMusicList(): List<Music> = musicList

    /**
     * 获取当前音乐
     */
    fun getCurrentMusic(): Music? = if (currentIndex in musicList.indices) musicList[currentIndex] else null

    /**
     * 获取当前播放状态
     */
    fun getState(): Int = state

    /**
     * 获取当前播放位置
     */
    fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L

    /**
     * 添加状态变化监听器
     */
    fun addOnPlayStateChangeListener(listener: OnPlayStateChangeListener) {
        listeners.add(listener)
        val music = getCurrentMusic()
        listener.onPlayStateChanged(state, music)
        if (mediaPlayer != null) {
            listener.onProgressChanged(getCurrentPosition(), getDuration())
        }
    }

    /**
     * 移除状态变化监听器
     */
    fun removeOnPlayStateChangeListener(listener: OnPlayStateChangeListener) {
        listeners.remove(listener)
    }

    /**
     * 播放指定索引的音乐
     */
    fun play(index: Int) {
        if (index !in musicList.indices) return
        currentIndex = index
        playCurrent()
    }

    /**
     * 播放当前音乐
     */
    fun playCurrent() {
        val music = getCurrentMusic() ?: return
        try {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(music.path)
                prepare()
                setOnCompletionListener {
                    // 播放完成，自动下一首
                    next()
                }
                start()
            }
            state = STATE_PLAYING
            startProgressUpdate()
            showNotification(music.title)
            notifyStateChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        when (state) {
            STATE_PLAYING -> pause()
            STATE_PAUSED -> resume()
            STATE_IDLE -> playCurrent()
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                state = STATE_PAUSED
                stopProgressUpdate()
                notifyStateChanged()
            }
        }
    }

    /**
     * 继续播放
     */
    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                state = STATE_PLAYING
                startProgressUpdate()
                notifyStateChanged()
            }
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        releaseMediaPlayer()
        state = STATE_IDLE
        notifyStateChanged()
    }

    /**
     * 上一首
     */
    fun prev() {
        if (musicList.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else musicList.size - 1
        playCurrent()
    }

    /**
     * 下一首
     */
    fun next() {
        if (musicList.isEmpty()) return
        currentIndex = if (currentIndex < musicList.size - 1) currentIndex + 1 else 0
        playCurrent()
    }

    /**
     * 跳到指定位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    /**
     * 获取当前音乐总时长
     */
    fun getDuration(): Long = mediaPlayer?.duration?.toLong() ?: (getCurrentMusic()?.duration ?: 0L)

    /**
     * 释放MediaPlayer资源
     */
    private fun releaseMediaPlayer() {
        stopProgressUpdate()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    /**
     * 开始多线程更新播放进度
     */
    private fun startProgressUpdate() {
        if (isUpdatingProgress) return
        isUpdatingProgress = true
        progressThread = Thread {
            while (isUpdatingProgress && mediaPlayer != null) {
                try {
                    Thread.sleep(200)
                    val position = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    val duration = mediaPlayer?.duration?.toLong() ?: 0L
                    runOnUiThread {
                        for (listener in listeners) {
                            listener.onProgressChanged(position, duration)
                        }
                    }
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    break
                }
            }
        }
        progressThread?.start()
    }

    /**
     * 停止进度更新线程
     */
    private fun stopProgressUpdate() {
        isUpdatingProgress = false
        progressThread?.interrupt()
        progressThread = null
    }

    /**
     * 通知监听器状态变化
     */
    private fun notifyStateChanged() {
        val music = getCurrentMusic()
        runOnUiThread {
            for (listener in listeners) {
                listener.onPlayStateChanged(state, music)
            }
        }
    }

    /**
     * 显示通知栏（Notification）
     */
    private fun showNotification(title: String) {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在播放")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 创建通知渠道（Android 8.0+）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音乐播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示当前播放的音乐"
                setShowBadge(false)
                setLockscreenVisibility(Notification.VISIBILITY_PRIVATE)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
