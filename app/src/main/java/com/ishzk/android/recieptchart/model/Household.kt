package com.ishzk.android.recieptchart.model

import android.widget.EditText
import androidx.databinding.InverseMethod
import com.google.firebase.firestore.DocumentId
import java.time.LocalDate
import java.time.LocalDateTime

data class Household(
    @DocumentId
    val id: String = "",
    val cost: Int,
    val date: LocalDateTime,
    val kind: String,
    val description: String = "",
    val userId: String
) {
}

sealed class ItemKind(val kind: String){
    object Consume: ItemKind("Consume")
    object Utility: ItemKind("Utility")
    object Food: ItemKind("Food")
    object Commodity: ItemKind("Commodity")
    object Other: ItemKind("Other")
    data class Custom(private val customKind: String): ItemKind(customKind)

    companion object {
        val values = listOf(Consume, Utility, Food, Commodity, Other).map { it.kind }
    }
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