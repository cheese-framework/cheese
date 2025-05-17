package net.codeocean.cheese

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.runtime.ScriptExecutionController
import net.codeocean.cheese.core.utils.PermissionsUtils.initPermissions
import net.codeocean.cheese.frontend.javascript.JavaScript

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        CoreEnv.runTime.isDebugMode = false
        ScriptExecutionController.runRelease(JavaScript(this))
    }
}