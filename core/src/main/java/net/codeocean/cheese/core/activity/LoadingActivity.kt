package net.codeocean.cheese.core.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import net.codeocean.cheese.core.CoreEnv.performFirstTimeSetup
import net.codeocean.cheese.core.Misc.showTermsAndConditionsDialog
import net.codeocean.cheese.core.utils.PermissionsUtils.initPermissions
import kotlin.concurrent.thread

class LoadingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }


        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }


        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )

        rootLayout.addView(progressBar, layoutParams)
        setContentView(rootLayout)

        initPermissions {
            thread {
                performFirstTimeSetup()
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, Class.forName("net.codeocean.cheese.MainActivity")))
                    finish()
                }, 500)
            }
        }

    }
}
