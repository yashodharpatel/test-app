package com.example.testapp

import android.annotation.SuppressLint
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

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var job: Job = Job()
    private lateinit var adapter: MyAdapter
    private lateinit var recyclerView: RecyclerView
    private val list = mutableListOf<DeviceInfoItem>() // Global mutable list

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



        // Initialization
        val fingerprinter = FingerprinterFactory.create(this)

        // Usage
        fingerprinter.getFingerprint(version = Fingerprinter.Version.V_5) { fingerprint ->
            // Use fingerprint
            Log.i("fingerprint", fingerprint);
//            val item = adapter.items.toMutableList()
            list.add(DeviceInfoItem("Fingerprint", fingerprint))
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
    }

    fun onResult(signals: List<FingerprintingSignal<*>>) {
//        val currentItems = adapter.items.toMutableList()
        signals.mapIndexed { _, signal ->
            val data = signal.toFingerprintItemData()
            list.add(DeviceInfoItem(data.signalName, data.signalValue.toString()))

            Log.i("Yash Data","${data.signalName} ${data.signalValue}")
        }


        adapter.updateItems(list)

    }
    private fun handleDeviceIdResult(result: DeviceIdResult) {
        list.add(DeviceInfoItem("Device ID", result.deviceId))
        list.add(DeviceInfoItem("Android ID", result.androidId ?: "Unavailable"))
        list.add(DeviceInfoItem("GSF ID", result.gsfId ?: "Unavailable"))
        list.add(DeviceInfoItem("Media DRM ID", result.mediaDrmId ?: "Unavailable"))

//        adapter.updateItems(list)
    }

}


