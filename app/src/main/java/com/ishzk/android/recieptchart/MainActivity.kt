package com.ishzk.android.recieptchart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import com.ishzk.android.recieptchart.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.root, binding.materialToolbar, R.string.app_name, R.string.app_name)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
    }
}