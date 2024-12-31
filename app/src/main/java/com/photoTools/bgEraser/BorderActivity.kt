package com.photoTools.bgEraser

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.adapter.ColorAdapter
import com.photoTools.bgEraser.databinding.ActivityBorderBinding
import java.io.File

class BorderActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityBorderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        loadImage(imageUriString)
        Log.d("BorderActivity", "Image URI: $imageUriString")
        binding.listBorder.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val cAdapter = ColorAdapter(this)
        cAdapter.setOnColorClick(object : ColorAdapter.ColorClickListener {
            override fun onItemClick(view: View, colorName: String) {
                binding.imageView.setBackgroundColor(Integer.valueOf(Color.parseColor(colorName)))
            }
        })
        binding.listBorder.adapter = cAdapter

        binding.seekbarBorder.setOnSeekBarChangeListener(BorderListener())
        binding.btnDone.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                setResult(RESULT_CANCELED)
                finish()
            }

            R.id.btn_done -> {
                val screenShot: Bitmap = Utilities.captureScreenShot(binding.frameLayout)
                val savedUri = Utilities.saveTempImageToFile(this, screenShot)
                val resultIntent = Intent().apply {
                    putExtra("IMAGE_URI", savedUri.toString())
                    Log.d("BgRemoverActivity5", "" + savedUri)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    inner class BorderListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.imageView.setPadding(progress, progress, progress, progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    private fun loadImage(file: File?) {
        Glide.with(this).load(file).into(binding.imageView)
    }
}