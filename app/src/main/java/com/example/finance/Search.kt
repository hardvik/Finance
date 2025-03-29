package com.example.finance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Search : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedCategoryFilter: String = "all"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val searchEditText = view.findViewById<EditText>(R.id.description)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val trg = view.findViewById<TextView>(R.id.transactionGone)

        // Знайдемо кнопки категорій
        val buttonAll = view.findViewById<Button>(R.id.buttonAll)
        val buttonExp = view.findViewById<Button>(R.id.buttonExp)
        val buttonInc = view.findViewById<Button>(R.id.buttonInc)

        // Обробники кліків для кнопок
        buttonAll.setOnClickListener {
            selectedCategoryFilter = "all"
            // Можна додатково змінити візуальне оформлення кнопок
            val searchText = searchEditText.text.toString().trim().lowercase(Locale.getDefault())
            buttonAll.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.active_color))
            buttonExp.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondarycolor))
            buttonInc.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondarycolor))
            loadTransactions(recyclerView, searchText, selectedCategoryFilter)
        }
        buttonExp.setOnClickListener {
            selectedCategoryFilter = "expense"
            val searchText = searchEditText.text.toString().trim().lowercase(Locale.getDefault())
            buttonAll.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondarycolor))
            buttonExp.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.active_color))
            buttonInc.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondarycolor))
            loadTransactions(recyclerView, searchText, selectedCategoryFilter)
        }
        buttonInc.setOnClickListener {
            selectedCategoryFilter = "income"
            val searchText = searchEditText.text.toString().trim().lowercase(Locale.getDefault())
            buttonAll.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondarycolor))
            buttonExp.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondarycolor))
            buttonInc.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.active_color))
            loadTransactions(recyclerView, searchText, selectedCategoryFilter)
        }

        // Обробник введення в EditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""
                loadTransactions(recyclerView, searchText, selectedCategoryFilter)
                // За бажанням – сховувати список, якщо текст порожній:
                if (searchText.isEmpty()) {
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadTransactions(
        recyclerView: RecyclerView,
        filterDescription: String,
        filterType: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        val trg = view?.findViewById<TextView>(R.id.transactionGone)

        // Починаємо з базового запиту
        var query = db.collection("transactions")
            .whereEqualTo("userId", userId)

        // Якщо вибрано не "Всі", додаємо фільтр за типом транзакції
        if (filterType != "all") {
            query = query.whereEqualTo("type", filterType)
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // Фільтруємо транзакції за описом (пошук нечутливий до регістру)
                val transactions = documents.mapNotNull { doc ->
                    val description = doc.getString("description") ?: ""
                    if (filterDescription.isEmpty() ||
                        description.lowercase(Locale.getDefault()).contains(filterDescription)
                    ) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        val type = doc.getString("type") ?: ""
                        Transaction(
                            date = SimpleDateFormat("dd MMM yyyy", Locale("uk", "UA"))
                                .format(doc.getTimestamp("timestamp")?.toDate() ?: Date()),
                            name = doc.getString("category") ?: "",
                            description = description,
                            amount = String.format("%.2f", amount),
                            type = type,
                            id = doc.id
                        )
                    } else null
                }

                // Якщо транзакцій немає – показуємо TextView, інакше – RecyclerView
                if (transactions.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    trg?.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    trg?.visibility = View.GONE
                }
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = TransactionAdapter(transactions)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Помилка завантаження: $e", Toast.LENGTH_SHORT).show()
                recyclerView.visibility = View.GONE
                trg?.visibility = View.VISIBLE
            }
    }

}
