package one.hix.myapplication.ui

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import one.hix.myapplication.OpenRemotePlayStore
import one.hix.myapplication.R
import one.hix.myapplication.adapters.CategoriesRecyclerAdapter
import one.hix.myapplication.models.Category
import one.hix.myapplication.viewmodels.PageViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class MainScreenFragment : Fragment() {

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): MainScreenFragment {
            return MainScreenFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private lateinit var pageViewModel: PageViewModel
    private val PICK_IMAGE = 1
    private val IMAGE_PATH = 2

    //    private lateinit var dataClient: DataClient
    private var count = 0
    private lateinit var openRemotePlayStore: OpenRemotePlayStore
    private lateinit var recyclerViewWithCategories: RecyclerView
    private lateinit var categoriesRecyclerAdapterdapter: CategoriesRecyclerAdapter
    private var categoryList = arrayListOf<Category>()
    private var mAllConnectedNodes: MutableList<Node>? = null

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
        val root = inflater.inflate(R.layout.fragment_main_screen, container, false)
//        val textView: TextView = root.findViewById(R.id.section_label)
//        pageViewModel.text.observe(this, Observer<String> {
//            textView.text = it
//        })
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("picturePath")?.observe(viewLifecycleOwner) { result ->
            categoryList.add(Category(result!!, "categoryName6445"))
            categoriesRecyclerAdapterdapter.notifyDataSetChanged()
            saveFile("user_${Random.nextInt(10_000)}", result)
        }

        recyclerViewWithCategories = view.findViewById(R.id.categories_recycler_view)
        recyclerViewWithCategories.layoutManager = LinearLayoutManager(context)

        initCategoryList(categoryList)

        categoriesRecyclerAdapterdapter = CategoriesRecyclerAdapter(categoryList)
        recyclerViewWithCategories.adapter = categoriesRecyclerAdapterdapter

        view.findViewById<ImageView>(R.id.plusImage).setOnClickListener {
            ObjectAnimator.ofObject(it, "backgroundColor", ArgbEvaluator(), resources.getColor(R.color.plusImageColor), Color.BLACK)
                .setDuration(600L).start()
            showPicsAppChooser()
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = activity?.contentResolver?.query(selectedImage, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
            val picturePath = columnIndex?.let { cursor?.getString(it) }
            cursor?.close()
            val params = Bundle().apply {
                putString("picturePath", picturePath)
            }
            findNavController().navigate(R.id.imageChooseFragment, params)
        } else {
            findNavController().popBackStack()
        }
    }

    //копируем все файлы из ассетов в data
    private fun initCategoryList(categoryList: ArrayList<Category>) {
        val listFilesInDir = File(context?.filesDir!!.parent).listFiles()
        println("sssss listFilesInDir.size ${listFilesInDir.size}")

        if (listFilesInDir.size < 30) {
            val assetFiles = requireContext().assets.list("")
            assetFiles!!.forEach {
                if (it.contains("jpg")) {
                    println("sssss ${it}")
                    val inputStream = requireContext().assets.open(it)
                    val outputStream = FileOutputStream(File(requireContext().filesDir.parent, it))
                    try {
                        inputStream.copyTo(outputStream, 1024)
                    } finally {
                        inputStream.close()
                        outputStream.flush()
                        outputStream.close()
                    }
                }
            }
        } else {
            listFilesInDir.forEach {
                if (!it.isDirectory) {
                    categoryList.add(Category(categoryName = it.name.replace("_.+".toRegex(), ""), fileUri = it.absolutePath))
                }
            }
        }
    }
//
//    private fun increaseCounter(dataClient: DataClient, appPackageName: String) {
//        val putDataReq = PutDataMapRequest.create("/count").run {
//            dataMap.putInt("COUNT_KEY", count)
////            dataMap.putString("START_INTENT", appPackageName)
//            asPutDataRequest()
//        }
//        dataClient.putDataItem(putDataReq)
//        notif()
//    }
//
    private fun notif() {
        val notificationId = 1
        val name = "my_channel_01"
        val descriptionText = "channel_discription"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("CHANNEL_ID", name, importance)
        mChannel.description = descriptionText
        val notificationManager = requireContext().getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val viewPendingIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=io.faceapp}")
        )
//        viewPendingIntent.putExtra("EXTRA_EVENT_ID", 2)
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, viewPendingIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(requireContext(), "CHANNEL_ID")
            .setSmallIcon(R.drawable.common_full_open_on_phone)
            .setContentTitle("Название")
            .setContentText("Сам техт сообщения")
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(requireContext()).apply {
            notify(notificationId, notificationBuilder.build())
        }


    }
//
//    override fun onPause() {
//        super.onPause()
//        openRemotePlayStore.removeListener(this)`
//    }


    @Throws(FileNotFoundException::class, IOException::class)
    private fun saveFile(fileName: String, picturePath: String) {
        // path to /data/data/yourapp/app_data, Create imageDir
        val mypath = File(context?.getFilesDir()!!.parent, "${fileName}.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // compress method on the BitMap object to write image to the OutputStream
            BitmapFactory.decodeFile(picturePath).compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun showPicsAppChooser() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, PICK_IMAGE)
    }

    fun sendStringToWear() {
        val putDataReq= PutDataMapRequest.create("/test").run {
            dataMap.putString("key", "TEST TEXT")
            asPutDataRequest()
        }
        //dataItem
        Wearable.getDataClient(requireContext()).putDataItem(putDataReq)
    }



}