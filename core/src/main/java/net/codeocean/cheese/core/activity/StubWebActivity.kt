package net.codeocean.cheese.core.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.webkit.WebViewAssetLoader
import com.google.android.material.switchmaterial.SwitchMaterial
import net.codeocean.cheese.backend.impl.BaseImpl.act
import net.codeocean.cheese.backend.impl.BaseImpl.cx
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.backend.impl.WebViewImpl
import net.codeocean.cheese.core.CoreApp.Companion.globalVM
import net.codeocean.cheese.core.CoreEnv
import java.io.File


class BitmapDisplayActivity : Activity() {

    companion object {
        var bitmap: (() -> Bitmap)? = null
        fun launch(context: Context, bit: Bitmap) {
            bitmap = { bit }
            val intent = Intent(context, BitmapDisplayActivity::class.java)
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bitmap = bitmap?.invoke()
        if (bitmap != null) {
            setContentView(createBitmapView(bitmap))
        } else {
            // 回退逻辑，防止空视图
            finish()
        }
    }

     fun createBitmapView(bitmap: Bitmap): View {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#6200EE"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(56)
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        // 使用系统返回图标（兼容方案）
        val backButton = AppCompatImageView(this).apply {
            // 使用系统导航图标（兼容旧版本）
            setImageResource(android.R.drawable.ic_menu_revert)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "返回" // 无障碍支持

            // 完美尺寸的点击区域
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(48), // 标准图标按钮尺寸
                dpToPx(48)
            ).apply {
                marginEnd = dpToPx(8) // 与标题的间距
            }

            // 优雅的波纹效果
            // 替换foreground设置
            foreground = RippleDrawable(
                ColorStateList.valueOf(Color.parseColor("#80FFFFFF")),
                null,
                null
            ).apply {
                radius = dpToPx(24) // 限制波纹范围为圆形
            }
            setOnClickListener { finish() }
        }

        val titleText = TextView(this).apply {
            text = "图片展示"
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        titleBar.addView(backButton)
        titleBar.addView(titleText)

        // 图片展示部分保持不变...
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val imageView = ImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        scrollView.addView(imageView)
        rootLayout.addView(titleBar)
        rootLayout.addView(scrollView)

        return rootLayout
    }

    // 无需额外方法，保持简洁
    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 避免内存泄漏
        bitmap?.invoke()?.recycle()
        bitmap = null
    }
}


@SuppressLint("SetJavaScriptEnabled")
fun configureWebView(webView: WebView) {
    val webSettings: WebSettings = webView.settings
    webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    with(webSettings) {
        javaScriptEnabled = true
        setSupportZoom(true)
        allowFileAccess = true
        allowContentAccess = true
        domStorageEnabled = true
        // 跨域问题设置（仅在需要时启用）
        allowFileAccess = true
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true
        val defaultUserAgent = WebSettings.getDefaultUserAgent(CoreEnv.envContext.context)
        webSettings.userAgentString = defaultUserAgent
    }
    val sourceDir = File(PathImpl.UI_DIRECTORY.absolutePath)
    val destinationDir = File(CoreEnv.envContext.context.filesDir, "app_webview")

    try {
        sourceDir.copyRecursively(destinationDir, overwrite = true)
        println("Files copied successfully!")
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error copying files: ${e.message}")
    }
    val assetLoader = WebViewAssetLoader.Builder()
        .setDomain("cheese") // 设置域名，通常是localhost或者自定义的域名
        .addPathHandler("/", WebViewAssetLoader.InternalStoragePathHandler(CoreEnv.envContext.context,destinationDir)) // 加载应用内部存储资源
        .build()

    webView.webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d("TAG", "Loading page: $url")
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            Log.e("TAG", "Error loading page: ${error.description}")
        }
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            return assetLoader.shouldInterceptRequest(request.url)
        }
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            request.grant(request.resources)
        }
    }

    webView.clearCache(true)
    webView.clearHistory()

    // 添加 JavaScript 接口
    webView.addJavascriptInterface(WebAppInterface(), "Android")


    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null) // 切换到软件加速


}

class WebAppInterface() {
    @JavascriptInterface
    fun number(arg1: String): Int {
        val args =  parseArgs(arg1)
        val result = WebViewImpl.iWebView!!.invoke(object {
            val  id = args["id"] as String
            val  params = args["params"]
        })
        return when (result) {
            is Double -> result.toInt()
            is Int -> result
            else -> throw IllegalStateException("Unexpected return type: ${result?.javaClass}")
        }
    }
    @JavascriptInterface
    fun string(arg1: String): String {
        val args =  parseArgs(arg1)
        return WebViewImpl.iWebView!!.invoke(object {
            val  id = args["id"] as String
            val  params = args["params"]
        }) as String
    }

