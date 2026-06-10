package com.app.tracker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
<<<<<<< Updated upstream
import androidx.appcompat.app.AppCompatDelegate
=======
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
<<<<<<< Updated upstream
<<<<<<< Updated upstream
        // Apply theme preferences before inflating views
        val sharedPrefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = if (sharedPrefs.contains("is_dark_mode")) {
            sharedPrefs.getBoolean("is_dark_mode", false)
        } else {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isSystemDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES
            sharedPrefs.edit().putBoolean("is_dark_mode", isSystemDark).apply()
            isSystemDark
        }

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
<<<<<<< Updated upstream
<<<<<<< Updated upstream
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

=======

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
>>>>>>> Stashed changes
=======

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
>>>>>>> Stashed changes
        bottomNav.setupWithNavController(navController)
    }
}