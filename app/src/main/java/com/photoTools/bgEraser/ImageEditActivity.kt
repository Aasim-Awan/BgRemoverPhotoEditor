package com.photoTools.bgEraser

import OptionAdapter
import Utilities
import Utilities.saveBitmap
import Utilities.showDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.databinding.ActivityImageEditBinding
import java.io.File

class ImageEditActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityImageEditBinding
    lateinit var display: DisplayMetrics
    var density: Float = 0.0f
    internal var D_height: Int = 0
    internal var D_width: Int = 0
    var imageFile: File? = null
    var array_img: TypedArray? = null
    var array_text: Array<String>? = null
    private val adsManager by lazy {
        AdsManager.getInstance(this)
    }
    private lateinit var exitDialog: AlertDialog

    companion object {
        lateinit var blend_bitmap: Bitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        array_img = resources.obtainTypedArray(R.array.img_options)
        array_text = resources.getStringArray(R.array.text_options)

        display = resources.displayMetrics
        density = resources.displayMetrics.density
        D_width = display.widthPixels
        D_height = (display.heightPixels.toFloat() - density * 150.0f).toInt()

        val sourceType = intent.getStringExtra("SOURCE")

        Log.d("BgRemoverActivity", "" + sourceType)

        imageFile = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        overwriteAndLoadImage(imageFile)

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
        Log.d("CameraIntentUri3", "" + imageFile)

        val adapter = OptionAdapter(
            resources.obtainTypedArray(R.array.img_options),
            resources.getStringArray(R.array.text_options)
        ) { position ->
            when (position) {
                0 -> {
                    if (imageFile != null) {
                        val intent = Intent(this, CropActivity::class.java)
                        intent.putExtra("IMAGE_URI", imageFile.toString())
                        Log.d("BgRemoverActivity", "" + imageFile)
                        cropImageLauncher.launch(intent)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                1 -> {
                    if (imageFile != null) {
                        val intent = Intent(this, FilterActivity::class.java)
                        intent.putExtra("IMAGE_URI", imageFile.toString())  // Pass updated image URI
                        imageResultLauncher.launch(intent)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                2 -> {
                    if (imageFile != null) {
                        val intent = Intent(this, EffectActivity::class.java)
                        intent.putExtra("IMAGE_URI", imageFile.toString())  // Pass updated image URI
                        effectImageLauncher.launch(intent)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                3 -> {
                    if (imageFile != null) {
                        val intent = Intent(this, AdjustActivity::class.java)
                        intent.putExtra("IMAGE_URI", imageFile.toString())  // Pass updated image URI
                        adjustImageLauncher.launch(intent)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                4 -> {
                    if (imageFile != null) {
                        val intent = Intent(this, HSLActivity::class.java)
                        intent.putExtra(
                            "IMAGE_URI",
                            imageFile.toString()
                        )  // Pass updated image URI")
                        hslImageLauncher.launch(intent)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                5 -> {
                    if (imageFile != null) {
                        val intent = Intent(this, LayerActivity::class.java)
                        intent.putExtra("IMAGE_URI", imageFile.toString())
                        layerImageLauncher.launch(intent)
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.listOptions.adapter = adapter
        binding.listOptions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.btnDownload.setOnClickListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitDialog.show()
            }
        })

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
                        this@ImageEditActivity,
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

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_download -> {
                showDialog(
                    context = this@ImageEditActivity,
                    title = "Save Image",
                    message = "Are you sure you want to save the Image",
                    positiveButtonText = "Yes",
                    negativeButtonText = "No",
                    onPositiveClick = {
                        val screenShot: Bitmap = Utilities.captureScreenShot(binding.imgMain)
                        val savedUri = saveBitmap(this, screenShot)
                        Log.d("SaveActivity", "Image URI saved: $savedUri")
                        if (savedUri != null) {
                            val intent = Intent(this, SaveActivity::class.java).apply {
                                putExtra("IMAGE_URI", savedUri.toString())
                            }
                            AdsManager.showAd = true
                            adsManager.showAd(
                                this@ImageEditActivity,
                                object : AdsManager.IInterstitialListener {
                                    override fun onError(message: String) {
                                        startActivity(intent)
                                        finish()
                                    }

                                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                        startActivity(intent)
                                        finish()
                                    }
                                })
                        } else {
                            Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    onNegativeClick = {}
                )
            }

            binding.btnDone.id -> {
                val image =  binding.imgMain.tag as? String ?: imageFile
                    val resultIntent = Intent().apply {
                        putExtra("IMAGE_URI", image.toString())
                        Log.d("BgRemoverActivity5", "" + image)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
            }

            binding.btnBack.id -> {
                exitDialog.show()
            }
        }
    }

    private val cropImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val croppedImageUri = result.data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                if (croppedImageUri != null) {
                    overwriteAndLoadImage(croppedImageUri)
                } else {
                    Toast.makeText(this, "No cropped image returned", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val imageResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val filteredImageUri = result.data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                if (filteredImageUri != null) {
                    overwriteAndLoadImage(filteredImageUri)
                } else {
                    Toast.makeText(this, "No filtered image returned", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val adjustImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val filteredImageUri = result.data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                if (filteredImageUri != null) {
                    overwriteAndLoadImage(filteredImageUri)
                } else {
                    Toast.makeText(this, "No filtered image returned", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val effectImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val effectImageUri = result.data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                if (effectImageUri != null) {
                    overwriteAndLoadImage(effectImageUri)
                } else {
                    Toast.makeText(this, "No filtered image returned", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val hslImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val hslImageUri = result.data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                if (hslImageUri != null) {
                    overwriteAndLoadImage(hslImageUri)
                } else {
                    Toast.makeText(this, "No filtered image returned", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val layerImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val layerImageUri = result.data?.getStringExtra("IMAGE_URI")?.let { File(it) }
                if (layerImageUri != null) {
                    overwriteAndLoadImage(layerImageUri)
                } else {
                    Toast.makeText(this, "No filtered image returned", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun overwriteAndLoadImage(imageUri: File?) {

        Glide.with(this)
            .load(imageUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imgMain)


        this.imageFile = imageUri
    }

}