    fun parseArgs(arg1: String): Map<String, Any> {
        // 解析 id 和 params 部分
        return arg1.split(", params=").let { parts ->
            // 处理 id 部分
            val id = parts[0].substringAfter("id=").trim().toIntOrNull() ?: parts[0].substringAfter("id=").trim()

            // 处理 params 部分
            val params = parts.getOrNull(1)
                ?.removeSurrounding("{", "}")
                ?.split(", ")
                ?.map { param ->
                    when {
                        param.toIntOrNull() != null -> param.toInt()
                        param.toDoubleOrNull() != null -> param.toDouble() // 处理 Double 类型
                        param.toBooleanStrictOrNull() != null -> param.toBoolean()
                        else -> param
                    }
                } ?: emptyList()

            // 返回结果的 Map
            mapOf("id" to id, "params" to params)
        }
    }

}

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "AppSettingsPrefs"
        private const val LOG_CHECKBOX_KEY = "logCheckbox"
        private const val CATCH_EXCEPTIONS_CHECKBOX_KEY = "catchExceptionsCheckbox"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取 SharedPreferences
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // 创建 ScrollView 包裹整个布局
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 创建父布局
        val parentLayout = createParentLayout()

        // 创建并添加标题文本
        val titleText = createTitleText()
        parentLayout.addView(titleText)

        // 读取 SharedPreferences 的状态
        val isLogEnabled = preferences.getBoolean(LOG_CHECKBOX_KEY, false)
        val isCatchExceptionsEnabled = preferences.getBoolean(CATCH_EXCEPTIONS_CHECKBOX_KEY, false)

        // 创建并添加 "是否打印日志" Switch
        val logSwitch = createSwitch("打印日志", isLogEnabled)
        parentLayout.addView(logSwitch)

        // 创建并添加 "是否开启异常捕获" Switch
        val catchExceptionsSwitch = createSwitch("异常捕获", isCatchExceptionsEnabled)
        parentLayout.addView(catchExceptionsSwitch)

        // 创建并添加保存按钮
        val saveButton = createSaveButton(preferences, logSwitch, catchExceptionsSwitch)
        parentLayout.addView(saveButton)

        // 使用 ScrollView 作为根布局
        scrollView.addView(parentLayout)

        // 设置内容视图
        setContentView(scrollView)
    }

    // 创建父布局
    private fun createParentLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
    }

    // 创建标题文本
    private fun createTitleText(): TextView {
        return TextView(this).apply {
            text = "设置" // 使用字符串资源
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
        }
    }

    // 创建通用 Switch
    private fun createSwitch(text: String, isChecked: Boolean): SwitchMaterial {
        return SwitchMaterial(this).apply {
            this.text = text
            this.isChecked = isChecked
            textSize = 18f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 20)
        }
    }

    // 创建保存按钮
    private fun createSaveButton(
        preferences: SharedPreferences,
        logSwitch: SwitchMaterial,
        catchExceptionsSwitch: SwitchMaterial
    ): Button {
        return Button(this).apply {
            text = "保存"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#3F51B5")) // Material 颜色
            setPadding(0, 20, 0, 20)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 40
            }

            setOnClickListener {
                // 保存状态到 SharedPreferences
                preferences.edit().apply {
                    putBoolean(LOG_CHECKBOX_KEY, logSwitch.isChecked)
                    putBoolean(CATCH_EXCEPTIONS_CHECKBOX_KEY, catchExceptionsSwitch.isChecked)
                    apply()
                }

                // 返回上一个 Activity
                finish()
            }
        }
    }
}

class StubWebActivity {
    class Keep : Activity() {
        @SuppressLint("SuspiciousIndentation")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Create the parent layout
            val parentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Create the toolbar
            val toolbar = Toolbar(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                title = ""
                elevation = 4f
                setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))  // Set toolbar background color
            }
            setActionBar(toolbar)
            val webViewToolbar = false
            if (webViewToolbar) {
                parentLayout.addView(toolbar)
            }
            val webView = WebView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            parentLayout.addView(webView)
            setContentView(parentLayout)
            webView.clearCache(true);
            webView.clearHistory();
            configureWebView(webView)
            globalVM.add<android.webkit.WebView>(webView)

            webView.loadUrl("https://cheese/index.html")

            val intent = Intent("webview.loaded")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        }

        // Dynamically create menu with the settings icon
        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            val settingsItem = menu?.add(0, 0, 0, "Settings")
            settingsItem?.setIcon(android.R.drawable.ic_menu_manage)
            settingsItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            return true
        }

        // Handle the settings button click
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.title) {
                "Settings" -> {
                    // Print "你好" when the settings button is clicked
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }

        private var backPressedOnce = false

        @Deprecated("Deprecated in Java")
        override fun onBackPressed() {

            if (!CoreEnv.runTime.isDebugMode) {
                if (backPressedOnce) {
                    // 启动主页的 Activity
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    backPressedOnce = true
                    Toast.makeText(this, "再按一次返回主页", Toast.LENGTH_SHORT).show()
                    // 重置标志变量，在2秒后重置
                    Handler(Looper.getMainLooper()).postDelayed({
                        backPressedOnce = false
                    }, 2000)
                }
            } else {
                super.onBackPressed()
            }


        }

    }

}