package com.photoTools.bgEraser

import Utilities
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.databinding.ActivityAdjustBinding
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


class AdjustActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAdjustBinding
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null
    private lateinit var originalBitmap: Bitmap
    private lateinit var gpuImage: GPUImage
    private var filteredBitmap: Bitmap? = null
    private var adjustPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdjustBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        if (imageUri != null) {
            Log.d("AdjustActivity", "Image URI: $imageUri")
            Glide.with(this).load(imageUri).into(binding.imgAdjust)
            loadOriginalBitmap(Uri.fromFile(imageUri))
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }

        setupRecyclerView()
        setupSeekBar()

        binding.btnDone.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
    }

    private fun loadOriginalBitmap(imageUri: Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        originalBitmap = BitmapFactory.decodeStream(inputStream)
        binding.imgAdjust.setImageBitmap(originalBitmap)

        gpuImage = GPUImage(this).apply {
            setImage(originalBitmap)
        }
    }

    private fun setupRecyclerView() {
        binding.listAdjust.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listAdjust.adapter = AdjustAdapter()
    }

    private fun setupSeekBar() {
        binding.seekbarAdjust1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                filterAdjuster?.adjust(progress)
                applyFilter(adjustPosition)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_done -> confirmSelection()
            R.id.btn_back -> cancelSelection()
        }
    }

    private fun confirmSelection() {
        filteredBitmap = gpuImage.bitmapWithFilterApplied

        val pd = ProgressDialog(this@AdjustActivity)
        pd.setMessage("loading")
        pd.show()
        CoroutineScope(Dispatchers.IO).launch {
            if (filteredBitmap != null) {
                val savedUri = Utilities.saveTempImageToFile(this@AdjustActivity, filteredBitmap!!)
                if (savedUri != null) {
                    runOnUiThread{
                        val resultIntent = Intent()
                        Log.d("ADJUST_ACTIVITY", "Saved URI: $savedUri")
                        resultIntent.putExtra("IMAGE_URI", savedUri.toString())
                        setResult(RESULT_OK, resultIntent)
                        pd.dismiss()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        pd.dismiss()
                        Toast.makeText(this@AdjustActivity, "Failed to save cropped image", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            } else {
                runOnUiThread {
                    pd.dismiss()
                    Toast.makeText(this@AdjustActivity, "No filtered image available", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun cancelSelection() {
        setResult(RESULT_CANCELED)
        finish()
    }

    inner class AdjustAdapter : RecyclerView.Adapter<AdjustAdapter.AdjustHolder>() {
        private var selectedIndex = 0
       private var imgsadjust: Array<Int> = arrayOf(
            R.drawable.icon_adjust_contrast,
            R.drawable.icon_adjust_fade,
            R.drawable.icon_adjust_tone,
            R.drawable.icon_adjust_grain,
            R.drawable.icon_adjust_convex,
            R.drawable.icon_adjust_exposure,
            R.drawable.icon_adjust_ambiance,
            R.drawable.icon_adjust_vignette,
            R.drawable.icon_adjust_sharpen,
            R.drawable.icon_adjust_temp,
            R.drawable.icon_adjust_vibrance,
            R.drawable.icon_adjust_saturation,
            R.drawable.icon_adjust_skintone
        )

        private var textsadjust: Array<String> = arrayOf(
            "Contrast",
            "Fade",
            "Tone",
            "Grain",
            "Convex",
            "Exposure",
            "Ambiance",
            "Vignette",
            "Sharpen",
            "Temperature",
            "Vibrance",
            "Saturation",
            "Skintone"
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdjustHolder {
            val view = LayoutInflater.from(this@AdjustActivity).inflate(R.layout.item_adjust, parent, false)
            return AdjustHolder(view)
        }

        override fun getItemCount(): Int = imgsadjust.size

        override fun onBindViewHolder(holder: AdjustHolder, position: Int) {
            holder.imgAdjust.setImageResource(imgsadjust[position])
            holder.textAdjust.text = textsadjust[position]
            holder.itemAdjust.setBackgroundColor(if (selectedIndex == position)
                resources.getColor(R.color.colorPrimary, null)
            else
                resources.getColor(R.color.transparent, null)
            )

            holder.itemAdjust.setOnClickListener {
                selectedIndex = position
                adjustPosition = position
                filterAdjuster = GPUImageFilterTools.FilterAdjuster(filteradjust[position])
                binding.seekbarAdjust1.progress = 40
                filterAdjuster?.adjust(binding.seekbarAdjust1.progress)
                applyFilter(adjustPosition)
                notifyDataSetChanged()
            }
        }

        inner class AdjustHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imgAdjust: ImageView = itemView.findViewById(R.id.img_adjust)
            var textAdjust: TextView = itemView.findViewById(R.id.text_adjust)
            var itemAdjust: LinearLayout = itemView.findViewById(R.id.item_adjust)
        }
    }

    var filteradjust: Array<GPUImageFilter> = arrayOf(
        GPUImageContrastFilter(),
        GPUImageHighlightShadowFilter(0.0f, 1.0f),//fade
        GPUImageSepiaToneFilter(),
        GPUImageOpacityFilter(1.0f),//grain
        GPUImageBilateralBlurFilter(),//convex
        GPUImageExposureFilter(0.0f),
        GPUImageRGBFilter(1.0f, 1.0f, 1.0f),  //  ambiance
        GPUImageVignetteFilter(PointF(0.5f, 0.5f), floatArrayOf(0.0f, 0.0f, 0.0f), 0.3f, 0.75f),
        GPUImageSharpenFilter(),
        GPUImageWhiteBalanceFilter(),
        GPUImageVibranceFilter(),
        GPUImageSaturationFilter(1.0f),
        GPUImageColorBalanceFilter()//skintone
    )

    private fun applyFilter(position: Int) {
        try {
            gpuImage.setFilter(filteradjust[position])
            filteredBitmap = gpuImage.bitmapWithFilterApplied
            binding.imgAdjust.setImageBitmap(filteredBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to apply filter: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
