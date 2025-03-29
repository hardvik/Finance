package com.example.finance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        if(Firebase.auth.currentUser == null) {
            val intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            loadFragment(Feed())
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(Feed())
                    true
                }
                R.id.nav_records -> {
                    loadFragment(MakeTransaction())
                    true
                }
                R.id.nav_diagrams -> {
                    loadFragment(Diagrams())
                    true
                }
                else -> false
            }
        }

    }
    private fun loadFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        val transaction = supportFragmentManager.beginTransaction()

        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }

        if (fragment is MakeTransaction) {
            transaction.setCustomAnimations(
                R.anim.slide_in_bottom, // Вхід знизу
                R.anim.slide_out_bottom // Вихід вниз
            )
        } else {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}