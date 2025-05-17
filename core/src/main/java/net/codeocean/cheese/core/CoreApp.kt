package net.codeocean.cheese.core

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.DefaultFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.LevelFileNameGenerator
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.hjq.toast.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import net.codeocean.cheese.backend.impl.DeviceImpl
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.core.api.Device
import net.codeocean.cheese.core.api.Path
import net.codeocean.cheese.core.service.AccessibilityService
import net.codeocean.cheese.core.service.ForegroundService


open class CoreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        globalVM = GlobalViewModel.globalVM
        globalVM.add(this@CoreApp)
        GlobalActivity.init(this)

        initLogging(PathImpl)
        DeviceIdentifier.register(this)
        Toaster.init(this)

        AccessibilityApi.init(this, AccessibilityService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            launch(Dispatchers.Main) {
                startForegroundService()
            }
        }


    }


    private fun initLogging(path: Path) {
        val filePrinter: Printer = FilePrinter.Builder(path.LOG_DIRECTORY.path)
            .fileNameGenerator(LevelFileNameGenerator())
            .backupStrategy(NeverBackupStrategy())
            .cleanStrategy(FileLastModifiedCleanStrategy(30L * 24L * 60L * 60L * 1000L))
            .flattener(DefaultFlattener())
            .build()
        val androidPrinter = AndroidPrinter(true)
        val config = LogConfiguration.Builder()
            .tag("cheese")
            .enableBorder()
            .enableStackTrace(1)
            .enableThreadInfo()
            .build()
        XLog.init(config, androidPrinter, filePrinter)
    }
    private fun startForegroundService() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, ForegroundService::class.java))
            } else {
                startService(Intent(this, ForegroundService::class.java))
            }
        }, 1000)
    }

    companion object {
        lateinit var globalVM: GlobalViewModel
    }

}