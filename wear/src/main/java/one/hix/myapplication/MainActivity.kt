//package one.hix.myapplication
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.support.wearable.activity.WearableActivity
//import android.widget.Toast
//import com.google.android.gms.wearable.*
//
//
//private const val COUNT_KEY = "com.example.key.count"
//
//class MainActivity : WearableActivity(), DataClient.OnDataChangedListener {
//
//    private var count = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Wearable.getDataClient(this).addListener(this)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        println("ssss onPause")
//        Wearable.getDataClient(this).removeListener(this)
//    }
//
//    override fun onDataChanged(dataEvents: DataEventBuffer) {
//        dataEvents.forEach { event ->
//            // DataItem changed
//            println("ssss event.type == ${event.type}")
//            if (event.type == DataEvent.TYPE_CHANGED) {
//                event.dataItem.also { item ->
//                    val dataMap = DataMapItem.fromDataItem(item).dataMap
//                    val datamapInt = dataMap.getInt("COUNT_KEY")
//                    Toast.makeText(applicationContext, datamapInt.toString(), Toast.LENGTH_SHORT).show()
////                    startActivity(
////                        Intent(
////                            Intent.ACTION_VIEW,
////                            Uri.parse("market://details?id=${dataMap.getString("START_INTENT")}")
////                        )
////                    )
//
//
//                }
//
//            } else if (event.type == DataEvent.TYPE_DELETED) {
//                println("ssss DataEvent.TYPE_DELETED")
//            }
//        }
//    }
//
//    private fun updateCount(int: Int) {
//
//    }
//
//}