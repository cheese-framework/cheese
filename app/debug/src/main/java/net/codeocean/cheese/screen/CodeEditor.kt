package net.codeocean.cheese.screen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elvishew.xlog.XLog
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.codeocean.cheese.ViewModelLogger
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.CoreFactory
import net.codeocean.cheese.core.runtime.debug.local.ProjectManager
import net.codeocean.cheese.core.runtime.ScriptExecutionController.cancelAndClean
import net.codeocean.cheese.core.runtime.debug.remote.DebugController
import net.codeocean.cheese.core.runtime.debug.local.LocalDebugController
import net.codeocean.cheese.core.utils.ViewLogger
import net.codeocean.cheese.frontend.javascript.JavaScript
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.roundToInt

@Composable
fun FileTreeView(
    name: String,
    files: List<FileItem>,
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    onFileClick: (FileItem) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onImport: (String, String) -> Unit = { _, _ -> },
    onRename: (String, String) -> Unit = { _, _ -> },
    onCopy: (String, String) -> Unit = { _, _ -> },
    onCut: (String, String) -> Unit = { _, _ -> },
    onCreateFolder: (String, String) -> Unit = { _, _ -> },
    onCreateFile: (String, String) -> Unit = { _, _ -> }
) {
    // 状态管理
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }
    var contextMenuTarget by remember { mutableStateOf<FileItem?>(null) }
    var showPathSelector by remember { mutableStateOf(false) }
    var currentOperation by remember { mutableStateOf<Operation?>(null) }
    var newName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var createType by remember { mutableStateOf<CreateType?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // 冲突处理状态
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictSourcePath by remember { mutableStateOf("") }
    var conflictTargetPath by remember { mutableStateOf("") }

    // 样式常量
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    // 默认路径
    val defaultPath = "/storage/emulated/0/"
    var selectedPath by remember { mutableStateOf(defaultPath) }

    // 文件图标映射
    val fileIcons = mapOf(
        "kt" to Icons.Default.Code,
        "java" to Icons.Default.Code,
        "xml" to Icons.Default.Settings,
        "gradle" to Icons.Default.Build,
        "md" to Icons.Default.Description
    )

    // 执行实际操作
    fun executeOperation(operation: Operation, sourcePath: String, targetPath: String) {
        when (operation) {
            Operation.COPY -> onCopy(sourcePath, targetPath)
            Operation.CUT -> onCut(sourcePath, targetPath)
            Operation.IMPORT -> onImport(sourcePath, targetPath)
        }
        showPathSelector = false
        contextMenuTarget = null
    }

    // 处理文件操作
    fun handleFileOperation(operation: Operation, sourcePath: String, targetPath: String) {
        if (operation.name == "IMPORT") {
            val targetFile =
                File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$sourcePath/${File(targetPath).name}")
            if (targetFile.exists()) {
                conflictSourcePath = sourcePath
                conflictTargetPath = targetPath
                currentOperation = operation
                showConflictDialog = true
            } else {
                executeOperation(operation, sourcePath, targetPath)
            }
        } else {
            val targetFile = File(targetPath, sourcePath)
            XLog.e(targetFile)
            if (targetFile.exists()) {
                conflictSourcePath = sourcePath
                conflictTargetPath = targetPath
                currentOperation = operation
                showConflictDialog = true
            } else {
                executeOperation(operation, sourcePath, targetPath)
            }
        }

    }

    Box(modifier = modifier.fillMaxSize()) {
        // 主文件树视图
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shapes.small)
                .background(colors.surface)
        ) {
            files.forEach { file ->
                val isExpanded = expandedItems[file.path] ?: false
                val isSelected = contextMenuTarget?.path == file.path

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) colors.primaryContainer.copy(alpha = 0.2f)
                            else Color.Transparent,
                            shape = shapes.small
                        )
                        .pointerInput(file.path) {
                            detectTapGestures(
                                onLongPress = {
                                    scope.launch {
                                        contextMenuTarget = file
                                        newName = ""
                                    }
                                },
                                onTap = {
                                    if (file.isDirectory) {
                                        expandedItems[file.path] =
                                            !(expandedItems[file.path] ?: false)
                                    } else {
                                        onFileClick(file)
                                    }
                                }
                            )
                        }
                        .padding(
                            start = (16 * indentLevel).dp,
                            top = 8.dp,
                            bottom = 8.dp,
                            end = 8.dp
                        )
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (file.isDirectory) {
                            if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder
                        } else {
                            fileIcons[file.extension.lowercase()] ?: Icons.Default.InsertDriveFile
                        },
                        contentDescription = if (file.isDirectory) "文件夹" else "文件",
                        modifier = Modifier.size(24.dp),
                        tint = if (file.isDirectory) colors.primary else colors.onSurface
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = file.name,
                        style = typography.bodyLarge,
                        color = if (file.isDirectory) colors.primary else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (file.isDirectory) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess
                            else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "收起" else "展开",
                            modifier = Modifier.size(20.dp),
                            tint = colors.primary.copy(alpha = 0.6f)
                        )
                    }
                }

                if (file.isDirectory && isExpanded) {
                    FileTreeView(
                        name,
                        files = file.children,
                        indentLevel = indentLevel + 1,
                        modifier = Modifier.fillMaxWidth(),
                        onFileClick = onFileClick,
                        onDelete = onDelete,
                        onImport = onImport,
                        onRename = onRename,
                        onCopy = onCopy,
                        onCut = onCut,
                        onCreateFolder = onCreateFolder,
                        onCreateFile = onCreateFile
                    )
                }
            }
        }

        // 上下文菜单对话框
        if (contextMenuTarget != null && !showPathSelector && !showCreateDialog) {
            val target = contextMenuTarget!!
            Dialog(onDismissRequest = { contextMenuTarget = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 480.dp),
                    shape = shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 顶部信息区域
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (target.isDirectory)
                                                colors.primaryContainer
                                            else colors.secondaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (target.isDirectory)
                                            Icons.Default.Folder
                                        else Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = if (target.isDirectory)
                                            colors.primary
                                        else colors.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = target.name,
                                    style = typography.titleMedium,
                                    color = colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (target.isDirectory) "文件夹操作" else "文件操作",
                                    style = typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        // 操作按钮区域
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 分组1：创建操作（仅对文件夹）
                            if (target.isDirectory) {
                                Text(
                                    text = "新建",
                                    style = typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            createType = CreateType.FOLDER
                                            showCreateDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.tertiaryContainer,
                                            contentColor = colors.onTertiaryContainer
                                        ),
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreateNewFolder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("文件夹", style = typography.labelMedium)
                                    }

                                    Button(
                                        onClick = {
                                            createType = CreateType.FILE
                                            showCreateDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.tertiaryContainer,
                                            contentColor = colors.onTertiaryContainer
                                        ),
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NoteAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("文件", style = typography.labelMedium)
                                    }
                                }
                            }

                            // 分组2：重命名
                            Text(
                                text = "重命名",
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    label = { Text("新名称") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(TextFieldDefaults.MinHeight),
                                    singleLine = true,
                                    textStyle = typography.bodyMedium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.outline,
                                        focusedLabelColor = colors.primary,
                                        unfocusedLabelColor = colors.onSurfaceVariant,
                                        cursorColor = colors.primary,
                                        focusedTextColor = colors.onSurface,
                                        unfocusedTextColor = colors.onSurface,
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (newName.isNotBlank() && newName != target.name) {
                                            onRename(target.path, newName)
                                            contextMenuTarget = null
                                        }
                                    },
                                    modifier = Modifier
                                        .height(TextFieldDefaults.MinHeight),
                                    enabled = newName.isNotBlank() && newName != target.name,
                                    shape = MaterialTheme.shapes.medium, // 设置为 Material 主题的中等圆角
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ) //
                                ) {
                                    Text("确定", style = typography.labelLarge)
                                }
                            }

                            // 分组3：文件操作
                            Text(
                                text = "操作",
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            currentOperation = Operation.COPY
                                            showPathSelector = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("复制", style = typography.labelMedium)
                                    }

                                    Button(
                                        onClick = {
                                            currentOperation = Operation.CUT
                                            showPathSelector = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("剪切", style = typography.labelMedium)
                                    }
                                }

                                if (target.isDirectory) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                currentOperation = Operation.IMPORT
                                                showPathSelector = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("导入", style = typography.labelMedium)
                                        }

                                        Button(
                                            onClick = {
                                                ProjectManager.openTermux(name, target.path)
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("终端", style = typography.labelMedium)
                                        }
                                    }
                                }
                            }

                            // 分组4：删除
                            Text(
                                text = "危险操作",
                                style = typography.labelSmall,
                                color = colors.error,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )

                            Button(
                                onClick = {
                                    onDelete(target.path)
                                    contextMenuTarget = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.errorContainer,
                                    contentColor = colors.onErrorContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("删除", style = typography.labelMedium)
                            }

                            // 取消按钮
                            OutlinedButton(
                                onClick = { contextMenuTarget = null },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("取消", style = typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // 创建文件/文件夹对话框
        if (showCreateDialog && contextMenuTarget != null && createType != null) {
            val target = contextMenuTarget!!
            val typeName = if (createType == CreateType.FOLDER) "文件夹" else "文件"

            AlertDialog(
                onDismissRequest = {
                    showCreateDialog = false
                    createType = null
                },
                title = { Text("新建$typeName", style = typography.titleMedium) },
                text = {
                    Column {
                        Text("在 ${target.name} 中创建新$typeName", style = typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("${typeName}名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when (createType) {
                                CreateType.FOLDER -> onCreateFolder(target.path, newName)
                                CreateType.FILE -> onCreateFile(target.path, newName)
                                else -> {}
                            }
                            showCreateDialog = false
                            createType = null
                            contextMenuTarget = null
                        },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("创建", style = typography.labelLarge)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showCreateDialog = false
                            createType = null
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("取消", style = typography.labelLarge)
                    }
                },
                shape = shapes.extraLarge
            )
        }

        // 路径选择器对话框
        if (showPathSelector && contextMenuTarget != null) {
            val target = contextMenuTarget!!
            Dialog(onDismissRequest = { showPathSelector = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = when (currentOperation) {
                                Operation.COPY -> "选择复制目标路径"
                                Operation.CUT -> "选择剪切目标路径"
                                Operation.IMPORT -> "选择要导入的文件/文件夹"
                                else -> "选择路径"
                            },
                            style = typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = selectedPath,
                            onValueChange = { selectedPath = it },
                            label = { Text("路径") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            singleLine = true,
                            textStyle = typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showPathSelector = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("取消", style = typography.labelLarge)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    currentOperation?.let { operation ->
                                        handleFileOperation(operation, target.path, selectedPath)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = selectedPath.isNotBlank()
                            ) {
                                Text("确认", style = typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // 冲突确认对话框
        if (showConflictDialog && currentOperation != null) {
            AlertDialog(
                onDismissRequest = { showConflictDialog = false },
                title = { Text("文件/文件夹，已存在", style = typography.titleMedium) },
                text = {
                    Column {
                        Text(
                            "目标路径已存在同名文件/文件夹，是否覆盖？",
                            style = typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            "路径: $conflictTargetPath",
//                            style = typography.bodySmall,
//                            color = colors.onSurfaceVariant
//                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentOperation?.let { operation ->
                                executeOperation(operation, conflictSourcePath, conflictTargetPath)
                            }
                            showConflictDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.errorContainer,
                            contentColor = colors.onErrorContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("覆盖", style = typography.labelLarge)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConflictDialog = false },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("取消", style = typography.labelLarge)
                    }
                },
                shape = shapes.extraLarge
            )
        }
    }
}

val runEngine = LocalDebugController(JavaScript(CoreEnv.envContext.context))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeDetailScreen(
    exampleId: Int,
    name: String,
    path: String,
    navController: NavController
) {
    // 状态管理部分 ======================================================
    val viewModel: MainViewModel = viewModel()
    val isRunning = remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val showLogPanel = remember { mutableStateOf(false) }
    val panelHeight = remember { mutableStateOf(200.dp) }
    val density = LocalDensity.current
    val containerHeight = remember { mutableStateOf(0.dp) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = rememberIsKeyboardVisible()
    val keyboardState = rememberKeyboardHeightState()
    val keyboardTriggerSource = remember { mutableStateOf<KeyboardTriggerSource?>(null) }

    // 特殊字符列表（用于底部快捷输入栏）
    val specialChars = listOf(
        "<", ">", "(", ")", "{", "}", "[", "]",
        "&", "|", "!", "=", "+", "-", "*", "/",
        "\"", "'", ":", ";", ",", ".", "\\", "`"
    )


    // 文件相关状态 =====================================================
    val openFiles = remember { mutableStateListOf<String>() } // 存储完整路径
    val selectedFile = remember { mutableStateOf("") } // 存储完整路径
    val filePathMap = remember { mutableStateMapOf<String, String>() } // 完整路径到实际路径的映射
    val selectedFilePath = remember { mutableStateOf("") }

    // 文件状态数据类（包含内容、选择范围、撤销/恢复栈）
    data class FileState(
        val content: String,
        val selection: TextRange = TextRange.Zero,
        val undoStack: List<Pair<String, TextRange>> = emptyList(),
        val redoStack: List<Pair<String, TextRange>> = emptyList(),
        val originalContent: String = content // 记录原始内容用于比较是否修改
    ) {
        // 检查文件是否被修改过
        fun isModified(): Boolean = content != originalContent

        // 添加撤销记录
        fun pushUndo(content: String, selection: TextRange): FileState {
            val newUndoStack = (undoStack.takeLast(49) + (content to selection))
            return copy(undoStack = newUndoStack, redoStack = emptyList())
        }

        // 执行撤销操作
        fun undo(): Pair<FileState, Pair<String, TextRange>?> {
            return if (undoStack.isNotEmpty()) {
                val (prevContent, prevSelection) = undoStack.last()
                val newState = copy(
                    content = prevContent,
                    selection = prevSelection,
                    undoStack = undoStack.dropLast(1),
                    redoStack = redoStack + (content to selection)
                )
                Pair(newState, prevContent to prevSelection)
            } else {
                Pair(this, null)
            }
        }

        // 执行恢复操作
        fun redo(): Pair<FileState, Pair<String, TextRange>?> {
            return if (redoStack.isNotEmpty()) {
                val (nextContent, nextSelection) = redoStack.last()
                val newState = copy(
                    content = nextContent,
                    selection = nextSelection,
                    undoStack = undoStack + (content to selection),
                    redoStack = redoStack.dropLast(1)
                )
                Pair(newState, nextContent to nextSelection)
            } else {
                Pair(this, null)
            }
        }
    }

    // 文件状态存储（包含内容和修改状态）
    val fileStates = remember {
        mutableStateMapOf(
            "main.js" to FileState("")
        )
    }

    // 文件修改状态跟踪（独立状态便于UI响应）
    val fileModifiedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            openFiles.forEach { fileName ->
                put(fileName, fileStates[fileName]?.isModified() ?: false)
            }
        }
    }

    // 当前文件状态（派生状态）
    val currentFileState by remember(selectedFile.value) {
        derivedStateOf {
            fileStates[selectedFile.value] ?: FileState("")
        }
    }

    // 文件结构定义
    fun scanProjectDirectory(basePath: String, currentPath: String = ""): List<FileItem> {
        val directory = File("$basePath/$currentPath")
        if (!directory.exists()) return emptyList()

        return directory.listFiles()?.map { file ->
            FileItem(
                name = file.name,
                path = if (currentPath.isEmpty()) file.name else "$currentPath/${file.name}",
                isDirectory = file.isDirectory,
                children = if (file.isDirectory) scanProjectDirectory(
                    basePath,
                    "$currentPath/${file.name}"
                ) else emptyList()
            )
        }?.sortedWith(compareBy(
            { !it.isDirectory }, // 目录在前
            { it.name.lowercase() }
        )) ?: emptyList()
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val (fileStructure, setFileStructure) = remember { mutableStateOf<List<FileItem>>(emptyList()) }
    LaunchedEffect(name, refreshTrigger) {
        val projectPath = if (exampleId != 0) {
            ProjectManager.getProjectPath(name)
        } else {
            path
        }

        // 在IO线程执行耗时扫描
        val result = withContext(Dispatchers.IO) {
            scanProjectDirectory(projectPath).also {
                println("重新加载文件结构，数量: ${it.size}")
            }
        }

        // 主线程更新状态
        setFileStructure(result)
    }


    // 示例代码初始化 ===================================================
    val example = remember(exampleId) {
        when (exampleId) {
            0 -> CodeExample(
                id = 0,
                title = name,
                content = File(path, "main.js").readText(),
                path = path
            )

            else -> CodeExample(
                id = 1,
                title = name,
                content = ProjectManager.readMain(name),
                path = ProjectManager.getProjectPath(name)
            )
        }
    }

    // 副作用和初始化 ===================================================
    LaunchedEffect(example) {
        val initialFilePath = if (exampleId != 0) {
            "${ProjectManager.getProjectPath(name)}/src/main/js/main.js"
        } else {
            "$path/main.js"
        }

        val initialDisplayPath = "main.js" // 初始文件直接显示文件名

        fileStates[initialDisplayPath] = FileState(example.content)
        fileModifiedStates[initialDisplayPath] = false
        filePathMap[initialDisplayPath] = initialFilePath
        openFiles.add(initialDisplayPath)
        selectedFile.value = initialDisplayPath

        viewModel.updateContent(example.content)
    }


    // 处理文件切换（同步编辑器状态）
    LaunchedEffect(selectedFile.value) {
        val editor = viewModel.editorState.value.editor
        val state = fileStates[selectedFile.value] ?: FileState("")
        selectedFilePath.value = filePathMap[selectedFile.value] ?: ""
        // 更新编辑器内容
        viewModel.updateContent(state.content)

        // 同步光标位置
        editor?.let {
            it.setText(state.content)
            it.cursor.set(state.selection.start, state.selection.end)
        }
    }

    // 键盘状态变化处理
    LaunchedEffect(isKeyboardVisible.value) {
        if (isKeyboardVisible.value) {
            showLogPanel.value = false
        }
    }

    // 业务逻辑 ========================================================
    fun toggleRunState() {
        if (isRunning.value) {
            runEngine.exit()
            isRunning.value = false
        } else {
            isRunning.value = true
            showLogPanel.value = true
            keyboardController?.hide()
            thread {
                ProjectManager.run(exampleId, example.path) { r ->
                    refreshTrigger++
                    if (r) {

                        CoreEnv.runTime.isRemoteMode = false
                        if (CoreEnv.executorMap.cancelAndClean("run")) {

                            val executor = DebugController.createNamedThreadPool()
                            CoreEnv.executorMap["run"] = executor.submit {
                                XLog.i("运行命令")
                                runEngine.run()
                                isRunning.value = false
                            }

                        }
                    } else {
                        viewModel.showMessage("运行失败")
                    }
                }
            }
        }
    }

    fun toggleRunUIState() {

//        viewModel.showMessage("预览UI功能开发中")
        thread {
            ProjectManager.runUi(exampleId, example.path) { r ->
                refreshTrigger++
                if (r) {

                    if (CoreEnv.executorMap.cancelAndClean("run")) {

                        val executor = DebugController.createNamedThreadPool()
                        CoreEnv.executorMap["run"] = executor.submit {
                            XLog.i("运行Ui命令")
                            CoreFactory.getWebView().runWebView("Keep")
                        }

                    }
                } else {
                    viewModel.showMessage("运行失败")
                }
            }
        }

    }

    fun getSafeSelectionRange(editor: CodeEditor): TextRange {
        return try {
            val line = editor.cursor.leftLine
            val column = editor.cursor.leftColumn
            TextRange(line, column)
        } catch (e: Exception) {
            TextRange(0, 0)
        }
    }

    ViewLogger.setViewLogger(ViewModelLogger(viewModel))
    fun handleFileClick(fileItem: FileItem) {
        if (!fileItem.isDirectory) {
            val filePath = if (exampleId != 0) {
                "${ProjectManager.getProjectPath(name)}/${fileItem.path}"
            } else {
                "$path/${fileItem.path}"
            }
            val f =File(filePath)

             if(f.name.endsWith(".apk", ignoreCase = true)){

                 val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", f)

                 val intent = Intent(Intent.ACTION_VIEW).apply {
                     setDataAndType(uri, "application/vnd.android.package-archive")
                     flags = Intent.FLAG_ACTIVITY_NEW_TASK
                 }

                 intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                 context.startActivity(intent)
                 return

            }

            val fileName = fileItem.name

            // 检查所有打开的文件中是否有同名文件
            val sameNameFiles = openFiles.filter {
                it.split("/").last() == fileName
            }

            // 决定显示名称：如果存在多个同名文件，所有同名文件都显示父目录
            val displayPath = if (sameNameFiles.size >= 1) {
                // 对于所有同名文件（包括新打开的和已经打开的），都更新为显示父目录
                sameNameFiles.forEach { existingPath ->
                    if (!existingPath.contains("/")) {
                        // 如果已有文件没有显示父目录，更新它
                        val index = openFiles.indexOf(existingPath)
                        if (index != -1) {
                            val existingItemPath = filePathMap[existingPath] ?: return@forEach
                            val parentDir = File(existingItemPath).parentFile?.name ?: ""
                            openFiles[index] = "$parentDir/$fileName"
                            filePathMap["$parentDir/$fileName"] = existingItemPath
                            filePathMap.remove(existingPath)

                            // 迁移文件状态
                            fileStates["$parentDir/$fileName"] =
                                fileStates[existingPath] ?: FileState("")
                            fileStates.remove(existingPath)
                            fileModifiedStates["$parentDir/$fileName"] =
                                fileModifiedStates[existingPath] ?: false
                            fileModifiedStates.remove(existingPath)

                            // 更新选中状态
                            if (selectedFile.value == existingPath) {
                                selectedFile.value = "$parentDir/$fileName"
                            }
                        }
                    }
                }

                // 对新打开的文件也显示父目录
                val parentDir = File(filePath).parentFile?.name ?: ""
                "$parentDir/$fileName"
            } else {
                fileName
            }

            if (!openFiles.contains(displayPath)) {
                openFiles.add(displayPath)
            }

            filePathMap[displayPath] = filePath
            selectedFile.value = displayPath

            if (!fileStates.containsKey(displayPath)) {
                val file = File(filePath)

                if (file.name.endsWith(".png", ignoreCase = true)) {
                    fileStates[displayPath] = FileState("")
                } else {
                    val fileContent = try {
                        file.readText()
                    } catch (e: Exception) {
                        "// 无法读取文件内容\n${e.message}"
                    }
                    fileStates[displayPath] = FileState(fileContent)
                }
                fileModifiedStates[displayPath] = false
            }

            scope.launch { drawerState.close() }
        }
    }


    // UI布局 =========================================================
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "项目文件结构",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    FileTreeView(
                        name = name,
                        files = fileStructure,
                        modifier = Modifier.fillMaxWidth(),
                        onFileClick = ::handleFileClick,
                        onCreateFolder = { parentPath, folderName ->
                            // 处理创建文件夹事件
                            println("Creating folder named $folderName in $parentPath")
                            ProjectManager.createFolder(name, parentPath, folderName)
                            refreshTrigger++
                        },
                        onDelete = { parentPath ->
                            println("rm $parentPath")
                            ProjectManager.rm(name, parentPath)
                            refreshTrigger++
                        },

                        onCut = { parentPath, target ->
                            println("Cut $parentPath in $target")
                            ProjectManager.cut(name, parentPath, target)
                            refreshTrigger++
                        },

                        onImport = { parentPath, source ->
                            println("Import $parentPath $source")
                            ProjectManager.import(name, parentPath, source)
                            refreshTrigger++
                        },

                        onCopy = { parentPath, target ->
                            println("Copy $parentPath in $target")
                            ProjectManager.copy(name, parentPath, target)
                            refreshTrigger++
                        },

                        onRename = { parentPath, newName ->
                            println("Rename $parentPath in $newName")
                            ProjectManager.rename(name, parentPath, newName)
                            refreshTrigger++

                        },

                        onCreateFile = { parentPath, fileName ->
                            // 处理创建文件事件
                            println("Creating file named $fileName in $parentPath")
                            ProjectManager.createFile(name, parentPath, fileName)
                            refreshTrigger++
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { scope.launch {
                        refreshTrigger++
                        // drawerState.close()
                    } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),  // 添加阴影效果
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(  // 添加按压效果
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                        disabledElevation = 0.dp
                    ),
                    border = BorderStroke(  // 添加边框
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(  // 使用Row布局图标和文本
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,  // 添加刷新图标
                            contentDescription = "刷新",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))  // 图标和文本之间的间距
                        Text(
                            text = "刷新",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp  // 增加字母间距
                            )
                        )
                    }
                }



            }
        }
    ) {


        val confirmDialogState = viewModel.confirmDialog.value
        if (confirmDialogState != null) {
            AlertDialog(
                onDismissRequest = {}, // 阻止点击外部关闭,
                title = { Text(confirmDialogState.title) },
                text = { Text(confirmDialogState.message) },
                confirmButton = {
                    TextButton(onClick = confirmDialogState.onConfirm) {
                        Text(confirmDialogState.confirmText)
                    }
                },
                dismissButton = {
                    TextButton(onClick = confirmDialogState.onDismiss) {
                        Text(confirmDialogState.dismissText)
                    }
                }
            )
        }

        var isBackPressedOnce by remember { mutableStateOf(false) }
        var backPressJob by remember { mutableStateOf<Job?>(null) }

        BackHandler(enabled = true) {
            XLog.e("按下返回键")

            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else {
                val hasUnsavedChanges = fileModifiedStates.any { it.value }

                if (hasUnsavedChanges) {
                    if (!isBackPressedOnce) {
                        // 第一次按返回键：提示用户再按一次
                        viewModel.showMessage("再按一次返回键退出")
                        isBackPressedOnce = true

                        // 3秒后自动重置状态
                        backPressJob?.cancel() // 取消之前的计时器（如果有）
                        backPressJob = scope.launch {
                            delay(3000) // 3秒后重置
                            isBackPressedOnce = false
                        }
                    } else {
                        // 第二次按返回键（3秒内）：显示确认对话框
                        backPressJob?.cancel() // 取消计时器
                        isBackPressedOnce = false // 重置状态

                        viewModel.showConfirmDialog(
                            title = "未保存的更改",
                            message = "有文件修改未保存，是否保存后再退出？",
                            confirmText = "保存并退出",
                            dismissText = "不保存退出",
                            onResult = { saveBeforeExit ->
                                scope.launch {
                                    if (saveBeforeExit) {
                                        fileStates.forEach { (fileName, state) ->
                                            if (fileModifiedStates[fileName] == true) {
                                                try {
                                                    val filePath = if (exampleId != 0) {
                                                        "${ProjectManager.getProjectPath(name)}/src/main/js/$fileName"
                                                    } else {
                                                        "$path/${fileName}"
                                                    }
                                                    File(filePath).writeText(state.content)
                                                    fileStates[fileName] =
                                                        state.copy(originalContent = state.content)
                                                    fileModifiedStates[fileName] = false
                                                    navController.popBackStack()
                                                } catch (e: Exception) {
                                                    viewModel.showMessage("保存文件 ${fileName} 失败: ${e.message}")
                                                }
                                            }
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        )
                    }
                } else {
                    if (!isBackPressedOnce) {
                        // 第一次按返回键：提示用户再按一次
                        viewModel.showMessage("再按一次返回键退出")
                        isBackPressedOnce = true

                        // 3秒后自动重置状态
                        backPressJob?.cancel()
                        backPressJob = scope.launch {
                            delay(3000)
                            isBackPressedOnce = false
                        }
                    } else {
                        // 第二次按返回键（3秒内）：直接退出
                        backPressJob?.cancel()
                        isBackPressedOnce = false
                        navController.popBackStack()
                    }
                }
            }
        }





        Scaffold(
            snackbarHost = {
                val message by viewModel.message
                AnimatedVisibility(
                    visible = message != null,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    message?.let { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(
                                        onClick = { viewModel.showMessage(null) }
                                    ) {
                                        Text("DISMISS")
                                    }
                                }
                            }
                        }
                    }
                }
            },

            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            example.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {

                            val hasUnsavedChanges = fileModifiedStates.any { it.value }
                            if (hasUnsavedChanges) {
                                viewModel.showConfirmDialog(
                                    title = "未保存的更改",
                                    message = "有文件修改未保存，是否保存后再退出？",
                                    confirmText = "保存并退出",
                                    dismissText = "不保存退出",
                                    onResult = { saveBeforeExit ->
                                        scope.launch {
                                            if (saveBeforeExit) {
                                                fileStates.forEach { (fileName, state) ->
                                                    if (fileModifiedStates[fileName] == true) {
                                                        try {
                                                            val filePath = if (exampleId != 0) {
                                                                "${
                                                                    ProjectManager.getProjectPath(
                                                                        name
                                                                    )
                                                                }/src/main/js/$fileName"
                                                            } else {
                                                                "$path/${fileName}"
                                                            }
                                                            File(filePath).writeText(state.content)
                                                            fileStates[fileName] =
                                                                state.copy(originalContent = state.content)
                                                            fileModifiedStates[fileName] = false
                                                            navController.popBackStack()
                                                        } catch (e: Exception) {
                                                            viewModel.showMessage("保存文件 ${fileName} 失败: ${e.message}")
                                                        }
                                                    }
                                                }
                                            } else {
                                                navController.popBackStack()
                                            }

                                        }
                                    }
                                )
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val fileName = selectedFile.value
                                fileStates[fileName]?.let { state ->
                                    val (newState, contentWithSelection) = state.undo()
                                    if (contentWithSelection != null) {
                                        val (prevContent, prevSelection) = contentWithSelection
                                        fileStates[fileName] = newState
                                        fileModifiedStates[fileName] = newState.isModified()
                                        viewModel.editorState.value.editor?.apply {
                                            setText(prevContent)
                                            cursor.set(prevSelection.start, prevSelection.end)
                                        }
                                    }
                                }
                            },
                            enabled = currentFileState.undoStack.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "撤销",
                                tint = if (currentFileState.undoStack.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        }

                        IconButton(
                            onClick = {
                                val fileName = selectedFile.value
                                fileStates[fileName]?.let { state ->
                                    val (newState, contentWithSelection) = state.redo()
                                    if (contentWithSelection != null) {
                                        val (nextContent, nextSelection) = contentWithSelection
                                        fileStates[fileName] = newState
                                        fileModifiedStates[fileName] = newState.isModified()
                                        viewModel.editorState.value.editor?.apply {
                                            setText(nextContent)
                                            cursor.set(nextSelection.start, nextSelection.end)
                                        }
                                    }
                                }
                            },
                            enabled = currentFileState.redoStack.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Redo,
                                contentDescription = "恢复",
                                tint = if (currentFileState.redoStack.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        }

                        IconButton(
                            onClick = { toggleRunState() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning.value) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning.value) "停止运行" else "开始运行",
                                tint = if (isRunning.value) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { toggleRunUIState() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "预览UI",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "更多操作",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("项目结构") },
                                    onClick = {
                                        expanded = false
                                        scope.launch { drawerState.open() }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Menu, null)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("编译") },
                                    onClick = {

                                        thread {
                                            ProjectManager.build(example.path) { r ->
                                                refreshTrigger++
                                                if (r) {
                                                    viewModel.showMessage("编译成功")
                                                } else {
                                                    viewModel.showMessage("编译失败")
                                                }
                                            }
                                        }

//                                        viewModel.showMessage("编译功能开发中")
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Build, null)
                                    }
                                )


                                DropdownMenuItem(
                                    text = { Text("终端") },
                                    onClick = {
//                                        val intent = Intent(Intent.ACTION_MAIN)
//                                        intent.setClassName(
//                                            "com.termux",
//                                            "com.termux.app.TermuxActivity"
//                                        )
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
//                                        context.startActivity(intent)
//                                        XLog.e(selectedFilePath.value)
                                        ProjectManager.openTermux(name,"")
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Terminal, null)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("保存") },
                                    onClick = {
                                        expanded = false
                                        val fileName = selectedFile.value
                                        fileStates[fileName]?.let { state ->
                                            fileStates[fileName] =
                                                state.copy(originalContent = state.content)
                                            fileModifiedStates[fileName] = false

                                            try {
                                                File(selectedFilePath.value).writeText(state.content)

                                                // 可选：显示保存成功提示
                                                scope.launch {
                                                    viewModel.showMessage("文件保存成功")
                                                }
                                            } catch (e: Exception) {
                                                scope.launch {
                                                    viewModel.showMessage("保存失败: ${e.message}")
                                                }
                                            }

                                        }
                                    },
                                    enabled = fileModifiedStates[selectedFile.value] == true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Save, null)
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            bottomBar = {
                if (keyboardTriggerSource.value == KeyboardTriggerSource.EDITOR) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .conditionalKeyboardPadding(keyboardState) // 使用修饰符
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        specialChars.forEach { char ->
                            TextButton(
                                onClick = {
                                    viewModel.editorState.value.editor?.insertText(
                                        char,
                                        char.length
                                    )
                                },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { layoutSize ->
                        containerHeight.value = with(density) { layoutSize.height.toDp() }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (openFiles.isNotEmpty()) {
                        var dragIndex by remember { mutableStateOf<Int?>(null) }
                        var dragOffset by remember { mutableStateOf(0f) }
                        var targetIndex by remember { mutableStateOf<Int?>(null) }
                        val scrollState = rememberScrollState()
                        var shouldScrollToSelectedTab by remember { mutableStateOf(false) }

                        // 存储每个标签的宽度
                        val tabWidths = remember { mutableStateMapOf<Int, Float>() }

                        // 自动滚动到选中标签的逻辑
                        LaunchedEffect(selectedFile.value, shouldScrollToSelectedTab) {
                            shouldScrollToSelectedTab = false
                            val selectedIndex = openFiles.indexOf(selectedFile.value)
                            if (selectedIndex != -1) {
                                // 等待布局完成
                                delay(20)

                                // 计算选中标签的位置
                                var position = 0f
                                for (i in 0 until selectedIndex) {
                                    position += tabWidths[i] ?: 0f
                                }

                                // 获取可视区域大小（正确方式）
                                val visibleWidth = with(density) {
                                    scrollState.maxValue.toDp().toPx()
                                }
                                val currentScroll = scrollState.value.toFloat()
                                val tabWidth = tabWidths[selectedIndex] ?: 0f

                                // 如果标签在可视区域左侧
                                if (position < currentScroll) {
                                    scrollState.animateScrollTo(
                                        position.toInt(),
                                        animationSpec = tween(durationMillis = 250)
                                    )
                                }
                                // 如果标签在可视区域右侧
                                else if (position + tabWidth > currentScroll + visibleWidth) {
                                    scrollState.animateScrollTo(
                                        (position + tabWidth - visibleWidth).toInt(),
                                        animationSpec = tween(durationMillis = 250)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .horizontalScroll(scrollState)
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            openFiles.forEachIndexed { index, fileName ->
                                val isDragging = dragIndex == index

                                // 计算每个标签的偏移量 - 优化动画参数
                                val offset by animateFloatAsState(
                                    targetValue = when {
                                        isDragging -> dragOffset
                                        dragIndex != null && dragIndex != index -> {
                                            when {
                                                index < dragIndex!! && index >= (targetIndex
                                                    ?: dragIndex!!) ->
                                                    tabWidths[dragIndex!!] ?: 0f

                                                index > dragIndex!! && index <= (targetIndex
                                                    ?: dragIndex!!) ->
                                                    -(tabWidths[dragIndex!!] ?: 0f)

                                                else -> 0f
                                            }
                                        }

                                        else -> 0f
                                    },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "tabOffset"
                                )

                                Box(
                                    modifier = Modifier
                                        .width(IntrinsicSize.Min)
                                        .height(48.dp)
                                        .padding(horizontal = 4.dp)
                                        .offset { IntOffset(offset.roundToInt(), 0) }
                                        .zIndex(if (isDragging) 2f else if (offset != 0f) 1f else 0f)
                                        .background(
                                            if (fileName == selectedFile.value) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(
                                                topStart = 8.dp,
                                                topEnd = 8.dp
                                            )
                                        )
                                        .onGloballyPositioned { coordinates ->
                                            tabWidths[index] = coordinates.size.width.toFloat()
                                        }
                                        .pointerInput(fileName, tabWidths) {
                                            coroutineScope {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        dragIndex = index
                                                        dragOffset = 0f
                                                        targetIndex = index
                                                        selectedFile.value = fileName
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        // 使用密度转换确保在不同设备上移动一致
                                                        val dragAmountPx = with(density) {
                                                            dragAmount.x.toDp().toPx()
                                                        }
                                                        dragOffset += dragAmountPx

                                                        val totalDragOffset = dragOffset

                                                        var newTargetIndex = dragIndex!!
                                                        var accumulatedWidth = 0f

                                                        if (totalDragOffset > 0) {
                                                            for (i in dragIndex!! + 1 until openFiles.size) {
                                                                val nextTabWidth =
                                                                    tabWidths[i] ?: 0f
                                                                accumulatedWidth += nextTabWidth
                                                                if (totalDragOffset > accumulatedWidth - nextTabWidth / 2) {
                                                                    newTargetIndex = i
                                                                } else {
                                                                    break
                                                                }
                                                            }
                                                        } else {
                                                            for (i in dragIndex!! - 1 downTo 0) {
                                                                val prevTabWidth =
                                                                    tabWidths[i] ?: 0f
                                                                accumulatedWidth -= prevTabWidth
                                                                if (totalDragOffset < accumulatedWidth + prevTabWidth / 2) {
                                                                    newTargetIndex = i
                                                                } else {
                                                                    break
                                                                }
                                                            }
                                                        }

                                                        if (newTargetIndex != targetIndex) {
                                                            targetIndex = newTargetIndex
                                                        }

                                                        // 自动滚动逻辑优化
                                                        val visibleWidth = size.width.toFloat()
                                                        val scrollPosition =
                                                            scrollState.value.toFloat()
                                                        var draggedPos = 0f
                                                        for (i in 0 until dragIndex!!) {
                                                            draggedPos += tabWidths[i] ?: 0f
                                                        }
                                                        draggedPos += totalDragOffset - scrollPosition

                                                        val scrollThreshold = 0.3f
                                                        val scrollFactor = 0.5f

                                                        if (draggedPos > visibleWidth * (1 - scrollThreshold) && scrollState.canScrollForward) {
                                                            launch {
                                                                scrollState.scrollBy(dragAmountPx * scrollFactor)
                                                            }
                                                        } else if (draggedPos < visibleWidth * scrollThreshold && scrollState.canScrollBackward) {
                                                            launch {
                                                                scrollState.scrollBy(dragAmountPx * scrollFactor)
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        if (dragIndex != null && targetIndex != null && dragIndex != targetIndex) {
                                                            val item = openFiles[dragIndex!!]
                                                            openFiles.removeAt(dragIndex!!)
                                                            openFiles.add(targetIndex!!, item)

                                                            val draggedWidth =
                                                                tabWidths.remove(dragIndex!!)
                                                            if (draggedWidth != null) {
                                                                tabWidths[targetIndex!!] =
                                                                    draggedWidth
                                                            }

                                                            if (selectedFile.value == item) {
                                                                selectedFile.value =
                                                                    openFiles[targetIndex!!]
                                                            }
                                                        }

                                                        dragIndex = null
                                                        dragOffset = 0f
                                                        targetIndex = null
                                                        shouldScrollToSelectedTab = true
                                                    },
                                                    onDragCancel = {
                                                        println("取消")
                                                    }
                                                )
                                            }
                                        }
                                        .clickable {
                                            viewModel.showMessage("文件名：$fileName")
                                            selectedFile.value = fileName }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (fileModifiedStates[fileName] == true) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    )
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(4.dp))
                                        }

                                        Row(
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    fileName.endsWith(".kt") -> Icons.Default.Code
                                                    fileName.endsWith(".xml") -> Icons.Default.DataObject
                                                    else -> Icons.Default.InsertDriveFile
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = fileName,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f),
                                                color = if (fileName == selectedFile.value)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val currentIndex = openFiles.indexOf(fileName)
                                                openFiles.removeAt(currentIndex)
                                                fileModifiedStates.remove(fileName)
                                                fileStates.remove(fileName)
                                                tabWidths.remove(currentIndex)

                                                // 检查是否还有同名文件
                                                val baseName = fileName.split("/").last()
                                                val remainingSameNameFiles = openFiles.filter {
                                                    it.split("/").last() == baseName
                                                }

                                                if (remainingSameNameFiles.size == 1) {
                                                    // 如果只剩一个同名文件，恢复为只显示文件名
                                                    val singlePath = remainingSameNameFiles[0]
                                                    val index = openFiles.indexOf(singlePath)
                                                    val filePath = filePathMap[singlePath] ?: ""

                                                    openFiles[index] = baseName
                                                    filePathMap[baseName] = filePath
                                                    filePathMap.remove(singlePath)

                                                    // 迁移文件状态
                                                    fileStates[baseName] =
                                                        fileStates[singlePath] ?: FileState("")
                                                    fileStates.remove(singlePath)
                                                    fileModifiedStates[baseName] =
                                                        fileModifiedStates[singlePath] ?: false
                                                    fileModifiedStates.remove(singlePath)

                                                    // 更新选中状态
                                                    if (selectedFile.value == singlePath) {
                                                        selectedFile.value = baseName
                                                    }
                                                }

                                                selectedFile.value = when {
                                                    openFiles.isEmpty() -> ""
                                                    currentIndex >= openFiles.size -> openFiles.last()
                                                    else -> openFiles[currentIndex.coerceAtMost(
                                                        openFiles.size - 1
                                                    )]
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "关闭标签",
                                                tint = if (fileName == selectedFile.value)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }


                                    }
                                }
                            }
                        }
                    }


                    // 代码编辑器区域
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = !showLogPanel.value)
                            .fillMaxWidth()
                    ) {
                        if (openFiles.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂未打开任何文件",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            val currentFile = selectedFile.value
                            if (currentFile.endsWith(".png", ignoreCase = true)) {
                                // 显示图片
                                val bitmap = remember {
                                    mutableStateOf<Bitmap?>(null)
                                }

                                LaunchedEffect(currentFile) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            bitmap.value =
                                                BitmapFactory.decodeFile(selectedFilePath.value)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    bitmap.value?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Image Preview",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } ?: run {
                                        Text("无法加载图片")
                                    }
                                }
                            } else {
                                // 显示代码编辑器
                                CodeEditor(
                                    modifier = Modifier.fillMaxSize(),
                                    state = viewModel.editorState.value,
                                    onEditorClicked = {
                                        keyboardTriggerSource.value = KeyboardTriggerSource.EDITOR
                                        showLogPanel.value = false
                                    }
                                )
                            }

                        }
                    }

                    // 日志面板
                    if (showLogPanel.value && !isKeyboardVisible.value) {
                        Column(
                            modifier = Modifier
                                .height(
                                    calculatePanelHeight(
                                        maxHeight = containerHeight.value * 0.8f,
                                        panelHeight = panelHeight.value,
                                        keyboardState
                                    )
                                )
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val newHeight = panelHeight.value - dragAmount.y.toDp()
                                            val minHeight = 100.dp
                                            val maxHeight = containerHeight.value * 0.8f
                                            panelHeight.value =
                                                newHeight.coerceIn(minHeight, maxHeight)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.6f
                                            ),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                                tonalElevation = 4.dp,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "运行日志",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            BasicTextField(
                                                value = viewModel.searchQuery,
                                                onValueChange = { viewModel.searchQuery = it },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(32.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    .onFocusChanged { focusState ->
                                                        if (focusState.isFocused) {
                                                            keyboardTriggerSource.value =
                                                                KeyboardTriggerSource.LOG_SEARCH
                                                        }
                                                    },
                                                singleLine = true,
                                                decorationBox = { innerTextField ->
                                                    if (viewModel.searchQuery.isEmpty()) {
                                                        Text(
                                                            text = "搜索日志...",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                                    alpha = 0.5f
                                                                )
                                                            )
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            )
                                            IconButton(
                                                onClick = { viewModel.clearLogs() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, "清除日志")
                                            }
                                            IconButton(
                                                onClick = { showLogPanel.value = false },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, "关闭日志")
                                            }
                                        }
                                    }
                                    LogList(
                                        logs = viewModel.getFilteredLogs(),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isKeyboardVisible.value) {
                    FloatingActionButton(
                        onClick = {
                            showLogPanel.value = !showLogPanel.value
                            if (showLogPanel.value) {
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = if (showLogPanel.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showLogPanel.value) "隐藏日志" else "显示日志"
                        )
                    }
                }
            }
        }

        // 编辑器内容变化监听
        DisposableEffect(viewModel.editorState.value.editor) {
            val editor = viewModel.editorState.value.editor
            val subscription = editor?.subscribeEvent(
                ContentChangeEvent::class.java
            ) { _, _ ->
                editor.let { e ->
                    val newText = e.text.toString()
                    val selection = getSafeSelectionRange(e)
                    val fileName = selectedFile.value

                    if (newText != fileStates[fileName]?.content) {
                        fileStates[fileName]?.let { currentState ->
                            val updatedState =
                                currentState.pushUndo(currentState.content, currentState.selection)
                                    .copy(
                                        content = newText,
                                        selection = selection
                                    )

                            fileStates[fileName] = updatedState
                            fileModifiedStates[fileName] = updatedState.isModified()
                        }
                    }
                }
            }

            onDispose {
                subscription?.unsubscribe()
            }
        }
    }
}