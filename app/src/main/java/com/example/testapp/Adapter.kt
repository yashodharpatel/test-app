package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(var items: List<DeviceInfoItem>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val value: TextView = view.findViewById(R.id.value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, detail) = items[position]
        holder.name.text = title
        holder.value.text = detail
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<DeviceInfoItem>) {
        items = newItems
        notifyDataSetChanged()
    }

}

data class DeviceInfoItem(
    val title: String,
    val detail: String
)
