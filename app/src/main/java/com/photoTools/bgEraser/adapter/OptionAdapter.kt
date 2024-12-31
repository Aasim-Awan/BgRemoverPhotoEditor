import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.photoTools.bgEraser.R

class OptionAdapter(
    private val imgOptions: TypedArray,
    private val textOptions: Array<String>,
    private val onOptionSelected: (position: Int) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionHolder>() {

    private var selectedIndex = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_oprion, parent, false)
        return OptionHolder(view)
    }

    override fun getItemCount(): Int {
        return textOptions.size
    }

    override fun onBindViewHolder(holder: OptionHolder, position: Int) {
        holder.imgOption.setImageResource(imgOptions.getResourceId(position, 0))
        holder.txtOption.setText(textOptions[position])

        val layoutParams = LinearLayout.LayoutParams(
            holder.itemView.context.resources.displayMetrics.widthPixels / 6,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        holder.llOption.layoutParams = layoutParams

        holder.llOption.setOnClickListener {
            selectedIndex = position
            onOptionSelected(position)
            notifyDataSetChanged()
        }
    }

    inner class OptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgOption: ImageView = itemView.findViewById(R.id.img_option)
        val txtOption: TextView = itemView.findViewById(R.id.txt_option)
        val llOption: LinearLayout = itemView.findViewById(R.id.ll_option)
    }
}
