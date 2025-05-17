package net.codeocean.cheese.screen

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.InputType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.codeocean.cheese.core.runtime.debug.local.ProjectManager
import java.io.File
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import kotlinx.coroutines.delay
import net.codeocean.cheese.Keyboard1Utils
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.core.CoreEnv
import org.eclipse.tm4e.core.registry.IThemeSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// 状态持有类
data class CodeEditorState(
    val initialContent: Content = Content("// 在这里输入代码..."),
    var editor: CodeEditor? = null
) {
    var content by mutableStateOf(initialContent)
    var selection by mutableStateOf(TextRange.Zero)
}

// 状态记忆函数
@Composable
fun rememberCodeEditorState(
    initialContent: Content = Content()
): CodeEditorState = remember {
    CodeEditorState(initialContent = initialContent)
}

// 编辑器组件
@Composable
fun CodeEditor(
    modifier: Modifier = Modifier,
    state: CodeEditorState = rememberCodeEditorState(),
    onEditorClicked: () -> Unit = {}
) {
    val context = LocalContext.current


    val editor = remember {
        CodeEditor(context).apply {
            setText(state.content)
            state.editor = this
            isFocusable = true
            isFocusableInTouchMode = true
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE



            setText(state.content)
            val themeRegistry = ThemeRegistry.getInstance()
            val name = "quietlight" // 主题名称
            val themeAssetsPath = "textmate/$name.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath),
                        themeAssetsPath,
                        null
                    ),
                    name
                ).apply {
                    // 如果主题是适用于暗色模式的，请额外添加以下内容
                    isDark = true
                }
            )

            ThemeRegistry.getInstance().setTheme("cheese")
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
            colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            val languageScopeName = "source.js" // 您目标语言的作用域名称
            val language = TextMateLanguage.create(
                languageScopeName, true /* true表示启用自动补全 */
            )
            setEditorLanguage(language)


            setLineSpacing(2f, 1.1f)

            setOnClickListener {
                onEditorClicked()
            }
        }
    }

    // 内容同步
    LaunchedEffect(state.content) {
        if (editor.text != state.content) {
            editor.setText(editor.text)
        }
    }

    AndroidView(
        factory = { editor },
        modifier = modifier,
        onRelease = { it.release() }
    )
}

// ViewModel
class MainViewModel : ViewModel() {
    val editorState = mutableStateOf(
        CodeEditorState(initialContent = Content("// 初始化代码...\nfun main() {\n    println(\"Hello World!\")\n}"))
    )
    val logs = mutableStateListOf<String>()
    var searchQuery by mutableStateOf("")

    // 添加消息状态
    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message


    // 在 MainViewModel 中添加
    private val _confirmDialog = mutableStateOf<ConfirmDialogState?>(null)
    val confirmDialog: State<ConfirmDialogState?> get() = _confirmDialog

    data class ConfirmDialogState(
        val title: String,
        val message: String,
        val confirmText: String,
        val dismissText: String,
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit
    )

    // 修改为回调方式，而不是 suspend 函数
    fun showConfirmDialog(
        title: String,
        message: String,
        confirmText: String = "确认",
        dismissText: String = "取消",
        onResult: (Boolean) -> Unit
    ) {
        _confirmDialog.value = ConfirmDialogState(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            onConfirm = {
                _confirmDialog.value = null
                onResult(true)
            },
            onDismiss = {
                _confirmDialog.value = null
                onResult(false)
            }
        )
    }

    fun updateContent(newContent: String) {
        editorState.value.editor?.setText(newContent)
    }

    fun addLog(message: String) {
        logs.add(message)
    }

    fun clearLogs() {
        logs.clear()
    }

