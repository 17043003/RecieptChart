package com.ishzk.android.recieptchart

import android.content.Context
import androidx.fragment.app.FragmentActivity

class SharedPreference(private val activity: FragmentActivity) {
    fun getValue(preferenceKey: String, valueKey: String): String{
        val sharedPref = activity.getSharedPreferences(
            preferenceKey, Context.MODE_PRIVATE)
        return sharedPref.getString(valueKey, "") ?: ""
    }
}