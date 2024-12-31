package com.photoTools.bgEraser

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.photoTools.bgEraser.adapter.FrameCategoryAdapter
import com.photoTools.bgEraser.databinding.FragmentAllFramesBinding
import com.photoTools.bgEraser.utils.FrameCategory
import com.photoTools.bgEraser.utils.OnCategorySelectedListener

class AllFramesFragment : Fragment(R.layout.fragment_all_frames) {

    private lateinit var binding: FragmentAllFramesBinding
    private lateinit var categories: List<FrameCategory>
    private var categorySelectedListener: OnCategorySelectedListener? = null

    fun setOnCategorySelectedListener(listener: OnCategorySelectedListener) {
        categorySelectedListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAllFramesBinding.bind(view)

        categories = listOf(
            FrameCategory(
                "All",
                listOf(R.drawable.ramadan1, R.drawable.ramadan2, R.drawable.ramadan3)
            ),
            FrameCategory(
                "Ramadan",
                listOf(
                    R.drawable.ramadan1,
                    R.drawable.ramadan2,
                    R.drawable.ramadan3,
                    R.drawable.ramadan4,
                    R.drawable.ramadan5
                )
            ),
            FrameCategory(
                "Easter",
                listOf(
                    R.drawable.easter1,
                    R.drawable.easter2,
                    R.drawable.easter3,
                    R.drawable.easter4,
                    R.drawable.easter5
                )
            ),
            FrameCategory(
                "Christmas",
                listOf(
                    R.drawable.christmas1,
                    R.drawable.christmas2,
                    R.drawable.christmas3,
                    R.drawable.christmas4,
                    R.drawable.christmas5
                )
            ),
            FrameCategory(
                "Halloween",
                listOf(
                    R.drawable.halloween1,
                    R.drawable.halloween2,
                    R.drawable.halloween3,
                    R.drawable.halloween4,
                    R.drawable.halloween5
                )
            ),
            FrameCategory(
                "Birthday",
                listOf(
                    R.drawable.birthday1,
                    R.drawable.birthday2,
                    R.drawable.birthday3,
                    R.drawable.birthday4,
                    R.drawable.birthday5
                )
            ),
            FrameCategory(
                "Holi",
                listOf(R.drawable.holi1, R.drawable.holi2, R.drawable.holi3, R.drawable.holi4)
            ),
            FrameCategory(
                "New Year",
                listOf(
                    R.drawable.newyear1,
                    R.drawable.newyear2,
                    R.drawable.newyear3,
                    R.drawable.newyear4,
                    R.drawable.newyear5
                )
            ),
            FrameCategory(
                "Diwali",
                listOf(
                    R.drawable.diwali1,
                    R.drawable.diwali2,
                    R.drawable.diwali3,
                    R.drawable.diwali4,
                    R.drawable.diwali5
                )
            ),
            FrameCategory("Eid", listOf(R.drawable.eid1, R.drawable.eid2, R.drawable.eid3))
        )

        val adapter = FrameCategoryAdapter(removeAllCategoriesExcept(), object : FrameCategoryAdapter.OnClickItem {
            override fun onItemClick(position: Int) {
                (requireContext() as ShowFramesActivity).onCategorySelected(position)
            }
        })

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun removeAllCategoriesExcept(): List<FrameCategory> {
        val list = categories.filter {
            it.names != "All"
        }
        return list
    }
}