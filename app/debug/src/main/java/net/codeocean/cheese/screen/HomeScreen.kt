package net.codeocean.cheese.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvishew.xlog.XLog
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import net.codeocean.cheese.connectData
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.runtime.debug.remote.DebugController
import net.codeocean.cheese.frontend.javascript.JavaScript

import kotlin.concurrent.thread

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var ipAddress by remember { mutableStateOf(getSavedText(context) ?: "") }
    var connectionStatus by remember { connectData }
    var pingTime by remember { mutableIntStateOf(0) }
    val historyList =
        remember { mutableStateListOf<String>().apply { addAll(getHistoryList(context)) } }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    var currentSwipedItem by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    val itemOffsets = remember { mutableStateMapOf<String, Animatable<Float, *>>() }

    val density = LocalDensity.current

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1A73E8),
            Color(0xFF34A853),
            Color(0xFFFBBC05)
        ),
        startX = 0f,
        endX = Float.POSITIVE_INFINITY
    )

    val elevation = 12.dp
    val elevationPx = with(density) { elevation.toPx() }

    fun toggleConnection() {
        if (connectionStatus) {
            DebugController.close()
            connectionStatus = false
        } else {
            if (ipAddress.isNotBlank()) {
                saveCurrentIp(context, ipAddress)
                focusManager.clearFocus()
                pingTime = (10..100).random()
                CoreEnv.runTime.ip = ipAddress
                thread {
                    DebugController.connect(
                        JavaScript(context),
                        CoreEnv.runTime.ip,
                        onOpen = {
                            connectionStatus = true
                            if (!historyList.contains(ipAddress)) {
                                historyList.add(0, ipAddress)
                                saveHistory(context, ipAddress)
                            }

                        },
                        onClose = { connectionStatus = false },
                        onError = { errorMsg ->
                            XLog.e(errorMsg)
                            Toaster.show(errorMsg)
                            connectionStatus = false

                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这个服务器地址吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { address ->
                            historyList.remove(address)
                            saveHistoryList(context, historyList.toSet())
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                        currentSwipedItem = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                        currentSwipedItem = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("清空历史记录") },
            text = { Text("确定要清空所有历史记录吗？此操作不可恢复") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyList.clear()
                        clearAllHistory(context)
                        showClearAllDialog = false
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearAllDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Cheese v${CoreEnv.sdk_version}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                blurRadius = elevationPx * 0.8f,
                                offset = Offset(0f, 2f)
                            ),
                            letterSpacing = 0.3.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(gradientBrush)
                    .shadow(elevation)
                    .height(80.dp),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { toggleConnection() },
                containerColor = if (connectionStatus) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (connectionStatus) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        imageVector = if (connectionStatus) Icons.Default.LinkOff
                        else Icons.Default.Link,
                        contentDescription = "连接状态"
                    )
                },
                text = {
                    Text(
                        if (connectionStatus) "断开连接" else "连接节点",
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        currentSwipedItem = null
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (connectionStatus)
                            Color.Green.copy(alpha = 0.1f)
                        else
                            Color.Gray.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (connectionStatus) Color.Green else Color.Gray
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (connectionStatus) Color.Green else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (connectionStatus) "已连接到节点" else "未连接",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (connectionStatus) Color.Green else Color.Gray
                            )
                        }

                        if (connectionStatus) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = "延迟",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("延迟: ${pingTime}ms", fontSize = 14.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { newIp ->
                        ipAddress = newIp
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                saveCurrentIp(context, ipAddress)
                            }
                        },
                    label = { Text("节点服务器地址") },
                    placeholder = { Text("例如: 192.168.1.1:8080") },
                    leadingIcon = {
                        Icon(Icons.Default.Public, contentDescription = "服务器地址")
                    },
                    trailingIcon = {
                        if (ipAddress.isNotEmpty()) {
                            IconButton(onClick = {
                                ipAddress = ""
                                saveCurrentIp(context, "")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "清除")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            saveCurrentIp(context, ipAddress)
                            focusManager.clearFocus()
                        }
                    )
                )

                Column {
                    Text(
                        "常用",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cheese.codeocean.net/"))
                                context.startActivity(intent)
//                                ipAddress = "127.0.0.1:8080"
//                                saveCurrentIp(context, ipAddress)
//                                focusManager.clearFocus()
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = "官网",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("官网")
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                if (historyList.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "历史连接",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            TextButton(
                                onClick = { showClearAllDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    "清空",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(historyList) { address ->
                                val offsetX = itemOffsets.getOrPut(address) { Animatable(0f) }

                                LaunchedEffect(currentSwipedItem) {
                                    if (currentSwipedItem != address) {
                                        offsetX.animateTo(0f, tween(300))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(x = offsetX.value.dp)
                                        .pointerInput(address) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    coroutineScope.launch {
                                                        if (offsetX.value < -100f) {
                                                            itemToDelete = address
                                                            showDeleteDialog = true
                                                        } else {
                                                            offsetX.animateTo(0f, tween(300))
                                                        }
                                                        currentSwipedItem = null
                                                    }
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    coroutineScope.launch {
                                                        if (currentSwipedItem == null || currentSwipedItem == address) {
                                                            currentSwipedItem = address
                                                            if (dragAmount < 0 || offsetX.value < 0) {
                                                                offsetX.snapTo(
                                                                    (offsetX.value + dragAmount)
                                                                        .coerceAtMost(0f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    itemToDelete = address
                                                    showDeleteDialog = true
                                                }
                                            )
                                        }
                                ) {
                                    Card(
                                        onClick = {
                                            coroutineScope.launch {
                                                ipAddress = address
                                                saveCurrentIp(context, address)
                                                offsetX.animateTo(0f, tween(300))
                                                currentSwipedItem = null
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (currentSwipedItem == address && offsetX.value < -50f)
                                                Color.Red.copy(alpha = 0.3f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.History,
                                                contentDescription = "历史记录",
                                                modifier = Modifier.size(20.dp),
                                                tint = if (currentSwipedItem == address && offsetX.value < -50f) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                address,
                                                fontSize = 14.sp,
                                                color = if (currentSwipedItem == address && offsetX.value < -50f) Color.White else MaterialTheme.colorScheme.onSurface
                                            )

                                            if (currentSwipedItem == address && offsetX.value < -50f) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "删除",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun clearAllHistory(context: Context) {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putStringSet("history", emptySet<String>())
        apply()
    }
}

private fun saveCurrentIp(context: Context, text: String) {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("ip", text)
        apply()
    }
}

private fun saveHistory(context: Context, text: String) {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val history =
        sharedPref.getStringSet("history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    history.add(text)
    with(sharedPref.edit()) {
        putStringSet("history", history)
        apply()
    }
}

private fun saveHistoryList(context: Context, history: Set<String>) {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putStringSet("history", history)
        apply()
    }
}

private fun getSavedText(context: Context): String? {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return sharedPref.getString("ip", null)
}

private fun getHistoryList(context: Context): Set<String> {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return sharedPref.getStringSet("history", emptySet()) ?: emptySet()
}