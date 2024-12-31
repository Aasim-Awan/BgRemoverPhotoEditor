package com.photoTools.bgEraser

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.databinding.PhotoPickerSelectedItemBinding

class SelectedPhotoAdapter(private  val context: Context, private val list: List<Uri>, private val listener: ClickListener):
    RecyclerView.Adapter<SelectedPhotoAdapter.ViewHolder>() {
    //private var list: MutableList<Uri> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PhotoPickerSelectedItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("selected File" ,"Selected File => ${list.size}")
        Glide.with(context).load(list[position]).into(holder.binding.ivImage)
        //holder.binding.ivImage.setImageURI(list[position])
        holder.binding.ivRemove.setOnClickListener {
            listener.onItemRemoved(list[position])
            //list.removeAt(position)
            //notifyItemRemoved(position)
        }
    }


    /*fun insertItem(uri:Uri){
        list.add(uri)
        notifyDataSetChanged()
    }

    fun removeItem(uri: Uri){
        val pos = list.indexOf(uri)
        list.removeAt(pos)
        notifyItemRemoved(pos)
    }*/

    fun getSelectedList(): List<Uri> {
        return list
    }

    class ViewHolder(val binding: PhotoPickerSelectedItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    interface ClickListener{
        fun onItemRemoved(uri: Uri) {

        }
    }
}