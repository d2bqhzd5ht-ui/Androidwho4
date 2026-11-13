package cn.stu.edu.sicnu.tay.tay2024110533

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity

class Activity4 : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private var elapsedTime: Long = 0
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_4)

        chronometer = findViewById(R.id.chronometer)

        if (savedInstanceState != null) {
            elapsedTime = savedInstanceState.getLong("elapsed_time", 0)
            isRunning = savedInstanceState.getBoolean("is_running", false)

            chronometer.base = SystemClock.elapsedRealtime() - elapsedTime
            if (isRunning) {
                chronometer.start()
            }
        } else {
            chronometer.base = SystemClock.elapsedRealtime()
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (!isRunning) {
                chronometer.base = SystemClock.elapsedRealtime() - elapsedTime
                chronometer.start()
                isRunning = true
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            if (isRunning) {
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                isRunning = false
            }
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            chronometer.stop()
            isRunning = false
            elapsedTime = 0
            chronometer.base = SystemClock.elapsedRealtime()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
        }
        outState.putLong("elapsed_time", elapsedTime)
        outState.putBoolean("is_running", isRunning)
    }
}
