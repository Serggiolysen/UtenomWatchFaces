package one.hix.myapplication.adapters

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import one.hix.myapplication.R
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


class InnerRecyclerAdapter(private val uriTempList: ArrayList<String>) : RecyclerView.Adapter<InnerRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerRecyclerAdapter.ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_inner_recycle, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = uriTempList.size

    inner class ViewHolder(viewItem: View) : RecyclerView.ViewHolder(viewItem) {
        var image = viewItem.findViewById<ShapeableImageView>(R.id.inner_image_in_recycler)

        fun bind(position: Int) {
            try {
                val file = File(uriTempList[position])
                val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                image.setImageBitmap(bitmap)
                image.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels/4
                image.layoutParams.width = Resources.getSystem().displayMetrics.heightPixels/5
                image.setShapeAppearanceModel(image.getShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 22f)
                    .build())

                val params = Bundle().apply {
                    putString("picturePath", uriTempList[position])
                }

                image.setOnClickListener {
                    findNavController(it).navigate(R.id.imageChooseFragment, params)
                }


            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}






























