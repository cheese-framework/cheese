package net.codeocean.cheese.backend.impl

import android.graphics.Bitmap
import android.util.Pair
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.api.Converters
import net.codeocean.cheese.core.utils.ConvertersUtils
import java.io.InputStream

object ConvertersImpl:Converters {
    override fun arrayToArrayList(objectList: List<String>): Array<Any?> {
      return  ConvertersUtils.arrayToArrayList(objectList)
    }

    override fun pairArray(vararg elements: Int): Array<Pair<Int, Int>> {
        val x=ConvertersUtils.pairArray(*elements)
        XLog.e(pairArrays(x))
        return  x
    }

    override fun pairArrays(vararg arrays: Any): Array<Array<Pair<Int, Int>>> {
        // 遍历传入的每个数组，确保它是 Array<Pair<Int, Int>> 类型
        val convertedArrays = arrays.mapNotNull { array ->
            if (array is Array<*>) {
                array.filterIsInstance<Pair<Int, Int>>().toTypedArray()
            } else {
                null
            }
        }.toTypedArray()

        // 将转换后的数组传递给 ConvertersUtils.pairArrays
        return ConvertersUtils.pairArrays(*convertedArrays)
    }


    override fun sdToStream(filePath: String): InputStream? {
        return ConvertersUtils.sdToStream(filePath)
    }

    override fun assetsToStream(filePath: String): InputStream? {
        return ConvertersUtils.assetsToStream(filePath)
    }

    override fun assetsToBitmap(filePath: String): Bitmap? {
        return ConvertersUtils.assetsToStream(filePath)
            ?.let { ConvertersUtils.streamToBitmap(it) }
    }

    override fun streamToBitmap(inputStream: InputStream): Bitmap? {
        return ConvertersUtils.streamToBitmap(inputStream)
    }

    override fun bitmapToStream(bitmap: Bitmap): InputStream {
        return ConvertersUtils.bitmapToStream(bitmap)
    }

    override fun base64ToBitmap(base64String: String): Bitmap? {
        return ConvertersUtils.base64ToBitmap(base64String)
    }

    override fun bitmapToBase64(bitmap: Bitmap): String? {
        return ConvertersUtils.bitmapToBase64(bitmap)
    }
}