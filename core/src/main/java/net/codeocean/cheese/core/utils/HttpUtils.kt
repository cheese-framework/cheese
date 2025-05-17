package net.codeocean.cheese.core.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpUtils {
    companion object {
        private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(10, TimeUnit.SECONDS)     // 读取超时
            .writeTimeout(10, TimeUnit.SECONDS)    // 写入超时
            .build()
        fun downloadFile(url: String, savePath: String): Boolean {
            val request = Request.Builder()
                .url(url)
                .build()
            try {

                val targetFile = File(savePath)
                targetFile.parentFile?.mkdirs()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("Failed to download file. Response Code: ${response.code}")
                        return false
                    }
                    val responseBody = response.body ?: run {
                        println("Response body is null.")
                        return false
                    }
                    savePath.let { path ->
                        FileOutputStream(File(path)).use { fileOutputStream ->
                            responseBody.byteStream().copyTo(fileOutputStream)
                            println("File downloaded successfully.")
                        }
                    }
                }
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }
    }

}