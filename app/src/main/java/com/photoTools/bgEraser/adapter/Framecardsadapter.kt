package com.photoTools.bgEraser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.photoTools.bgEraser.R

class FrameCardsAdapter(
    private val frames: List<Int> // List of drawable resource IDs for frames
) : RecyclerView.Adapter<FrameCardsAdapter.FrameCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_frame_card, parent, false)
        return FrameCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FrameCardViewHolder, position: Int) {
        val frame = frames[position]
        holder.bind(frame)
    }

    override fun getItemCount(): Int = frames.size

    class FrameCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivFrame)

        fun bind(frameRes: Int) {
            imageView.setImageResource(frameRes)
        }
    }
}

