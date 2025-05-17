package net.codeocean.cheese.backend.impl

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.CoreFactory

import net.codeocean.cheese.core.Script
import net.codeocean.cheese.core.api.Color
import net.codeocean.cheese.core.utils.AdvancedColorComparator

import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils.bitmapToMat
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import kotlin.math.abs
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

object ColorImpl : Color {

    init {
        try {
            OpenCVLoader.initDebug()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val RGB = 0
    val ARGB = 1


    private fun getColorComponents(color: Int, format: Int): IntArray {
        return when (format) {
            ARGB -> intArrayOf(
                android.graphics.Color.alpha(color),
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color)
            )

            RGB -> intArrayOf(
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color)
            )

            else -> throw IllegalArgumentException("Invalid color format: $format")
        }
    }

    private fun isInBounds(bitmap: Bitmap, x: Int, y: Int): Boolean {
        return x in 0 until bitmap.width && y in 0 until bitmap.height
    }

    //
//    ## compareColor 参数说明
//    - `maxDistance`: 最大允许颜色距离（0.0~1.0）
//    - 0.0: 必须完全一致
//    - 0.12-0.15: 推荐严格模式
//    - ≥0.25: 宽松匹配
//    - `hueWeight`: 色调权重（建议0.6-0.8）
//    - `saturationWeight`: 饱和度权重（建议0.1-0.3）
//    - `valueWeight`: 明度权重（建议0.1-0.2）
//    - `considerAlpha`: 是否比较透明度通道


    /**
     * 比较两种颜色是否相似
     * @param baseColorHex 基准颜色(16进制字符串)
     * @param targetColorHex 目标颜色(16进制字符串)
     * @param options 比较选项:
     *   - maxDistance: 最大允许距离(0.0-1.0)
     *   - hueWeight: 色相权重(0.0-1.0)
     *   - saturationWeight: 饱和度权重(0.0-1.0)
     *   - valueWeight: 明度权重(0.0-1.0)
     *   - considerAlpha: 是否考虑透明度
     * @return 颜色是否相似
     * @throws IllegalArgumentException 如果颜色格式无效
     */
    override fun compareColor(
        baseColorHex: String,
        targetColorHex: String,
        options: Map<String, Any>
    ): Boolean {
        val DEFAULT_MAX_DISTANCE = 0.15
        val DEFAULT_HUE_WEIGHT = 0.7
        val DEFAULT_SATURATION_WEIGHT = 0.2
        val DEFAULT_VALUE_WEIGHT = 0.1
        // 解析选项参数
        val maxDistance =
            (options["maxDistance"] as? Double)?.coerceIn(0.0, 1.0) ?: DEFAULT_MAX_DISTANCE
        val hueWeight = (options["hueWeight"] as? Double)?.coerceIn(0.0, 1.0) ?: DEFAULT_HUE_WEIGHT
        val saturationWeight = (options["saturationWeight"] as? Double)?.coerceIn(0.0, 1.0)
            ?: DEFAULT_SATURATION_WEIGHT
        val valueWeight =
            (options["valueWeight"] as? Double)?.coerceIn(0.0, 1.0) ?: DEFAULT_VALUE_WEIGHT
        val considerAlpha = options["considerAlpha"] as? Boolean ?: false

        // 归一化权重
        val totalWeight = hueWeight + saturationWeight + valueWeight
        val (normalizedHue, normalizedSat, normalizedVal) = if (totalWeight > 0) {
            Triple(
                hueWeight / totalWeight,
                saturationWeight / totalWeight,
                valueWeight / totalWeight
            )
        } else {
            Triple(DEFAULT_HUE_WEIGHT, DEFAULT_SATURATION_WEIGHT, DEFAULT_VALUE_WEIGHT)
        }

        // 创建比较器
        val comparator = AdvancedColorComparator(
            baseColor = android.graphics.Color.parseColor(baseColorHex),
            maxAllowedDistance = maxDistance,
            hueWeight = normalizedHue,
            saturationWeight = normalizedSat,
            valueWeight = normalizedVal
        )

        // 执行比较
        val targetColor = android.graphics.Color.parseColor(targetColorHex)
        return comparator.isSimilar(
            alpha = android.graphics.Color.alpha(targetColor),
            red = android.graphics.Color.red(targetColor),
            green = android.graphics.Color.green(targetColor),
            blue = android.graphics.Color.blue(targetColor),
            considerAlpha = considerAlpha
        )
    }

