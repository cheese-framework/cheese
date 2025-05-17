package net.codeocean.cheese.core.utils

import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun timeFormat (timestamp: Long, pn: String): String {
        val sdf = SimpleDateFormat(pn, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    fun getTime(): Long {
        return System.currentTimeMillis()
    }

}