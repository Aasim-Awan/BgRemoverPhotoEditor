import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.model.TemplateItem
import com.photoTools.bgEraser.R

class TemplateAdapter(
    private val templateList: List<TemplateItem>,
    private val onTemplateClick: (TemplateItem) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

    inner class TemplateViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val templateImageView: ImageView = view.findViewById(R.id.templateImageView)

        fun bind(templateItem: TemplateItem) {
            // Load the template image into the ImageView (e.g., using Glide or other libraries)
            Glide.with(view.context).load(templateItem.photoItemList).into(templateImageView)

            view.setOnClickListener {
                onTemplateClick(templateItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templateList[position])
    }

    override fun getItemCount(): Int = templateList.size
}
