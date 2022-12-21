package com.ishzk.android.recieptchart.model

import java.text.SimpleDateFormat
import java.util.*

data class Receipt(
    val pages: List<Page>,
    val text: String,
){
    fun toPlaceAndText(): List<PlacedText> {
        return this.pages[0].blocks.flatMap { block ->
            block.paragraphs.flatMap { paragraph ->
                paragraph.words.flatMap { word ->
                    word.symbols.map { symbol ->
                        val topLeft = symbol.boundingBox.vertices[0]
                        PlacedText(symbol.text,  topLeft.y, topLeft.x) // x and y are swapped
                    }
                }
            }
        }.sortedBy { it.y }.reversed()
    }
}

data class Page(
    val blocks: List<Block>
)

data class Block(
    val paragraphs: List<Paragraph>
)

data class Paragraph(
    val words: List<Word>
)

data class Word(
    val symbols: List<Symbol>,
)

data class Symbol(
    val boundingBox: BoundingBox,
    val text: String,
)

data class BoundingBox(
    val vertices: List<Vertices>
)

data class Vertices(
    val x: Int,
    val y: Int,
)

data class PlacedText(
    val text: String,
    val x: Int,
    val y: Int,
)

data class CapturedCost(
    val id: Int,
    val cost: Int,
    val description: String,
)

data class CapturedReceiptData(
    val costs: MutableList<CapturedCost>,
    val date: Date,
)

object DateConverter{
    @JvmStatic
    fun toString(date: Date): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd")
        return formatter.format(date)
    }
}

class CaptureError(errorMessage: String): Error(errorMessage)