package net.codeocean.cheese.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material.icons.outlined.WrapText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.codeocean.cheese.R
import net.codeocean.cheese.appLogger

// 定义日志等级和对应颜色
enum class LogLevel(val tag: String, val color: Color) {
    VERBOSE("V", Color(0xFF9E9E9E)),    // 灰色
    DEBUG("D", Color(0xFF2196F3)),      // 蓝色
    INFO("I", Color(0xFF4CAF50)),       // 绿色
    WARN("W", Color(0xFFFFC107)),      // 黄色
    ERROR("E", Color(0xFFF44336)),      // 红色
    ASSERT("A", Color(0xFF673AB7)),     // 紫色
    UNKNOWN("?", Color(0xFF000000))     // 黑色
}

// 解析日志等级
fun parseLogLevel(log: String): LogLevel {
    return when {
        log.contains(" V") -> LogLevel.VERBOSE
        log.contains(" D") -> LogLevel.DEBUG
        log.contains(" I") -> LogLevel.INFO
        log.contains(" W") -> LogLevel.WARN
        log.contains(" E") -> LogLevel.ERROR
        log.contains(" A") -> LogLevel.ASSERT
        else -> LogLevel.UNKNOWN
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DebugScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSingleLineMode by remember { mutableStateOf(false) } // 新增：单行模式状态
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val elevation: Dp = 12.dp

    // 判断是否需要显示返回底部按钮
    val showScrollToBottom by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                false
            } else {
                val lastVisibleItemIndex = visibleItemsInfo.last().index
                val totalItemsCount = layoutInfo.totalItemsCount
                lastVisibleItemIndex < totalItemsCount - 1
            }
        }
    }

    // 判断是否需要显示返回顶部按钮
    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    // 自动滚动到底部
    LaunchedEffect(appLogger.size) {
        if (appLogger.isNotEmpty()) {
            coroutineScope.launch {
                withFrameNanos {}
                lazyListState.animateScrollToItem(
                    index = appLogger.size - 1,
                    scrollOffset = 0,
                )
                delay(50)
                if (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != appLogger.size - 1) {
                    lazyListState.scrollToItem(appLogger.size - 1)
                }
            }
        }
    }

    val filteredLogs = remember(searchQuery, appLogger) {
        if (searchQuery.isEmpty()) {
            appLogger
        } else {
            appLogger.filter { log ->
                log.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // 正常状态下的标题
                    AnimatedVisibility(
                        visible = !isSearchExpanded,
                        enter = fadeIn(animationSpec = tween(150)),
                        exit = fadeOut(animationSpec = tween(150)),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            "ADB日志",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = shadowColor,
                                    blurRadius = elevation.value,
                                    offset = Offset(0f, 2f)
                                )
                            )
                        )
                    }

                    // 右侧按钮组（搜索+清除+显示模式切换）
                    AnimatedVisibility(
                        visible = !isSearchExpanded,
                        enter = fadeIn(animationSpec = tween(150)),
                        exit = fadeOut(animationSpec = tween(150)),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Row {
                            // 新增：显示模式切换按钮
                            IconButton(
                                onClick = { isSingleLineMode = !isSingleLineMode },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (isSingleLineMode) Icons.Filled.WrapText else Icons.Outlined.WrapText,
                                    contentDescription = if (isSingleLineMode) "多行显示" else "单行显示",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = { isSearchExpanded = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "搜索",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = { appLogger.clear() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete),
                                    contentDescription = "清除所有日志",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 搜索栏（全宽度）
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = fadeIn(animationSpec = tween(200)) +
                                expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut(animationSpec = tween(200)) +
                                shrinkHorizontally(shrinkTowards = Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "搜索日志...",
                                            maxLines = 1,
                                            overflow = TextOverflow.Visible,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "搜索",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { searchQuery = "" },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "清除",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                )

                                // 取消按钮
                                Text(
                                    "取消",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            isSearchExpanded = false
                                            searchQuery = ""
                                        }
                                )
                            }
                        }
                    }
                }
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (filteredLogs.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "暂无日志记录",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(filteredLogs) { log ->
                            val logLevel = parseLogLevel(log)

                            if (isSingleLineMode) {
                                // 单行模式（可水平滚动）
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentWidth(unbounded = true),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = logLevel.color.copy(alpha = 0.1f)
                                        ),
                                        elevation = CardDefaults.cardElevation(1.dp)
                                    ) {
                                        Text(
                                            text = log,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = logLevel.color,
                                            modifier = Modifier.padding(12.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Visible
                                        )
                                    }
                                }
                            } else {
                                // 多行模式
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = logLevel.color.copy(alpha = 0.1f)
                                    ),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Text(
                                        text = log,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = logLevel.color,
                                        modifier = Modifier.padding(12.dp),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // 返回顶部按钮 (右上角)
                    AnimatedVisibility(
                        visible = showScrollToTop,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(0)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "返回顶部",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // 返回底部按钮 (右下角)
                    AnimatedVisibility(
                        visible = showScrollToBottom,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (filteredLogs.isNotEmpty()) {
                                        lazyListState.animateScrollToItem(
                                            index = filteredLogs.size - 1,
                                            scrollOffset = 0
                                        )
                                        delay(50)
                                        if (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != appLogger.size - 1) {
                                            lazyListState.scrollToItem(appLogger.size - 1)
                                        }
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "返回底部",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}