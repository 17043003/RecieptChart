package com.ishzk.android.recieptchart

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.*
import java.io.ByteArrayOutputStream

class CharacterRecognition {
    private lateinit var functions: FirebaseFunctions

    fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth =
                (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight =
                (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        } else if (originalHeight == originalWidth) {
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

    fun recognition(request: JsonObject){
        annotateImage(request.toString())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Task failed with an exception

                } else {
                    // Task completed successfully
                    val text =
                        task.result.asJsonArray[0].asJsonObject["fullTextAnnotation"].asJsonObject["text"]
                    Log.i(TAG, text.toString())
                }
            }
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

    companion object {
        const val TAG = "CharacterRecognition"
    }
}