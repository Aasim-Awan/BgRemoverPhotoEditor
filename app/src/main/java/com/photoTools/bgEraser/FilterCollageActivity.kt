package com.photoTools.bgEraser

import Utilities
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.photoTools.bgEraser.MainActivity.Companion.isFromSaved
import com.photoTools.bgEraser.adapter.FilterNameAdapter
import com.photoTools.bgEraser.adapters.FilterDetailAdapter
import com.photoTools.bgEraser.model.FilterData
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.databinding.ActivityFilterCollageBinding

class FilterCollageActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFilterCollageBinding
    private lateinit var bmp: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterCollageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bitmapPath = this.cacheDir.absolutePath + "/tempBMP"
        bmp = BitmapFactory.decodeFile(bitmapPath)
        binding.imgFilter.setImageBitmap(bmp)

        binding.btnDone.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)

        val filters = AndroidUtils.filter_clr1

        binding.listFilterstype.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = FilterDetailAdapter(this,filters , bmp, binding.imgFilter,binding.progressBar)
        binding.listFilterstype.adapter = adapter

        binding.filterNames.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val filterNameAdapter = FilterNameAdapter(this, resources.getStringArray(R.array.filters))

        fun setFilterAdapter(position: Int) {
            val selectedFilters = when (position) {
                0 -> AndroidUtils.filter_fresh
                1 -> AndroidUtils.filter_clr2
                2 -> AndroidUtils.filter_duo
                3 -> AndroidUtils.filter_pink
                4 -> AndroidUtils.filter_fresh
                5 -> AndroidUtils.filter_euro
                6 -> AndroidUtils.filter_dark
                7 -> AndroidUtils.filter_ins
                8 -> AndroidUtils.filter_elegant
                9 -> AndroidUtils.filter_golden
                10 -> AndroidUtils.filter_tint
                11 -> AndroidUtils.filter_film
                12 -> AndroidUtils.filter_lomo
                13 -> AndroidUtils.filter_movie
                14 -> AndroidUtils.filter_retro
                15 -> AndroidUtils.filter_bw
                else -> AndroidUtils.filter_clr1
            }

            val newAdapter =
                FilterDetailAdapter(this, selectedFilters, bmp, binding.imgFilter,binding.progressBar)
            binding.listFilterstype.adapter = newAdapter
            newAdapter.notifyDataSetChanged()
        }

        filterNameAdapter.setOnFilterNameClick(object : FilterNameAdapter.FilterNameClickListener {
            override fun onItemClick(view: View, position: Int) {
                setFilterAdapter(position)
            }
        })

        binding.filterNames.adapter = filterNameAdapter

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_done -> {
                isFromSaved = true

                Utilities.showDialog(
                    context = this,
                    title = "Save Image",
                    message = "Are you sure you want to save this image?",
                    positiveButtonText = "Yes",
                    negativeButtonText = "No",
                    onPositiveClick = {
                        val screenShot: Bitmap = Utilities.captureScreenShot(binding.imgFilter)
                        val savedUri = Utilities.saveBitmap(this, screenShot)
                        Log.d("SaveActivity", "Image URI saved: $savedUri")
                        if (savedUri != null) {
                            val intent = Intent(this, SaveActivity::class.java).apply {
                                putExtra("IMAGE_URI", savedUri.toString())
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onNegativeClick = {}
                )
            }

            R.id.btn_back -> {
                Utilities.showDialog(
                    context = this@FilterCollageActivity,
                    title = "Discard Image",
                    message = "Are you sure you want to discard this image?",
                    positiveButtonText = "Yes",
                    negativeButtonText = "No",
                    onPositiveClick = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    onNegativeClick = {}
                )
            }
        }
    }

}
