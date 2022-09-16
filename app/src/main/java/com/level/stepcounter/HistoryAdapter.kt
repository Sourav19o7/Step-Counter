package com.level.stepcounter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.text.SimpleDateFormat

class History_Adapter : RecyclerView.Adapter<HistoryViewHolder>() {
    private val items: ArrayList<String> = ArrayList()
    private var DAY_IN_MS = 1000 * 60 * 60 * 24
    private val info: ArrayList<Days> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = items[position]
        holder.steps.text = currentItem
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateSteps(updatedSteps: MutableMap<String,String>) {
        items.clear()

        for (i in updatedSteps)
        {
            items.add(0, i.key + "\n" + i.value.substring(4))
        }
        notifyDataSetChanged()
    }
}

class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val steps: TextView = itemView.findViewById(R.id.steps)
}