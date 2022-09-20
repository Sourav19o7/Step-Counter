package com.level.stepcounter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.text.SimpleDateFormat

class HistoryAdapter : RecyclerView.Adapter<HistoryViewHolder>() {
    private val items: ArrayList<Days> = ArrayList()
    private val days: ArrayList<String> = ArrayList()
    private var progressMax = 10000
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = items[position]
        holder.steps.text = currentItem.steps
        holder.day.text=days[position]
        holder.time.text = currentItem.time + " mins"
        holder.distance.text = currentItem.distance.substring(0,6) + " m"
        holder.calories.text = currentItem.calories.substring(0,5) + " kcal"
        holder.target.text = "/$progressMax"
        holder.progressBar.max = progressMax
        holder.progressBar.progress = currentItem.steps.toInt()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateSteps(updatedSteps: MutableMap<String,Days>) {
        items.clear()

        for (i in updatedSteps)
        {
            days.add(0, i.key)
            items.add(0,i.value)
        }
        notifyDataSetChanged()
    }

    fun setTarget(targetSteps: Int)
    {
        progressMax = targetSteps
    }
}

class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val steps: TextView = itemView.findViewById(R.id.item_steps)
    val day: TextView = itemView.findViewById(R.id.item_day)
    val progressBar: ProgressBar = itemView.findViewById(R.id.item_progressbar)
    val time: TextView = itemView.findViewById(R.id.item_time)
    val distance: TextView = itemView.findViewById(R.id.item_distance)
    val calories: TextView = itemView.findViewById(R.id.item_calories)
    val target: TextView = itemView.findViewById(R.id.item_target_progress)
}