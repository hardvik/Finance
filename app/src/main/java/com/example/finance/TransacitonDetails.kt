package com.example.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransacitonDetails : Fragment() {
    private var transactionId: String? = null

    companion object {
        fun newInstance(transactionId: String): TransacitonDetails {
            val fragment = TransacitonDetails()
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
        return inflater.inflate(R.layout.activity_transaciton_details, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionId = arguments?.getString("transactionId")
        loadTransactionDetails(view)
        // Встановлюємо дані на UI


        view.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            // Видалення транзакції з Firebase
            if (transactionId != null) {
                FirebaseFirestore.getInstance().collection("transactions")
                    .document(transactionId!!)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Транзакція видалена", Toast.LENGTH_SHORT).show()
                        // Повернення до попереднього фрагмента
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Помилка видалення транзакції", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        view.findViewById<Button>(R.id.editButton).setOnClickListener {
            if (transactionId != null) {
                val fragment = MakeTransaction.newInstance(transactionId!!)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        view.findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            val fragmentManager = (it.context as AppCompatActivity).supportFragmentManager
            // Починаємо транзакцію
            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_bottom, // Анімація входу
                    R.anim.slide_out_bottom, // Анімація виходу
                )
                // Використовуємо commit() після popBackStack() для застосування анімацій (це може працювати не в усіх випадках)
                .commit()
            fragmentManager.popBackStack()
        }

    }

    private fun loadTransactionDetails(view: View) {
        if (transactionId != null) {
            FirebaseFirestore.getInstance().collection("transactions")
                .document(transactionId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val timestamp = document.getTimestamp("timestamp")
                        val date = timestamp?.toDate() ?: Date()
                        val name = document.getString("category")
                        val description = document.getString("description") ?: "Відсутній"
                        val amount = document.getDouble("amount") // Читаємо як Double
                        val amountAsString = amount?.toString() ?: "0.0"
                        var type = document.getString("type")
                        val dateString = SimpleDateFormat("dd MMM yyyy", Locale("uk", "UA")).format(date)
                        type = if (type == "expense") "Витрата" else "Дохід"

                        // Оновлення UI
                        view.findViewById<TextView>(R.id.categoryName).text = name
                        view.findViewById<TextView>(R.id.detailDescription).text = description
                        view.findViewById<TextView>(R.id.detailAmount).text = amountAsString
                        view.findViewById<TextView>(R.id.detailCategory).text = type
                        view.findViewById<TextView>(R.id.detailDate).text = dateString

                        // Встановлення іконки
                        val iconResId = getIconResource(name ?: "")
                        view.findViewById<ImageView>(R.id.categoryIcon).setImageResource(iconResId)
                    } else {
                        Toast.makeText(requireContext(), "Transaction not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load transaction details", Toast.LENGTH_SHORT).show()
                }
        }
    }

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