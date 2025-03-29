package com.example.finance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        var backArrow = findViewById<ImageView>(R.id.backArrow)
        var regButton = findViewById<Button>(R.id.regButton)
        var email = findViewById<TextView>(R.id.emailText)
        var user = findViewById<TextView>(R.id.userText)
        var password = findViewById<TextView>(R.id.passText)

        backArrow.setOnClickListener() {
            val intent = Intent(this@Register, Login::class.java)
            startActivity(intent)
        }

        regButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val userText = user.text.toString().trim()

            if (emailText.isNotEmpty() && passwordText.isNotEmpty() && userText.isNotEmpty()) {
                register(auth, emailText, passwordText, userText)
            } else {
                Toast.makeText(this, "Заповніть всі поля!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun register(auth: FirebaseAuth, email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                    }
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Log.d("Повідомлення", "Реєстрація успішна. Ім'я користувача збережено")
                                Toast.makeText(
                                    baseContext,
                                    "Реєстрація успішна",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                val intent = Intent(this@Register, Login::class.java)
                                startActivity(intent)
                            } else {
                                Log.w("Повідомлення", "Не вдалося зберегти ім'я користувача", profileTask.exception)
                                Toast.makeText(
                                    baseContext,
                                    "Не вдалося зберегти ім'я користувача",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                } else {
                    Log.w("Повідомлення", "Реєстрація провалена", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Реєстрація провалена",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

}