    override fun getPointColor(inputImage: Bitmap, format: Int, x: Int, y: Int): IntArray? {
        return if (isInBounds(inputImage, x, y)) {
            getColorComponents(inputImage.getPixel(x, y), format)
        } else {
            Log.w("ColorUtils", "Coordinates out of bounds: ($x, $y)")
            null
        }
    }

    override fun getPointHEX(inputImage: Bitmap, format: Int, x: Int, y: Int): String {
        return if (isInBounds(inputImage, x, y)) {
            val color = inputImage.getPixel(x, y)
            val formattedColor = if (format == ARGB) {
                String.format(
                    "#%02X%02X%02X%02X",
                    android.graphics.Color.alpha(color),
                    android.graphics.Color.red(color),
                    android.graphics.Color.green(color),
                    android.graphics.Color.blue(color)
                )
            } else {
                String.format(
                    "#%02X%02X%02X",
                    android.graphics.Color.red(color),
                    android.graphics.Color.green(color),
                    android.graphics.Color.blue(color)
                )
            }
            formattedColor
        } else {
            val message = if (format == ARGB) "ARGB: Out of bounds" else "HEX: Out of bounds"
            Log.w("ColorUtils", message)
            message
        }
    }

    override fun rgbToHEX(r: Int, g: Int, b: Int): String {
        return String.format(
            "#%02X%02X%02X",
            r.coerceIn(0, 255),
            g.coerceIn(0, 255),
            b.coerceIn(0, 255)
        )
    }

    override fun argbToHEX(a: Int, r: Int, g: Int, b: Int): String {
        return String.format(
            "#%02X%02X%02X%02X",
            a.coerceIn(0, 255),
            r.coerceIn(0, 255),
            g.coerceIn(0, 255),
            b.coerceIn(0, 255)
        )
    }

    override fun parseHex(hex: String): IntArray? {
        return if (hex.trimStart('#').length == 8) {
            parseHex(hex, 8)
        }else{
            parseHex(hex, 6)
        }

    }



    private fun parseHex(hex: String, expectedLength: Int): IntArray? {
        val sanitizedHex = hex.trimStart('#')
        return if (sanitizedHex.length == expectedLength) {
            IntArray(expectedLength / 2) {
                sanitizedHex.substring(it * 2, it * 2 + 2).toInt(16)
            }
        } else {
            Log.e("ColorUtils", "Invalid hex length for '$hex', expected length: $expectedLength")
            null
        }
    }


    override fun parseColor(colorString: String): Int? {
        return try {
            android.graphics.Color.parseColor(colorString)
        } catch (e: IllegalArgumentException) {
            Log.e("ColorUtils", "Error parsing color: ${e.message}")
            null
        }
    }

    fun convertBGRtoARGB(bgrMat: Mat): Mat {
        val argbMat = Mat()
        Imgproc.cvtColor(bgrMat, argbMat, Imgproc.COLOR_BGR2RGBA)
        return argbMat
    }

    private fun findMultiColors(
        img1: Bitmap,
        firstColor: String,
        paths: Array<IntArray>,
        options: Map<String, Any>
    ): Point? {
        val img = Mat()
        bitmapToMat(img1, img)
        val effectiveOptions = options ?: emptyMap()
        val list = paths.flatMap { p -> listOf(p[0], p[1], p[2]) }.toIntArray()
        val distance = effectiveOptions["maxDistance"] as? Int ?: 30
        val firstPoints =
            findColorInRange(
                img,
                android.graphics.Color.parseColor(firstColor),
                distance,
                null
            )?.toArray()
                ?: emptyArray()

        return firstPoints.firstOrNull { point ->
            point != null && checksPath(img, point, distance, list)
        }.also {
            if (it != null) println("成功")
        }

    }


    override fun findMultiColors(
        img1: Bitmap,
        firstColor: String,
        paths: Any,
        options: Any
    ): Point? {
        val script = CoreEnv.globalVM.get<Script>()
        require(script != null) { "Script must not be null" }
        val pathList = mutableListOf<IntArray>()
        for (item in script.convertNativeArrayToList(paths)) {
            when (item) {
                is List<*> -> {
                    val coordinates = item
                    if (coordinates.size == 3) {
                        val x = (coordinates[0] as Double).toInt()
                        val y = (coordinates[1] as Double).toInt()
                        val color = coordinates[2] as String
                        pathList.add(intArrayOf(x, y, android.graphics.Color.parseColor(color)))
                    } else {
                        println("Unexpected list format")
                    }
                }

                else -> {
                    // 处理其他类型的项
                    println("Unknown type: $item")
                }
            }
        }

        val stringMap = HashMap<String, Any>()
        script.convertNativeObjectToMap(options).forEach { (key, value) ->
            stringMap[key.toString()] = value // 强制将键转为 String
        }

        return try {
            findMultiColors(
                img1, firstColor,
                pathList.toTypedArray(), stringMap
            )
        } catch (e: Exception) {
            throw Error(e)
        }
    }


