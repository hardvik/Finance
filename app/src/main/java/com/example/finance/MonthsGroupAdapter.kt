package com.example.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MonthsGroupAdapter(
    private val monthsGroups: List<List<String>>,
    private val onMonthClick: (String) -> Unit
) : RecyclerView.Adapter<MonthsGroupAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView = view.findViewById(R.id.month1)
        val textView2: TextView = view.findViewById(R.id.month2)
        val textView3: TextView = view.findViewById(R.id.month3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = monthsGroups[position]

        // Встановлюємо текст для кожного TextView
        holder.textView1.text = group.getOrNull(0) ?: ""
        holder.textView2.text = group.getOrNull(1) ?: ""
        holder.textView3.text = group.getOrNull(2) ?: ""

        // Встановлюємо кліки для кожного TextView
        holder.textView1.setOnClickListener {
            group.getOrNull(0)?.let { month -> onMonthClick(month) }
        }
        holder.textView2.setOnClickListener {
            group.getOrNull(1)?.let { month -> onMonthClick(month) }
        }
        holder.textView3.setOnClickListener {
            group.getOrNull(2)?.let { month -> onMonthClick(month) }
        }

        // Приховуємо TextView, якщо місяць відсутній
        holder.textView1.visibility = if (group.getOrNull(0) != null) View.VISIBLE else View.GONE
        holder.textView2.visibility = if (group.getOrNull(1) != null) View.VISIBLE else View.GONE
        holder.textView3.visibility = if (group.getOrNull(2) != null) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = monthsGroups.size
}

