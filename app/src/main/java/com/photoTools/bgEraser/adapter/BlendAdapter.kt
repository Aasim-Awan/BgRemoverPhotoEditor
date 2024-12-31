//package com.photoTools.bgEraser.adapter
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.media.ThumbnailUtils
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.RelativeLayout
//import androidx.recyclerview.widget.RecyclerView
//import com.photoTools.bgEraser.EffectActivity
//import com.photoTools.bgEraser.R
//import jp.co.cyberagent.android.gpuimage.GPUImage
//import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
//import jp.co.cyberagent.android.gpuimage.filter.GPUImageTwoInputFilter
//
//class BlendAdapter(
//    private val images: Array<Int>,    // Image resources to blend
//    private val activity: EffectActivity, // The context or activity to apply effects
//    private val originalBitmap: Bitmap,  // Original image for applying blend effects
//    private val filtersBlend: Array<Class<out GPUImageTwoInputFilter>> // Blend filters
//) : RecyclerView.Adapter<BlendAdapter.BlendHolder>() {
//
//    private var selectedIndex = 0
//    private var blendImagePosition = 0
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendHolder {
//        val view: View = LayoutInflater.from(activity)
//            .inflate(R.layout.item_blend, parent, false)
//        return BlendHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return images.size
//    }
//
//    override fun onBindViewHolder(holder: BlendHolder, position: Int) {
//        // Set the image thumbnail for blend type
//        holder.thumbnailBlend.setImageResource(images[position])
//
//        // Highlight the selected blend effect
//        if (selectedIndex == position) {
//            holder.rlBlendItem.setBackgroundColor(activity.resources.getColor(R.color.colorPrimary))
//        } else {
//            holder.rlBlendItem.setBackgroundColor(activity.resources.getColor(R.color.transparent))
//        }
//
//        holder.thumbnailBlend.setOnClickListener {
//        var isFromGallery = false
//            blendImagePosition = position
//            selectedIndex = position
//
//            // Prepare the image based on aspect ratio
//            var blendImage: Bitmap = BitmapFactory.decodeResource(
//                activity.resources, images[blendImagePosition]
//            )
//            blendImage = resizeAndCropToMatchOriginal(blendImage, originalBitmap)
//
//            // Apply the blend effect using GPUImage
//            applyBlendEffect(blendImage)
//
//            // Notify the adapter that the data set has changed, so it can refresh the UI
//            notifyDataSetChanged()
//        }
//    }
//
//    /**
//     * Resize and crop the blend image to match the aspect ratio of the original image.
//     */
//    private fun resizeAndCropToMatchOriginal(blendImage: Bitmap, originalBitmap: Bitmap): Bitmap {
//        return if (originalBitmap.width > originalBitmap.height) {
//            ThumbnailUtils.extractThumbnail(
//                blendImage,
//                originalBitmap.width,
//                originalBitmap.height,
//                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
//            )
//        } else if (originalBitmap.width < originalBitmap.height) {
//            ThumbnailUtils.extractThumbnail(
//                blendImage,
//                originalBitmap.width,
//                originalBitmap.height,
//                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
//            )
//        } else {
//            blendImage
//        }
//    }
//
//    /**
//     * Apply the selected blend effect on the original image and update the view.
//     */
//    private fun applyBlendEffect(blendImage: Bitmap) {
//        val gpuImage = GPUImage(activity)
//        gpuImage.setImage(originalBitmap)
//
//        // Create and set the blend filter
//        val blendFilter = createBlendFilter(filtersBlend[selectedIndex], blendImage)
//        gpuImage.setFilter(blendFilter)
//
//        // Get the bitmap with the applied filter
//        val effectBitmap = gpuImage.bitmapWithFilterApplied
//
//        // Update the image view with the applied effect
//        activity.binding.imgEffect.setImageBitmap(effectBitmap)
//    }
//
//    /**
//     * Create a blend filter using GPUImage and the selected blend mode.
//     */
//    private fun createBlendFilter(
//        filterClass: Class<out GPUImageTwoInputFilter>,
//        blendImage: Bitmap
//    ): GPUImageFilter {
//        return try {
//            filterClass.getDeclaredConstructor().newInstance().apply {
//                bitmap = blendImage
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            GPUImageFilter() // Return a default filter if there's an error
//        }
//    }
//
//    inner class BlendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val thumbnailBlend: ImageView = itemView.findViewById(R.id.thumbnail_blend)
//        val rlBlendItem: RelativeLayout = itemView.findViewById(R.id.rl_blenditem)
//    }
//}
