package net.codeocean.cheese.core.api

import android.graphics.Bitmap
import android.util.Pair
import java.io.InputStream

interface Converters {
    fun arrayToArrayList(objectList: List<String>): Array<Any?>
    fun pairArray(vararg elements: Int): Array<Pair<Int, Int>>
    fun pairArrays(vararg arrays: Any):  Array<Array<Pair<Int, Int>>>
    fun sdToStream(filePath: String):  InputStream?
    fun assetsToStream(filePath: String):  InputStream?
    fun assetsToBitmap(filePath: String):  Bitmap?
    fun streamToBitmap(inputStream: InputStream):  Bitmap?
    fun bitmapToStream(bitmap: Bitmap):  InputStream
    fun base64ToBitmap(base64String: String): Bitmap?
    fun bitmapToBase64(bitmap: Bitmap): String?
}