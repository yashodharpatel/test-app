package com.example.testapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fingerprintjs.android.fingerprint.DeviceIdResult
import com.fingerprintjs.android.fingerprint.Fingerprinter
import com.fingerprintjs.android.fingerprint.FingerprinterFactory
import com.fingerprintjs.android.fingerprint.fingerprinting_signals.FingerprintingSignal
import com.fingerprintjs.android.fingerprint.signal_providers.StabilityLevel
import com.fingerprintjs.android.fingerprint.toFingerprintItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import android.os.BatteryManager
import com.fingerprintjs.android.fingerprint.OSResult
import com.fingerprintjs.android.fingerprint.other_info.BatteryUtils
import com.fingerprintjs.android.fingerprint.other_info.DeviceInfoItem

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var job: Job = Job()
    private lateinit var adapter: MyAdapter
    private lateinit var recyclerView: RecyclerView
    private val list = mutableListOf<DeviceInfoItem>() // Global mutable list
    private var kernel = "" // Global mutable list

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(list)
        recyclerView.adapter = adapter
        val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager

        // Get the battery percentage and store it in a INT variable
        val batLevel:Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)


        // Initialization
        val fingerprinter = FingerprinterFactory.create(this)

        fingerprinter.getOS(version = Fingerprinter.Version.V_5) { result ->
            launch(Dispatchers.Main) {
                kernel = result.kernel
                Log.i("Update List", "Added Kernel Version, list size: ${list.size}")
            }
        }

        // Usage
        fingerprinter.getFingerprint(version = Fingerprinter.Version.V_5) { fingerprint ->
            // Use fingerprint
            Log.i("fingerprint", fingerprint);
//            val item = adapter.items.toMutableList()
            list.add(DeviceInfoItem("Fingerprint", fingerprint))
            list.add(DeviceInfoItem("battery level",batLevel.toString()))

            adapter.notifyDataSetChanged()
//            adapter.updateItems(list)


        }

        fingerprinter.getDeviceId(version = Fingerprinter.Version.V_5) { result ->
            val deviceId = result.deviceId
            // Use deviceId
            Log.i("result",result.toString())
            Log.i("deviceId",deviceId)
            Log.i("androidId",result.androidId)
            Log.i("gsdId",result.gsfId)
            Log.i("mediaDrmId",result.mediaDrmId)
            handleDeviceIdResult(result)
            adapter.notifyDataSetChanged()

        }




        launch {
            val signals = withContext(context = Dispatchers.IO) {
                fingerprinter.getFingerprintingSignalsProvider()?.getSignalsMatching(
                    version = Fingerprinter.Version.V_5,
                    stabilityLevel = StabilityLevel.STABLE
                ).orEmpty()
            }

            onResult(signals) // onResult is called on the main thread
        }

//        launch {
//            val os = withContext(context = Dispatchers.IO) {
//                fingerprinter.getFingerprintingSignalsProvider()?.getSignalsMatching(
//                    version = Fingerprinter.Version.V_5,
//                    stabilityLevel = StabilityLevel.STABLE
//                ).orEmpty()
//            }
//
//            onResult(os) // onResult is called on the main thread
//        }
        Log.i("list data",list.toString())
    }

//    fun getDeviceImei(context: Context): List<String>? {
//        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            val imeiList = mutableListOf<String>()
//            imeiList.add(telephonyManager.getImei(0))
//            imeiList.add(telephonyManager.getImei(1))
//            return imeiList
//        }
//        return null
//    }

//    fun getKernelInfo(): String {
//        return File("/proc/version").readText()
//    }



    fun onResult(signals: List<FingerprintingSignal<*>>) {
//        val currentItems = adapter.items.toMutableList()
        signals.mapIndexed { _, signal ->
            val data = signal.toFingerprintItemData()
            list.add(DeviceInfoItem(data.signalName, data.signalValue.toString()))

            Log.i("Yash Data","${data.signalName} ${data.signalValue}")
        }

        list.addAll(BatteryUtils.collectDeviceInfo(this))

        adapter.updateItems(list)

    }
    private fun handleDeviceIdResult(result: DeviceIdResult) {
        list.add(DeviceInfoItem("Device ID", result.deviceId))
        list.add(DeviceInfoItem("Android ID", result.androidId ?: "Unavailable"))
        list.add(DeviceInfoItem("GSF ID", result.gsfId ?: "Unavailable"))
        list.add(DeviceInfoItem("Media DRM ID", result.mediaDrmId ?: "Unavailable"))

//        adapter.updateItems(list)
    }
    private fun handleOSResult(result: OSResult) {
        list.add(DeviceInfoItem("Kernel version", result.kernel))
//        list.add(DeviceInfoItem("Android ID", result.android ?: "Unavailable"))
//        list.add(DeviceInfoItem("GSF ID", result.sdk ?: "Unavailable"))
//        list.add(DeviceInfoItem("Media DRM ID", result.fingerprint ?: "Unavailable"))

//        adapter.updateItems(list)
    }

}


