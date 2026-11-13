package cn.stu.edu.sicnu.tay.tay2024110533

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class Activity3 : AppCompatActivity() {

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3)

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            takePictureLauncher.launch(null)
        }
    }
}