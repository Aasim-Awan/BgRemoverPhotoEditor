package com.photoTools.bgEraser

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.photoTools.bgEraser.databinding.ItemFramesBinding

class FrameAdapter2(
    private val context: Context,
    private var frames: Array<Int>, // Changed to var for mutability
    private val onFrameClick: (Int) -> Unit
) : RecyclerView.Adapter<FrameAdapter2.FrameViewHolder>() {
    private var selectedIndex = -1 // Start with no selection

    inner class FrameViewHolder(val binding: ItemFramesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val binding = ItemFramesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FrameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val frameId = frames[position]
        holder.binding.imgFrame.setImageResource(frameId)

        holder.itemView.setOnClickListener {
            if (selectedIndex != position) {
                notifyItemChanged(selectedIndex)
                selectedIndex = position
                notifyItemChanged(selectedIndex)
                onFrameClick(frameId)
            }
        }
    }

    fun updateFrames(newFrames: Array<Int>) {
        frames = newFrames
        notifyDataSetChanged()
    }

    override fun getItemCount() = frames.size
}
