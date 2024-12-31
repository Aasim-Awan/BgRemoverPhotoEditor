package com.photoTools.bgEraser.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.photoTools.bgEraser.R

class FilterNamesAdapter(
    private val context: Context,
    private val filterNames: Array<String>
) : RecyclerView.Adapter<FilterNamesAdapter.FilterNameHolder>() {

    private var selectedIndex = 0
    private lateinit var filterNameClickListener: FilterNameClickListener

    fun setOnFilterNameClick(listener: FilterNameClickListener) {
        this.filterNameClickListener = listener
    }

    interface FilterNameClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterNameHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_font_tab, parent, false)
        return FilterNameHolder(view)
    }

    override fun getItemCount() = filterNames.size

    override fun onBindViewHolder(holder: FilterNameHolder, position: Int) {
        holder.txtFilterTab.text = filterNames[position]

        // Highlight the selected item
        holder.llFilterItem.setBackgroundColor(
            if (selectedIndex == position) {
                context.resources.getColor(R.color.colorPrimary)
            }else context.resources.getColor(R.color.transparent)
        )

        holder.llFilterItem.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()  // Refresh the adapter to reflect changes
            filterNameClickListener.onItemClick(position)  // Trigger the click listener
        }
    }

    inner class FilterNameHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtFilterTab: TextView = itemView.findViewById(R.id.txt_filter_tab)
        val llFilterItem: LinearLayout = itemView.findViewById(R.id.ll_filteritem)
    }
}
