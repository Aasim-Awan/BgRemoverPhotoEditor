import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.databinding.ItemImageBinding
import java.io.File

class RecentImagesAdapter(
    private val images: List<File>,
    private val onImageClick: (File) -> Unit
) : RecyclerView.Adapter<RecentImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageFile = images[position]
        holder.bind(imageFile)
    }

    override fun getItemCount(): Int = images.size

    inner class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageFile: File) {
            Glide.with(binding.imageViewRecent.context)
                .load(imageFile)
                .into(binding.imageViewRecent)

            binding.imageViewRecent.setOnClickListener {
                onImageClick(imageFile)
            }
        }
    }
}
