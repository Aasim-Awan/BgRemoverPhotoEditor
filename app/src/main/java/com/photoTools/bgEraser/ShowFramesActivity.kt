package com.photoTools.bgEraser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.adapter.FramesPagerAdapter
import com.photoTools.bgEraser.databinding.ActivityShowFramesBinding
import com.photoTools.bgEraser.fragments.FrameCategoryFragment
import com.photoTools.bgEraser.utils.OnCategorySelectedListener

class ShowFramesActivity : AppCompatActivity(), OnCategorySelectedListener,
    FrameCategoryFragment.FrameSelectionCallback {

    private lateinit var binding: ActivityShowFramesBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowFramesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabLayoutWithViewPager()

        val allFramesFragment = supportFragmentManager.findFragmentById(R.id.viewPagerFrames) as? AllFramesFragment
        allFramesFragment?.setOnCategorySelectedListener(this)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTabLayoutWithViewPager() {
       // val imageUri = intent.getStringExtra("IMAGE_URI")?.let { Uri.parse(it) }

        val categories = listOf(
            "ALL" to AndroidUtils.ramadan + AndroidUtils.easter + AndroidUtils.christmas + AndroidUtils.halloween
                    + AndroidUtils.birthday + AndroidUtils.holi + AndroidUtils.newyear + AndroidUtils.eid + AndroidUtils.diwali,
            "Ramadan" to AndroidUtils.ramadan,
            "Easter" to AndroidUtils.easter,
            "Christmas" to AndroidUtils.christmas,
            "Halloween" to AndroidUtils.halloween,
            "Birthday" to AndroidUtils.birthday,
            "Holi" to AndroidUtils.holi,
            "New Year" to AndroidUtils.newyear,
            "Eid" to AndroidUtils.eid,
            "Diwali" to AndroidUtils.diwali,
        )
        val adapter = FramesPagerAdapter(this, categories,) { position ->
            FrameCategoryFragment.newInstance(categories[position].second)
        }

        binding.viewPagerFrames.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPagerFrames) { tab, position ->
            tab.text = categories[position].first
        }.attach()
    }

    override fun onFrameSelected(frameUri: Uri) {
        val intent = Intent().apply {
            putExtra("FRAME_URI", frameUri.toString())
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCategorySelected(position: Int) {
        binding.viewPagerFrames.setCurrentItem(position, true)
    }
}
