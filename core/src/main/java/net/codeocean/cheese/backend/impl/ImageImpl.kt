package net.codeocean.cheese.backend.impl

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import net.codeocean.cheese.backend.impl.BaseImpl.act
import net.codeocean.cheese.backend.impl.BaseImpl.cx
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.R
import net.codeocean.cheese.core.activity.BitmapDisplayActivity

import net.codeocean.cheese.core.api.Image

import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.features2d.BFMatcher
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.ORB
import org.opencv.features2d.SIFT
import org.opencv.imgproc.Imgproc
import java.nio.charset.Charset

object ImageImpl:Image {
    private inline fun <reified T> Any.castOrThrow(paramName: String): T {
        return this as? T
            ?: throw IllegalArgumentException("Expected $paramName to be of type ${T::class}, but got ${this::class}")
    }

    override fun drawRectOnBitmap(vararg elements: Any): Bitmap? {
        return when (elements.size) {
            5 -> {
                val bitmap = elements[0].castOrThrow<Bitmap>("bit")
                val text = elements[1].castOrThrow<String>("text")
                val textColor = elements[2].castOrThrow<Int>("textColor")
                val borderedColor = elements[3].castOrThrow<Int>("borderedColor")
                val rect = elements[4].castOrThrow<Rect>("rect")
                bitmap.drawRectOnBitmap(
                    left = rect.left.toFloat(),
                    top = rect.top.toFloat(),
                    right = rect.right.toFloat(),
                    bottom = rect.bottom.toFloat(),
                    borderColor = borderedColor,
                    text = text,
                    textColor = textColor
                )
            }

            3 -> {
                val textColor = Color.GREEN
                val borderedColor = Color.RED
                val bitmap = elements[0].castOrThrow<Bitmap>("bit")
                val text = elements[1].castOrThrow<String>("text")
                val rect = elements[2].castOrThrow<Rect>("rect")
                bitmap.drawRectOnBitmap(
                    left = rect.left.toFloat(),
                    top = rect.top.toFloat(),
                    right = rect.right.toFloat(),
                    bottom = rect.bottom.toFloat(),
                    borderColor = borderedColor,
                    text = text,
                    textColor = textColor
                )
            }

            else -> null
        }
    }

    override fun drawPointOnBitmap(vararg elements: Any): Bitmap? {
        return when (elements.size) {
            6 -> {
                val bitmap = elements[0].castOrThrow<Bitmap>("bit")
                val text = elements[1].castOrThrow<String>("text")
                val textColor = elements[2].castOrThrow<Int>("textColor")
                val pointColor = elements[3].castOrThrow<Int>("pointColor")
                val x = elements[4].castOrThrow<Double>("x").toInt()
                val y = elements[5].castOrThrow<Double>("y").toInt()
                bitmap.drawPointOnBitmap(
                    x = x.toFloat(),
                    y = y.toFloat(),
                    pointColor = pointColor,
                    text = text,
                    textColor = textColor
                )
            }

            4 -> {
                val textColor = Color.GREEN
                val pointColor = Color.RED
                val bitmap = elements[0].castOrThrow<Bitmap>("bit")
                val text = elements[1].castOrThrow<String>("text")
                val x = elements[2].castOrThrow<Double>("x").toInt()
                val y = elements[3].castOrThrow<Double>("y").toInt()
                bitmap.drawPointOnBitmap(
                    x = x.toFloat(),
                    y = y.toFloat(),
                    pointColor = pointColor,
                    text = text,
                    textColor = textColor
                )
            }

            else -> null
        }
    }


    override fun showBitmapView(bitmap: Bitmap) {
        val bit:Bitmap =bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        runOnUi {
            BitmapDisplayActivity.launch(cx, bit)
        }
    }


    /**
     * 在 Bitmap 上绘制一个点及其文字
     */
    fun Bitmap.drawPointOnBitmap(
        x: Float,
        y: Float,
        pointColor: Int = Color.GREEN,
        text: String? = null,
        textColor: Int = Color.RED,
        textSize: Float = 30f,
        radius: Float = 6f
    ): Bitmap {
        val configToUse = config ?: Bitmap.Config.ARGB_8888
        val result = copy(configToUse, true)
        val canvas = Canvas(result)

        val pointPaint = Paint().apply {
            color = pointColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = textColor
            this.textSize = textSize
            isAntiAlias = true
        }

        // 绘制点
        canvas.drawCircle(x, y, radius, pointPaint)

        // 绘制文字（如果有）
        text?.let {
            canvas.drawText(it, x + 10f, y - 10f, textPaint)
        }

        return result
    }

