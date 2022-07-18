package com.ishzk.android.recieptchart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.navigation.ui.setupWithNavController
import com.ishzk.android.recieptchart.databinding.ActivityMainBinding
import com.ishzk.android.recieptchart.fragment.ChartFragment

class MainActivity : AppCompatActivity() {
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

        // navigation menu setting.
        val navHostFragment =
            supportFragmentManager.fragments[0] as ChartFragment
        val navController = navHostFragment.navController
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
}