package com.example.finance

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class MakeTransaction : Fragment() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var numberPanel: View
    private lateinit var amountTextView: TextView
    private lateinit var expenseButton: Button
    private lateinit var incomeButton: Button
    private lateinit var description: String
    private lateinit var transactionDay: Date
    private var selectedCategory: String? = null
    private var isExpense: Boolean = true
    private var isToday: Boolean = true
    private var isNumberPanelAnimated = false
    private var isEditing: Boolean = false

    companion object {
        fun newInstance(transactionId: String): MakeTransaction {
            val fragment = MakeTransaction()
            val args = Bundle()
            args.putString("transactionId", transactionId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Підключаємо XML-файл до фрагмента
        return inflater.inflate(R.layout.make_transaction, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val expenseButton = view.findViewById<Button>(R.id.expenseButton)
        val incomeButton = view.findViewById<Button>(R.id.incomeButton)
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        numberPanel = view.findViewById(R.id.numberPanel)
        amountTextView = view.findViewById(R.id.amountTextView)
        val buttons = listOf(
            view.findViewById<Button>(R.id.button0),
            view.findViewById<Button>(R.id.button1),
            view.findViewById<Button>(R.id.button2),
            view.findViewById<Button>(R.id.button3),
            view.findViewById<Button>(R.id.button4),
            view.findViewById<Button>(R.id.button5),
            view.findViewById<Button>(R.id.button6),
            view.findViewById<Button>(R.id.button7),
            view.findViewById<Button>(R.id.button8),
            view.findViewById<Button>(R.id.button9)
        )
        val doneButton = view.findViewById<Button>(R.id.buttonDone)
        val backspaceButton = view.findViewById<Button>(R.id.buttonBackSpace)
        val dotButton = view.findViewById<Button>(R.id.buttonDot)
        val description = view.findViewById<EditText>(R.id.description)
        val yesterdayButton = view.findViewById<Button>(R.id.buttonYesterday)
        val buttonAfterYesterday = view.findViewById<Button>(R.id.buttonAfterYesterday)
        val buttonToday = view.findViewById<Button>(R.id.buttonToday)
        categoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        // Завантаження початкових категорій
        loadCategories(isExpense = true)

        val transactionId = arguments?.getString("transactionId")
        if (transactionId != null) {
            // Це редагування транзакції
            isEditing = true
            loadTransactionForEditing(transactionId)
        } else {
            // Це додавання нової транзакції
            isEditing = false
            println("Додавання нової транзакції")
        }

        buttons.forEach { button ->
            button.setOnClickListener {
                val currentAmount = amountTextView.text.toString()
                val newDigit = button.text.toString()
                amountTextView.text = if (currentAmount == "0") {
                    newDigit // Замінюємо 0 на першу цифру
                } else {
                    currentAmount + newDigit // Додаємо цифру в кінець
                }
            }
        }

        backspaceButton.setOnClickListener {
            val currentAmount = amountTextView.text.toString()
            if (currentAmount.isNotEmpty()) {
                amountTextView.text = currentAmount.dropLast(1).ifEmpty { "0" }
            }
        }

        buttonToday.setOnClickListener {
            yesterdayButton.setTextColor(Color.parseColor("#FFFFFF"))
            buttonAfterYesterday.setTextColor(Color.parseColor("#FFFFFF"))
            buttonToday.setTextColor(Color.parseColor("#388E3C"))
            showDatePicker()
        }

        yesterdayButton.setOnClickListener {
            isToday = false
            // Встановлюємо кольори кнопок (якщо потрібно, або викликаємо updateDateButtonsUI)
            yesterdayButton.setTextColor(Color.parseColor("#388E3C"))
            buttonAfterYesterday.setTextColor(Color.parseColor("#FFFFFF"))
            buttonToday.setTextColor(Color.parseColor("#FFFFFF"))
            val yesterday = com.google.firebase.Timestamp.now().toDate().let {
                Calendar.getInstance().apply {
                    time = it
                    add(Calendar.DAY_OF_YEAR, -1)
                }.time
            }
            transactionDay = yesterday
            updateDateButtonsUI(transactionDay)
        }

        buttonAfterYesterday.setOnClickListener {
            isToday = false
            yesterdayButton.setTextColor(Color.parseColor("#FFFFFF"))
            buttonToday.setTextColor(Color.parseColor("#FFFFFF"))
            buttonAfterYesterday.setTextColor(Color.parseColor("#388E3C"))
            val afterYesterday = com.google.firebase.Timestamp.now().toDate().let {
                Calendar.getInstance().apply {
                    time = it
                    add(Calendar.DAY_OF_YEAR, -2)
                }.time
            }
            transactionDay = afterYesterday
            updateDateButtonsUI(transactionDay)
        }


        doneButton.setOnClickListener {
            val amount = amountTextView.text.toString().toDoubleOrNull()
            val descriptionText = view.findViewById<EditText>(R.id.description)?.text.toString()
            if (selectedCategory != null && amount != null) {
                if (transactionId != null) {
                    // Оновлення існуючої транзакції
                    updateTransactionInFirebase(transactionId, selectedCategory!!, amount, isExpense, descriptionText)
                } else {
                    // Додавання нової транзакції
                    saveTransactionToFirebase(selectedCategory!!, amount, isExpense, descriptionText)
                }
            } else {
                println("Категорію або суму не вибрано")
            }
        }

        dotButton.setOnClickListener {
            val currentAmount = amountTextView.text.toString()
            if (!currentAmount.contains(".")) { // Додаємо кому, якщо її ще немає
                amountTextView.text = currentAmount + "."
            }
        }
        // Обробка натискання кнопок "Витрата" та "Дохід"
        expenseButton.setOnClickListener {
            isExpense = true
            loadCategories(isExpense = true)
            activeButton(expenseButton)
        }
        incomeButton.setOnClickListener {
            isExpense = false
            loadCategories(isExpense = false)
            activeButton(incomeButton)
        }
    }

    private fun showDatePicker() {
        isToday = false
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time

                val currentDate = Calendar.getInstance().time
                if (selectedDate <= currentDate) {
                    transactionDay = selectedDate
                    // Оновлюємо UI після вибору дати:
                    updateDateButtonsUI(transactionDay)
                } else {
                    Toast.makeText(requireContext(), "Виберіть дату, яка не більша за теперішню", Toast.LENGTH_SHORT).show()
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }


    private fun updateTransactionInFirebase(transactionId: String, category: String, amount: Double, isExpense: Boolean, description: String) {
        val transactionData = hashMapOf(
            "category" to category,
            "amount" to if (isExpense) -amount else amount,
            "type" to if (isExpense) "expense" else "income",
            "description" to description,
            // Оновлюємо timestamp: якщо користувач вибрав сьогоднішню дату – використовуємо поточний час,
            // інакше – збережену дату transactionDay
            "timestamp" to if (isToday) com.google.firebase.Timestamp.now() else com.google.firebase.Timestamp(transactionDay)
        )

        FirebaseFirestore.getInstance()
            .collection("transactions")
            .document(transactionId)
            .update(transactionData as Map<String, Any>)
            .addOnSuccessListener {
                println("Транзакція оновлена успішно")
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                println("Помилка оновлення транзакції: $e")
            }
    }


    private fun loadTransactionForEditing(transactionId: String) {
        FirebaseFirestore.getInstance().collection("transactions")
            .document(transactionId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val amount = document.getDouble("amount") ?: 0.0
                    val category = document.getString("category") ?: ""
                    val descriptionText = document.getString("description") ?: ""
                    val type = document.getString("type") ?: "expense"

                    // Завантаження дати транзакції
                    val timestamp = document.getTimestamp("timestamp")
                    if (timestamp != null) {
                        transactionDay = timestamp.toDate()
                        updateDateButtonsUI(transactionDay)

                        // Перевірка, чи дата транзакції співпадає із сьогоднішньою
                        val todayCal = Calendar.getInstance()
                        val transactionCal = Calendar.getInstance().apply { time = transactionDay }
                        isToday = todayCal.get(Calendar.YEAR) == transactionCal.get(Calendar.YEAR) &&
                                todayCal.get(Calendar.DAY_OF_YEAR) == transactionCal.get(Calendar.DAY_OF_YEAR)
                    } else {
                        transactionDay = Date()
                        isToday = true
                    }

                    // Оновлення UI:
                    // Якщо сума зберігається як від'ємна для витрат, показуємо її абсолютне значення:
                    amountTextView.text = kotlin.math.abs(amount).toString()
                    this.selectedCategory = category
                    view?.findViewById<EditText>(R.id.description)?.setText(descriptionText)
                    isExpense = (type == "expense")

                    // Оновлення категорій та активного стану кнопок:
                    if (isExpense) {
                        loadCategories(isExpense = true)
                        activeButton(view?.findViewById(R.id.expenseButton)!!)
                    } else {
                        loadCategories(isExpense = false)
                        activeButton(view?.findViewById(R.id.incomeButton)!!)
                    }

                    // Робимо панель видимою (якщо вона ще не показана)
                    if (numberPanel.visibility != View.VISIBLE) {
                        numberPanel.visibility = View.VISIBLE
                        // Запуск анімації, якщо потрібно
                        if (!isNumberPanelAnimated) {
                            val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
                            numberPanel.startAnimation(slideUpAnimation)
                            isNumberPanelAnimated = true
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Не вдалося завантажити дані транзакції: $e")
            }
    }





    private fun saveTransactionToFirebase(category: String, amount: Double, isExpense: Boolean, description: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val transactionData = hashMapOf(
                "userId" to userId,
                "category" to category,
                "amount" to if (isExpense) -amount else amount,
                "type" to if (isExpense) "expense" else "income",
                "timestamp" to if (isToday) com.google.firebase.Timestamp.now() else com.google.firebase.Timestamp(transactionDay),
                "description" to description
            )
            FirebaseFirestore.getInstance()
                .collection("transactions")
                .add(transactionData)
                .addOnSuccessListener {
                    println("Транзакція збережена успішно")
                    Toast.makeText(requireContext(), "Транзакція збережена успішно", Toast.LENGTH_SHORT).show()
                    val feed = Feed() // Створіть об'єкт вашого фрагмента Feed
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, feed) // Замініть fragment_container на ID вашого контейнера для фрагментів
                        .addToBackStack(null) // Опціонально: додаємо транзакцію в стек
                        .commit()
                }
                .addOnFailureListener { e ->
                    println("Помилка збереження транзакції: $e")
                }
        } else {
            println("Користувач не автентифікований")
        }
    }

    private fun loadCategories(isExpense: Boolean) {
        val categories = if (isExpense) {
            listOf("Продукти", "Транспорт", "Житло", "Комуналка", "Розваги", "Освіта", "Медицина", "Одяг", "Косметика", "Подарунки", "Подорожі", "Спорт", "Інтернет") // Витрати
        } else {
            listOf("Зарплата", "Підробіток", "Подарунки", "Стипендія", "Продаж", "Виплати", "Пенсія", "Інвестиції") // Доходи
        }

        // Оновлення RecyclerView
        categoryRecyclerView.adapter = CategoryAdapter(categories) { selectedCategory ->
            showNumberPanel(selectedCategory)
        }
    }

    private fun showNumberPanel(selectedCategory: String) {
        this.selectedCategory = selectedCategory
        // Якщо це НЕ редагування (тобто створення нової транзакції),
        // скидаємо поле суми та опис
        if (!isEditing) {
            amountTextView.text = "0" // Початкове значення
            view?.findViewById<EditText>(R.id.description)?.text = null
        }
        // Якщо панель ще не видима, робимо її видимою
        if (numberPanel.visibility != View.VISIBLE) {
            numberPanel.visibility = View.VISIBLE
        }
        // Запускаємо анімацію лише один раз
        if (!isNumberPanelAnimated) {
            val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
            numberPanel.startAnimation(slideUpAnimation)
            isNumberPanelAnimated = true
        }
    }

    private fun activeButton(activeButton: Button) {
        // Встановлюємо колір для активної кнопки
        activeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorActive)) // #080E26
        val expenseButton = view?.findViewById<Button>(R.id.expenseButton)
        val incomeButton = view?.findViewById<Button>(R.id.incomeButton)
        // Відновлюємо колір для неактивної кнопки
        if (activeButton != expenseButton) {
            expenseButton?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorInactive)) // Іноді краще зробити якесь стандартне значення
        }
        if (activeButton != incomeButton) {
            incomeButton?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorInactive)) // Іноді краще зробити якесь стандартне значення
        }
    }

    private fun updateDateButtonsUI(date: Date) {
        val calendarTransaction = Calendar.getInstance().apply { time = date }
        val calendarToday = Calendar.getInstance()

        val buttonToday = view?.findViewById<Button>(R.id.buttonToday)
        val yesterdayButton = view?.findViewById<Button>(R.id.buttonYesterday)
        val buttonAfterYesterday = view?.findViewById<Button>(R.id.buttonAfterYesterday)

        // Скидаємо кольори всім кнопкам
        buttonToday?.setTextColor(Color.parseColor("#FFFFFF"))
        yesterdayButton?.setTextColor(Color.parseColor("#FFFFFF"))
        buttonAfterYesterday?.setTextColor(Color.parseColor("#FFFFFF"))

        fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        when {
            isSameDay(calendarTransaction, calendarToday) -> {
                buttonToday?.setTextColor(Color.parseColor("#388E3C"))
            }
            isSameDay(calendarTransaction, Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }) -> {
                yesterdayButton?.setTextColor(Color.parseColor("#388E3C"))
            }
            isSameDay(calendarTransaction, Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }) -> {
                buttonAfterYesterday?.setTextColor(Color.parseColor("#388E3C"))
            }
            // Якщо дата не належить жодній з кнопок – можна встановити додаткову логіку
        }
    }


}