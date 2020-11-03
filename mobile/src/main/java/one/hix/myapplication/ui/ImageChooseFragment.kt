package one.hix.myapplication.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import one.hix.myapplication.R
import one.hix.myapplication.viewmodels.PageViewModel

class ImageChooseFragment : Fragment() {

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): ImageChooseFragment {
            return ImageChooseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_image_choose, container, false)
//        val textView: TextView = root.findViewById(R.id.section_label)
//        pageViewModel.text.observe(this, Observer<String> {
//            textView.text = it
//        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            val picturePath = arguments?.getString("picturePath")
            view.findViewById<ImageView>(R.id.image_choose_fragment).setImageBitmap((BitmapFactory.decodeFile(picturePath)))

            view.findViewById<Button>(R.id.save_button_choose_fragment).setOnClickListener {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("picturePath", picturePath)
                findNavController().popBackStack()
            }

    }


}