    fun lowerBound(color: Int, threshold: Int): Scalar {
        val red = android.graphics.Color.red(color).toDouble()
        val green = android.graphics.Color.green(color).toDouble()
        val blue = android.graphics.Color.blue(color).toDouble()
        val lowerBound = Scalar(
            (red - threshold).coerceAtLeast(0.0),
            (green - threshold).coerceAtLeast(0.0),
            (blue - threshold).coerceAtLeast(0.0),
            255.0
        )
        return lowerBound
    }

    fun upperBound(color: Int, threshold: Int): Scalar {
        val red = android.graphics.Color.red(color).toDouble()
        val green = android.graphics.Color.green(color).toDouble()
        val blue = android.graphics.Color.blue(color).toDouble()
        val upperBound = Scalar(
            (red + threshold).coerceAtMost(255.0),
            (green + threshold).coerceAtMost(255.0),
            (blue + threshold).coerceAtMost(255.0),
            255.0
        )

        return upperBound
    }

    fun findColorInRange(
        image: Mat,
        color: Int,
        threshold: Int,
        rect: Rect?
    ): MatOfPoint? {
        val lowerBound = lowerBound(color, threshold)
        val upperBound = upperBound(color, threshold)
        val bi = Mat()
        XLog.e(lowerBound)
        XLog.e(upperBound)

        val matToProcess = if (rect != null) Mat(image, rect) else image
        Core.inRange(matToProcess, lowerBound, upperBound, bi)

        if (rect != null) matToProcess.release()

        val nonZeroPos = Mat()
        Core.findNonZero(bi, nonZeroPos)

        val result = if (nonZeroPos.empty()) null else {
            // Extract points from Mat
            val points = mutableListOf<org.opencv.core.Point>()
            for (i in 0 until nonZeroPos.rows()) {
                val point = nonZeroPos.get(i, 0)
                if (point != null) {
                    points.add(org.opencv.core.Point(point[0], point[1]))
                }
            }
            MatOfPoint(*points.toTypedArray())
        }

        bi.release()
        nonZeroPos.release()

        return result
    }

    fun checksPath(
        image: Mat,
        startingPoint: org.opencv.core.Point,
        distance: Int,
        points: IntArray
    ): Boolean {

        for (i in points.indices step 3) {
            val x = points[i] + startingPoint.x.toInt()
            val y = points[i + 1] + startingPoint.y.toInt()
            val color = points[i + 2]
            val colorDetector = Detector(color, distance)
            if (x !in 0 until image.width() || y !in 0 until image.height()) {
                return false
            }

            val c = getPixel(image, x, y)
            val red = (c shr 16) and 0xFF
            val green = (c shr 8) and 0xFF
            val blue = c and 0xFF

            if (!colorDetector.Color(red, green, blue)) {
                return false
            }
        }
        return true
    }

    data class Detector(val color: Int, val distance: Int) {
        private val mDistance = distance * 3
        val R: Int = (color shr 16) and 0xFF
        val G: Int = (color shr 8) and 0xFF
        val B: Int = color and 0xFF

        fun Color(R: Int, G: Int, B: Int): Boolean {
            return abs(R - this.R) + abs(G - this.G) + abs(B - this.B) <= mDistance
        }
    }

    fun getPixel(mat: Mat, x: Int, y: Int): Int {
        require(!mat.empty()) { "Image is empty" }
        require(x in 0 until mat.cols() && y in 0 until mat.rows()) {
            "Coordinates ($x, $y) are out of bounds."
        }
        val channels = mat.get(y, x)
            ?: throw NullPointerException("Channels data is null for coordinates ($x, $y)")
        return when (channels.size) {
            3 -> argb(
                255,
                channels[2].toInt(),
                channels[1].toInt(),
                channels[0].toInt()
            ) // BGR image
            4 -> android.graphics.Color.argb(
                255,
                channels[0].toInt(),
                channels[1].toInt(),
                channels[2].toInt()
            ) // ARGB image
            else -> throw IllegalStateException("Unexpected number of channels: ${channels.size}")
        }
    }

    fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

}