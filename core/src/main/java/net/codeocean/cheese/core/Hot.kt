package net.codeocean.cheese.core

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.ProgressBar
import android.widget.Toast
import net.codeocean.cheese.core.runtime.ScriptExecutionController
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URL


object Hot {
    fun checkUpdate(mode:Int,c: () -> Unit) {

        if (mode==1){
            if (!CoreEnv.runTime.isDebugMode){
                c()
                return
            }

        }else{
            if (CoreEnv.runTime.isDebugMode){
                c()
                return
            }
        }

        val file = CoreFactory.getPath().WORKING_DIRECTORY
        if (!file.exists()) {
            c()
            return
        }

        val result = ScriptExecutionController.parseCheeseToml(file)
        val version = result?.getTable("build")
            ?.getTable("hot")
            ?.getString("version").orEmpty()
        val url = result?.getTable("build")
            ?.getTable("hot")
            ?.getString("url").orEmpty()

        if (version.isNotEmpty() && url.isNotEmpty()) {
            CoreEnv.envContext.activity?.let { showUpdateDialog(it, version, url, c) } ?: run { c() }
            return
        }
        c()
    }


    // 显示更新对话框，检查是否有新版本
    fun showUpdateDialog(
        context: Activity,
        currentVersion: String,
        url: String,
        onComplete: () -> Unit
    ) {
        // 创建请求更新信息的异步任务
        val updateTask = object : AsyncTask<Void, Void, UpdateInfo?>() {

            override fun doInBackground(vararg params: Void?): UpdateInfo? {
                return getUpdateInfoFromServer(url)
            }

            override fun onPostExecute(updateInfo: UpdateInfo?) {
                super.onPostExecute(updateInfo)

                if (updateInfo == null) {
                    Toast.makeText(context, "无法获取更新信息", Toast.LENGTH_SHORT).show()
                    onComplete() // 版本相同，调用回调
                    return
                }

                // 比较本地版本和服务器版本
                if (isNewVersionAvailable(currentVersion, updateInfo.updateVersion)) {
                    // 如果有新版本，显示更新对话框
                    showVersionUpdateDialog(context, currentVersion, updateInfo, onComplete)
                } else {
                    Toast.makeText(context, "当前已经是最新版本", Toast.LENGTH_SHORT).show()
                    onComplete() // 版本相同，调用回调
                }
            }
        }

        // 执行异步任务
        updateTask.execute()
    }

    // 从服务器获取更新信息
    private fun getUpdateInfoFromServer(url: String): UpdateInfo? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string() ?: return null
                val jsonObject = JSONObject(jsonResponse)
                return UpdateInfo(
                    updateVersion = jsonObject.getString("update_version"),
                    updateContent = jsonObject.getString("update_content"),
                    downloadURL = jsonObject.getString("download_url")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    // 比较本地版本和服务器版本
    private fun isNewVersionAvailable(currentVersion: String, serverVersion: String): Boolean {
        return serverVersion > currentVersion
    }

    // 显示版本更新对话框
    private fun showVersionUpdateDialog(
        context: Context,
        currentVersion: String,
        updateInfo: UpdateInfo,
        onComplete: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("版本更新")

        builder.setMessage("本地版本：${currentVersion} 服务器版本：${updateInfo.updateVersion}\n\n更新内容:\n${updateInfo.updateContent}\n\n是否立即更新？")

        builder.setPositiveButton("更新") { dialog, which ->
            downloadUpdate(context, updateInfo.downloadURL, onComplete)
        }

        builder.setNegativeButton("取消") { dialog, which ->
            Toast.makeText(context, "取消更新", Toast.LENGTH_SHORT).show()
            onComplete()
        }

        builder.create().show()
    }

    private fun downloadUpdate(context: Context, downloadUrl: String, onComplete: () -> Unit) {
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.max = 100
        progressBar.progress = 0


        val progressDialog = AlertDialog.Builder(context)
            .setTitle("更新中...")
            .setView(progressBar)
            .setCancelable(false)
            .create()


        progressDialog.show()

        val outputDir = CoreFactory.getPath().WORKING_DIRECTORY
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }


        val outputFile = File(outputDir, "cheese.hot")
        val outputStream: OutputStream = outputFile.outputStream()
        object : AsyncTask<Void, Int, Boolean>() {

            override fun doInBackground(vararg params: Void?): Boolean {
                var result = false
                try {
                    val url = URL(downloadUrl)
                    val connection = url.openConnection()
                    connection.connect()
                    val totalSize = connection.contentLengthLong // 获取文件总大小

                    val inputStream: InputStream = connection.getInputStream()


                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var downloadedSize: Long = 0

                    // 下载文件并实时更新进度
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead

                        // 计算当前进度并通过 publishProgress 发送到 UI 线程
                        val progress = (downloadedSize * 100 / totalSize).toInt()
                        publishProgress(progress)
                    }

                    // 下载完成后，关闭输入输出流
                    outputStream.close()
                    inputStream.close()
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return result
            }

            override fun onProgressUpdate(vararg values: Int?) {
                // 在 UI 线程上更新进度条
                values[0]?.let {
                    progressBar.progress = it
                }
            }

            override fun onPostExecute(result: Boolean) {
                super.onPostExecute(result)



                progressDialog.dismiss()

                if (result) {

                    Toast.makeText(context, "下载完成，开始热更新", Toast.LENGTH_SHORT).show()
                    File(CoreFactory.getPath().WORKING_DIRECTORY, "main").deleteRecursively()
                    File(
                        CoreFactory.getPath().WORKING_DIRECTORY,
                        "node_modules"
                    ).deleteRecursively()
                    File(CoreFactory.getPath().WORKING_DIRECTORY, "cheese.toml").delete()
                    CoreFactory.getZip().decompress(
                        outputFile.path,
                        CoreFactory.getPath().WORKING_DIRECTORY.path,
                        ""
                    )
                    restartApp(context)
//                    onComplete() // 下载完成后调用回调
                } else {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                    onComplete() // 下载失败后调用回调
                }
            }

        }.execute()
    }

    fun restartApp(context: Context) {
        val intent = Intent(context, Class.forName("net.codeocean.cheese.MainActivity"))
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        System.exit(0)
    }


    // 更新信息的数据类
    data class UpdateInfo(
        val updateVersion: String,
        val updateContent: String,
        val downloadURL: String
    )

}