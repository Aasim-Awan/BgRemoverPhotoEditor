package com.photoTools.bgEraser

import Utilities
import Utilities.showDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.photoTools.bgEraser.adapter.ColorAdapter
import com.photoTools.bgEraser.databinding.ActivityFrameBinding
import java.io.File
import java.io.InputStream

class FrameActivity : AppCompatActivity(), View.OnClickListener {

    private var originalbitmap: Bitmap? = null
    var imageUriString: File? = null
    private lateinit var binding: ActivityFrameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        imageUriString = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        loadImage(imageUriString)
        loadOriginalBitmap(imageUriString)
        Log.d("FrameActivity", "Image URI: $imageUriString")

        val frameUriString = intent.getStringExtra("FRAME_URI")?.let { Uri.parse(it) }
        frameUriString?.let { loadFrame(it) }
        Log.d("FrameActivity", "Frame URI: $frameUriString")

        val sourceType = intent.getStringExtra("SOURCE")
        when (sourceType) {
            "DashBoard" -> {
                binding.btnDone.visibility = View.GONE
                binding.btnDownload.visibility = View.VISIBLE
            }

            "BgRemover" -> {
                binding.btnDone.visibility = View.VISIBLE
                binding.btnDownload.visibility = View.GONE
            }
        }

       // binding.llBorder.setOnClickListener(this)
        binding.llFrames.setOnClickListener(this)
        binding.frameBack.setOnClickListener(this)
        binding.undo.setOnClickListener(this)
        binding.redo.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.btnDownload.setOnClickListener(this)
        binding.llRFrames.setOnClickListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialog(
                    context = this@FrameActivity,
                    title = "Discard Image",
                    message = "Are you sure you want to discard the Image",
                    positiveButtonText = "yes",
                    negativeButtonText = "NO",
                    onPositiveClick = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    onNegativeClick = {}
                )
            }
        })

        binding.imageView.setOnTouchListener(MultiTouchListener())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_border -> {
                val intent = Intent(this, BorderActivity::class.java).apply {
                    putExtra("IMAGE_URI", imageUriString.toString())
                }
                launcherForBorder.launch(intent)
            }

            R.id.ll_frames -> {
                val intent = Intent(this, ShowFramesActivity::class.java)
                showFramesActivityResultLauncher.launch(intent)
            }

            R.id.ll_RFrames -> {
                removeFrame()
            }

            binding.btnDone.id -> {
                val screenShot: Bitmap = Utilities.captureScreenShot(binding.flFrame)
                val savedUri = Utilities.saveTempImageToFile(this, screenShot)
                val resultIntent = Intent().apply {
                    putExtra("IMAGE_URI", savedUri.toString())
                    Log.d("BgRemoverActivity5", "" + savedUri)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            R.id.btn_back -> {
                showDialog(
                    context = this@FrameActivity,
                    title = "Discard Image",
                    message = "Are you sure you want to discard the Image?",
                    positiveButtonText = "YES",
                    negativeButtonText = "NO",
                    onPositiveClick = {
                        finish()
                    }
                )
            }

            R.id.btn_download -> {
                showDialog(
                    context = this@FrameActivity,
                    title = "Save Image",
                    message = "Are you sure you want to save the Image",
                    positiveButtonText = "Yes",
                    negativeButtonText = "No",
                    onPositiveClick = {
                        val screenShot: Bitmap = Utilities.captureScreenShot(binding.flFrame)
                        val savedUri = Utilities.saveBitmap(this, screenShot)
                        Log.d("SaveActivity", "Image URI saved: $savedUri")
                        if (savedUri != null) {
                            val intent = Intent(this, SaveActivity::class.java).apply {
                                putExtra("IMAGE_URI", savedUri.toString())
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    onNegativeClick = {}
                )
            }
        }
    }

    private fun loadOriginalBitmap(imageFile: File?) {
        try {
            imageFile?.let {
                originalbitmap = BitmapFactory.decodeFile(it.absolutePath)
                binding.imageView.setImageBitmap(originalbitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            originalbitmap = null
        }
    }

    private fun loadImage(file: File?) {
        Glide.with(this)
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imageView)

        this.imageUriString = file

        binding.progressBar.visibility = View.GONE
    }

    private fun loadFrame(frameUri: Uri) {
        Glide.with(this).load(frameUri).into(binding.ivFrame)
    }

    private fun removeFrame() {
        binding.ivFrame.setImageDrawable(null)
    }

    private val showFramesActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val selectedFrameUri = data?.getStringExtra("FRAME_URI")?.let { Uri.parse(it) }
                selectedFrameUri?.let { loadFrame(it) }
                Log.d("FrameActivity", "Selected Frame URI: $selectedFrameUri")
            }
        }

    private val launcherForBorder =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val selectedImageFile = data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                selectedImageFile?.let { loadImage(it) }
            }
        }

}
