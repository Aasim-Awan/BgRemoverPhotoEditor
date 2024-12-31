package com.photoTools.bgEraser

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.databinding.ActivitySaveBinding
import java.io.File

class SaveActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("IMAGE_URI")?.let { File(it) }

        Glide.with(this).load(imageUriString).into(binding.imageView)

        binding.btnShare.setOnClickListener {
            shareImage(imageUriString!!)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun shareImage(imageFile: File) {
        val imageUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            imageFile
        )


        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }

}
