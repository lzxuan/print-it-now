package com.triplicity.printitnow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.triplicity.printitnow.entity.User
import com.triplicity.printitnow.ui.profile.ProfileViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: ProfileViewModel
    private lateinit var currentUser: FirebaseUser
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_prints, R.id.nav_about), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        val navTitle: TextView = navView.getHeaderView(0).findViewById(R.id.user_name)
        val navSubtitle: TextView = navView.getHeaderView(0).findViewById(R.id.user_email)
        val navImage: ImageView = navView.getHeaderView(0).findViewById(R.id.user_photo)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        viewModel.uid = currentUser.uid
        viewModel.setUser(User(currentUser.displayName, currentUser.email, currentUser.phoneNumber))

        viewModel.user.observe(this, Observer {
            if (it != null) {
                navTitle.text = currentUser.displayName
                navSubtitle.text = currentUser.email
                Glide.with(this).load(currentUser.photoUrl).circleCrop().into(navImage)
            }
        })

        sharedPrefs = getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE)

        val defValue = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val isNightModeOn: Boolean = sharedPrefs.getBoolean("NightMode", defValue)

        val switchTheme: MenuItem = navView.menu.findItem(R.id.switch_theme)
        val toggleTheme: Switch = switchTheme.actionView.findViewById(R.id.toggle_theme)

        toggleTheme.isChecked = setDarkTheme(isNightModeOn)

        switchTheme.setOnMenuItemClickListener {
            toggleTheme.isChecked = toggleTheme(isNightModeOn)
            true
        }
        toggleTheme.setOnClickListener() {
            toggleTheme.isChecked = toggleTheme(isNightModeOn)
        }

        val signOutBtn: MenuItem = navView.menu.findItem(R.id.nav_sign_out)
        signOutBtn.setOnMenuItemClickListener {
            signOut()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setDarkTheme(isNightModeOn: Boolean): Boolean {
        val theme = if (isNightModeOn) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(theme)

        return isNightModeOn
    }

    private fun toggleTheme(isNightModeOn: Boolean): Boolean {
        val theme = if (isNightModeOn) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        AppCompatDelegate.setDefaultNightMode(theme)

        val sharePrefsEdit = sharedPrefs.edit()
        sharePrefsEdit.putBoolean("NightMode", !isNightModeOn)
        sharePrefsEdit.apply()

        return !isNightModeOn
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                startSignInActivity()
            }
    }

    private fun startSignInActivity() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
