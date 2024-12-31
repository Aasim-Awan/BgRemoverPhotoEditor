package com.photoTools.bgEraser

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.databinding.PhotoPickerItemBinding

class PhotoPickerAdapter(private  val context: Context, private val list: List<Uri>, private val selectedList: List<Uri>, private val listener: ClickListener):
    RecyclerView.Adapter<PhotoPickerAdapter.ViewHolder>() {
    private var pickerType = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PhotoPickerItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("selected File" ,"Selected File => ${list.size}")
        Glide.with(context).load(list[position]).into(holder.binding.ivImage)
        if (pickerType != 0){
            holder.binding.cbSelected.visibility = View.VISIBLE
        }else{
            holder.binding.cbSelected.visibility = View.GONE
        }
        holder.binding.cbSelected.isChecked = selectedList.contains(list[position])
        holder.binding.ivImage.setOnClickListener {
            if (selectedList.size >= 10){
                Toast.makeText(context, "You can't select more than 10 images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!holder.binding.cbSelected.isChecked){
                holder.binding.cbSelected.isChecked = true
            }else{
                holder.binding.cbSelected.isChecked = false
            }
            listener.onItemClick(holder.binding.cbSelected.isChecked, list[position])

        }
    }

    fun setPickerType(type:Int){
        pickerType = type
    }


    class ViewHolder(val binding: PhotoPickerItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    interface ClickListener{
        fun onItemClick(isChecked: Boolean, uri: Uri) {

        }
    }
}