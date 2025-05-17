package net.codeocean.cheese.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Pair
import net.codeocean.cheese.backend.impl.PathImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object ConvertersUtils {
    infix fun <A, B> A.t(that: B): Pair<A, B> = Pair(this, that)
    fun arrayToArrayList(objectList: List<Any>): Array<Any?> {
        val stringArray = arrayOfNulls<Any>(objectList.size)
        for (i in objectList.indices) {
            val obj: Any = objectList[i]
            stringArray[i] = obj.toString()
        }
        return stringArray
    }
    fun pairArray(vararg pairs: Int): Array<Pair<Int, Int>> {
        require(pairs.size % 2 == 0) { "The number of arguments must be even." }
        val result = Array(pairs.size / 2) { index ->
            pairs[index * 2] t pairs[index * 2 + 1]

        }
        return result
    }
    fun pairArrays(vararg arrays: Array<Pair<Int, Int>>): Array<Array<Pair<Int, Int>>> {
        val result = Array(arrays.size) { index ->
            arrays[index]
        }
        return result
    }
    fun sdToStream(filePath: String): InputStream? {
        return try {
            FileInputStream(filePath)
        } catch (e: java.io.IOException) {
            e.printStackTrace()
            null
        }
    }
    fun assetsToStream(fileName: String): InputStream? {
        val newPath = if (fileName.startsWith("/")) {
            fileName.substring(1)
        } else {
            fileName
        }
        val filePath = "${PathImpl.ASSETS_DIRECTORY}/$newPath"
        return sdToStream(filePath)
    }
    fun streamToBitmap(inputStream: InputStream): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }
    fun bitmapToStream(bitmap: Bitmap): InputStream {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }
    fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            Base64.encodeToString(bitmapToByteArray(bitmap), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}