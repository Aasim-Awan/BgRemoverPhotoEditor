package com.photoTools.bgEraser.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.photoTools.bgEraser.R
import com.photoTools.bgEraser.utils.FrameCategory

class FrameCategoryAdapter(
    private val categories: List<FrameCategory>,
    private val onClickItem: OnClickItem
) : RecyclerView.Adapter<FrameCategoryAdapter.FrameCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameCategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_frame_category, parent, false)
        return FrameCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: FrameCategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.itemView.visibility = View.VISIBLE
        holder.bind(category)

        holder.frameRecyclerView.setOnClickListener {
            val index = categories.indexOfFirst { it.names == category.names }
            onClickItem.onItemClick(index+1)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class FrameCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val frameRecyclerView: RecyclerView = itemView.findViewById(R.id.rvFrames)
        private val btnViewAll: Button = itemView.findViewById(R.id.btnViewAll)

        fun bind(category: FrameCategory) {
            categoryName.text = category.names
            val frameAdapter = FrameCardsAdapter(category.frames)
            frameRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = frameAdapter
            }

            btnViewAll.setOnClickListener {
                val index = categories.indexOfFirst { it.names == category.names }
                onClickItem.onItemClick(index+1)
            }
        }
    }

    interface OnClickItem {
        fun onItemClick(position: Int)
    }
}