    /**
     * 在 Bitmap 上绘制矩形框及其文字
     */
    fun Bitmap.drawRectOnBitmap(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        borderColor: Int = Color.GREEN,
        borderWidth: Float = 2f,
        text: String? = null,
        textColor: Int = Color.RED,
        textSize: Float = 30f
    ): Bitmap {
        val configToUse = config ?: Bitmap.Config.ARGB_8888
        val result = copy(configToUse, true)
        val canvas = Canvas(result)

        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = textColor
            this.textSize = textSize
            isAntiAlias = true
        }

        val rect = RectF(left, top, right, bottom)

        // 绘制矩形
        canvas.drawRect(rect, borderPaint)

        // 绘制文字（如果有）
        text?.let {
            canvas.drawText(it, left, top - 10f, textPaint)
        }

        return result
    }


    override fun release(bit: Bitmap) {
         bit.recycle()
    }

    class CPoint(x: Double, y: Double, maxVal: Double) : Point(x, y) {
        private var maxVal: Double

        init {
            this.maxVal = maxVal
        }

        fun getMaxVal(): Double {
            return maxVal
        }

        fun setMaxVal(maxVal: Double) {
            this.maxVal = maxVal
        }

        override fun toString(): String {
            return (("CPoint{" +
                    "x=" + x).toString() +
                    ", y=" + y).toString() +
                    ", maxVal='" + maxVal + '\'' +
                    '}'
        }
    }

    override fun decodeQRCode(bitmap: Bitmap): String? {
        try {
            // 将Bitmap转换为LuminanceSource
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            // 创建一个LuminanceSource
            val source: LuminanceSource = RGBLuminanceSource(width, height, pixels)

            // 创建一个BinaryBitmap
            val bitmapBin = BinaryBitmap(HybridBinarizer(source))

            // 使用QRCodeReader来解码二维码
            val reader = QRCodeReader()
            val result = reader.decode(bitmapBin)

            return result.text  // 返回解码后的内容
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun read(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun clip(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Bitmap {
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }



    override fun generateQRCode(content: String, width: Int, height: Int): Bitmap? {
        try {
            // 设置二维码的参数
            val hints = hashMapOf<EncodeHintType, Any>(
                EncodeHintType.MARGIN to 1, // 设置二维码的边距
                EncodeHintType.CHARACTER_SET to Charset.forName("UTF-8") // 设置字符集编码
            )

            // 创建二维码的BitMatrix
            val bitMatrix: BitMatrix =
                MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            // 转换为Bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(
                                x,
                                y
                            )
                        )Color.BLACK else Color.WHITE
                    )
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun drawJsonBoundingBoxes(bitmap: Bitmap, jsonStr: String): Bitmap {
        // 使用原生Canvas绘制（比OpenCV更稳定且支持中文）
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        try {
            val gson = Gson()
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val boundingBoxes: List<Map<String, Any>> = gson.fromJson(jsonStr, type)

            // 预定义样式（可配置）
            val boxPaint = Paint().apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.RED
                textSize = 22f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            for (bbox in boundingBoxes) {
                val left = (bbox["left"] as? Number)?.toFloat() ?: 0f
                val top = (bbox["top"] as? Number)?.toFloat() ?: 0f
                val right = (bbox["right"] as? Number)?.toFloat() ?: 0f
                val bottom = (bbox["bottom"] as? Number)?.toFloat() ?: 0f
                val text = bbox["text"] as? String ?: ""


                canvas.drawRect(left, top, right, bottom, boxPaint)


                val displayText = if (text.isNotEmpty()) text else "你好"
                val textWidth = textPaint.measureText(displayText)


                val textX = when {
                    left + textWidth > canvas.width -> right - textWidth - 5
                    else -> left + 5
                }

                val textY = when {
                    top - 10 < 0 -> bottom + textPaint.textSize + 5
                    else -> top - 10
                }


                textPaint.style = Paint.Style.STROKE
                textPaint.strokeWidth = 2f
                textPaint.color = Color.BLACK
                canvas.drawText(displayText, textX, textY, textPaint)

                textPaint.style = Paint.Style.FILL
                textPaint.color = Color.RED
                canvas.drawText(displayText, textX, textY, textPaint)
            }
        } catch (e: Exception) {
            Log.e("DrawBoxes", "Error: ${e.message}")
        }

        return resultBitmap
    }


    /**
     * 使用SIFT特征匹配算法在输入图像中查找目标图像的位置
     * @param inputImage 输入图像（搜索区域）
     * @param targetImage 目标图像（要查找的模板）
     * @param maxDistance 最大允许的特征距离阈值，越小匹配要求越严格（建议值0.1-0.5）
     * @return 返回匹配位置的Point对象，未找到返回null
     */
    override fun findImgBySift(inputImage: Bitmap, targetImage: Bitmap, maxDistance: Double): Point? {
        // 读取输入图像和目标图像
        val imgScene = Mat()
        // 读取图像
        val imgObject = Mat()
        Utils.bitmapToMat(inputImage, imgScene, false)
        Utils.bitmapToMat(targetImage, imgObject, false)

        // 检查图像是否为空
        if (imgScene.empty() || imgObject.empty()) {
            Log.e("ImageDebug", "Input image or target image is empty")
            return null
        }

        Log.d("ImageDebug", "imgScene size: ${imgScene.size()}")
        Log.d("ImageDebug", "imgObject size: ${imgObject.size()}")

        // 创建 SIFT 检测器
        val sift = SIFT.create()

        // 检测和计算描述符
        val keypointsObject = MatOfKeyPoint()
        val keypointsScene = MatOfKeyPoint()
        val descriptorsObject = Mat()
        val descriptorsScene = Mat()

        sift.detectAndCompute(imgObject, Mat(), keypointsObject, descriptorsObject)
        sift.detectAndCompute(imgScene, Mat(), keypointsScene, descriptorsScene)

        Log.d("ImageDebug", "keypointsObject size: ${keypointsObject.size()}")
        Log.d("ImageDebug", "keypointsScene size: ${keypointsScene.size()}")

        // 创建 BFMatcher（暴力匹配器）
        val matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE)
        val matches = MatOfDMatch()
        matcher.match(descriptorsObject, descriptorsScene, matches)

        Log.d("ImageDebug", "Number of matches: ${matches.rows()}")

        // 筛选匹配点
        val matchesList = matches.toList()
        val goodMatchesList = matchesList.filter { it.distance < maxDistance }

        Log.d("ImageDebug", "Number of good matches: ${goodMatchesList.size}")

        // 如果找到足够的匹配点，计算图像的位置
        if (goodMatchesList.size >= 5) {
            val keypointsObjectList = keypointsObject.toList()
            val keypointsSceneList = keypointsScene.toList()

            val objList = mutableListOf<Point>()
            val sceneList = mutableListOf<Point>()
            goodMatchesList.forEach { match ->
                objList.add(keypointsObjectList[match.queryIdx].pt)
                sceneList.add(keypointsSceneList[match.trainIdx].pt)
            }

            val obj = MatOfPoint2f()
            obj.fromList(objList)

            val scene = MatOfPoint2f()
            scene.fromList(sceneList)

            val H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 3.0)

            val objCorners = Mat(4, 1, CvType.CV_32FC2)
            val sceneCorners = Mat(4, 1, CvType.CV_32FC2)

            // 定义目标图像的四个角点
            objCorners.put(0, 0, 0.0, 0.0)
            objCorners.put(1, 0, imgObject.cols().toDouble(), 0.0)
            objCorners.put(2, 0, imgObject.cols().toDouble(), imgObject.rows().toDouble())
            objCorners.put(3, 0, 0.0, imgObject.rows().toDouble())

            // 计算目标图像的四个角点在输入图像中的位置
            Core.perspectiveTransform(objCorners, sceneCorners, H)

            val sceneCornersList = mutableListOf<Point>()
            for (i in 0 until sceneCorners.rows()) {
                val point = sceneCorners.get(i, 0)
                sceneCornersList.add(Point(point[0], point[1]))
            }

            // 计算矩形的中心点
            val tl = sceneCornersList[0]
            val br = sceneCornersList[2]
            val centerX = (tl.x + br.x) / 2
            val centerY = (tl.y + br.y) / 2

            // 释放资源
            imgScene.release()
            imgObject.release()
            keypointsObject.release()
            keypointsScene.release()
            descriptorsObject.release()
            descriptorsScene.release()
            matches.release()

            // 返回矩形的中心点坐标
            return Point(centerX, centerY)
        } else {
            // 释放资源
            imgScene.release()
            imgObject.release()
            keypointsObject.release()
            keypointsScene.release()
            descriptorsObject.release()
            descriptorsScene.release()
            matches.release()

            return null
        }
    }
    /**
     * 使用模板匹配算法查找目标图像在源图像中的位置
     * @param inputImage 源图像（在该图像中搜索目标）
     * @param targetImage 目标图像（要搜索的模板）
     * @param similarityThreshold 相似度阈值(0.0-1.0)，值越大匹配要求越严格
     * @return 返回匹配目标的中心点坐标，未找到返回null
     */
    override fun findImgByTemplate(
        inputImage: Bitmap,
        targetImage: Bitmap,
        similarityThreshold: Double
    ): Point? {
        // 将 Bitmap 转换为 Mat
        val largeImage = Mat()
        val templateImage = Mat()
        Utils.bitmapToMat(inputImage, largeImage)
        Utils.bitmapToMat(targetImage, templateImage)

        // 检查图像是否为空
        if (largeImage.empty() || templateImage.empty()) {
            println("Error: 图像为空")
            return null
        }

        // 进行模板匹配
        val result = Mat()
        Imgproc.matchTemplate(largeImage, templateImage, result, Imgproc.TM_CCOEFF_NORMED)

        // 找到匹配结果中的最大值和最小值
        val minMaxResult = Core.minMaxLoc(result)

        // 如果最大相似度小于阈值，则认为未找到匹配
        if (minMaxResult.maxVal < similarityThreshold) {
            println("未找到匹配区域，最大相似度为: ${minMaxResult.maxVal}")
            return null
        }

        // 计算小图的中心坐标
        val centerX = minMaxResult.maxLoc.x + templateImage.cols() / 2.0
        val centerY = minMaxResult.maxLoc.y + templateImage.rows() / 2.0

        // 返回中心坐标
        return Point(centerX, centerY)
    }

    /**
     * 通过缩放大图后进行模板匹配来查找目标图像位置
     * @param inputImage 输入的大图像（搜索区域）
     * @param targetImage 要查找的目标小图像
     * @param similarityThreshold 相似度阈值(0-1)，越大匹配要求越严格
     * @param width 缩放后的目标宽度
     * @param height 缩放后的目标高度
     * @return 返回匹配中心点的CPoint对象(包含坐标和置信度)，未找到返回null
     */
    override fun findImgByResize(
        inputImage: Bitmap,
        targetImage: Bitmap,
        similarityThreshold: Double,
        width: Int,
        height: Int
    ): Point? {
        val bigImage: Mat
        val smallImage: Mat
        val originalTopLeft: Point
        val originalBottomRight: Point
        // 读取大图和小图
        val big = Mat()
        val small = Mat()

        // 将 Bitmap 转换为 Mat
        Utils.bitmapToMat(inputImage, big)
        Utils.bitmapToMat(targetImage, small)
        bigImage = big
        smallImage = small


        // 确认图像读取成功
        if (bigImage.empty()) {
            return null
        }
        if (smallImage.empty()) {
            return null
        }

        // 获取大图的原始尺寸
        val originalHeight = bigImage.rows()
        val originalWidth = bigImage.cols()

        // 指定缩放后的分辨率
        val newWidth = width
        val newHeight = height
        val dim = Size(newWidth.toDouble(), newHeight.toDouble())

        // 缩放大图
        val resizedBigImage = Mat()
        Imgproc.resize(bigImage, resizedBigImage, dim, 0.0, 0.0, Imgproc.INTER_AREA)

        // 使用模板匹配
        val result = Mat()
        Imgproc.matchTemplate(resizedBigImage, smallImage, result, Imgproc.TM_CCOEFF_NORMED)

        // 获取匹配结果中的最大值及其位置
        val mmr = Core.minMaxLoc(result)
        val topLeft = mmr.maxLoc
        if (mmr.maxVal < similarityThreshold) return null

        // 获取小图的尺寸
        val h = smallImage.rows()
        val w = smallImage.cols()

        // 计算匹配位置的右下角坐标
        val bottomRight = Point(topLeft.x + w, topLeft.y + h)

        // 将坐标转换回原始大图中的坐标
        val scaleX = originalWidth.toDouble() / newWidth
        val scaleY = originalHeight.toDouble() / newHeight
        originalTopLeft = Point(topLeft.x * scaleX, topLeft.y * scaleY)
        originalBottomRight = Point(bottomRight.x * scaleX, bottomRight.y * scaleY)
        val originalCenter = Point(
            (originalTopLeft.x + originalBottomRight.x) / 2,
            (originalTopLeft.y + originalBottomRight.y) / 2
        )
        return CPoint(originalCenter.x, originalCenter.y, mmr.maxVal)
    }
    /**
     * 使用模板匹配快速查找目标图像在输入图像中的位置
     * @param inputImage 输入图像（大图）
     * @param targetImage 目标图像（小图模板）
     * @param similarityThreshold 相似度阈值（0-1），越大匹配要求越严格
     * @return 返回匹配中心点的CPoint对象（包含坐标和置信度），未找到返回null
     */
    override fun fastFindImg(
        inputImage: Bitmap,
        targetImage: Bitmap,
        similarityThreshold: Double
    ): Point? {
        // 转换 Bitmap 为 Mat
        val bigMat = Mat()
        val smallMat = Mat()
        Utils.bitmapToMat(inputImage, bigMat)
        Utils.bitmapToMat(targetImage, smallMat)

        // 检查尺寸是否有效
        if (bigMat.empty() || smallMat.empty()) return null
        if (bigMat.rows() < smallMat.rows() || bigMat.cols() < smallMat.cols()) return null

        // 模板匹配
        val result = Mat()
        Imgproc.matchTemplate(bigMat, smallMat, result, Imgproc.TM_CCOEFF_NORMED)

        // 获取匹配结果
        val mmr = Core.minMaxLoc(result)
        if (mmr.maxVal < similarityThreshold) return null

        // 计算匹配位置
        val matchLocation = mmr.maxLoc
        val centerX = matchLocation.x + smallMat.cols() / 2.0
        val centerY = matchLocation.y + smallMat.rows() / 2.0

        return CPoint(centerX, centerY, mmr.maxVal)
    }


   override fun resize(inputBitmap: Bitmap, scale: Double): Bitmap {
        val inputMat = Mat()
        Utils.bitmapToMat(inputBitmap, inputMat)

        val outputMat = Mat()
        Imgproc.resize(
            inputMat,
            outputMat,
            Size(inputMat.cols() * scale, inputMat.rows() * scale)
        )

        val outputBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(outputMat, outputBitmap)

        return outputBitmap
    }


    override fun binarize(inputBitmap: Bitmap, threshold: Double): Bitmap {
        // 检查输入图像是否有效
        if (inputBitmap.width == 0 || inputBitmap.height == 0) {
            throw IllegalArgumentException("Invalid input image")
        }

        // 将 Bitmap 转换为 Mat（默认转换为 BGR 格式）
        val inputMat = Mat()
        Utils.bitmapToMat(inputBitmap, inputMat)

        // 如果是彩色图像，则转换为灰度图像
        var grayMat = Mat()
        if (inputMat.channels() > 1) {
            // 如果是彩色图像，则转换为灰度图像
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)
        } else {
            // 如果已经是灰度图像，直接使用输入图像的副本
            grayMat = inputMat.clone()
        }

        // 进行二值化处理
        val outputMat = Mat()
        Imgproc.threshold(grayMat, outputMat, threshold, 255.0, Imgproc.THRESH_BINARY)

        // 将 Mat 转换回 Bitmap
        val outputBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(outputMat, outputBitmap)

        // 释放 Mat 对象，避免内存泄漏
        inputMat.release()
        grayMat.release()
        outputMat.release()

        return outputBitmap
    }
}