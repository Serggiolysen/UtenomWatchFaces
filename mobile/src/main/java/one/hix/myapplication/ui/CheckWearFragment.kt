package one.hix.myapplication.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import one.hix.myapplication.OpenRemotePlayStore
import one.hix.myapplication.R
import one.hix.myapplication.viewmodels.PageViewModel


class CheckWearFragment : Fragment() {

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): CheckWearFragment {
            return CheckWearFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private lateinit var pageViewModel: PageViewModel
    //    private lateinit var dataClient: DataClient
    private var count = 0
    private lateinit var openRemotePlayStore: OpenRemotePlayStore


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
        val root = inflater.inflate(R.layout.check_wear_fragment, container, false)
//        val textView: TextView = root.findViewById(R.id.section_label)
//        pageViewModel.text.observe(this, Observer<String> {
//            textView.text = it
//        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkRemoteWearAppInstalled(view)
    }

    fun checkRemoteWearAppInstalled(view: View):Boolean{
//        val dataClient = Wearable.getDataClient(requireContext())
//        val appPackageName = "one.hix.myapplication"
        val appPackageName = "io.faceapp"

        openRemotePlayStore = OpenRemotePlayStore(requireContext())
        openRemotePlayStore.initWearListener(requireContext())
        openRemotePlayStore.openPlayStoreOnWearDevicesWithoutApp(requireContext(), appPackageName)

        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }



    private fun increaseCounter(dataClient: DataClient, appPackageName: String) {
        val putDataReq = PutDataMapRequest.create("/count").run {
            dataMap.putInt("COUNT_KEY", count)
//            dataMap.putString("START_INTENT", appPackageName)
            asPutDataRequest()
        }
        dataClient.putDataItem(putDataReq)
        notif()
    }


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

    override fun onPause() {
        super.onPause()
        openRemotePlayStore.removeListener(requireContext())
    }


}