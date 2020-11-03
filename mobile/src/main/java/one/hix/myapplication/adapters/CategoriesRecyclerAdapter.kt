package one.hix.myapplication.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import one.hix.myapplication.R
import one.hix.myapplication.models.Category
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class CategoriesRecyclerAdapter(private val dataset: List<Category>) : RecyclerView.Adapter<CategoriesRecyclerAdapter.ViewHolder>() {

    val categoryTempList = arrayListOf<String>()

    init {
        dataset.forEach {
            if (!categoryTempList.contains(it.categoryName)) {
                categoryTempList.add(it.categoryName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_categories_recycler, parent, false), dataset)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataset[position])

    override fun getItemCount() = categoryTempList.size

    inner class ViewHolder(itemView: View, private val dataset: List<Category>) : RecyclerView.ViewHolder(itemView) {
        var emojiImage = itemView.findViewById<ImageView>(R.id.image_view_cell_categories)
        var categoryName = itemView.findViewById<TextView>(R.id.text_view_cell_categories)
        var innerRecycler = itemView.findViewById<RecyclerView>(R.id.watch_faces_incide_recycler)

        fun bind(model: Category) {
            try {
                if (model.fileUri.contains("smile")) {
                    val emojiImage = File(model.fileUri)
                    this.emojiImage.setImageBitmap(BitmapFactory.decodeStream(FileInputStream(emojiImage)))
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            categoryName.text = categoryTempList[adapterPosition]

            innerRecycler.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

            val uriTempList = arrayListOf<String>()
            dataset.forEach {
                if (it.categoryName.equals(categoryTempList[adapterPosition])) {
                    uriTempList.add(it.fileUri)
                }
            }
            innerRecycler.adapter = InnerRecyclerAdapter(uriTempList)
        }
    }
}






























