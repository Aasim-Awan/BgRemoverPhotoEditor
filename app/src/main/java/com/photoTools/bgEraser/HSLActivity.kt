package com.photoTools.bgEraser

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.databinding.ActivityHslBinding
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class HSLActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityHslBinding
    private lateinit var originalbitmap: Bitmap
    private lateinit var hslbitmap: Bitmap
    private var filterAdjusterhue: GPUImageFilterTools.FilterAdjuster? = null
    private var filterAdjustersat: GPUImageFilterTools.FilterAdjuster? = null
    private var filterAdjusterbright: GPUImageFilterTools.FilterAdjuster? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHslBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.imgHsl)
            loadOriginalBitmap(imageUri)
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }

        binding.seekbarHue.setOnSeekBarChangeListener(Huelistener())
        binding.seekbarSaturation.setOnSeekBarChangeListener(Saturationlistener())
        binding.seekbarBrightness.setOnSeekBarChangeListener(Brightnesslistener())

        hslbitmap = originalbitmap
        groupfilter(
            binding.seekbarHue.progress,
            binding.seekbarSaturation.progress,
            binding.seekbarBrightness.progress
        )

        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnBack.id -> {
                setResult(RESULT_CANCELED)
                finish()
            }

            binding.btnDone.id -> {
                val pd = ProgressDialog(this@HSLActivity)
                pd.setMessage("loading")
                pd.show()
                CoroutineScope(Dispatchers.IO).launch {
                val savedUri = Utilities.saveTempImageToFile(this@HSLActivity, hslbitmap)
                if (savedUri != null) {
                    runOnUiThread {
                        val resultIntent = Intent()
                        Log.d("HSLACTIVITY", "Saved URI: $savedUri")
                        resultIntent.putExtra("IMAGE_URI", savedUri.toString())
                        setResult(RESULT_OK, resultIntent)
                        finish()
                        pd.dismiss()
                    }

                } else {
                    runOnUiThread{
                    Toast.makeText(this@HSLActivity, "Failed to save cropped image", Toast.LENGTH_SHORT)
                        .show()
                        pd.dismiss()
                    }
                }}
            }

        }
    }

    fun groupfilter(progresshue: Int, progresssat: Int, progressbright: Int) {

        val gpuImage1 = GPUImage(this@HSLActivity)
        gpuImage1.setImage(originalbitmap)

        val group = GPUImageFilterGroup()
        group.addFilter(GPUImageHueFilter())
        group.addFilter(GPUImageSaturationFilter())
        group.addFilter(GPUImageBrightnessFilter())

        val mergedFilters = group.mergedFilters
        filterAdjusterhue = GPUImageFilterTools.FilterAdjuster(mergedFilters[0])
        filterAdjusterhue!!.adjust(progresshue)
        filterAdjustersat = GPUImageFilterTools.FilterAdjuster(mergedFilters[1])
        filterAdjustersat!!.adjust(progresssat)
        filterAdjusterbright = GPUImageFilterTools.FilterAdjuster(mergedFilters[2])
        filterAdjusterbright!!.adjust(progressbright)

        gpuImage1.setFilter(group)
        hslbitmap = gpuImage1.bitmapWithFilterApplied
        binding.imgHsl.setImageBitmap(hslbitmap)
    }

    inner class Huelistener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filterAdjusterhue!!.adjust(progress)

            groupfilter(
                progress,
                binding.seekbarSaturation.progress,
                binding.seekbarBrightness.progress
            )
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    inner class Saturationlistener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filterAdjustersat!!.adjust(progress)
            groupfilter(binding.seekbarHue.progress, progress, binding.seekbarBrightness.progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    inner class Brightnesslistener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filterAdjusterbright!!.adjust(progress)

            groupfilter(binding.seekbarHue.progress, binding.seekbarSaturation.progress, progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    private fun loadOriginalBitmap(imageUri: File?) {
        val inputStream: InputStream? = contentResolver.openInputStream(Uri.fromFile(imageUri))
        originalbitmap = BitmapFactory.decodeStream(inputStream)
        binding.imgHsl.setImageBitmap(originalbitmap)
    }

}