package com.photoTools.bgEraser

import Utilities
import Utilities.saveBitmap
import Utilities.saveTempImageToFile
import Utilities.showDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.gabrielbb.cutout.CutOut
import com.github.gabrielbb.cutout.CutOutActivity
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.databinding.ActivityBgRemoverBinding
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Stack

class BgRemoverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBgRemoverBinding
    private val undoStack = Stack<File>()
    private val redoStack = Stack<File>()
    private val adsManager by lazy {
        AdsManager.getInstance(this)
    }
    private lateinit var exitDialog: AlertDialog
    private val sourceType = "BgRemover"
    var imageUri: File? = null

    companion object {
        private const val CUTOUT_REQUEST_CODE = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBgRemoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        imageUri = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        Glide.with(this).load(imageUri).into(binding.imageView)
        binding.imageView.tag = imageUri.toString()

        Log.d("BgRemoverActivity", "Image URI: $imageUri")

        if (imageUri != null) {
            undoStack.push(imageUri)
        }

        binding.btnBgR.setOnClickListener {
            val image = binding.imageView.tag as? File ?: imageUri
            removeBackground(image)
        }

        binding.btnManual.setOnClickListener {
            val updatedImageUri = binding.imageView.tag as? String ?: imageUri

            Log.d("BgRemoverActivity", "BG Remover: $updatedImageUri")
            if (updatedImageUri != null) {
                val intent = Intent(this, CutOutActivity::class.java).apply {
                    putExtra(CutOutActivity.CUTOUT_IMAGE_SOURCE, updatedImageUri)
                }
                startActivityForResult(intent, CUTOUT_REQUEST_CODE)
            } else {
                Toast.makeText(this, "No image available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.edit.setOnClickListener {
            val image = binding.imageView.tag as? String ?: imageUri

            launchEditActivity(image.toString())
        }

        binding.btnFrame.setOnClickListener {
            val image = binding.imageView.tag as? String ?: imageUri
            Log.d("BgRemoverActivity3", "Image URI being sent: $image")
            launchFrameActivity(image.toString())
        }

        binding.llBorder.setOnClickListener {
            val image = binding.imageView.tag as? String ?: imageUri
            launchBorderActivity(image.toString())
        }

        binding.undo.setOnClickListener {
            undo()
        }

        binding.redo.setOnClickListener {
            redo()
        }

        binding.btnBack.setOnClickListener {
            exitDialog.show()
        }

        binding.btnDone.setOnClickListener {
            showDialog(
                context = this@BgRemoverActivity,
                title = "Save Image",
                message = "Are you sure you want to save the Image",
                positiveButtonText = "Yes",
                negativeButtonText = "No",
                onPositiveClick = {
                    val screenShot: Bitmap = Utilities.captureScreenShot(binding.imageView)
                    val savedUri = saveBitmap(this, screenShot)
                    Log.d("SaveActivity", "Image URI saved: $savedUri")
                    if (savedUri != null) {
                        AdsManager.showAd = true
                        adsManager.showAd(
                            this@BgRemoverActivity,
                            object : AdsManager.IInterstitialListener {
                                override fun onError(message: String) {
                                    val intent = Intent(
                                        this@BgRemoverActivity,
                                        SaveActivity::class.java
                                    ).apply {
                                        putExtra("IMAGE_URI", savedUri.toString())
                                    }
                                    startActivity(intent)
                                    finish()
                                }

                                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                    val intent = Intent(
                                        this@BgRemoverActivity,
                                        SaveActivity::class.java
                                    ).apply {
                                        putExtra("IMAGE_URI", savedUri.toString())
                                    }
                                    startActivity(intent)
                                    finish()
                                }

                            })

                    } else {
                        Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT).show()
                    }
                },
                onNegativeClick = {}
            )
        }

        // binding.imageView.setOnTouchListener(MultiTouchListener())

        adsManager.loadBanner(this, object : AdsManager.IBannerListener {
            override fun onBannerLoaded(root: AdView) {
                binding.rlAdCont.removeAllViews()
                binding.rlAdCont.addView(root)
            }

            override fun onBannerError(s: String) {
                binding.rlAdCont.removeAllViews()
            }

        })
        exitDialog = ExitDialogBuilder.Builder(this)
            .withTitle("Discard")
            .withMessage("Are you sure you want to discard?")
            .withButtonListener("Discard", object : ExitDialogBuilder.OnOkClick {
                override fun onClick(dialogs: AlertDialog) {
                    AdsManager.showAd = true
                    adsManager.showAd(
                        this@BgRemoverActivity,
                        object : AdsManager.IInterstitialListener {
                            override fun onError(message: String) {
                                dialogs.dismiss()
                                finish()
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                dialogs.dismiss()
                                finish()
                            }

                        })
                }
            })
            .withCancelButtonListener("No", object : ExitDialogBuilder.OnCancelClick {
                override fun onCancel(dialogs: AlertDialog) {
                    dialogs.dismiss()
                }
            })
            .build()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitDialog.show()
            }

        })

    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uriString = result.data?.getStringExtra("IMAGE_URI")
                uriString?.let { uri ->
                    Log.d("ActivityResult", "Received URI: $uri")
                    Glide.with(this).load(uri).into(binding.imageView)
                    binding.imageView.tag = uri
                }
            } else {
                Log.d("ActivityResult", "No result received or result canceled")
            }
        }

    private fun launchFrameActivity(uri: String) {
        val intent = Intent(this, FrameActivity::class.java).apply {
            putExtra("IMAGE_URI", uri)
            putExtra("SOURCE", sourceType)
        }
        galleryLauncher.launch(intent)
    }

    private fun launchEditActivity(uri: String) {
        val intent = Intent(this, ImageEditActivity::class.java).apply {
            putExtra("IMAGE_URI", uri)
            putExtra("SOURCE", sourceType)
        }
        galleryLauncher.launch(intent)
    }

    private fun launchBorderActivity(uri: String) {
        val intent = Intent(this, BorderActivity::class.java).apply {
            putExtra("IMAGE_URI", uri)
        }
        galleryLauncher.launch(intent)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream = contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("BgRemoverActivity", "Error loading bitmap: ${e.message}")
        }
        return bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CUTOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            val resultUri = data?.getStringExtra(CutOut.CUTOUT_EXTRA_RESULT)
            if (resultUri != null) {
                pushToUndoStack()
                redoStack.clear()
                Glide.with(this).load(resultUri).into(binding.imageView)
                binding.imageView.tag = resultUri.toString()
            } else {
                Toast.makeText(this, "No result received", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeBackground(file: File?) {

        binding.progressBar.visibility = View.VISIBLE
        val remover = RemoveBg(this)

        val bitmap = getBitmapFromUri(Uri.fromFile(file))
        if (bitmap != null) {
            MainScope().launch {
                try {
                    remover.clearBackground(bitmap).collect { outputBitmap ->
                        outputBitmap?.let {
                            pushToUndoStack()
                            redoStack.clear()
                            Glide.with(this@BgRemoverActivity).load(it).into(binding.imageView)
                            val updatedImageUri =
                                saveTempImageToFile(this@BgRemoverActivity, it)
                            binding.progressBar.visibility = android.view.View.GONE
                            updatedImageUri?.let { uri ->
                                binding.imageView.tag = uri.toString()
                            } ?: Toast.makeText(
                                this@BgRemoverActivity,
                                "Failed to save updated image",
                                Toast.LENGTH_SHORT
                            ).show()
                        } ?: Toast.makeText(
                            this@BgRemoverActivity,
                            "Background removal failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@BgRemoverActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun pushToUndoStack() {
        val currentImageUri = binding.imageView.tag as? String
        if (currentImageUri != null) {
            undoStack.push(File(currentImageUri))
        }
    }

    private fun undo() {
        if (undoStack.size > 1) {
            val currentUri = undoStack.pop() // Remove the current image
            redoStack.push(currentUri) // Add the current image to the redo stack
            val previousUri = undoStack.peek() // Get the previous image
            Glide.with(this).load(previousUri).into(binding.imageView)
            binding.imageView.tag = previousUri.toString()
        } else {
            Toast.makeText(this, "No more undo steps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val redoUri = redoStack.pop()
            undoStack.push(redoUri) // Add the redone image to the undo stack
            Glide.with(this).load(redoUri).into(binding.imageView)
            binding.imageView.tag = redoUri.toString()
        } else {
            Toast.makeText(this, "No more redo steps", Toast.LENGTH_SHORT).show()
        }
    }

}
