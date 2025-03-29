package com.example.finance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Feed : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedMonthYear: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Інфлейт макет для фрагмента
        return inflater.inflate(R.layout.activity_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = Firebase.auth
        db = Firebase.firestore

        val userName = view.findViewById<TextView>(R.id.userName)
        val viewPager = view.findViewById<ViewPager2>(R.id.monthsViewPager)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val monthsForDisplay = getMonthsForDisplay()
        val groupedMonths = monthsForDisplay.chunked(3)

        auth.currentUser?.reload()?.addOnCompleteListener {
            val updatedUser = auth.currentUser
            userName.text = updatedUser?.displayName ?: "User"
        }
        userName.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Вихід")
                .setMessage("Ви хочете вийти?")
                .setPositiveButton("Так") { dialog, _ ->
                    auth.signOut()
                    dialog.dismiss()
                    val intent = Intent(requireContext(), Login::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Ні") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        view.findViewById<ImageView>(R.id.searchButton).setOnClickListener {
            val searhFrag = (it.context as AppCompatActivity).supportFragmentManager.beginTransaction()
            searhFrag.setCustomAnimations(
                R.anim.slide_in_bottom, // Вхід знизу
                R.anim.slide_out_bottom // Вихід вниз
            )
                .replace(R.id.fragmentContainer, Search())
                .addToBackStack(null)
                .commit()
        }
        val monthsAdapter = MonthsGroupAdapter(groupedMonths) {
        }
        viewPager.adapter = monthsAdapter

        val currentPageIndex = groupedMonths.indexOfFirst { group ->
            group.contains(monthsForDisplay.last())
        }
        viewPager.setCurrentItem(currentPageIndex, false)
        loadTransactions(recyclerView) { monthsWithTransactions ->
            setupMonthsViewPager(viewPager, monthsWithTransactions) { clickedMonth ->
                // Перевіряємо, чи натиснуто на обраний місяць
                if (selectedMonthYear == clickedMonth) {
                    selectedMonthYear = null // Скидаємо фільтр
                    loadTransactions(recyclerView) // Показуємо всі транзакції
                    Toast.makeText(requireContext(), "Ви обрали всі транзакції", Toast.LENGTH_SHORT).show()
                } else {
                    selectedMonthYear = clickedMonth
                    Toast.makeText(requireContext(), "Ви обрали $clickedMonth", Toast.LENGTH_SHORT).show()
                    loadTransactions(recyclerView, selectedMonthYear) // Завантажуємо транзакції за місяцем
                }
            }
        }
    }

    private fun setupMonthsViewPager(
        viewPager: ViewPager2,
        monthsWithTransactions: List<String>,
        onMonthClick: (String) -> Unit
    ) {
        val groupedMonths = monthsWithTransactions.chunked(3)
        val monthsAdapter = MonthsGroupAdapter(groupedMonths) { clickedMonth ->
            onMonthClick(clickedMonth) // Викликаємо callback для обробки натискання
        }
        viewPager.adapter = monthsAdapter

        // Встановлюємо поточний місяць
        val currentPageIndex = groupedMonths.indexOfFirst { group ->
            group.contains(monthsWithTransactions.lastOrNull())
        }
        viewPager.setCurrentItem(currentPageIndex, false)
    }


    private fun loadTransactions(
        recyclerView: RecyclerView,
        filterMonthYear: String? = null,
        onMonthsLoaded: ((List<String>) -> Unit)? = null
    ) {
        val userId = auth.currentUser?.uid
        val balance = view?.findViewById<TextView>(R.id.balance)
        val incomeText = view?.findViewById<TextView>(R.id.income)
        val expenseText = view?.findViewById<TextView>(R.id.expense)

        if (userId != null) {
            db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    var totalIncome = 0.0
                    var totalExpense = 0.0
                    val monthsWithTransactions = mutableSetOf<String>() // Унікальні місяці з роками
                    val transactions = mutableListOf<Transaction>()

                    documents.forEach { doc ->
                        val timestamp = doc.getTimestamp("timestamp")
                        val date = timestamp?.toDate() ?: Date()

                        val monthYear = SimpleDateFormat("MMMM yyyy", Locale("uk", "UA")).format(date)
                        monthsWithTransactions.add(monthYear)

                        // Фільтруємо транзакції за обраним місяцем
                        if (filterMonthYear == null || filterMonthYear == monthYear) {
                            val amount = doc.getDouble("amount") ?: 0.0
                            val type = doc.getString("type") ?: ""

                            when (type) {
                                "income" -> totalIncome += amount
                                "expense" -> totalExpense += amount
                            }

                            transactions.add(
                                Transaction(
                                    date = SimpleDateFormat("dd MMM yyyy", Locale("uk", "UA")).format(date),
                                    name = doc.getString("category") ?: "",
                                    description = doc.getString("description") ?: "",
                                    amount = String.format("%.2f", amount),
                                    type = type,
                                    id = doc.id
                                )
                            )
                        }
                    }

                    // Оновлення балансу, доходів та витрат
                    val totalBalance = totalIncome + totalExpense
                    balance?.text = String.format("%.2f", totalBalance)
                    incomeText?.text = String.format("%.2f", totalIncome)
                    expenseText?.text = String.format("%.2f", totalExpense)

                    // Сортуємо місяці за датою
                    val sortedMonths = monthsWithTransactions.map { monthYear ->
                        SimpleDateFormat("MMMM yyyy", Locale("uk", "UA")).parse(monthYear) to monthYear
                    }.sortedBy { it.first }.map { it.second }

                    // Встановлюємо адаптер для транзакцій
                    val adapter = TransactionAdapter(transactions)
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    recyclerView.adapter = adapter

                    val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.slide_up)
                    recyclerView.layoutAnimation = controller
                    recyclerView.scheduleLayoutAnimation()

                    // Повертаємо відсортовані місяці через callback
                    onMonthsLoaded?.invoke(sortedMonths)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Помилка завантаження: $e", Toast.LENGTH_SHORT).show()
                    println("Помилка виводу: $e")
                }
        } else {
            Toast.makeText(requireContext(), "Користувач не автентифікований", Toast.LENGTH_SHORT).show()
        }
    }




    fun getMonthsForDisplay(): List<String> {
        val months = listOf(
            "Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень",
            "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень"
        )

        val calendar = Calendar.getInstance()
        val currentMonthIndex = calendar.get(Calendar.MONTH)

        // Повертаємо список місяців до поточного
        return months.take(currentMonthIndex + 1)
    }

}