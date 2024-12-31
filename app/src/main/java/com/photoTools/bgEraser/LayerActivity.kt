package com.photoTools.bgEraser

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.adapter.ColorAdapter
import com.photoTools.bgEraser.adapter.FontAdapter
import com.photoTools.bgEraser.adapter.StickerAdapter
import com.photoTools.bgEraser.adapter.StickerTabAdapter
import com.photoTools.bgEraser.stickerview.StickerTextView
import com.photoTools.bgEraser.stickerview.StickerView
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.databinding.ActivityLayerBinding
import com.photoTools.bgEraser.stickerview.StickerImageView
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import java.io.File
import java.io.InputStream

class LayerActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityLayerBinding
    private var originalbitmap: Bitmap? = null
    private var blurbitmap: Bitmap? = null
    private var layerBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(binding.imgLayer)
            loadOriginalBitmap(imageUri)
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }

        binding.seekbarBorder.setOnSeekBarChangeListener(Borderlistener())

        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.llText.setOnClickListener(this)
        binding.llSticker.setOnClickListener(this)
        binding.llBorder.setOnClickListener(this)
        binding.borderBack.setOnClickListener(this)
        binding.borderBlur.setOnClickListener(this)

        blurbitmap = originalbitmap

        val gpuImage = GPUImage(this@LayerActivity)
        gpuImage.setImage(blurbitmap)
        gpuImage.setFilter(GPUImageGaussianBlurFilter(5F))

        binding.borderBlur.setImageBitmap(gpuImage.bitmapWithFilterApplied)

        binding.listBorder.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val cAdapter = ColorAdapter(this)
        cAdapter.setOnColorClick(object : ColorAdapter.ColorClickListener {
            override fun onItemClick(view: View, colorName: String) {
                binding.imgLayer.setBackgroundColor(Integer.valueOf(Color.parseColor(colorName)))
            }
        })
        binding.listBorder.adapter = cAdapter

        binding.imgFrame.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                if (event!!.action == MotionEvent.ACTION_DOWN) {
                    hideStickers()
                }
                return true
            }
        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ll_text -> {
                // checkClick()
                opendialogtext()
            }

            R.id.ll_sticker -> {
                // checkClick()
                opendialogSticker()
            }

            R.id.ll_border -> {
                binding.layerLayout.visibility = View.GONE
                binding.borderLayout.visibility = View.VISIBLE
            }

            R.id.border_back -> {
                binding.layerLayout.visibility = View.VISIBLE
                binding.borderLayout.visibility = View.GONE
            }

            R.id.border_blur -> {
                val gpuImage = GPUImage(this@LayerActivity)
                gpuImage.setImage(blurbitmap)
                gpuImage.setFilter(GPUImageGaussianBlurFilter(5F))

                binding.imgFrame.background =
                    BitmapDrawable(resources, gpuImage.bitmapWithFilterApplied)
            }

            R.id.btn_back -> {
                hideStickers()
                setResult(RESULT_CANCELED, Intent())
                finish()
            }

            R.id.btn_done -> {
                hideStickers()

                layerBitmap = getBitmapFromView(binding.imgFrame)

                val savedUri = Utilities.saveTempImageToFile(this, layerBitmap!!)
                if (savedUri != null) {
                    val resultIntent = Intent()
                    Log.d("EffectActivity", "Saved URI: $savedUri")
                    resultIntent.putExtra("IMAGE_URI", savedUri.toString())
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun hideStickers() {
        val fm = binding.imgFrame
        val childcount: Int = binding.imgFrame.childCount

        if (childcount != 0) {
            for (i in 0 until childcount) {
                val v: View = fm.getChildAt(i)

                if (v is StickerView) {
                    v.setControlItemsHidden(true)
                }
            }
        }
    }

    private fun opendialogtext() {

        Log.d("LayerActivity", "opendialogtext called")

        var dialog = Dialog(this)
        val inflater = LayoutInflater.from(this@LayerActivity)
        val subview = inflater.inflate(R.layout.textdialog_layout, null)

        val editText: EditText = subview.findViewById(R.id.dialogEditText)
        val btndone: Button = subview.findViewById(R.id.btn_done)
        val listfont: RecyclerView = subview.findViewById(R.id.list_font)
        val listcolor: RecyclerView = subview.findViewById(R.id.list_color)

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(subview)
        alert.setCancelable(true)

        Log.d("LayerActivity", "Setting up font adapter and color adapter")

        listfont.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val fontadapter: FontAdapter = FontAdapter(this)
        listfont.adapter = fontadapter

        fontadapter.setOnFontClick(object : FontAdapter.FontClickListener {
            override fun onItemClick(view: View, fontName: String) {
                Log.d("LayerActivity", "Font clicked: $fontName")
                editText.typeface = Typeface.createFromAsset(assets, fontName)
            }
        })

        listcolor.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val colorAdapter = ColorAdapter(this)
        listcolor.adapter = colorAdapter
        colorAdapter.setOnColorClick(object : ColorAdapter.ColorClickListener {
            override fun onItemClick(view: View, colorName: String) {
                Log.d("LayerActivity", "Color clicked: $colorName")
                editText.setTextColor(Color.parseColor(colorName))
            }
        })

        btndone.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d("LayerActivity", "Done button clicked in text dialog")
                val tvsticker = StickerTextView(this@LayerActivity)

                tvsticker.tv_main!!.text = editText.text.toString()
                tvsticker.tv_main!!.typeface = editText.typeface
                tvsticker.tv_main!!.setTextColor(editText.textColors)
                binding.imgFrame.addView(tvsticker)
                dialog.dismiss()
            }
        })


        dialog = alert.create()
        dialog.show()
        Log.d("LayerActivity", "Text dialog shown")
    }

    private fun opendialogSticker() {
        var dialog = Dialog(this)
        val inflater = LayoutInflater.from(this@LayerActivity)
        val subview = inflater.inflate(R.layout.stickerdialog_layout, null)

        val liststicker: RecyclerView = subview.findViewById(R.id.list_sticker)
        val liststickertab: RecyclerView = subview.findViewById(R.id.list_sticker_tab)

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(subview)
        alert.setCancelable(true)


        liststickertab.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val stickerTabAdapter = StickerTabAdapter(this)
        liststickertab.adapter = stickerTabAdapter

        liststicker.layoutManager = GridLayoutManager(this, 7, GridLayoutManager.VERTICAL, false)
        var stickerAdapter = StickerAdapter(this, 0)
        liststicker.adapter = stickerAdapter

        stickerTabAdapter.setTabClickListener(object : StickerTabAdapter.StickerTabListener {
            override fun onTabSelected(view: View, position: Int) {
                Log.d("LayerActivity", "Sticker tab selected at position: $position")
                stickerAdapter = StickerAdapter(this@LayerActivity, position)
                liststicker.adapter = stickerAdapter
                stickerAdapter.notifyDataSetChanged()

                stickerAdapter.setOnStickerClick(object : StickerAdapter.StickerListener {
                    override fun onStickerClick(view: View, drawable: Drawable) {
                        Log.d("LayerActivity", "Sticker clicked")
                        val ivsticker = StickerImageView(this@LayerActivity)
                        ivsticker.setImageDrawable(drawable)
                        binding.imgFrame.addView(ivsticker)
                        dialog.dismiss()
                    }
                })
            }
        })

        stickerAdapter.setOnStickerClick(object : StickerAdapter.StickerListener {
            override fun onStickerClick(view: View, drawable: Drawable) {
                Log.d("LayerActivity", "Sticker clicked (default tab)")
                val ivsticker = StickerImageView(this@LayerActivity)
                ivsticker.setImageDrawable(drawable)
                binding.imgFrame.addView(ivsticker)
                dialog.dismiss()
            }
        })

        dialog = alert.create()
        dialog.show()
    }

    inner class Borderlistener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.imgLayer.setPadding(progress, progress, progress, progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    private fun loadOriginalBitmap(imageUri: File?) {
        val inputStream: InputStream? = contentResolver.openInputStream(Uri.fromFile(imageUri))
        originalbitmap = BitmapFactory.decodeStream(inputStream)
        binding.imgLayer.setImageBitmap(originalbitmap)
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

}