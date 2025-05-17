package net.codeocean.cheese.core.api

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.View
import org.opencv.core.Point
interface Image {
    fun release(bit: Bitmap)
    fun drawRectOnBitmap(vararg elements: Any): Bitmap?
    fun drawPointOnBitmap(vararg elements: Any): Bitmap?
    fun showBitmapView(bitmap: Bitmap)
    fun decodeQRCode(bitmap: Bitmap): String?
    fun read(path: String): Bitmap?
    fun clip(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Bitmap
    fun generateQRCode(content: String, width: Int = 500, height: Int = 500): Bitmap?
    fun drawJsonBoundingBoxes(bitmap: Bitmap, jsonStr: String): Bitmap
    fun findImgBySift(inputImage: Bitmap, targetImage: Bitmap, maxDistance: Double): Point?
    fun findImgByTemplate(
        inputImage: Bitmap,
        targetImage: Bitmap,
        similarityThreshold: Double
    ): Point?
    fun findImgByResize(
        inputImage: Bitmap,
        targetImage: Bitmap,
        similarityThreshold: Double,
        width: Int,
        height: Int
    ): Point?
    fun fastFindImg(inputImage: Bitmap, targetImage: Bitmap, similarityThreshold: Double): Point?
    fun resize(inputBitmap: Bitmap, scale: Double): Bitmap
    fun binarize(inputBitmap: Bitmap, threshold: Double): Bitmap
}