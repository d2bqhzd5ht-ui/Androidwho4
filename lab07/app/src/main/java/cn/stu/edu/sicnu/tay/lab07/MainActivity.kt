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
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private var musicList: MutableList<Music> = mutableListOf()
    private var musicAdapter: MusicAdapter? = null
    private var isServiceBound = false
    private var isUpdatingSeekBar = false

    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: false
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }
        if (audioGranted) {
            copyAssetsMusicToSdcard()
            loadMusic()
            bindMusicService()
        } else {
            Toast.makeText(this, "需要读取音乐权限才能使用", Toast.LENGTH_LONG).show()
        }
    }

    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isServiceBound = true
            musicService?.addOnPlayStateChangeListener(this@MainActivity)
            musicService?.setMusicList(musicList)  // 把 Activity 的列表传给 Service
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

        // 处理状态栏重叠：给根布局 top padding
        val rootLayout = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop + systemBars.top,
                view.paddingRight, view.paddingBottom + systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(rootLayout)

        initViews()
        setupRecyclerView()
        setupSeekBar()
        setupClickListeners()
        checkPermissions()
    }

    /**
     * 把项目 assets/music/ 里的 mp3 文件复制到 /sdcard/Music/lab07/
     * ContentResolver 才能在 MediaStore 里查到这些文件
     */
    private fun copyAssetsMusicToSdcard() {
        try {
            val targetDir = java.io.File("/sdcard/Music/lab07")
            if (!targetDir.exists()) targetDir.mkdirs()
            val scanUri = Uri.parse("file://${targetDir.absolutePath}")

            val assetFiles = assets.list("music") ?: return
            var copied = 0
            for (name in assetFiles) {
                if (!name.endsWith(".mp3")) continue
                val target = java.io.File(targetDir, name)
                if (target.exists() && target.length() > 0) continue
                assets.open("music/$name").use { input ->
                    java.io.FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
                copied++
            }

            if (copied > 0) {
                // Android 10+ 需要用 MediaScannerConnection
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    android.media.MediaScannerConnection.scanFile(
                        this, arrayOf(targetDir.absolutePath), null, null
                    )
                } else {
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, scanUri))
                }
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

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(musicList) { position ->
            if (!isServiceBound || musicService == null) {
                Toast.makeText(this, "服务未连接", Toast.LENGTH_SHORT).show()
                return@MusicAdapter
            }
            if (position !in musicList.indices) {
                Toast.makeText(this, "无效的歌曲索引", Toast.LENGTH_SHORT).show()
                return@MusicAdapter
            }
            musicService?.play(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = musicAdapter
    }

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

    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener { musicService?.togglePlayPause() }
        btnPrev.setOnClickListener { musicService?.prev() }
        btnNext.setOnClickListener { musicService?.next() }
        btnStop.setOnClickListener { musicService?.stop() }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(audioPermission)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            copyAssetsMusicToSdcard()
            loadMusic()
            bindMusicService()
        }
    }

    private fun loadMusic() {
        musicList.clear()
        val resolver: ContentResolver = contentResolver
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
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
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathIdx) ?: ""
                if (path.isEmpty()) continue
                musicList.add(Music(
                    id = it.getLong(idIdx),
                    title = it.getString(titleIdx) ?: "未知标题",
                    artist = it.getString(artistIdx) ?: "未知艺术家",
                    album = it.getString(albumIdx) ?: "未知专辑",
                    duration = it.getLong(durIdx),
                    path = path
                ))
            }
        }

        musicAdapter?.notifyDataSetChanged()

        if (musicList.isEmpty()) {
            Toast.makeText(this, "未找到音乐文件", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "加载了 ${musicList.size} 首音乐", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

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

        btnPlayPause.setImageResource(
            if (state == MusicService.STATE_PLAYING)
                android.R.drawable.ic_media_pause
            else
                android.R.drawable.ic_media_play
        )
    }

    // ========== MusicService.OnPlayStateChangeListener ==========

    override fun onPlayStateChanged(state: Int, music: Music?) {
        runOnUiThread {
            updateUI()
            if (music != null) {
                val index = musicList.indexOf(music)
                musicAdapter?.setCurrentPlayingIndex(index)
            } else {
                musicAdapter?.setCurrentPlayingIndex(-1)
            }
        }
    }

    override fun onProgressChanged(currentPosition: Long, duration: Long) {
        runOnUiThread {
            if (!isUpdatingSeekBar) {
                seekBar.max = duration.toInt().coerceAtLeast(1)
                seekBar.progress = currentPosition.toInt()
                tvCurrentTime.text = Music.formatPosition(currentPosition)
            }
        }
    }
}
