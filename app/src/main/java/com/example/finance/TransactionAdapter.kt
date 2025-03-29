package com.example.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.transactionDate)
        val icon: ImageView = view.findViewById(R.id.transactionIcon)
        val title: TextView = view.findViewById(R.id.transactionTitle)
        val description: TextView = view.findViewById(R.id.transactionDescription)
        val amount: TextView = view.findViewById(R.id.transactionAmount)
        val totalDateAmount: TextView = view.findViewById(R.id.totalMonthAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Відображення дати
        if (position == 0 || transactions[position].date != transactions[position - 1].date) {
            holder.date.visibility = View.VISIBLE
            holder.date.text = transaction.date
        } else {
            holder.date.visibility = View.GONE
        }

        // Відображення загального балансу місяця
        val currentDate = transaction.date
        if (position == 0 || transactions[position].date != transactions[position - 1].date) {
            // Використовуємо NumberFormat для парсингу чисел за поточною локаллю
            val numberFormat = NumberFormat.getInstance(Locale.getDefault())
            val dailyTotal = transactions.filter { it.date == currentDate }
                .sumOf { amountStr ->
                    try {
                        // Парсимо рядок у число з урахуванням локалі
                        numberFormat.parse(amountStr.amount)?.toDouble() ?: 0.0
                    } catch (e: ParseException) {
                        0.0
                    }
                }

            holder.totalDateAmount.visibility = View.VISIBLE
            holder.totalDateAmount.text = String.format("%.2f", dailyTotal)
        } else {
            holder.totalDateAmount.visibility = View.GONE
        }

        // Відображення деталей транзакції
        holder.title.text = transaction.name
        holder.description.text = transaction.description
        holder.amount.text = transaction.amount
        holder.amount.setTextColor(
            if (transaction.amount.startsWith("-")) android.graphics.Color.parseColor("#D32F2F")
            else android.graphics.Color.parseColor("#4CAF50")
        )

        // Встановлення іконки
        holder.icon.setImageResource(getIconResource(transaction.name))

        holder.itemView.setOnClickListener {
            // Викликаємо метод в Activity або Fragment для відкриття нового фрагмента
            val transactionDetailFragment = TransacitonDetails.newInstance(transaction.id)
            val fragmentTransaction = (it.context as AppCompatActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_bottom, // Вхід знизу
                R.anim.slide_out_bottom // Вихід вниз
            )
            fragmentTransaction.replace(R.id.fragmentContainer, transactionDetailFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    override fun getItemCount(): Int = transactions.size

    private fun getIconResource(name: String): Int {
        return when (name) {
            "Продукти" -> R.drawable.ico_product
            "Транспорт" -> R.drawable.ico_transport
            "Житло" -> R.drawable.ico_home
            "Комуналка" -> R.drawable.ico_comynalka
            "Розваги" -> R.drawable.ico_rozvagi
            "Освіта" -> R.drawable.ico_osvita
            "Медицина" -> R.drawable.ico_medicine
            "Одяг" -> R.drawable.ico_odiag
            "Косметика" -> R.drawable.ico_makeup
            "Подарунки" -> R.drawable.ico_gift
            "Подорожі" -> R.drawable.ico_travel
            "Спорт" -> R.drawable.ico_sport
            "Інтернет" -> R.drawable.ico_internet
            "Зарплата" -> R.drawable.ico_zarplata
            "Підробіток" -> R.drawable.ico_pidrobitoc
            "Стипендія" -> R.drawable.ico_stipendia
            "Продаж" -> R.drawable.ico_sell
            "Виплати" -> R.drawable.ico_viplati
            "Пенсія" -> R.drawable.ico_pensia
            "Інвестиції" -> R.drawable.ico_investicii
            else -> R.drawable.ic_circle
        }
    }
}
