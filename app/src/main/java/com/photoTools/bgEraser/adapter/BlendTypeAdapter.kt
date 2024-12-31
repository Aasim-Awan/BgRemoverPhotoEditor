//package com.photoTools.bgEraser.adapter
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.media.ThumbnailUtils
//import android.os.AsyncTask
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.photoTools.bgEraser.EffectActivity
//import com.photoTools.bgEraser.R
//import jp.co.cyberagent.android.gpuimage.GPUImage
//import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
//
//class BlendTypeAdapter(
//    private val images: Array<Int>,
//    private val effectActivity: EffectActivity,
//    private val originalBitmap: Bitmap,
//    private val isFromGallery: Boolean,
//    private val blendImagePosition: Int
//) : RecyclerView.Adapter<BlendTypeAdapter.BlendTypeHolder>() {
//
//    private var selectedIndex = 0
//    private val textBlendType: Array<String> = arrayOf(
//        "Alpha", "Normal", "Lighten", "Screen", "Color Dodge", "Linear Burn", "Darken",
//        "Multiply", "Overlay", "Hard Light", "Exclusion", "Difference", "Divide"
//    )
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendTypeHolder {
//        val view = LayoutInflater.from(effectActivity)
//            .inflate(R.layout.item_blend_type, parent, false)
//        return BlendTypeHolder(view)
//    }
//
//    override fun getItemCount(): Int = textBlendType.size
//
//    override fun onBindViewHolder(holder: BlendTypeHolder, position: Int) {
//        holder.textBlendType.text = textBlendType[position]
//
//        // Highlight selected item
//        holder.itemAdjust.setBackgroundColor(
//            if (selectedIndex == position)
//                effectActivity.resources.getColor(R.color.colorPrimary)
//            else
//                effectActivity.resources.getColor(R.color.transparent)
//        )
//
//        holder.itemAdjust.setOnClickListener {
//            selectedIndex = position
//            effectActivity.blendfilter_position = position // Update blend filter position
//
//            // Apply blend filter
//            applyBlendFilter(position)
//
//            // Refresh the adapter to update selected item
//            notifyDataSetChanged()
//        }
//    }
//
//    private fun applyBlendFilter(position: Int) {
//        val gpuImage = GPUImage(effectActivity).apply {
//            setImage(originalBitmap)  // Set original image
//        }
//
//        val blendFilter: GPUImageFilter
//        if (!isFromGallery) {
//            // Decode resource image
//            var image = BitmapFactory.decodeResource(effectActivity.resources, images[blendImagePosition])
//
//            // Resize image to match original bitmap dimensions
//            image = resizeBitmapToMatchOriginal(image, originalBitmap)
//
//            // Create and apply blend filter
//            blendFilter = effectActivity.createBlendFilter(
//                effectActivity.filters_blend[position],
//                image
//            )
//
//            gpuImage.setFilter(blendFilter)
//            effectActivity.effectBitmap = gpuImage.bitmapWithFilterApplied
//            effectActivity.binding.imgEffect.setImageBitmap(effectActivity.effectBitmap)
//        } else {
//            // If loading image from gallery
//            effectActivity.creaate_bmp().executeOnExecutor(
//                AsyncTask.THREAD_POOL_EXECUTOR,
//              //  effectActivity.blend_bitmap
//            )
//        }
//    }
//
//    private fun resizeBitmapToMatchOriginal(image: Bitmap, original: Bitmap): Bitmap {
//        return if (original.width > original.height) {
//            ThumbnailUtils.extractThumbnail(
//                image, original.width, original.height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT
//            )
//        } else {
//            ThumbnailUtils.extractThumbnail(
//                image, original.width, original.height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT
//            )
//        }
//    }
//
//    inner class BlendTypeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val textBlendType: TextView = itemView.findViewById(R.id.text_blend_type)
//        val itemAdjust: LinearLayout = itemView.findViewById(R.id.item_adjust)
//    }
//}
