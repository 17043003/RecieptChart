package com.ishzk.android.recieptchart

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.ishzk.android.recieptchart.auth.Authentication
import com.ishzk.android.recieptchart.databinding.ActivityMainBinding
import com.ishzk.android.recieptchart.fragment.ChartFragment
import com.ishzk.android.recieptchart.fragment.HouseholdFragment
import com.ishzk.android.recieptchart.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val authentication = Authentication()
    private val viewModel: MainViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        val userID = authentication.onSignInResult(res)?.uid
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString(getString(R.string.user_id), userID)
            apply()
        }
    }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // show toolbar.
        setSupportActionBar(binding.materialToolbar)

        // associate toolbar and navigation menu.
        val toggle = ActionBarDrawerToggle(
            this, binding.root, binding.materialToolbar, R.string.app_name, R.string.app_name)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()

        // bottom navigation setting.
        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // drawer navigation menu setting.
        binding.navigationMenu.setupWithNavController(navController)
        binding.navigationMenu.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.item_receipt_capture -> {
                    binding.root.closeDrawers()
                    val intent = Intent(this, ReceiptCameraActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // show sign in ui.
        val signInIntent = authentication.createIntent()
        if(viewModel.currentUser == null){
            signInLauncher.launch(signInIntent)
        }
    }
}