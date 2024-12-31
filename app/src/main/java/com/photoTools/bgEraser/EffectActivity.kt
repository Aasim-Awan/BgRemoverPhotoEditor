package com.photoTools.bgEraser

import Utilities
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.ImageEditActivity.Companion.blend_bitmap
import com.photoTools.bgEraser.adapter.FilterNameAdapter
import com.photoTools.bgEraser.model.EffectData
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.databinding.ActivityEffectBinding
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorDodgeBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDarkenBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDifferenceBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDivideBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExclusionBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHardLightBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLightenBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLinearBurnBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMultiplyBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageOverlayBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageScreenBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageTwoInputFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class EffectActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityEffectBinding
    var selectedPosition: Int = 0
    lateinit var original_bitmap: Bitmap
    var blendfilter_position: Int = 0
    var bledImage_position: Int = 0
    var effectBitmap: Bitmap? = null
    var isFromGallery: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEffectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.imgEffect)
            loadOriginalBitmap(imageUri)
            Log.d("BgRemoverActivity4", "" + imageUri)
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }

        callBlendAdapter()

        binding.llBlendType.visibility = View.VISIBLE
        binding.llBlend.isSelected = true
        binding.relativeseek.visibility = View.GONE
        binding.llBlend.setBackgroundColor(resources.getColor(R.color.colorPrimary))

        binding.llBlend.setOnClickListener(this)
        binding.llLight.setOnClickListener(this)
        binding.llTexture.setOnClickListener(this)
        binding.llWeather.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.btnApply.setOnClickListener(this)
    }

    fun callBlendAdapter() {
        binding.listBlendType.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val blendAdapter =
            BlendTypeAdapter(img_blend)
        binding.listBlendType.adapter = blendAdapter

        binding.listBlend.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val blendAdapterList = BlendAdapter(img_blend)
        binding.listBlend.adapter = blendAdapterList
    }

    private fun loadOriginalBitmap(imageUri: File?) {
        val inputStream: InputStream? = contentResolver.openInputStream(Uri.fromFile(imageUri))
        original_bitmap = BitmapFactory.decodeStream(inputStream)
        binding.imgEffect.setImageBitmap(original_bitmap)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.btn_done -> {
                val pd = ProgressDialog(this@EffectActivity)
                pd.setMessage("loading")
                pd.show()

                CoroutineScope(Dispatchers.IO).launch {
                    binding.effect.isDrawingCacheEnabled = true
                    val bitmap = Bitmap.createBitmap(binding.effect.drawingCache)
                    binding.effect.isDrawingCacheEnabled = false

                    val savedUri = Utilities.saveTempImageToFile(this@EffectActivity, bitmap)
                    if (savedUri != null) {
                        runOnUiThread {
                            val resultIntent = Intent()
                            Log.d("EffectActivity", "Saved URI: $savedUri")
                            resultIntent.putExtra("IMAGE_URI", savedUri.toString())
                            setResult(RESULT_OK, resultIntent)
                            pd.dismiss()
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            pd.dismiss()
                            Toast.makeText(
                                this@EffectActivity,
                                "Failed to save image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }


            R.id.btn_apply -> {
                binding.overlayImage.isDrawingCacheEnabled = true
                binding.imgEffect.isDrawingCacheEnabled = true

                val overlayBitmap = Bitmap.createBitmap(binding.overlayImage.drawingCache)
                val baseBitmap = Bitmap.createBitmap(binding.imgEffect.drawingCache)

                binding.overlayImage.isDrawingCacheEnabled = false
                binding.imgEffect.isDrawingCacheEnabled = false

                effectBitmap =
                    Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, baseBitmap.config)
                val canvas = Canvas(effectBitmap!!)
                canvas.drawBitmap(baseBitmap, 0f, 0f, null)
                canvas.drawBitmap(overlayBitmap, 0f, 0f, null)

                binding.imgEffect.setImageBitmap(effectBitmap)
            }

            R.id.btn_back -> {
                setResult(RESULT_CANCELED, Intent())
                finish()
            }

            R.id.ll_blend -> {
                binding.llLight.isSelected = false
                binding.llTexture.isSelected = false
                binding.llWeather.isSelected = false
                binding.llBlend.isSelected = true

                binding.llBlend.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                binding.llLight.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llTexture.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llWeather.setBackgroundColor(resources.getColor(R.color.transparent))

                binding.overlayImage.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(effect_listener())
                //binding.effectGallery.visibility = View.GONE
                binding.relativeseek.visibility = View.GONE
                callBlendAdapter()
                binding.llEffectType.visibility = View.VISIBLE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_light -> {
                binding.llLight.isSelected = true
                binding.llTexture.isSelected = false
                binding.llWeather.isSelected = false
                binding.llBlend.isSelected = false

                binding.llBlend.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llLight.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                binding.llTexture.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llWeather.setBackgroundColor(resources.getColor(R.color.transparent))

                // binding.effectGallery.visibility = View.GONE
                binding.relativeseek.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(effect_listener())
                val light_adapter =
                    FilterNameAdapter(this, resources.getStringArray(R.array.effect_light))
                binding.listBlendType.adapter = light_adapter

                binding.listBlend.adapter = LightAdapter(light1_array, binding.overlayImage)
                binding.overlayImage.visibility = View.VISIBLE

                light_adapter.setOnFilterNameClick(object :
                    FilterNameAdapter.FilterNameClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                binding.overlayImage.visibility = View.GONE
                                setLight(binding.overlayImage, light1_array)
                            }

                            1 -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(light1_array, binding.overlayImage)
                                setLight(binding.overlayImage, light1_array)
                            }

                            2 -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(light2_array, binding.overlayImage)
                                setLight(binding.overlayImage, light2_array)
                            }

                            3 -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(festival_array, binding.overlayImage)
                                setLight(binding.overlayImage, festival_array)
                            }

                            4 -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(love_array, binding.overlayImage)
                                setLight(binding.overlayImage, love_array)
                            }

                            5 -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(prism_array, binding.overlayImage)
                                setLight(binding.overlayImage, prism_array)
                            }

                            6 -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(neon_array, binding.overlayImage)
                                setLight(binding.overlayImage, neon_array)
                            }

                            else -> {
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(light1_array, binding.overlayImage)
                                setLight(binding.overlayImage, light1_array)
                            }
                        }
                    }
                })


                binding.llEffectType.visibility = View.VISIBLE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_texture -> {
                binding.llLight.isSelected = false
                binding.llTexture.isSelected = true
                binding.llWeather.isSelected = false
                binding.llBlend.isSelected = false

                binding.llBlend.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llLight.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llTexture.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                binding.llWeather.setBackgroundColor(resources.getColor(R.color.transparent))


                //  binding.effectGallery.visibility = View.GONE
                binding.relativeseek.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(effect_listener())
                val texture_adapter =
                    FilterNameAdapter(this, resources.getStringArray(R.array.effect_texture))
                binding.listBlendType.adapter = texture_adapter

                binding.listBlend.adapter = LightAdapter(dust_array, binding.overlayImage)
                binding.overlayImage.visibility = View.VISIBLE

                texture_adapter.setOnFilterNameClick(object :
                    FilterNameAdapter.FilterNameClickListener {

                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                // "None" option to display the original image without texture
                                binding.overlayImage.visibility = View.GONE
                            }

                            1 -> {
                                // Dust effect
                                binding.imgEffect.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(dust_array, binding.imgEffect)
                                setLight(binding.imgEffect, dust_array)
                            }

                            2 -> {
                                // Stain effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(stain_array, binding.overlayImage)
                                setLight(binding.overlayImage, stain_array)
                            }

                            3 -> {
                                // Vintage effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(vintage_array, binding.overlayImage)
                                setLight(binding.overlayImage, vintage_array)
                            }

                            4 -> {
                                // Scratch effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(scratch_array, binding.overlayImage)
                                setLight(binding.overlayImage, scratch_array)
                            }

                            else -> {
                                // Default to Dust effect if no match
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(dust_array, binding.overlayImage)
                                setLight(binding.overlayImage, dust_array)
                            }
                        }
                    }
                })

                binding.llEffectType.visibility = View.VISIBLE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_weather -> {
                binding.llLight.isSelected = false
                binding.llTexture.isSelected = false
                binding.llWeather.isSelected = true
                binding.llBlend.isSelected = false

                binding.llBlend.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llLight.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llTexture.setBackgroundColor(resources.getColor(R.color.transparent))
                binding.llWeather.setBackgroundColor(resources.getColor(R.color.colorPrimary))


                //  binding.effectGallery.visibility = View.GONE
                binding.relativeseek.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(effect_listener())

                val weather_adapter =
                    FilterNameAdapter(this, resources.getStringArray(R.array.effect_weather))
                binding.listBlendType.adapter = weather_adapter

                binding.listBlend.adapter = LightAdapter(snow_array, binding.overlayImage)
                binding.overlayImage.visibility = View.VISIBLE

                weather_adapter.setOnFilterNameClick(object :
                    FilterNameAdapter.FilterNameClickListener {

                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                // "None" option to display the original image
                                binding.overlayImage.visibility = View.GONE
                            }

                            1 -> {
                                // Snow effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(snow_array, binding.overlayImage)
                                setLight(binding.overlayImage, snow_array)
                            }

                            2 -> {
                                // Cloud effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(cloud_array, binding.overlayImage)
                                setLight(binding.overlayImage, cloud_array)
                            }

                            3 -> {
                                // Fog effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(fog_array, binding.overlayImage)
                                setLight(binding.overlayImage, fog_array)
                            }

                            4 -> {
                                // Sunlight effect
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(sunlight_array, binding.overlayImage)
                                setLight(binding.overlayImage, sunlight_array)
                            }

                            else -> {
                                // Default to snow effect if no match
                                binding.overlayImage.visibility = View.VISIBLE
                                binding.listBlend.adapter =
                                    LightAdapter(snow_array, binding.overlayImage)
                                setLight(binding.overlayImage, snow_array)
                            }
                        }
                    }
                })


                binding.llEffectType.visibility = View.VISIBLE
                binding.llBlendType.visibility = View.VISIBLE
            }

        }
    }

    var light1_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("Light1_1", R.drawable.light1_1),
        EffectData("Light1_2", R.drawable.light1_2),
        EffectData("Light1_3", R.drawable.light1_3),
        EffectData("Light1_4", R.drawable.light1_4),
        EffectData("Light1_5", R.drawable.light1_5),
        EffectData("Light1_6", R.drawable.light1_6),
        EffectData("Light1_7", R.drawable.light1_7),
        EffectData("Light1_8", R.drawable.light1_8),
        EffectData("Light1_8", R.drawable.light1_9)
    )

    var light2_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("Light2_1", R.drawable.light2_1),
        EffectData("Light2_2", R.drawable.light2_2),
        EffectData("Light2_3", R.drawable.light2_3),
        EffectData("Light2_4", R.drawable.light2_4),
        EffectData("Light2_4", R.drawable.light2_5),
        EffectData("Light2_4", R.drawable.light2_6),
        EffectData("Light2_4", R.drawable.light2_7),
        EffectData("Light2_5", R.drawable.light2_8)
    )

    var festival_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("festival_1", R.drawable.festival_1),
        EffectData("festival_2", R.drawable.festival_2),
        EffectData("festival_3", R.drawable.festival_3),
        EffectData("festival_4", R.drawable.festival_4),
        EffectData("festival_5", R.drawable.festival_5),
        EffectData("festival_6", R.drawable.festival_6)
    )

    var love_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("love_1", R.drawable.love_1),
        EffectData("love_2", R.drawable.love_2),
        EffectData("love_3", R.drawable.love_3),
        EffectData("love_4", R.drawable.love_4),
        EffectData("love_5", R.drawable.love_5)
    )

    var prism_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("prism_1", R.drawable.prism_1),
        EffectData("prism_2", R.drawable.prism_2),
        EffectData("prism_3", R.drawable.prism_3),
        EffectData("prism_4", R.drawable.prism_4),
        EffectData("prism_5", R.drawable.prism_5)
    )

    var neon_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("neon_1", R.drawable.neon_1),
        EffectData("neon_2", R.drawable.neon_2),
        EffectData("neon_3", R.drawable.neon_3),
        EffectData("neon_4", R.drawable.neon_4),
        EffectData("neon_5", R.drawable.neon_5)
    )

    var dust_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("Dust_1", R.drawable.dust_1),
        EffectData("Dust_2", R.drawable.dust_2),
        EffectData("Dust_3", R.drawable.dust_3),
        EffectData("Dust_4", R.drawable.dust_4),
        EffectData("Dust_5", R.drawable.dust_5)
    )

    var scratch_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("scratch_1", R.drawable.scratch_1),
        EffectData("scratch_2", R.drawable.scratch_2),
        EffectData("scratch_3", R.drawable.scratch_3),
        EffectData("scratch_4", R.drawable.scratch_4),
        EffectData("scratch_5", R.drawable.scratch_5)
    )

    var vintage_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("vintage_1", R.drawable.vintage_1),
        EffectData("vintage_2", R.drawable.vintage_2),
        EffectData("vintage_3", R.drawable.vintage_3),
        EffectData("vintage_4", R.drawable.vintage_4),
        EffectData("vintage_5", R.drawable.vintage_5)
    )

    var cloud_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("cloud_1", R.drawable.cloud_1),
        EffectData("cloud_2", R.drawable.cloud_2),
        EffectData("cloud_3", R.drawable.cloud_3),
        EffectData("cloud_4", R.drawable.cloud_4),
        EffectData("cloud_5", R.drawable.cloud_5)
    )

    var fog_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("fog_1", R.drawable.fog_1),
        EffectData("fog_2", R.drawable.fog_2),
        EffectData("fog_3", R.drawable.fog_3),
        EffectData("fog_4", R.drawable.fog_4),
        EffectData("fog_5", R.drawable.fog_5)
    )

    var snow_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("snow_1", R.drawable.snow_1),
        EffectData("snow_2", R.drawable.snow_2),
        EffectData("snow_3", R.drawable.snow_3),
        EffectData("snow_4", R.drawable.snow_4),
        EffectData("snow_5", R.drawable.snow_5)
    )

    var stain_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("stain_1", R.drawable.stain_1),
        EffectData("stain_2", R.drawable.stain_2),
        EffectData("stain_3", R.drawable.stain_3),
        EffectData("stain_4", R.drawable.stain_4),
        EffectData("stain_5", R.drawable.stain_5)
    )

    var sunlight_array: Array<EffectData> = arrayOf(
        EffectData("None", R.drawable.transparent),
        EffectData("sunlight_1", R.drawable.sunlight_1),
        EffectData("sunlight_2", R.drawable.sunlight_2),
        EffectData("sunlight_3", R.drawable.sunlight_3),
        EffectData("sunlight_4", R.drawable.sunlight_4),
        EffectData("sunlight_5", R.drawable.sunlight_5)
    )

    var filters_blend: Array<Class<out GPUImageTwoInputFilter>> = arrayOf(
        GPUImageAlphaBlendFilter::class.java,
        GPUImageLightenBlendFilter::class.java,
        GPUImageScreenBlendFilter::class.java,
        GPUImageColorDodgeBlendFilter::class.java,
        GPUImageLinearBurnBlendFilter::class.java,
        GPUImageDarkenBlendFilter::class.java,
        GPUImageMultiplyBlendFilter::class.java,
        GPUImageOverlayBlendFilter::class.java,
        GPUImageHardLightBlendFilter::class.java,
        GPUImageExclusionBlendFilter::class.java,
        GPUImageDifferenceBlendFilter::class.java,
        GPUImageDivideBlendFilter::class.java
    )

    var img_blend: Array<Int> = arrayOf(
        R.drawable.transparent,
        R.drawable.blend_1,
        R.drawable.blend_2,
        R.drawable.blend_3,
        R.drawable.blend_4,
        R.drawable.blend_5,
        R.drawable.blend_6,
        R.drawable.blend_7,
        R.drawable.blend_8,
        R.drawable.blend_9,
        R.drawable.blend_10,
        R.drawable.blend_11,
        R.drawable.blend_12,
        R.drawable.blend_13,
        R.drawable.blend_14,
        R.drawable.blend_15,
        R.drawable.blend_16,
        R.drawable.blend_17,
        R.drawable.blend_18,
        R.drawable.blend_19,
        R.drawable.blend_20
    )

    fun setLight(img_light: ImageView, effect: Array<EffectData>) {

        img_light.visibility = View.VISIBLE
        val main_bitmap = (binding.imgEffect.getDrawable() as BitmapDrawable).bitmap
        var bitmap =
            (resources.getDrawable(effect[0].icon) as BitmapDrawable).bitmap
        bitmap = Bitmap.createScaledBitmap(
            bitmap,
            main_bitmap.width,
            main_bitmap.height,
            true
        )
        img_light.setImageBitmap(bitmap)

        binding.seekbarBlend.progress = 50
        img_light.imageAlpha = binding.seekbarBlend.progress

    }


    inner class effect_listener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            // Map SeekBar progress (0-100) to imageAlpha (0-255)
            val alpha = (progress * 2.55).toInt()
            binding.overlayImage.imageAlpha = alpha
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            // You can add any custom action when the user starts interacting with the SeekBar
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // You can add any custom action when the user stops interacting with the SeekBar
        }
    }


    inner class LightAdapter(effectList: Array<EffectData>, imageview: ImageView) :
        RecyclerView.Adapter<LightAdapter.LightHolder>() {
        var selectedindex = 0
        var effects: Array<EffectData>?
        var img_overlay = imageview

        init {
            effects = effectList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightHolder {
            val view: View = LayoutInflater.from(this@EffectActivity)
                .inflate(R.layout.item_filter, parent, false)

            return LightHolder(view)
        }

        override fun getItemCount(): Int {
            return effects!!.size
        }

        override fun onBindViewHolder(holder: LightHolder, position: Int) {
            holder.filterName.setText(effects!![position].name)
            holder.thumbnailFilter.setImageResource(effects!![position].icon)

            if (selectedindex == position) {
                holder.rl_filteritem.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            } else {
                holder.rl_filteritem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.rl_filteritem.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {

                    selectedindex = position
                    selectedPosition = position
                    img_overlay.visibility = View.VISIBLE

                    val main_bitmap = (binding.imgEffect.getDrawable() as BitmapDrawable).bitmap
                    var bitmap =
                        (resources.getDrawable(effects!![position].icon) as BitmapDrawable).bitmap
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        main_bitmap.width,
                        main_bitmap.height,
                        true
                    )

                    /* var bmp: Bitmap

                     if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                         bmp = ThumbnailUtils.extractThumbnail(
                             bitmap,
                             original_bitmap.getWidth(),
                             original_bitmap.getHeight(),
                             ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                         )
                     } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                         bmp = ThumbnailUtils.extractThumbnail(
                             bitmap,
                             original_bitmap.getWidth(),
                             original_bitmap.getHeight(),
                             ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                         )
                     } else {
                         bmp = bitmap
                     }*/

                    img_overlay.setImageBitmap(bitmap)

                    binding.seekbarBlend.progress = 90
                    img_overlay.imageAlpha = binding.seekbarBlend.progress
                    notifyDataSetChanged()
                }
            })
        }

        inner class LightHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbnailFilter: ImageView = itemView.findViewById(R.id.thumbnail_filter)
            var filterName: TextView = itemView.findViewById(R.id.filterName)
            var rl_filteritem: RelativeLayout = itemView.findViewById(R.id.rl_filteritem)
        }
    }

    private fun createBlendFilter(
        filterClass: Class<out GPUImageTwoInputFilter>,
        image: Bitmap
    ): GPUImageFilter {
        return try {
            /*      var bmp: Bitmap = image
                  if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                      bmp = ThumbnailUtils.extractThumbnail(
                          image,
                          original_bitmap.getWidth(),
                          original_bitmap.getHeight(),
                          ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                      )
                  } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                      bmp = ThumbnailUtils.extractThumbnail(
                          image,
                          original_bitmap.getWidth(),
                          original_bitmap.getHeight(),
                          ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                      )
                  } else {
                      bmp = image
                  }
      */
            filterClass.newInstance().apply {
                bitmap = image
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GPUImageFilter()
        }
    }

    inner class creaate_bmp() : AsyncTask<Bitmap, Void, Bitmap>() {
        override fun doInBackground(vararg params: Bitmap?): Bitmap? {

            var bmp = params[0]
            bmp = AndroidUtils.resizeImageToNewSize(
                bmp!!,
                bmp.width / 2,
                bmp.height / 2
            )

            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            blend_bitmap = bmp

            if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                blend_bitmap = ThumbnailUtils.extractThumbnail(
                    bmp,
                    original_bitmap.getWidth(),
                    original_bitmap.getHeight(),
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
            } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                blend_bitmap = ThumbnailUtils.extractThumbnail(
                    bmp,
                    original_bitmap.getWidth(),
                    original_bitmap.getHeight(),
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
            } else {
                blend_bitmap = bmp
            }

            return blend_bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)

            val gpuImage1 = GPUImage(this@EffectActivity)
            gpuImage1.setImage(original_bitmap)
            gpuImage1.setFilter(
                createBlendFilter(
                    filters_blend[blendfilter_position],
                    blend_bitmap
                )
            )
            effectBitmap = gpuImage1.bitmapWithFilterApplied
            binding.imgEffect.setImageBitmap(effectBitmap)
        }
    }

    inner class BlendTypeAdapter(images: Array<Int>) :
        RecyclerView.Adapter<BlendTypeAdapter.BlendTypeHolder>() {

        var selectedindex = 0
        var text_Blend_type: Array<String> = arrayOf(
            "Alpha",
            "Lighten",
            "Screen",
            "Color Dodge",
            "Linear Burn",
            "Darken",
            "Multiply",
            "Overlay",
            "Hard Light",
            "Exclusion",
            "Difference",
            "Divide"
        )

        var img_effect = images

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendTypeHolder {
            var view: View = LayoutInflater.from(this@EffectActivity)
                .inflate(R.layout.item_blend_type, parent, false)

            return BlendTypeHolder(view)
        }

        override fun getItemCount(): Int {
            return text_Blend_type.size
        }

        override fun onBindViewHolder(holder: BlendTypeHolder, position: Int) {
            holder.text_blend_type.setText(text_Blend_type[position])

            if (selectedindex == position) {
                holder.item_adjust.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            } else {
                holder.item_adjust.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.item_adjust.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {

                    blendfilter_position = position
                    selectedindex = position

                    var gpuImage1 = GPUImage(this@EffectActivity)
                    gpuImage1.setImage(original_bitmap)

                    var blendFilter: GPUImageFilter
                    if (!isFromGallery) {

                        var image: Bitmap = BitmapFactory.decodeResource(
                            resources, img_effect[bledImage_position]
                        )
                        if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                            image = ThumbnailUtils.extractThumbnail(
                                image,
                                original_bitmap.getWidth(),
                                original_bitmap.getHeight(),
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                            )
                        } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                            image = ThumbnailUtils.extractThumbnail(
                                image,
                                original_bitmap.getWidth(),
                                original_bitmap.getHeight(),
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                            )
                        } else {
                            image = image
                        }

                        blendFilter = createBlendFilter(
                            filters_blend[blendfilter_position],
                            image
                        )
                        gpuImage1.setFilter(blendFilter)
                        binding.imgEffect.setImageBitmap(gpuImage1.bitmapWithFilterApplied)
                    } else {

                        creaate_bmp().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            blend_bitmap
                        )

//                        blendFilter = createBlendFilter(
//                            filters_blend[blendfilter_position],
//                            blend_bitmap
//                        )
                    }


                    notifyDataSetChanged()
                }
            })
        }

        inner class BlendTypeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var text_blend_type: TextView = itemView.findViewById(R.id.text_blend_type) as TextView
            var item_adjust: LinearLayout = itemView.findViewById(R.id.item_adjust) as LinearLayout
        }
    }

    inner class BlendAdapter(images: Array<Int>) :
        RecyclerView.Adapter<BlendAdapter.BlendHolder>() {
        var selectedindex = 0

        var img_effects = images
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendHolder {
            var view: View = LayoutInflater.from(this@EffectActivity)
                .inflate(R.layout.item_blend, parent, false)

            return BlendHolder(view)
        }

        override fun getItemCount(): Int {
            return img_effects.size
        }

        override fun onBindViewHolder(holder: BlendHolder, position: Int) {
            holder.thumbnail_blend.setImageResource(img_effects[position])

            if (selectedindex == position) {
                holder.rl_blenditem.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            } else {
                holder.rl_blenditem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.thumbnail_blend.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    isFromGallery = false
                    bledImage_position = position
                    selectedindex = position

                    var image: Bitmap = BitmapFactory.decodeResource(
                        resources, img_effects[bledImage_position]
                    )
                    if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                        image = ThumbnailUtils.extractThumbnail(
                            image,
                            original_bitmap.getWidth(),
                            original_bitmap.getHeight(),
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                        )
                    } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                        image = ThumbnailUtils.extractThumbnail(
                            image,
                            original_bitmap.getWidth(),
                            original_bitmap.getHeight(),
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                        )
                    } else {
                        image = image
                    }

                    var gpuImage1 = GPUImage(this@EffectActivity)
                    gpuImage1.setImage(original_bitmap)
                    gpuImage1.setFilter(
                        createBlendFilter(
                            filters_blend[blendfilter_position],
                            image
                        )
                    )
                    binding.imgEffect.setImageBitmap(gpuImage1.bitmapWithFilterApplied)
                    notifyDataSetChanged()
                }

            })
        }


        inner class BlendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbnail_blend: ImageView =
                itemView.findViewById(R.id.thumbnail_blend) as ImageView
            var rl_blenditem: RelativeLayout =
                itemView.findViewById(R.id.rl_blenditem) as RelativeLayout
        }
    }

}