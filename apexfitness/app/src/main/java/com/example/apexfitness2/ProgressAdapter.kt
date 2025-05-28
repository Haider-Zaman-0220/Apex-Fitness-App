package com.example.apexfitness2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ProgressAdapter(private val progressList: List<Progress>) :
    RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    private val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        holder.bind(progressList[position])
    }

    override fun getItemCount(): Int = progressList.size

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weightTextView: TextView = itemView.findViewById(R.id.weightTextView)
        private val bodyFatTextView: TextView = itemView.findViewById(R.id.bodyFatTextView)
        private val workoutsDoneTextView: TextView = itemView.findViewById(R.id.workoutsDoneTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        private val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        fun bind(progress: Progress) {
            weightTextView.text = "Weight: %.1f kg".format(progress.weight)
            bodyFatTextView.text = "Body Fat: %.1f %%".format(progress.bodyFat)
            workoutsDoneTextView.text = "Workouts Done: ${progress.workoutsDone}"
            val timeStr = progress.timestamp?.toDate()?.let { sdf.format(it) } ?: "N/A"
            timestampTextView.text = timeStr
        }
    }
}
