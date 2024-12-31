package com.photoTools.bgEraser.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.model.FilterData
import com.photoTools.bgEraser.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterDetailAdapter(
    private val context: Context,
    private val filters: Array<FilterData>,
    private val originalBitmap: Bitmap,
    private val targetImageView: ImageView,
    private val progressBar: ProgressBar // Add ProgressBar here
) : RecyclerView.Adapter<FilterDetailAdapter.FilterDetailHolder>() {

    private var selectedIndex = 0
    private val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterDetailHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false)
        return FilterDetailHolder(view)
    }

    override fun getItemCount(): Int = filters.size

    override fun onBindViewHolder(holder: FilterDetailHolder, position: Int) {
        val filter = filters[position]

        holder.rlFilterItem.setBackgroundColor(
            if (selectedIndex == position) context.resources.getColor(R.color.colorPrimary)
            else context.resources.getColor(R.color.transparent)
        )



        CoroutineScope(Dispatchers.IO).launch {
            val filteredBitmap = applyFilter(resizedBitmap, filter)

            withContext(Dispatchers.Main) {

                Glide.with(context)
                    .load(filteredBitmap)
                    .into(holder.thumbnailFilter)

              //  progressBar.visibility = View.GONE
            }
        }

        holder.filterName.text = filter.text

        holder.rlFilterItem.setOnClickListener {
            selectedIndex = position
            applySelectedFilter(filter)
            notifyDataSetChanged()
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun applyFilter(bmp: Bitmap, filterData: FilterData): Bitmap {
        val bitmap = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix().apply {
            setSaturation(filterData.saturation)
            postConcat(ColorMatrix().apply { setScale(filterData.red, filterData.green, filterData.blue, 1f) })
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        return bitmap
    }

    private fun applySelectedFilter(filterData: FilterData) {
        AsyncFilterTask(originalBitmap, targetImageView,progressBar).execute(
            filterData.red,
            filterData.green,
            filterData.blue,
            filterData.saturation
        )
    }

    inner class FilterDetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailFilter: ImageView = itemView.findViewById(R.id.thumbnail_filter)
        val filterName: TextView = itemView.findViewById(R.id.filterName)
        val rlFilterItem: RelativeLayout = itemView.findViewById(R.id.rl_filteritem)
    }

    class AsyncFilterTask(
        private val originalBitmap: Bitmap,
        private val imgMain: ImageView,
        private val progressBar: ProgressBar
    ) : AsyncTask<Float, Void, Bitmap>() {



        override fun doInBackground(vararg params: Float?): Bitmap {
            val (r, g, b, saturation) = params
            return applyFilter(originalBitmap, r ?: 1f, g ?: 1f, b ?: 1f, saturation ?: 1f)
        }

        override fun onPostExecute(result: Bitmap) {
            progressBar.visibility = View.GONE
            imgMain.setImageBitmap(result)
        }

        private fun applyFilter(bmp: Bitmap, red: Float, green: Float, blue: Float, saturation: Float): Bitmap {
            val bitmap = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()

            val colorMatrix = ColorMatrix().apply {
                setSaturation(saturation)
                postConcat(ColorMatrix().apply { setScale(red, green, blue, 1f) })
            }

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(bmp, 0f, 0f, paint)
            return bitmap
        }
    }
}
