package com.example.finance

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

class Diagrams : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedMonthYear: String? = null
    private lateinit var pieChart: PieChart
    private var transactionTypeFilter: String? = "expense"
    private val categoryColors = mapOf(
        "Продукти" to Color.parseColor("#FF7043"),      // Помаранчево-червоний
        "Транспорт" to Color.parseColor("#26A69A"),     // Бірюзовий
        "Житло" to Color.parseColor("#8D6E63"),         // Коричневий
        "Комуналка" to Color.parseColor("#29B6F6"),     // Голубий
        "Розваги" to Color.parseColor("#AB47BC"),       // Фіолетовий
        "Освіта" to Color.parseColor("#7E57C2"),        // Темно-фіолетовий
        "Медицина" to Color.parseColor("#EC407A"),      // Рожевий
        "Одяг" to Color.parseColor("#FFCA28"),          // Жовтий
        "Косметика" to Color.parseColor("#F06292"),     // Яскраво-рожевий
        "Подарунки" to Color.parseColor("#FF8A65"),     // Світло-оранжевий
        "Подорожі" to Color.parseColor("#42A5F5"),      // Синій
        "Спорт" to Color.parseColor("#66BB6A"),         // Зелений
        "Інтернет" to Color.parseColor("#5C6BC0"),      // Індиго
        "Зарплата" to Color.parseColor("#FFB74D"),      // Світло-жовтогарячий
        "Підробіток" to Color.parseColor("#8D8D8D"),    // Сірий
        "Стипендія" to Color.parseColor("#26C6DA"),     // Блакитний
        "Продаж" to Color.parseColor("#D4E157"),        // Жовто-зелений
        "Виплати" to Color.parseColor("#78909C"),       // Синьо-сірий
        "Пенсія" to Color.parseColor("#FF7043"),        // Оранжевий
        "Інвестиції" to Color.parseColor("#43A047")        // Сірий (дефолтний колір)
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Інфлейт макет для фрагмента
        return inflater.inflate(R.layout.activity_diagrams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore

        val viewPager = view.findViewById<ViewPager2>(R.id.monthsViewPager)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        pieChart = view.findViewById(R.id.pieChart)

        val expenseButton = view.findViewById<Button>(R.id.expenseButton)
        val incomeButton = view.findViewById<Button>(R.id.incomeButton)

        // Слухач для кнопки "Витрати"
        expenseButton.setOnClickListener {
            transactionTypeFilter = "expense"
            Log.d("ButtonDebug", "Expense button clicked. Filter: $transactionTypeFilter")
            activeButton(expenseButton)
            loadTransactions(recyclerView, selectedMonthYear, transactionTypeFilter)
        }

        // Слухач для кнопки "Дохід"
        incomeButton.setOnClickListener {
            transactionTypeFilter = "income"
            Log.d("ButtonDebug", "Income button clicked. Filter: $transactionTypeFilter")
            activeButton(incomeButton)
            loadTransactions(recyclerView, selectedMonthYear, transactionTypeFilter)
        }

        // Показуємо транзакції для поточного місяця
        val currentMonthYear = SimpleDateFormat("MMMM yyyy", Locale("uk", "UA")).format(Date())
        selectedMonthYear = currentMonthYear

        // Завантаження транзакцій
        loadTransactions(recyclerView, selectedMonthYear, transactionTypeFilter) { monthsWithTransactions ->
            setupMonthsViewPager(viewPager, monthsWithTransactions) { clickedMonth ->
                if (selectedMonthYear == clickedMonth) {
                    selectedMonthYear = null // Скидаємо фільтр
                    loadTransactions(recyclerView, null, transactionTypeFilter)
                } else {
                    selectedMonthYear = clickedMonth
                    loadTransactions(recyclerView, selectedMonthYear, transactionTypeFilter)
                }
            }
        }
    }

    private fun loadPieChartData(totalAmount: Double, pieEntries: List<PieEntry>, centerTextLabel: String) {
        val colors = mutableListOf<Int>()

        pieEntries.forEach { pieEntry ->
            val category = pieEntry.label.substringBefore(" (") // Видаляємо відсотки
            val color = categoryColors[category] ?: Color.LTGRAY
            Log.d("PieChartDebug", "Category: '$category', Assigned Color: $color")
            colors.add(color)
        }

        if (pieEntries.isEmpty()) {
            pieChart.data = null
            pieChart.invalidate()
            pieChart.centerText = "Немає даних для відображення"
            return
        }

        val dataSet = PieDataSet(pieEntries, centerTextLabel)
        dataSet.colors = colors
        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)
        data.setDrawValues(false)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)

        pieChart.data = data
        pieChart.centerText = String.format("Загальні %s\n%.2f грн", centerTextLabel, totalAmount)
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(Color.WHITE)

        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.parseColor("#1e1f22"))
        pieChart.setTransparentCircleColor(Color.parseColor("#080e26"))
        pieChart.setTransparentCircleAlpha(110)
        pieChart.animateY(1000)
        pieChart.invalidate()
        pieChart.legend.isEnabled = false
    }



    private fun setupMonthsViewPager(
        viewPager: ViewPager2,
        monthsWithTransactions: List<String>,
        onMonthClick: (String) -> Unit
    ) {
        // Групуємо місяці по 3
        val groupedMonths = monthsWithTransactions.chunked(3)

        // Створюємо адаптер
        val monthsAdapter = MonthsGroupAdapter(groupedMonths, onMonthClick)

        // Встановлюємо адаптер для ViewPager2
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
        filterType: String? = null,
        onMonthsLoaded: ((List<String>) -> Unit)? = null
    ) {
        val userId = auth.currentUser?.uid ?: return

        var query = db.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

        if (filterType != null) {
            query = query.whereEqualTo("type", filterType)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (!isAdded || context == null) return@addOnSuccessListener // Перевірка перед оновленням UI

                val monthsWithTransactions = mutableSetOf<String>()
                val categoryTotals = mutableMapOf<String, Double>()
                var totalAmount = 0.0

                val noRecordsText = view?.findViewById<TextView>(R.id.noRecordsText)

                val filteredDocuments = if (filterType != null) {
                    documents.filter { it.getString("type") == filterType }
                } else {
                    documents
                }

                filteredDocuments.forEach { doc ->
                    val timestamp = doc.getTimestamp("timestamp")
                    val date = timestamp?.toDate() ?: Date()
                    val monthYear = SimpleDateFormat("MMMM yyyy", Locale("uk", "UA")).format(date)

                    monthsWithTransactions.add(monthYear)

                    if (filterMonthYear == null || filterMonthYear == monthYear) {
                        val amount = doc.getDouble("amount")?.absoluteValue ?: 0.0
                        val category = doc.getString("category") ?: "Інше"

                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
                        totalAmount += amount
                    }
                }

                val sortedMonths = monthsWithTransactions.sortedBy {
                    SimpleDateFormat("MMMM yyyy", Locale("uk", "UA")).parse(it)
                }

                val summarizedTransactions = categoryTotals.map { (category, total) ->
                    val percentage = if (totalAmount > 0) (total / totalAmount * 100) else 0.0
                    val formattedAmount = String.format("%.2f", total)
                    val displayAmount = if (filterType == "expense") "-$formattedAmount" else formattedAmount

                    Transaction(
                        date = "",
                        name = category,
                        description = "Сума: $displayAmount грн (%.1f%%)".format(percentage),
                        amount = displayAmount,
                        type = filterType ?: "",
                        id = ""
                    )
                }

                if (summarizedTransactions.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    noRecordsText?.visibility = View.VISIBLE
                    pieChart.visibility = View.GONE
                    pieChart.data = null
                    pieChart.invalidate()
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noRecordsText?.visibility = View.GONE
                    pieChart.visibility = View.VISIBLE

                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = TransactionWithProgressAdapter(summarizedTransactions, totalAmount)

                    val pieEntries = categoryTotals.map { (category, total) ->
                        PieEntry(total.toFloat(), "$category (%.1f%%)".format(total / totalAmount * 100))
                    }

                    loadPieChartData(totalAmount, pieEntries, if (filterType == "expense") "Витрати" else "Доходи")
                }

                onMonthsLoaded?.invoke(sortedMonths)
            }
            .addOnFailureListener { e ->
                if (!isAdded || context == null) return@addOnFailureListener // Уникнення помилок, якщо фрагмент знищено
                Log.e("Diagrams", "Помилка завантаження транзакцій: ", e)
                Toast.makeText(context, "Помилка завантаження: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun activeButton(activeButton: Button) {
        val expenseButton = view?.findViewById<Button>(R.id.expenseButton)
        val incomeButton = view?.findViewById<Button>(R.id.incomeButton)

        context?.let { ctx ->
            expenseButton?.setBackgroundColor(
                ContextCompat.getColor(ctx, if (activeButton == expenseButton) R.color.colorActive else R.color.colorInactive)
            )
            incomeButton?.setBackgroundColor(
                ContextCompat.getColor(ctx, if (activeButton == incomeButton) R.color.colorActive else R.color.colorInactive)
            )
        }
    }


}