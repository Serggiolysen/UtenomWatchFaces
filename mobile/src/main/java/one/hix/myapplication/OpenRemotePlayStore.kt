package one.hix.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.ResultReceiver
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.wearable.intent.RemoteIntent
import java.util.*

class OpenRemotePlayStore(private val applicationContext: Context) : OnCapabilityChangedListener {

    companion object {
        private val INSTALLED_SOME_DEVICES_MESSAGE = ("Wear app installed on some your device(s) (%s)!\n\n"  +  "To install the Wear app on the other devices, please click on the button " + "below.\n")
        private val INSTALLED_ALL_DEVICES_MESSAGE = ("Wear app installed on all your devices (%s)!\n\n")
        // Name of capability listed in Wear app's wear.xml. This should be named differently than your Phone app's capability.
        private val CAPABILITY_WEAR_APP = "verify_remote_wear_app"
    }

    private val resultReciever  = ResultReceiver(Handler())
    private var mWearNodesWithApp: Set<Node>? = null
    private var mAllConnectedNodes: List<Node>? = null


    fun initWearListener(context: Context) {

        Wearable.getCapabilityClient(context).addListener(this, CAPABILITY_WEAR_APP)
        val capabilityInfoTask = Wearable.getCapabilityClient(applicationContext)
            .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL)

        capabilityInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("ssss ----5")
                val capabilityInfo = task.result
                mWearNodesWithApp = capabilityInfo!!.nodes
                verifyNodeAndUpdateUI()
            } else {
                println("ssss task is not successful - 1")
            }
        }
        findAllWearDevices()
    }


    fun openPlayStoreOnWearDevicesWithoutApp(context: Context, playStoreAppPackageName: String) {
        // Create a List of Nodes (Wear devices) without your app.
        val nodesWithoutApp = ArrayList<Node>()
        for (node: Node in mAllConnectedNodes!!) {
            if (!mWearNodesWithApp!!.contains(node)) {
                nodesWithoutApp.add(node)
            }
        }
        if (!nodesWithoutApp.isEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("market://details?id=${playStoreAppPackageName}"))
            for (node: Node in nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(
                    context, intent, resultReciever, node.id
                )
            }
        }
    }


    fun removeListener(context: Context) {
        Wearable.getCapabilityClient(context).removeListener(this, CAPABILITY_WEAR_APP)
    }

    //Updates UI when capabilities change (install/uninstall wear app).
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        mWearNodesWithApp = capabilityInfo.nodes
        findAllWearDevices()
        verifyNodeAndUpdateUI()
    }


    private fun findAllWearDevices() {
        val NodeListTask = Wearable.getNodeClient(applicationContext).connectedNodes
        NodeListTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("ssss task  successful")
                mAllConnectedNodes = task.result
            } else {
                println("ssss task is not successful - 2")
            }
        }
    }

    private fun verifyNodeAndUpdateUI() {
        if ((mWearNodesWithApp == null) || (mAllConnectedNodes == null)) {
            println("ssss mWearNodesWithApp == null) || (mAllConnectedNodes == null)")
        } else if (mAllConnectedNodes!!.isEmpty()) {
            println("ssss mAllConnectedNodes!!.isEmpty()")
        } else if (mWearNodesWithApp!!.isEmpty()) {
            println("ssss mWearNodesWithApp!!.isEmpty()")
        } else if (mWearNodesWithApp!!.size < mAllConnectedNodes!!.size) {
            // code to communicate with the wear app(s) via Wear APIs (MessageApi, DataApi, etc.)
            val installMessage = String.format(INSTALLED_SOME_DEVICES_MESSAGE, mWearNodesWithApp)
            println("ssss ${installMessage}")
        } else {
            // code to communicate with the wear app(s) via Wear APIs (MessageApi, DataApi, etc.)
            val installMessage = String.format(INSTALLED_ALL_DEVICES_MESSAGE, mWearNodesWithApp)
            println("ssss ${installMessage}")
        }
    }
}