    fun getFilteredLogs(): List<String> {
        return if (searchQuery.isBlank()) {
            logs
        } else {
            logs.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    fun showMessage(msg: String?) {
        _message.value = msg
        // 3秒后自动清除消息
        viewModelScope.launch {
            delay(3000)
            _message.value = null
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

// 定义代码示例的数据结构
data class CodeExample(
    val id: Int,
    val title: String,
    val content: String,
    val path: String
)

// 定义项目数据结构
data class Project(
    val id: Int,
    val name: String,
    val packageName: String,
    val language: String,
    val ui: String,
    val path: String,
    val createTime: Long // 新增创建时间字段，使用时间戳表示
)

// 定义代码分类
data class CodeCategory(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val examples: List<CodeExample>,
    var expanded: Boolean = false
)

@Composable
fun ProjectItem(
    project: Project,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // 使用 remember(project) 而不是 remember(project.id) 确保状态绑定到项目对象
    var isLongPressed by remember(project) { mutableStateOf(false) }
    val interactionSource = remember(project) { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(project) {  // 绑定到项目对象而不是ID
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        isLongPressed = true
                        onLongClick()
                    },
                    onPress = {
                        if (tryAwaitRelease()) {
                            isLongPressed = false
                        }
                    }
                )
            }
            .indication(interactionSource, LocalIndication.current),
        colors = CardDefaults.cardColors(
            containerColor = if (isLongPressed)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isLongPressed)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (isLongPressed)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = "${project.packageName} (${project.language})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isLongPressed)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                Text(
                    text = "创建于: ${formatDate(project.createTime)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isLongPressed)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "查看项目",
                modifier = Modifier.size(20.dp),
                tint = if (isLongPressed)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

fun generateCodeCategories(): List<CodeCategory> {
    val rootDir = PathImpl.SD_DEMO_DIRECTORY
    val categories = mutableListOf<CodeCategory>()
    var idCounter = 1
    var exampleIdCounter = 100

    rootDir.listFiles()?.filter { it.isDirectory }?.forEach { categoryDir ->
        val examples = categoryDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { exampleDir ->
                val demoFile = exampleDir.listFiles()
                    ?.firstOrNull { it.extension == "txt" || it.extension == "kt" }
                val content = demoFile?.readText() ?: "// 内容为空"

                CodeExample(
                    id = exampleIdCounter++,
                    title = exampleDir.name,
                    content = content,
                    path = exampleDir.absolutePath
                )
            } ?: emptyList()

        if (examples.isNotEmpty()) {
            categories.add(
                CodeCategory(
                    id = idCounter++,
                    name = categoryDir.name,
                    icon = Icons.Default.Folder, // 或自定义图标
                    examples = examples
                )
            )
        }
    }

    return categories
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(navController: NavController) {
    val context = LocalContext.current
    // 使用 remember 保存 Dialog 的显示状态
    var showDialog by remember { mutableStateOf(false) }

    // 检查 Termux 是否安装
    val isTermuxInstalled by remember {
        derivedStateOf {
            try {
                context.packageManager.getPackageInfo("com.termux", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    // 进入界面时检查，如果未安装则弹出 Dialog
    LaunchedEffect(Unit) {
        if (!isTermuxInstalled) {
            showDialog = true
        }
    }

    // 未安装时显示 Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { }, // 点击外部或返回键关闭 Dialog
            title = { Text("需要 Termux") },
            text = { Text("此功能需要 Termux 应用支持，请先安装") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 跳转到浏览器下载 Termux
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data =
                                Uri.parse("https://pan.baidu.com/s/13lP8ThawELOd7sYPkR7CzQ?pwd=c4gi")
                        }
                        context.startActivity(intent)
                        showDialog = false // 关闭 Dialog
                    }
                ) {
                    Text("前往下载")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false } // 点击 "取消" 关闭 Dialog
                ) {
                    Text("取消")
                }
            }
        )
    }


    // 分类数据
    val categories = remember {
        mutableStateOf(
            generateCodeCategories()
        )
    }

    // 状态管理
    val lazyListState = rememberLazyListState()
    var projectsExpanded by rememberSaveable { mutableStateOf(false) }
    val projects = remember { mutableStateOf<List<Project>>(emptyList()) }
    val showNewProjectDialog = remember { mutableStateOf(false) }
    val newProjectName = remember { mutableStateOf("") }
    val newPackageName = remember { mutableStateOf("") }
    val selectedLanguage = remember { mutableStateOf("js") }
    val selectedUi = remember { mutableStateOf("vue-element-vform") }
//    val Languagetemplates = listOf("js", "ts")
    val Languagetemplates = listOf("js")
//    val UiTemplates = listOf("vue-element-vform", "xml")
    val UiTemplates = listOf("vue-element-vform")
    var showProjectMenu by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
// 新增：记录搜索前的展开状态
    var wasExpandedBeforeSearch by remember { mutableStateOf(projectsExpanded) }
    var isSearching by remember { mutableStateOf(false) }


    val (filteredProjects, filteredCategories) = remember(
        projects.value,
        categories.value,
        searchQuery
    ) {
        if (searchQuery.isBlank()) {
            if (isSearching) {
                projectsExpanded = wasExpandedBeforeSearch
                isSearching = false
            }
            Pair(projects.value, categories.value)
        } else {

            if (!isSearching) {
                wasExpandedBeforeSearch = projectsExpanded
                isSearching = true
            }

            // 1. 严格过滤项目（仅按名称）
            val filteredProjects = projects.value.filter { project ->
                project.name.contains(searchQuery, ignoreCase = true)
            }

            // 2. 严格过滤分类和示例
            val filteredCategories = categories.value.mapNotNull { category ->
                // 2.1 仅检查分类名称和示例标题（不检查示例内容）
                val hasMatchingExample = category.examples.any { example ->
                    example.title.contains(searchQuery, ignoreCase = true)
                }

                val categoryMatches = category.name.contains(searchQuery, ignoreCase = true)

                // 2.2 只有当分类名称或示例标题匹配时才保留
                if (categoryMatches || hasMatchingExample) {
                    // 只显示匹配的示例（如果分类名称不匹配）
                    val examplesToShow = if (categoryMatches) {
                        category.examples
                    } else {
                        category.examples.filter { example ->
                            example.title.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    category.copy(
                        expanded = true,
                        examples = examplesToShow
                    )
                } else {
                    null
                }
            }
            projectsExpanded = filteredProjects.isNotEmpty()

            Pair(filteredProjects, filteredCategories)
        }
    }
    LaunchedEffect(showNewProjectDialog.value, showProjectMenu, showRenameDialog) {
        if (!showNewProjectDialog.value) {
            projects.value = ProjectManager.getProjectList().mapIndexed { index, (name, path) ->
                // 获取文件夹创建时间
                val createTime = try {
                    File(path).lastModified() // 使用最后修改时间作为创建时间
                } catch (e: Exception) {
                    System.currentTimeMillis() // 如果获取失败，使用当前时间
                }

                Project(
                    id = index + 1,
                    name = name,
                    packageName = "com.cc",
                    language = "js",
                    ui = "vue",
                    path = path,
                    createTime = createTime // 添加创建时间
                )
            }
        }
    }
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val  elevation: Dp = 12.dp
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(

                        text = "本地项目",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = shadowColor,
                                blurRadius = elevation.value,
                                offset = Offset(0f, 2f)
                            )
                        )
                    )

                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .shadow(elevation)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog.value = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建项目")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {
                // 项目列表
                if (filteredProjects.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .animateContentSize()
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { projectsExpanded = !projectsExpanded }
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "我的项目",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${filteredProjects.size}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (projectsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            AnimatedVisibility(
                                visible = projectsExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        end = 12.dp,
                                        bottom = 12.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    filteredProjects.forEach { project ->
                                        ProjectItem(
                                            project = project,
                                            onClick = { navController.navigate("codeDetail/${project.id}/${project.name}/") },
                                            onLongClick = {
                                                selectedProject = project
                                                renameText = project.name
                                                showProjectMenu = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 如果没有搜索结果，显示提示
                if (searchQuery.isNotBlank() && filteredProjects.isEmpty() && filteredCategories.isEmpty()) {
                    item(key = "no_results") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "没有找到匹配的项目或代码示例",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }

                // 分类标题 - 只在有分类数据且搜索为空或有匹配的分类时显示
                if (filteredCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "代码示例分类",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // 分类列表
                    items(
                        items = filteredCategories,
                        key = { it.id }
                    ) { category ->
                        CodeCategoryItem(
                            category = category,
                            onCategoryClick = {
                                categories.value = categories.value.map {
                                    if (it.id == category.id) it.copy(expanded = !it.expanded) else it
                                }
                            },
                            onExampleClick = { example ->
                                navController.navigate(
                                    "codeDetail/${0}/${example.title}/${
                                        Uri.encode(
                                            example.path
                                        )
                                    }"
                                )
//                                navController.navigate("codeDetail/${example.id}/")
                            }
                        )
                    }
                }

                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // 项目操作菜单
    if (showProjectMenu && selectedProject != null) {
        Dialog(
            onDismissRequest = { showProjectMenu = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "项目管理",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(
                            onClick = { showProjectMenu = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        ),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = selectedProject?.name ?: "",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            var isExpanded by remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { isExpanded = !isExpanded }
                            ) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(20.dp))
                                Text(
                                    text = selectedProject?.path ?: "",
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "折叠" else "展开",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedProject?.let { project ->
                                    projects.value = projects.value.filter { it.id != project.id }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        ProjectManager.deleteProject(project.path)
                                    }
                                }
                                showProjectMenu = false
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("删除项目")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                showProjectMenu = false
                                showRenameDialog = true
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "重命名",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重命名")
                        }
                    }
                }
            }
        }
    }

    // 重命名对话框
    if (showRenameDialog && selectedProject != null) {
        Dialog(
            onDismissRequest = { showRenameDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "重命名项目",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("新项目名称") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (renameText.isNotBlank() && renameText != selectedProject?.name) {
                                    selectedProject?.let { project ->
                                        val updatedProject = project.copy(name = renameText)
                                        projects.value = projects.value.map {
                                            if (it.id == project.id) updatedProject else it
                                        }
                                        CoroutineScope(Dispatchers.IO).launch {
                                            ProjectManager.renameProject(project.path, renameText)
                                        }
                                    }
                                    showRenameDialog = false
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showRenameDialog = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedProject?.let { project ->
                                    val updatedProject = project.copy(name = renameText)
                                    projects.value = projects.value.map {
                                        if (it.id == project.id) updatedProject else it
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        ProjectManager.renameProject(project.path, renameText)
                                    }
                                }
                                showRenameDialog = false
                            },
                            enabled = renameText.isNotBlank() && renameText != selectedProject?.name,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("确认")
                        }
                    }
                }
            }
        }
    }

    // 新建项目对话框
    if (showNewProjectDialog.value) {
        Dialog(
            onDismissRequest = { showNewProjectDialog.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "新建项目",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = newProjectName.value,
                            onValueChange = { newProjectName.value = it },
                            label = { Text("项目名称") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = null
                                )
                            }
                        )

                        OutlinedTextField(
                            value = newPackageName.value,
                            onValueChange = { newPackageName.value = it },
                            label = { Text("包名 (如: com.example)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null
                                )
                            }
                        )

                        var expanded1 by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded1,
                            onExpandedChange = { expanded1 = !expanded1 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedUi.value,
                                onValueChange = {},
                                label = { Text("Ui模板") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = MaterialTheme.shapes.small,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1)
                                },
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DesignServices,
                                        contentDescription = null
                                    )
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = expanded1,
                                onDismissRequest = { expanded1 = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                UiTemplates.forEach { template ->
                                    DropdownMenuItem(
                                        text = { Text(template) },
                                        onClick = {
                                            selectedUi.value = template
                                            expanded1 = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (template) {
                                                    "vue-element-vform" -> Icons.Default.Code
                                                    "xml" -> Icons.Default.DataArray
                                                    else -> Icons.Default.DeveloperMode
                                                },
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }


                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedLanguage.value,
                                onValueChange = {},
                                label = { Text("语言") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = MaterialTheme.shapes.small,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DesignServices,
                                        contentDescription = null
                                    )
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                Languagetemplates.forEach { template ->
                                    DropdownMenuItem(
                                        text = { Text(template) },
                                        onClick = {
                                            selectedLanguage.value = template
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (template) {
                                                    "js" -> Icons.Default.Code
                                                    "ts" -> Icons.Default.DataArray
                                                    else -> Icons.Default.DeveloperMode
                                                },
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showNewProjectDialog.value = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newProjectName.value.isNotBlank() && newPackageName.value.isNotBlank()) {
                                    val currentTime = System.currentTimeMillis()
                                    val newProject = Project(
                                        id = projects.value.size + 1,
                                        name = newProjectName.value,
                                        packageName = newPackageName.value,
                                        language = selectedLanguage.value,
                                        ui = selectedUi.value,
                                        createTime = currentTime,
                                        path = ""
                                    )

                                    CoroutineScope(Dispatchers.Main).launch {
                                        withContext(Dispatchers.IO) {
                                            ProjectManager.createProject(
                                                ProjectManager.Project(
                                                    id = newProject.id,
                                                    name = newProject.name,
                                                    packageName = newProject.packageName,
                                                    language = newProject.language,
                                                    ui = newProject.ui
                                                )
                                            ) {
                                                projects.value += newProject
                                                newProjectName.value = ""
                                                newPackageName.value = ""
                                                selectedLanguage.value = "js"
                                                selectedUi.value = "vue-element-vform"
                                                showNewProjectDialog.value = false
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = newProjectName.value.isNotBlank() && newPackageName.value.isNotBlank(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("创建")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        placeholder = {
            Text(
                "搜索项目名称",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

sealed class KeyboardTriggerSource {
    object EDITOR : KeyboardTriggerSource()
    object LOG_SEARCH : KeyboardTriggerSource()
}

@Stable
class KeyboardHeightState {
    var rawHeightPx by mutableStateOf(0)
    var isKeyboardVisible by mutableStateOf(false)
}

@Composable
fun rememberKeyboardHeightState(): KeyboardHeightState {
    val state = remember { KeyboardHeightState() }

    Keyboard1Utils.registerKeyboardHeightListener(CoreEnv.envContext.activity) {
        state.rawHeightPx = it
        state.isKeyboardVisible = it > 0
        XLog.w("当前的软键盘高度：$it")

    }

    return state
}

@Composable
fun Modifier.conditionalKeyboardPadding(
    state: KeyboardHeightState,
    minVisibleHeight: Dp = 100.dp
): Modifier {
    val minVisiblePx = with(LocalDensity.current) { minVisibleHeight.roundToPx() }
    val shouldApplyPadding = state.isKeyboardVisible && state.rawHeightPx >= minVisiblePx

    return if (shouldApplyPadding) {
        val bottomPadding = with(LocalDensity.current) {
            state.rawHeightPx.toDp().coerceAtLeast(minVisibleHeight)
        }
        this.padding(bottom = bottomPadding)
    } else {
        this
    }
}

@Composable
fun calculatePanelHeight(
    maxHeight: Dp,
    panelHeight: Dp,
    state: KeyboardHeightState,
    minVisibleHeight: Dp = 100.dp,
): Dp {
    val minVisiblePx = with(LocalDensity.current) { minVisibleHeight.roundToPx() }
    val shouldApplyPadding = state.isKeyboardVisible && state.rawHeightPx >= minVisiblePx

    return if (shouldApplyPadding) {
        maxHeight
    } else {
        panelHeight
    }

}

@Composable
fun rememberIsKeyboardVisible(): State<Boolean> {
    val keyboardVisible = remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

//    SideEffect {
//        if (keyboardVisible.value != isImeVisible) {
//            keyboardVisible.value = isImeVisible
//        }
//    }
    return keyboardVisible
}


@Composable
fun LogList(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var lastLogSize by remember { mutableIntStateOf(0) }

    // 自动滚动逻辑优化 - 修复点1：添加空列表检查
    LaunchedEffect(logs.size) {
        if (logs.isEmpty()) {
            lastLogSize = 0
            return@LaunchedEffect
        }

        if (logs.size != lastLogSize) {
            val newItemCount = logs.size - lastLogSize
            lastLogSize = logs.size

            // 修复点2：确保目标索引有效
            val targetIndex = logs.size - 1
            if (targetIndex >= 0) {
                if (newItemCount > 10) {
                    scrollState.scrollToItem(targetIndex)
                } else {
                    coroutineScope.launch {
                        try {
                            scrollState.animateScrollToItem(targetIndex)
                        } catch (e: IllegalArgumentException) {
                            // 修复点3：捕获异常并回退到安全滚动
                            if (logs.isNotEmpty()) {
                                scrollState.scrollToItem(0)
                            }
                        }
                    }
                }
            }
        }
    }

    // 预定义颜色方案
    val colorScheme = mapOf(
        "I" to Color(0xFF4CAF50),
        "D" to Color(0xFF2196F3),
        "E" to Color(0xFFF44336),
        "W" to Color(0xFFFF9800)
    )

    LazyColumn(
        state = scrollState,
        modifier = modifier,
    ) {
        items(
            count = logs.size,
            key = { index -> index }
        ) { index ->
            val log = logs[index]

            // 颜色计算
            val levelColor = when {
                " E " in log -> colorScheme["E"]!!
                " W " in log -> colorScheme["W"]!!
                " I " in log -> colorScheme["I"]!!
                " D " in log -> colorScheme["D"]!!
                else -> MaterialTheme.colorScheme.onSurface
            }

            val backgroundColor = when {
                " E " in log -> colorScheme["E"]!!.copy(alpha = 0.1f)
                " W " in log -> colorScheme["W"]!!.copy(alpha = 0.1f)
                else -> Color.Transparent
            }

            Text(
                text = log,
                color = levelColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}


enum class CreateType {
    FOLDER, FILE
}

enum class Operation {
    COPY, CUT, IMPORT
}

// 文件数据类
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileItem> = emptyList(),
    val extension: String = if (!isDirectory) name.substringAfterLast('.', "").lowercase() else ""
)



@Composable
fun CodeCategoryItem(
    category: CodeCategory,
    onCategoryClick: () -> Unit,
    onExampleClick: (CodeExample) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(200))
    ) {
        Card(
            onClick = onCategoryClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (category.expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (category.expanded) "收起" else "展开",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (category.expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                category.examples.forEach { example ->
                    CodeExampleItem(
                        example = example,
                        onClick = { onExampleClick(example) }
                    )
                }
            }
        }
    }
}

@Composable
fun CodeExampleItem(
    example: CodeExample,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = example.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "查看代码",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}