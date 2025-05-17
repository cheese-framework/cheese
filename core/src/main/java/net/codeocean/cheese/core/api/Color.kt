package net.codeocean.cheese.core.api

import android.graphics.Bitmap
import org.opencv.core.Point
interface Color {
    fun compareColor(  baseColorHex: String,
                       targetColorHex: String,
                       options: Map<String, Any> = emptyMap()): Boolean
    fun getPointColor(inputImage: Bitmap, format: Int, x: Int, y: Int): IntArray?
    fun getPointHEX(inputImage: Bitmap, format: Int, x: Int, y: Int): String
    fun rgbToHEX(r: Int, g: Int, b: Int): String
    fun argbToHEX(a: Int, r: Int, g: Int, b: Int): String
    fun parseHex(hex: String): IntArray?
    fun parseColor(colorString: String): Int?
    fun findMultiColors(
        img1: Bitmap,
        firstColor: String,
        paths: Any,
        options: Any
    ): Point?
}