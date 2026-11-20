package cn.stu.edu.sicnu.tay.lab07

import android.Manifest
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 主界面Activity
 * - 使用ContentResolver获取音乐文件列表
 * - 绑定MusicService获取服务Binder
 * - SeekBar实时显示和调整播放进度
 * - 播放/暂停/停止/上一首/下一首控制
 */
class MainActivity : AppCompatActivity(), MusicService.OnPlayStateChangeListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: android.widget.TextView
    private lateinit var tvTotalTime: android.widget.TextView
    private lateinit var tvSongTitle: android.widget.TextView
    private lateinit var tvSongArtist: android.widget.TextView
    private lateinit var btnPlayPause: android.widget.ImageButton
    private lateinit var btnPrev: android.widget.ImageButton
    private lateinit var btnNext: android.widget.ImageButton
    private lateinit var btnStop: android.widget.ImageButton

    private var musicService: MusicService? = null
    private var musicAdapter: MusicAdapter? = null
    private var isServiceBound = false
    private var isUpdatingSeekBar = false // 防止拖动时被自动更新覆盖

    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            loadMusic()
            bindMusicService()
        } else {
            Toast.makeText(this, "需要权限才能读取音乐文件", Toast.LENGTH_SHORT).show()
        }
    }

    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isServiceBound = true
            musicService?.addOnPlayStateChangeListener(this@MainActivity)
            musicService?.setMusicList(musicAdapter?.let { getMusicListFromAdapter(it) } ?: emptyList())
            updateUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService?.removeOnPlayStateChangeListener(this@MainActivity)
            musicService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupSeekBar()
        setupClickListeners()
        copyAssetsMusicToSdcard()  // 把 assets 里的 mp3 拷到 sdcard
        checkPermissions()
    }

    /**
     * 把项目 assets/music/ 里的 mp3 文件复制到 /sdcard/Music/lab07/
     * 这样 ContentResolver 就能扫描到它们
     */
    private fun copyAssetsMusicToSdcard() {
        try {
            val targetDir = java.io.File("/sdcard/Music/lab07")
            if (!targetDir.exists()) targetDir.mkdirs()

            val assetFiles = assets.list("music") ?: return
            var copied = 0
            for (name in assetFiles) {
                if (!name.endsWith(".mp3")) continue
                val target = java.io.File(targetDir, name)
                if (target.exists() && target.length() > 0) continue  // 已拷过就跳过
                assets.open("music/$name").use { input ->
                    java.io.FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
                copied++
            }

            if (copied > 0) {
                // 通知 MediaScanner 重新扫描
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://${targetDir.absolutePath}")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            musicService?.removeOnPlayStateChangeListener(this)
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvSongArtist = findViewById(R.id.tvSongArtist)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnStop = findViewById(R.id.btnStop)
    }

    /**
     * 设置RecyclerView音乐列表
     */
    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(emptyList()) { position ->
            musicService?.play(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = musicAdapter
    }

    /**
     * 设置SeekBar进度条
     */
    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = Music.formatPosition(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUpdatingSeekBar = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUpdatingSeekBar = false
                musicService?.seekTo(seekBar?.progress?.toLong() ?: 0L)
            }
        })
    }

    /**
     * 设置按钮点击事件
     */
    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            musicService?.togglePlayPause()
        }
        btnPrev.setOnClickListener {
            musicService?.prev()
        }
        btnNext.setOnClickListener {
            musicService?.next()
        }
        btnStop.setOnClickListener {
            musicService?.stop()
        }
    }

    /**
     * 检查权限
     */
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Android 13+ 使用 READ_MEDIA_AUDIO，旧版本使用 READ_EXTERNAL_STORAGE
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(audioPermission)
        }

        // Android 13+ 需要通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            loadMusic()
            bindMusicService()
        }
    }

    /**
     * 使用ContentResolver加载音乐文件
     */
    private fun loadMusic() {
        val musicList = mutableListOf<Music>()
        val resolver: ContentResolver = contentResolver
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // 查询条件：只获取有效的音乐文件
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val cursor: Cursor? = resolver.query(
            collection,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            ),
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val music = Music(
                    id = it.getLong(idIndex),
                    title = it.getString(titleIndex) ?: "未知标题",
                    artist = it.getString(artistIndex) ?: "未知艺术家",
                    album = it.getString(albumIndex) ?: "未知专辑",
                    duration = it.getLong(durationIndex),
                    path = it.getString(pathIndex) ?: ""
                )
                // 只添加有路径的音乐
                if (music.path.isNotEmpty()) {
                    musicList.add(music)
                }
            }
        }

        // 更新适配器
        musicAdapter = MusicAdapter(musicList) { position ->
            musicService?.play(position)
        }
        recyclerView.adapter = musicAdapter

        // 提示
        if (musicList.isEmpty()) {
            Toast.makeText(this, "未找到音乐文件，请在设备上添加音乐后重试", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "加载了 ${musicList.size} 首音乐", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 绑定音乐服务
     */
    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 更新UI状态
     */
    private fun updateUI() {
        val music = musicService?.getCurrentMusic()
        val state = musicService?.getState() ?: MusicService.STATE_IDLE

        if (music != null) {
            tvSongTitle.text = music.title
            tvSongArtist.text = music.artist
            tvTotalTime.text = music.formatDuration()
            seekBar.max = music.duration.toInt()
        } else {
            tvSongTitle.text = "未选择歌曲"
            tvSongArtist.text = ""
            tvTotalTime.text = "00:00"
            tvCurrentTime.text = "00:00"
            seekBar.progress = 0
            seekBar.max = 100
        }

        // 更新播放/暂停按钮图标
        btnPlayPause.setImageResource(
            if (state == MusicService.STATE_PLAYING)
                android.R.drawable.ic_media_pause
            else
                android.R.drawable.ic_media_play
        )
    }

    /**
     * 从适配器获取音乐列表
     */
    private fun getMusicListFromAdapter(adapter: MusicAdapter): List<Music> {
        // 直接从适配器获取（需要反射或添加方法）
        return musicAdapter?.let { adapter }?.let { musicService?.getMusicList() } ?: emptyList()
    }

    // ========== MusicService.OnPlayStateChangeListener 实现 ==========

    override fun onPlayStateChanged(state: Int, music: Music?) {
        runOnUiThread {
            updateUI()
            if (music != null) {
                val index = musicService?.getMusicList()?.indexOf(music) ?: -1
                musicAdapter?.setCurrentPlayingIndex(index)
            }
        }
    }

    override fun onProgressChanged(currentPosition: Long, duration: Long) {
        runOnUiThread {
            if (!isUpdatingSeekBar) {
                seekBar.max = duration.toInt()
                seekBar.progress = currentPosition.toInt()
                tvCurrentTime.text = Music.formatPosition(currentPosition)
            }
        }
    }
}
