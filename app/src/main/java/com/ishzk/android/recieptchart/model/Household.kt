package com.ishzk.android.recieptchart.model

import androidx.databinding.InverseMethod
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.*

data class Household(
    @DocumentId
    val id: String = "",
    val cost: Int,
    val date: Timestamp,
    val kind: String,
    val description: String = "",
    val userId: String
) {
    companion object {
        fun init(): Household = Household("", 0, Timestamp(Date()), ItemKind.Consume.kind, "", "")
    }
}

enum class ItemKind(val kind: String){
    Consume("Consume"),
    Utility("Utility"),
    Food("Food"),
    Commodity("Commodity"),
    Other("Other"),
}

object Converter {
    @InverseMethod("inverseToInt")
    @JvmStatic
    fun toString(value: Int): String {
        return value.toString()
    }

    @JvmStatic
    fun inverseToInt(value: String?): Int {
        return value?.toIntOrNull() ?: 0
    }
}