package net.codeocean.cheese.core.utils

import android.graphics.Color
import com.elvishew.xlog.XLog
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 颜色相似度比较器接口
 */
interface ColorSimilarityComparator {
    /**
     * 比较RGB颜色相似度
     * @return 相似度是否在阈值范围内
     */
    fun isSimilar(red: Int, green: Int, blue: Int): Boolean

    /**
     * 比较ARGB颜色相似度
     * @param considerAlpha 是否考虑透明度比较
     */
    fun isSimilar(alpha: Int, red: Int, green: Int, blue: Int, considerAlpha: Boolean = false): Boolean
}

/**
 * 颜色比较器基类
 */
abstract class BaseColorComparator(
    protected val baseColor: Int
) : ColorSimilarityComparator {
    protected val baseR: Int = Color.red(baseColor)
    protected val baseG: Int = Color.green(baseColor)
    protected val baseB: Int = Color.blue(baseColor)
    protected val baseA: Int = Color.alpha(baseColor)
}

/**
 * 高级颜色比较器实现
 * 使用HSV颜色空间进行更准确的相似度计算
 */

class AdvancedColorComparator(
    baseColor: Int,
    private val maxAllowedDistance: Double,
    private val hueWeight: Double = 0.6,
    private val saturationWeight: Double = 0.3,
    private val valueWeight: Double = 0.1
) : BaseColorComparator(baseColor) {

    companion object {
        private const val MAX_HUE = 360.0
        private const val MAX_SATURATION = 1.0
        private const val MAX_VALUE = 1.0
        private const val OPPOSITE_HUE_DIFF = 120.0

        // 标准化参数
        private const val MIN_SIMILARITY = 0.1
        private const val MAX_SIMILARITY = 1.0

        // 新增色系判断参数
        private const val COLOR_FAMILY_THRESHOLD = 45.0 // 色相差超过此值视为不同色系
        private const val MIN_SATURATION_FOR_FAMILY = 0.2 // 饱和度低于此值不参与色系判断
    }

    override fun isSimilar(red: Int, green: Int, blue: Int): Boolean {
        // 快速路径：完全相同颜色
        if (baseR == red && baseG == green && baseB == blue) return true

        // 转换为HSV颜色空间
        val (baseH, baseS, baseV) = convertToHSV(baseR, baseG, baseB)
        val (targetH, targetS, targetV) = convertToHSV(red, green, blue)

        // 新增：色系判断（在原有对立色判断之前）
        if (isDifferentColorFamily(baseH, baseS, targetH, targetS)) {
            XLog.d("Different color family - direct return false")
            return false
        }

        // 原有对立色判断逻辑保持不变
        val hueDiff = calculateHueDifference(baseH, targetH)
        val isOpposite = when {
            baseS < 0.01 || targetS < 0.01 -> true
            else -> hueDiff > OPPOSITE_HUE_DIFF && abs(baseV - targetV) > 0.3
        }
        if (isOpposite) return false

        // 原有相似度计算逻辑完全保持不变
        val normalizedHueDiff = hueDiff / 180.0
        val satDiff = abs(baseS - targetS) / MAX_SATURATION
        val valDiff = abs(baseV - targetV) / MAX_VALUE

        val rawDistance = calculateCombinedDistance(normalizedHueDiff, satDiff, valDiff)
        val similarity = MIN_SIMILARITY + (MAX_SIMILARITY - MIN_SIMILARITY) * rawDistance
        XLog.d("Distance:$similarity")

        return similarity <= maxAllowedDistance
    }

    // 新增：智能色系判断方法
    private fun isDifferentColorFamily(h1: Double, s1: Double, h2: Double, s2: Double): Boolean {
        // 低饱和度颜色不参与色系判断
        if (s1 < MIN_SATURATION_FOR_FAMILY || s2 < MIN_SATURATION_FOR_FAMILY) {
            return false
        }

        // 计算色相差
        val hueDiff = calculateHueDifference(h1, h2)

        // 色相差超过阈值视为不同色系
        return hueDiff > COLOR_FAMILY_THRESHOLD
    }

    // 保持原有方法不变
    override fun isSimilar(alpha: Int, red: Int, green: Int, blue: Int, considerAlpha: Boolean): Boolean {
        return isSimilar(red, green, blue) && (!considerAlpha || baseA == alpha)
    }

    private fun calculateHueDifference(h1: Double, h2: Double): Double {
        return when {
            h1.isNaN() || h2.isNaN() -> 0.0
            else -> {
                val normalizedDiff = abs(h1.mod(360.0) - h2.mod(360.0))
                min(normalizedDiff, MAX_HUE - normalizedDiff)
            }
        }
    }

    private fun calculateCombinedDistance(
        hueDiff: Double,
        satDiff: Double,
        valDiff: Double
    ): Double {
        return sqrt(
            hueWeight * hueDiff.pow(2) +
                    saturationWeight * satDiff.pow(2) +
                    valueWeight * valDiff.pow(2)
        )
    }

    private fun convertToHSV(r: Int, g: Int, b: Int): Triple<Double, Double, Double> {
        val hsvArray = FloatArray(3).apply {
            Color.RGBToHSV(r, g, b, this)
        }
        return Triple(hsvArray[0].toDouble(), hsvArray[1].toDouble(), hsvArray[2].toDouble())
    }
}