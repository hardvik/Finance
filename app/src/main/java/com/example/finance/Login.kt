package com.example.finance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        var goToReg = findViewById<TextView>(R.id.goToReg)
        var logginButton = findViewById<Button>(R.id.loginButton)
        var email = findViewById<TextView>(R.id.emailText)
        var password = findViewById<TextView>(R.id.passText)

        goToReg.setOnClickListener() {
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent)
        }
        logginButton.setOnClickListener() {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isNotEmpty() || passwordText.isNotEmpty()) {
                login(auth, emailText, passwordText)
            } else {
                Toast.makeText(this, "Заповніть всі поля!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun login(auth: FirebaseAuth, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Повідомлення", "Авторизація: Успішна")
                    Toast.makeText(
                        baseContext,
                        "Авторизація: Успішна",
                        Toast.LENGTH_SHORT,
                    ).show()

                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Log.w("Повідомлення", "Авторизація: Провалена", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Авторизація: Провалена",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

}