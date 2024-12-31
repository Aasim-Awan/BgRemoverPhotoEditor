package com.photoTools.bgEraser

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.adapter.FilterNameAdapter
import com.photoTools.bgEraser.adapters.FilterDetailAdapter
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.databinding.ActivityFilterBinding
import java.io.File

class FilterActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val imageUriString = intent.getStringExtra("IMAGE_URI")?.let { File(it) }

        if (imageUriString != null) {
            Glide.with(this)
                .asBitmap()
                .load(imageUriString)
                .into(binding.imgFilter)
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
            return
        }

        val originalBitmap = BitmapFactory.decodeFile(imageUriString.absolutePath)

//        binding.progressBar.visibility = View.VISIBLE
        val filters = AndroidUtils.filter_clr1

        binding.listFilterstype.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = FilterDetailAdapter(this, filters, originalBitmap, binding.imgFilter,binding.progressBar)
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
                FilterDetailAdapter(this, selectedFilters, originalBitmap, binding.imgFilter,binding.progressBar)
            binding.listFilterstype.adapter = newAdapter
//            binding.progressBar.visibility = View.VISIBLE
            newAdapter.notifyDataSetChanged()
        }

        filterNameAdapter.setOnFilterNameClick(object : FilterNameAdapter.FilterNameClickListener {
            override fun onItemClick(view: View, position: Int) {

                setFilterAdapter(position)
            }
        })

        binding.filterNames.adapter = filterNameAdapter

        binding.btnBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
    }

    private fun saveFilteredImageAndReturnUri() {
        val filteredBitmap = (binding.imgFilter.drawable as BitmapDrawable).bitmap

        val savedUri = Utilities.saveTempImageToFile(this, filteredBitmap!!)
        if (savedUri != null) {
            val resultIntent = Intent()
            resultIntent.putExtra("IMAGE_URI", savedUri.toString())
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                setResult(RESULT_CANCELED, Intent())
                finish()
            }

            R.id.btn_done -> {
                saveFilteredImageAndReturnUri()
            }
        }
    }
}
