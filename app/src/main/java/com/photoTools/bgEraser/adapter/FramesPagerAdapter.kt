package com.photoTools.bgEraser.adapter

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.photoTools.bgEraser.AllFramesFragment
import com.photoTools.bgEraser.fragments.FrameCategoryFragment

class FramesPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val categories: List<Pair<String, List<Int>>>,
    private val onFrameClick: (Int) -> Unit
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            AllFramesFragment()
        } else {
            val categoryFrames = categories[position].second
            FrameCategoryFragment.newInstance(categoryFrames)
        }
    }
}
