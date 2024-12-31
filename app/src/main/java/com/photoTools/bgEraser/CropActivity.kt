package com.photoTools.bgEraser

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.adapter.ResizeAdapter
import com.isseiaoki.simplecropview.CropImageView
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.databinding.ActivityCropBinding
import java.io.File

class CropActivity : AppCompatActivity(), View.OnClickListener,
    ResizeAdapter.OnResizeClickListener {

    private lateinit var binding: ActivityCropBinding
    private var cropImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { File(it) }
        Log.d("BgRemoverActivity2", "" + imageUri)
        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(binding.cropImageView)
            binding.cropImageView.setCropMode(CropImageView.CropMode.FREE)
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }

        binding.listResize.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listResize.adapter = ResizeAdapter(this, this)

        binding.txtRotate.isSelected = true

        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.cropRotateLeft.setOnClickListener(this)
        binding.cropRotateRight.setOnClickListener(this)
        binding.flipHorizontal.setOnClickListener(this)
        binding.flipVertical.setOnClickListener(this)
        binding.txtResize.setOnClickListener(this)
        binding.txtRotate.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                setResult(RESULT_CANCELED, Intent())
                finish()
            }

            R.id.btn_done -> {
                cropImage = binding.cropImageView.croppedBitmap

                if (cropImage != null) {
                    val savedUri = Utilities.saveTempImageToFile(this, cropImage!!)
                    if (savedUri != null) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("IMAGE_URI", savedUri.toString())
                        setResult(RESULT_OK, resultIntent)
                    } else {
                        Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "No cropped image available", Toast.LENGTH_SHORT).show()
                }
                finish()
            }

            R.id.crop_rotate_left -> {
                binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D)
            }

            R.id.crop_rotate_right -> {
                binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
            }

            R.id.flip_horizontal -> {
                binding.cropImageView.imageBitmap =
                    flip(binding.cropImageView.imageBitmap, LinearLayoutManager.HORIZONTAL)
            }

            R.id.flip_vertical -> {
                binding.cropImageView.imageBitmap =
                    flip(binding.cropImageView.imageBitmap, LinearLayoutManager.VERTICAL)
            }

            R.id.txt_rotate -> {
                binding.txtRotate.isSelected = true
                binding.txtResize.isSelected = false

                // Rotate selected
                binding.txtRotate.setBackgroundColor(getColor(R.color.colorPrimary))
                binding.txtRotate.setBackgroundResource(R.drawable.round_corner)
                binding.textRotate.setTextColor(getColor(R.color.white))

                // Resize unselected
                binding.txtResize.setBackgroundColor(getColor(R.color.text))
                binding.txtResize.setBackgroundResource(R.drawable.round_corner)
                binding.textResize.setTextColor(getColor(R.color.black))

                // Show Rotate options, hide Resize
                binding.llRotate.visibility = View.VISIBLE
                binding.llResize.visibility = View.GONE
            }

            R.id.txt_resize -> {
                binding.txtRotate.isSelected = false
                binding.txtResize.isSelected = true

                // Resize selected
                binding.txtResize.setBackgroundColor(getColor(R.color.colorPrimary))
                binding.txtResize.setBackgroundResource(R.drawable.round_corner)
                binding.textResize.setTextColor(getColor(R.color.white))

                // Rotate unselected
                binding.txtRotate.setBackgroundColor(getColor(R.color.text))
                binding.txtRotate.setBackgroundResource(R.drawable.round_corner)
                binding.textRotate.setTextColor(getColor(R.color.black))

                // Show Resize options, hide Rotate
                binding.llRotate.visibility = View.GONE
                binding.llResize.visibility = View.VISIBLE
            }

        }
    }

    override fun onResizeClick(position: Int) {
        when (position) {
            0 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.FIT_IMAGE)
            }

            1 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.FREE)
            }

            2 -> {
                binding.cropImageView.setCustomRatio(1, 1)
            }

            3 -> {
                binding.cropImageView.setCustomRatio(4, 5)
            }

            4 -> {
                binding.cropImageView.setCustomRatio(2, 3)
            }

            5 -> {
                binding.cropImageView.setCustomRatio(3, 2)
            }

            6 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_3_4)
            }

            7 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_4_3)
            }

            8 -> {
                binding.cropImageView.setCustomRatio(1, 2)
            }

            9 -> {
                binding.cropImageView.setCustomRatio(2, 1)
            }

            10 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_9_16)
            }

            11 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9)
            }
        }
    }

    private fun flip(src: Bitmap, type: Int): Bitmap {
        val matrix = Matrix()

        when (type) {
            LinearLayoutManager.VERTICAL -> {
                matrix.preScale(1.0f, -1.0f)
            }

            LinearLayoutManager.HORIZONTAL -> {
                matrix.preScale(-1.0f, 1.0f)
            }

            else -> {
                return src
            }
        }

        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

}