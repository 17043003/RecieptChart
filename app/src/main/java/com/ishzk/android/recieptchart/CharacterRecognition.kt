package com.ishzk.android.recieptchart

import android.graphics.Bitmap
import android.util.Base64
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.*
import com.ishzk.android.recieptchart.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CharacterRecognition {
    private lateinit var functions: FirebaseFunctions

    fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val resizedWidth: Int
        val resizedHeight: Int
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth =
                (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight =
                (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        } else {
            resizedHeight = maxDimension
            resizedWidth = maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }

    fun encode2Base64(bitmap: Bitmap): String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    suspend fun recognition(request: JsonObject): com.github.michaelbull.result.Result<CapturedReceiptData, CaptureError> = try {
        withContext(Dispatchers.IO){
            val result = annotateImage(request.toString()).toSuspend()
            val gson = Gson()
            val data = gson.fromJson(result.asJsonArray[0].asJsonObject["fullTextAnnotation"], Receipt::class.java)

            val placedTexts = data.toPlaceAndText()
            val rowText = toRowText(placedTexts)

            val capturedCosts = mutableListOf<CapturedCost>()
            var capturedDateString = ""

            for(text in rowText){
                // pick up date string.
                if(capturedDateString.isEmpty()) {
                    val dateRegex = Regex("""(\d{4})[年/] *(\d{1,2})[月/] *(\d{1,2})[日/]""")
                    val matchDateResult = dateRegex.matchEntire(text)
                    val dateValues = matchDateResult?.groupValues ?: listOf()
                    if(dateValues.size > 3) {
                        capturedDateString = "${dateValues[1]}/${dateValues[2]}/${dateValues[3]}"
                    }
                }

                // pick up each costs and descriptions.
                val costRegex = Regex("""^([^計税釣%％]*)[¥￥](\d+)$""")
                val matchResult = costRegex.matchEntire(text)
                val values = matchResult?.groupValues ?: continue
                if(values.size >= 2) {
                    capturedCosts.add(
                        CapturedCost(
                            cost = values[2].toInt(),
                            description = values[1]
                        )
                    )
                }
            }

            val formatter = SimpleDateFormat("yyyy/MM/dd")
            val capturedDate = if(capturedDateString.isEmpty()) Date()
            else { formatter.parse(capturedDateString) }

            val capturedData = CapturedReceiptData(capturedCosts, capturedDate)
            Ok(capturedData)
        }
    }catch (e: Exception){
        Err(CaptureError(e.message ?: "Unknown error."))
    }

    fun createRequest(base64encoded: String): JsonObject{
        val request = JsonObject()

        val image = JsonObject()
        image.add("content", JsonPrimitive(base64encoded))
        request.add("image", image)

        val feature = JsonObject()
        feature.add("type", JsonPrimitive("TEXT_DETECTION"))

        val features = JsonArray()
        features.add(feature)
        request.add("features", features)

        return request
    }

    private fun annotateImage(requestJson: String): Task<JsonElement> {
        functions = FirebaseFunctions.getInstance()
        return functions
            .getHttpsCallable("annotateImage")
            .call(requestJson)
            .continueWith { task ->
                val result = task.result?.data
                JsonParser.parseString(Gson().toJson(result))
            }
    }

    private fun toRowText(paragraphInBlocks: List<PlacedText>): List<String> {
        val rowText = mutableListOf<String>()
        val rowPlaceText = mutableListOf<PlacedText>()
        var tmpText = ""
        var oldY = -1
        for(paragraph in paragraphInBlocks){
            val threshold = 2
            if(oldY - threshold <= paragraph.y && paragraph.y <= oldY + threshold) {
                tmpText += paragraph.text
                rowPlaceText.add(paragraph)
            }else if(oldY == -1){
                tmpText = paragraph.text
                rowPlaceText.clear()
                rowPlaceText.add(paragraph)
            }else{
                rowPlaceText.sortBy { it.x }
                rowText.add(rowPlaceText.reversed().joinToString(separator = ""){ it.text })

                rowPlaceText.clear()
                rowPlaceText.add(paragraph)
                tmpText = ""
            }
            oldY = paragraph.y
        }

        if(tmpText.isNotEmpty()) {
            rowText.add(tmpText)
        }
        return rowText
    }

    companion object {
        const val TAG = "CharacterRecognition"
    }
}

suspend fun<T> Task<T>.toSuspend(): T = suspendCoroutine { continuation ->
    this.addOnCompleteListener { task ->
        if(task.isSuccessful){
            continuation.resume(task.result)
        }else if(task.isCanceled){
            continuation.resumeWithException(CancellationException())
        }else{
            continuation.resumeWithException(task.exception ?: Exception("Unknown error."))
        }
    }
}