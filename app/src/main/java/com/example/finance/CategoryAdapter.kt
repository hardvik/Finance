package com.example.finance

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit // Колбек для обробки кліків
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = -1 // Індекс вибраної категорії
    private val categoryIcons = mapOf(
        "Продукти" to R.drawable.ico_product,
        "Транспорт" to R.drawable.ico_transport,
        "Житло" to R.drawable.ico_home,
        "Комуналка" to R.drawable.ico_comynalka,
        "Розваги" to R.drawable.ico_rozvagi,
        "Освіта" to R.drawable.ico_osvita,
        "Медицина" to R.drawable.ico_medicine,
        "Одяг" to R.drawable.ico_odiag,
        "Косметика" to R.drawable.ico_makeup,
        "Подарунки" to R.drawable.ico_gift,
        "Подорожі" to R.drawable.ico_travel,
        "Спорт" to R.drawable.ico_sport,
        "Інтернет" to R.drawable.ico_internet,
        "Зарплата" to R.drawable.ico_zarplata,
        "Підробіток" to R.drawable.ico_pidrobitoc,
        "Стипендія" to R.drawable.ico_stipendia,
        "Продаж" to R.drawable.ico_sell,
        "Виплати" to R.drawable.ico_viplati,
        "Пенсія" to R.drawable.ico_pensia,
        "Інвестиції" to R.drawable.ico_investicii
    )

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CategoryViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val category = categories[position]
        holder.categoryName.text = category

        if (position == selectedPosition) {
            holder.categoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.colorActive))
        } else {
            holder.categoryIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
        holder.categoryIcon.setImageResource(categoryIcons[category] ?: R.drawable.ic_circle)

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onCategoryClick(category) // Викликати колбек при натисканні
        }
    }

    override fun getItemCount(): Int = categories.size
}
