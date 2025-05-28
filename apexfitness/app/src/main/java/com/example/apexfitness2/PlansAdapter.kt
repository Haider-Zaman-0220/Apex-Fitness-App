package com.example.apexfitness2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apexfitness2.Plan

class PlansAdapter(
    private val plansList: List<Plan>,
    private val role: String
) : RecyclerView.Adapter<PlansAdapter.PlanViewHolder>() {

    class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.planTitleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.planDescriptionTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.planTypeTextView)
        val assignedToTextView: TextView = itemView.findViewById(R.id.assignedToTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plansList[position]
        holder.titleTextView.text = plan.title
        holder.descriptionTextView.text = plan.description
        holder.typeTextView.text = "Type: ${plan.type}"
        holder.assignedToTextView.text = "Assigned to: ${plan.assignedToMemberId}"
    }

    override fun getItemCount(): Int = plansList.size
}
