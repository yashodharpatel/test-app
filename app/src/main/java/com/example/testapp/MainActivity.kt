package com.example.testapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fingerprintjs.android.fingerprint.custom_info.CustomUtils
import com.fingerprintjs.android.fingerprint.custom_info.DeviceInfoItem

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private lateinit var recyclerView: RecyclerView
    private val list = mutableListOf<DeviceInfoItem>() // Global mutable list

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

        // SDK
        adapter.updateItems(CustomUtils.collectDeviceInfo(this))
    }
}


