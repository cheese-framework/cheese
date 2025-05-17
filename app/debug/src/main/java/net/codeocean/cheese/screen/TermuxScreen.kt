//package net.codeocean.cheese.screen
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.os.Build
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.focusable
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.text.selection.SelectionContainer
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.input.key.*
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.platform.LocalTextToolbar
//import androidx.compose.ui.platform.TextToolbar
//import androidx.compose.ui.semantics.Role.Companion.Button
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import net.codeocean.cheese.backend.impl.TermuxAidlTerminal
//import java.io.BufferedReader
//import java.io.InputStreamReader
//
//
//
//
//
//
//
//import androidx.compose.foundation.text.selection.SelectionContainer
//import androidx.compose.material3.LocalTextStyle
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.platform.ClipboardManager
//import androidx.compose.ui.platform.LocalClipboardManager
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.TextLayoutResult
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.withStyle
//import androidx.compose.ui.unit.Dp
//
//
//// 过滤终端输出
//private fun filterTerminalOutput(output: String): String {
//    return output.replace("\r", "").trim().takeIf { it.isNotEmpty() } ?: ""
//}
//
//// 解析 ANSI 颜色
//fun parseTerminalText(text: String): AnnotatedString {
//    return buildAnnotatedString {
//        if (text.isEmpty()) return@buildAnnotatedString
//
//        // 标准化换行符处理
//        val normalizedText = text.replace("\r\n", "\n").replace('\r', '\n')
//        val lines = normalizedText.split('\n')
//
//        lines.forEachIndexed { index, line ->
//            // 添加换行符（除了第一行之前和最后一行之后）
//            if (index > 0) append("\n")
//
//            when {
//                // 改进的命令行检测（支持多种提示符格式）
//                line.trim().endsWith('$') -> {
//                    val promptEnd = line.indexOfLast { it == '$' } + 1
//
//                    // 1. 提示符部分（白色）
//                    if (promptEnd > 0) {
//                        withStyle(SpanStyle(color = Color.White)) {
//                            append(line.substring(0, promptEnd))
//                        }
//                    }
//
//                    // 2. 命令部分（绿色）
//                    if (promptEnd < line.length) {
//                        val command = line.substring(promptEnd).trimStart()
//                        if (command.isNotEmpty()) {
//                            withStyle(SpanStyle(color = Color(0xFF33FF33))) {
//                                append(" $command") // 保留一个空格
//                            }
//                        }
//                    }
//                }
//
//                // 空行处理（保持布局）
//                line.isEmpty() -> {
//                    withStyle(SpanStyle(color = Color.White)) {
//                        append(" ")
//                    }
//                }
//
//                // 普通输出行（白色）
//                else -> {
//                    withStyle(SpanStyle(color = Color.White)) {
//                        append(line)
//                    }
//                }
//            }
//        }
//    }
//}
////private fun parseAnsiColors(input: String): AnnotatedString {
////    val ansiPattern = "\\u001B\\[([0-9;]*)([a-zA-Z])".toRegex()
////    val builder = AnnotatedString.Builder()
////
////    var lastIndex = 0
////    ansiPattern.findAll(input).forEach { matchResult ->
////        val range = matchResult.range
////        val code = matchResult.groupValues[1]
////        val command = matchResult.groupValues[2]
////
////        // 添加之前的普通文本
////        builder.append(input.substring(lastIndex, range.start))
////
////        if (command == "m") {
////            val colorCode = code.split(';').lastOrNull()?.toIntOrNull()
////            val color = when (colorCode) {
////                31 -> Color.Red // 红色
////                32 -> Color.Green // 绿色
////                33 -> Color.Yellow // 黄色
////                34 -> Color.Blue // 蓝色
////                35 -> Color.Magenta // 紫色
////                36 -> Color.Cyan // 青色
////                37 -> Color.White // 白色
////                else -> Color.Gray // 默认颜色
////            }
////            builder.pushStyle(SpanStyle(color = color))
////        }
////        lastIndex = range.endInclusive + 1
////    }
////    builder.append(input.substring(lastIndex))
////    return builder.toAnnotatedString()
////}
//
////// 终端管理器
////class TerminalManager(context: Context) {
////    private val termuxTerminal = TermuxAidlTerminal(context) { output, _ ->
////        val filteredOutput = filterTerminalOutput(output)
////        if (filteredOutput.isNotEmpty()) {
////
////            println(">>>>>>>>>>>>"+filteredOutput)
////
////            if (filteredOutput.contains(" $")){
////                if (filteredOutput.contains("/")){
////                    if (pathText!=filteredOutput.replace("$", "").trim()){
////                        pathText = filteredOutput.replace("$", "").trim()
////                    }
////                }
////                isAwaitingInput = true
////            }else {
////                _terminalLines.add(filteredOutput)
////                isAwaitingInput = false
////            }
////
////        }
////    }
////    var isAwaitingInput by mutableStateOf(true)
////
////    private val _terminalLines = mutableStateListOf<String>()
////    val terminalLines: List<String> = _terminalLines
////    var pathText by mutableStateOf("")
////    var currentInputText by mutableStateOf("")
////    var isScrollLocked by mutableStateOf(false) // 滚动锁定状态
////    private val commandHistory = mutableListOf<String>()
////    private var historyIndex = -1
////
////    init {
////        termuxTerminal.connect()
////    }
////
////    fun sendCommand() {
////        val input = currentInputText.trim()
////        if (input.isNotEmpty()) {
////            _terminalLines.add("$pathText $ $input") // 显示用户输入
////            termuxTerminal.sendInput(input)
////            commandHistory.add(input) // 添加到历史记录
////            historyIndex = commandHistory.size // 重置历史索引
////        }
////        currentInputText = ""
////        isScrollLocked = false // 发送命令后解锁滚动
////    }
////
////    fun navigateHistory(delta: Int) {
////        val newIndex = historyIndex + delta
////        if (newIndex in 0 until commandHistory.size) {
////            historyIndex = newIndex
////            currentInputText = commandHistory[historyIndex]
////        } else if (newIndex == commandHistory.size) {
////            historyIndex = newIndex
////            currentInputText = ""
////        }
////    }
////
////    fun disconnect() {
////        termuxTerminal.disconnect()
////    }
////}
////
////
////@Composable
////fun RealTerminal(terminalManager: TerminalManager) {
////    val scrollState = rememberScrollState()
////    val focusRequester = remember { FocusRequester() }
////    val keyboardController = LocalSoftwareKeyboardController.current
////    val density = LocalDensity.current
////    val coroutineScope = rememberCoroutineScope()
////
////    // 获取键盘高度和导航栏高度
////    val imeBottom = WindowInsets.ime.getBottom(density)
////    val navigationBarsBottom = WindowInsets.navigationBars.getBottom(density)
////
////    val infiniteTransition = rememberInfiniteTransition()
////    val cursorAlpha by infiniteTransition.animateFloat(
////        initialValue = 1f,
////        targetValue = 0f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(800),
////            repeatMode = RepeatMode.Reverse
////        )
////    )
////
////    // 自动聚焦和滚动控制
////    LaunchedEffect(Unit) {
////        focusRequester.requestFocus()
////    }
////
////    LaunchedEffect(terminalManager.terminalLines.size, imeBottom) {
////        if (!terminalManager.isScrollLocked) {
////            val additionalPadding = if (imeBottom > 0) {
////                // 当键盘显示时，考虑导航栏和键盘的复合高度
////                (imeBottom + navigationBarsBottom).coerceAtLeast(0)
////            } else 0
////
////            coroutineScope.launch {
////                scrollState.animateScrollTo(scrollState.maxValue + additionalPadding)
////            }
////        }
////    }
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.Black)
////            .padding(16.dp)
////            .pointerInput(Unit) {
////                detectTapGestures(onTap = {
////                    focusRequester.requestFocus()
////                    keyboardController?.show()
////                })
////            }
////    ) {
////        Column(
////            modifier = Modifier
////                .fillMaxSize()
////                .verticalScroll(scrollState)
////                .imePadding()
////                .navigationBarsPadding()
////        ) {
////            SelectionContainer(
////                modifier = Modifier
////                    .weight(1f)
////            ) {
////                Column(
////                    modifier = Modifier.padding(bottom = 8.dp) // 添加底部间距
////                ) {
////                    terminalManager.terminalLines.forEach { line ->
////                        Text(
////                            text = parseTerminalText(line),
////                            style = LocalTextStyle.current.copy(
////                                fontSize = 11.sp,
////                                color = Color(0xFF33FF33),
////                                fontFamily = FontFamily.Monospace
////                            ),
////                            modifier = Modifier.padding(bottom = 4.dp)
////                        )
////                    }
////
////                    if (terminalManager.isAwaitingInput) {
////                        val inputText = buildAnnotatedString {
////                            // 第一部分：路径和$符号（白色）
////                            withStyle(style = SpanStyle(color = Color.White)) {
////                                append("${terminalManager.pathText} \$ ")
////                            }
////                            // 第二部分：用户输入文本（保持原色）
////                            append(terminalManager.currentInputText)
////                        }
//////                        val inputText = "${terminalManager.pathText} \$ ${terminalManager.currentInputText}"
////                        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
////
////                        Box(modifier = Modifier.padding(bottom = 4.dp)) {
////                            Text(
////                                text = inputText,
////                                style = LocalTextStyle.current.copy(
////                                    fontSize = 11.sp,
////                                    color = Color(0xFF33FF33),
////                                    fontFamily = FontFamily.Monospace
////                                ),
////                                onTextLayout = { layoutResult ->
////                                    textLayoutResult = layoutResult
////                                }
////                            )
////
////                            textLayoutResult?.let { result ->
////                                val lineCount = result.lineCount
////                                if (lineCount > 0) {
////                                    val lastLineRight = result.getLineRight(lineCount - 1)
////                                    val lastLineTop = result.getLineTop(lineCount - 1)
////                                    val lastLineBottom = result.getLineBottom(lineCount - 1)
////
////                                    Box(
////                                        modifier = Modifier
////                                            .offset(
////                                                x = with(density) { lastLineRight.toDp() },
////                                                y = with(density) {
////                                                    // 使用绝对位置计算
////                                                    val textHeight = lastLineBottom - lastLineTop
////                                                    val cursorHeight = textHeight * 0.5f
////                                                    (lastLineTop + textHeight/2 - cursorHeight/2).toDp()
////                                                }
////                                            )
////                                            .width(5.dp)
////                                            .height(with(density) {
////                                                val lineHeightPx = lastLineBottom - lastLineTop
////                                                (lineHeightPx * 0.5f).toDp() // 缩放后转回Dp
////                                            })
////                                            .alpha(cursorAlpha)
////                                            .background(Color(0xFF33FF33))
////                                    )
////                                }
////                            }
////                        }
////                    }
////                }
////            }
////
////            // 隐藏的输入框
////            BasicTextField(
////                value = terminalManager.currentInputText,
////                onValueChange = { terminalManager.currentInputText = it },
////                textStyle = TextStyle(
////                    fontSize = 14.sp,
////                    color = Color.Transparent,
////                    fontFamily = FontFamily.Monospace
////                ),
////                singleLine = true,
////                keyboardOptions = KeyboardOptions(
////                    imeAction = ImeAction.Done,
////                    keyboardType = KeyboardType.Ascii
////                ),
////                keyboardActions = KeyboardActions(
////                    onDone = { terminalManager.sendCommand() }
////                ),
////                modifier = Modifier
////                    .focusRequester(focusRequester)
////                    .graphicsLayer(alpha = 0f)
////                    .height(0.dp), // 完全隐藏但保持功能
////                cursorBrush = SolidColor(Color.Transparent)
////            )
////        }
////    }
////}
//
//// 终端界面
////@Composable
////fun RealTerminal(terminalManager: TerminalManager) {
////    val scrollState = rememberScrollState()
////    val focusRequester = remember { FocusRequester() }
////
////    val infiniteTransition = rememberInfiniteTransition()
////    val keyboardController = LocalSoftwareKeyboardController.current
////
////    val cursorAlpha by infiniteTransition.animateFloat(
////        initialValue = 1f,
////        targetValue = 0f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(800),
////            repeatMode = RepeatMode.Reverse
////        )
////    )
////
////    LaunchedEffect(Unit) {
////        focusRequester.requestFocus()
////    }
////    LaunchedEffect(terminalManager.terminalLines.size) {
////        if (!terminalManager.isScrollLocked) {
////            scrollState.animateScrollTo(scrollState.maxValue)
////        }
////    }
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.Black)
////            .padding(16.dp)
////            .pointerInput(Unit) {
////                detectTapGestures(onTap = {
////                    println("点击")
////                    focusRequester.requestFocus()
////                    keyboardController?.show()
////                })
////            }
////    ) {
////        Column(
////            modifier = Modifier
////                .fillMaxSize()
////                .verticalScroll(scrollState)
////        ) {
////            SelectionContainer(
////                modifier = Modifier
////                    .weight(1f)
////                    .navigationBarsPadding()
////                    .imePadding()
////            ) {
////                Column() {
////                    // 显示历史记录
////                    terminalManager.terminalLines.forEach { line ->
////                        Text(
////                            text = parseAnsiColors(line), // 解析 ANSI 颜色
////                            style = TextStyle(
////                                fontSize = 14.sp,
////                                color = Color(0xFF33FF33),
////                                fontFamily = FontFamily.Monospace
////                            ),
////                            modifier = Modifier.padding(bottom = 4.dp)
////                        )
////                    }
////
////
////
//////                    val inputText = "${terminalManager.pathText} \$ ${terminalManager.currentInputText}"
////
//////                    val annotatedString = buildAnnotatedString {
//////                        append(inputText) // 添加正常文本
//////                        // 添加光标并控制其透明度
//////                        withStyle(style = SpanStyle(
//////                            fontSize = 14.sp,
//////                            color = Color(0xFF33FF33).copy(alpha = cursorAlpha),  // 修改光标颜色的透明度
//////                            fontFamily = FontFamily.Monospace
//////                        )) {
//////                            append("|") // 这里的 | 表示光标
//////                        }
//////                    }
////// 当前输入行 + 闪烁光标
////
//////                        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
//////                    Box {
//////                        Text(
//////                            text = inputText,
//////                            style = TextStyle(
//////                                fontSize = 14.sp,
//////                                color = Color(0xFF33FF33),
//////                                fontFamily = FontFamily.Monospace
//////                            ),
//////                            onTextLayout = { layoutResult ->
//////                                textLayoutResult = layoutResult
//////                            }
//////                        )
//////                        if (textLayoutResult != null) {
//////                            val lineCount = textLayoutResult?.lineCount ?: 0
//////                            val lastLineWidth = textLayoutResult?.getLineRight(lineCount - 1) ?: 0f
//////                            val lastLineTop = textLayoutResult?.getLineTop(lineCount - 1) ?: 0f
//////                            val lastLineBottom = textLayoutResult?.getLineBottom(lineCount - 1) ?: 0f
//////                            Box(
//////                                Modifier
//////                                    .offset(
//////                                        x = with(LocalDensity.current) { lastLineWidth.toDp() },
//////                                        y = with(LocalDensity.current) { lastLineTop.toDp() }
//////                                    )
//////                                    .size(8.dp, 18.dp)
//////                                    .height(
//////                                        with(LocalDensity.current) {
//////                                            (lastLineBottom - lastLineTop).toDp()
//////                                        }
//////                                    )
//////                                    .alpha(cursorAlpha)
//////                                    .background(Color(0xFF33FF33))
//////                            )
//////                        }
//////                    }
////
////                    if (terminalManager.isAwaitingInput) {
////                        val inputText = "${terminalManager.pathText} \$ ${terminalManager.currentInputText}"
////
////                        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
////                        Box {
////                            Text(
////                                text = inputText,
////                                style = TextStyle(
////                                    fontSize = 14.sp,
////                                    color = Color(0xFF33FF33),
////                                    fontFamily = FontFamily.Monospace
////                                ),
////                                onTextLayout = { layoutResult ->
////                                    textLayoutResult = layoutResult
////                                }
////                            )
////                            if (textLayoutResult != null) {
////                                val lineCount = textLayoutResult?.lineCount ?: 0
////                                val lastLineWidth = textLayoutResult?.getLineRight(lineCount - 1) ?: 0f
////                                val lastLineTop = textLayoutResult?.getLineTop(lineCount - 1) ?: 0f
////                                val lastLineBottom = textLayoutResult?.getLineBottom(lineCount - 1) ?: 0f
////                                Box(
////                                    Modifier
////                                        .offset(
////                                            x = with(LocalDensity.current) { lastLineWidth.toDp() },
////                                            y = with(LocalDensity.current) { lastLineTop.toDp() }
////                                        )
////                                        .width(5.dp)
////                                        .height(
////                                            with(LocalDensity.current) {
////                                                (lastLineBottom - lastLineTop).toDp()
////                                            }
////                                        )
////                                        .alpha(cursorAlpha)
////                                        .background(Color(0xFF33FF33))
////                                )
////                            }
////                        }
////                    }
////
////                }
////            }
////
////            // 隐藏的输入框
////            BasicTextField(
////                value = terminalManager.currentInputText,
////                onValueChange = { terminalManager.currentInputText = it },
////                textStyle = TextStyle(
////                    fontSize = 14.sp,
////                    color = Color.Transparent,
////                    fontFamily = FontFamily.Monospace
////                ),
////                singleLine = true,
////                keyboardOptions = KeyboardOptions(
////                    imeAction = ImeAction.Done,
////                    keyboardType = KeyboardType.Ascii
////                ),
////                keyboardActions = KeyboardActions(
////                    onDone = {
////                        terminalManager.sendCommand()
////                    }
////                ),
////                modifier = Modifier
////                    .focusRequester(focusRequester)
////
////                    .graphicsLayer(alpha = 0f), // 使用 graphicsLayer 而不是 alpha
////                cursorBrush = SolidColor(Color.Transparent)
////            )
////        }
////    }
////}
//
////private fun filterTerminalOutput(output: String): String {
////    return output
////        .replace("\u001B\\[[;\\d]*[mK]".toRegex(), "") // 移除ANSI控制字符
////        .replace("\r", "") // 移除回车符
////        .trim() // 移除首尾空白
////        .takeIf { it.isNotEmpty() } // 只保留非空内容
////        ?: ""
////}
////class TerminalManager(context: Context) {
////    private val termuxTerminal = TermuxAidlTerminal(context) { output, _ ->
////
////        val filteredOutput = filterTerminalOutput(output)
////        if (filteredOutput.isNotEmpty()) {
////            _terminalLines.add(filteredOutput)
////        }
////    }
////
////    private val _terminalLines = mutableStateListOf<String>()
////    val terminalLines: List<String> = _terminalLines
////
////    var currentInputText by mutableStateOf("")
////
////    init {
////        termuxTerminal.connect()
////    }
////
////    fun sendCommand() {
////        val input = currentInputText
////        _terminalLines.add("$ $input") // 显示用户输入
////        termuxTerminal.sendInput(input)
////        currentInputText = ""
////    }
////
////    fun disconnect() {
////        termuxTerminal.disconnect()
////    }
////}
////
////@Composable
////fun RealTerminal(terminalManager: TerminalManager) {
////    val scrollState = rememberScrollState()
////    val focusRequester = remember { FocusRequester() }
////
////
////    val infiniteTransition = rememberInfiniteTransition()
////    val cursorAlpha by infiniteTransition.animateFloat(
////        initialValue = 1f,
////        targetValue = 0f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(800),
////            repeatMode = RepeatMode.Reverse
////        )
////    )
////
////    LaunchedEffect(Unit) {
////        focusRequester.requestFocus()
////    }
////    LaunchedEffect(terminalManager.terminalLines.size) {
////        scrollState.animateScrollTo(scrollState.maxValue)
////    }
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.Black)
////            .padding(16.dp)
////            .clickable { focusRequester.requestFocus() }
////    ) {
////        Column(
////            modifier = Modifier
////                .fillMaxSize()
////                .verticalScroll(scrollState)
////        ) {
////            SelectionContainer(
////                modifier = Modifier
////                    .weight(1f)
////                    .navigationBarsPadding()
////                    .imePadding()
////            ) {
////                Column {
////                    // 显示历史记录
////                    terminalManager.terminalLines.forEach { line ->
////                        Text(
////                            text = line,
////                            style = TextStyle(
////                                fontSize = 14.sp,
////                                color = Color(0xFF33FF33),
////                                fontFamily = FontFamily.Monospace
////                            ),
////                            modifier = Modifier.padding(bottom = 4.dp)
////                        )
////                    }
////
////                    // 当前输入行 + 闪烁光标
////                    Row(verticalAlignment = Alignment.CenterVertically) {
////                        Text(
////                            text = "$ ${terminalManager.currentInputText}",
////                            style = TextStyle(
////                                fontSize = 14.sp,
////                                color = Color(0xFF33FF33),
////                                fontFamily = FontFamily.Monospace
////                            )
////                        )
////
////                        Box(
////                            modifier = Modifier
////                                .size(8.dp, 18.dp)
////                                .alpha(cursorAlpha)
////                                .background(Color(0xFF33FF33))
////                        )
////                    }
////                }
////            }
////
////            // 隐藏的输入框
////            BasicTextField(
////                value = terminalManager.currentInputText,
////                onValueChange = { terminalManager.currentInputText = it },
////                textStyle = TextStyle(
////                    fontSize = 14.sp,
////                    color = Color.Transparent,
////                    fontFamily = FontFamily.Monospace
////                ),
////                singleLine = true,
////                keyboardOptions = KeyboardOptions(
////                    imeAction = ImeAction.Done,
////                    keyboardType = KeyboardType.Ascii
////                ),
////                keyboardActions = KeyboardActions(
////                    onDone = {
////                        terminalManager.sendCommand()
////                    }
////                ),
////                modifier = Modifier
////                    .focusRequester(focusRequester)
////                    .alpha(0f),
////                cursorBrush = SolidColor(Color.Transparent)
////            )
////        }
////    }
////}
//
//
//
//
//
//
//
//
////@Composable
////fun RealTerminal() {
////    val terminalLines = remember { mutableStateListOf<String>() }
////    var currentInputText by remember { mutableStateOf("") }
////    val scrollState = rememberScrollState()
////    val focusRequester = remember { FocusRequester() }
////    val clipboardManager: ClipboardManager = LocalClipboardManager.current
////
////    val infiniteTransition = rememberInfiniteTransition()
////    val cursorAlpha by infiniteTransition.animateFloat(
////        initialValue = 1f,
////        targetValue = 0f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(800),
////            repeatMode = RepeatMode.Reverse
////        )
////    )
////
////    LaunchedEffect(Unit) {
////        focusRequester.requestFocus()
////    }
////    LaunchedEffect(terminalLines.size) {
////        scrollState.animateScrollTo(scrollState.maxValue)
////    }
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.Black)
////            .padding(16.dp)
////            .clickable { focusRequester.requestFocus() }
////    ) {
////        Column(
////            modifier = Modifier
////                .fillMaxSize()
////                .verticalScroll(scrollState)
////        ) {
////            SelectionContainer(
////                modifier = Modifier
////                    .weight(1f)
////                    .navigationBarsPadding()
////                    .imePadding()
////            ) {
////                Column {
////                    // 显示历史记录
////                    terminalLines.forEach { line ->
////                        Text(
////                            text = line,
////                            style = TextStyle(
////                                fontSize = 14.sp,
////                                color = Color(0xFF33FF33),
////                                fontFamily = FontFamily.Monospace
////                            ),
////                            modifier = Modifier.padding(bottom = 4.dp)
////                        )
////                    }
////
////                    // 当前输入行 + 闪烁光标
////                    Row(verticalAlignment = Alignment.CenterVertically) {
////                        Text(
////                            text = "$ $currentInputText",
////                            style = TextStyle(
////                                fontSize = 14.sp,
////                                color = Color(0xFF33FF33),
////                                fontFamily = FontFamily.Monospace
////                            )
////                        )
////
////                        Box(
////                            modifier = Modifier
////                                .size(8.dp, 18.dp) // 18sp高度，和字体高度差不多
////                                .alpha(cursorAlpha)
////                                .background(Color(0xFF33FF33))
////                        )
////                    }
////                }
////            }
////
////            // 隐藏的输入框
////            BasicTextField(
////                value = currentInputText,
////                onValueChange = { currentInputText = it },
////                textStyle = TextStyle(
////                    fontSize = 14.sp,
////                    color = Color.Transparent,
////                    fontFamily = FontFamily.Monospace
////                ),
////                singleLine = true,
////                keyboardOptions = KeyboardOptions(
////                    imeAction = ImeAction.Done,
////                    keyboardType = KeyboardType.Ascii
////                ),
////                keyboardActions = KeyboardActions(
////                    onDone = {
////                        val inputText = currentInputText
////
////                        terminalLines.add("$ $inputText")
////
////                        if (inputText == "n") {
////                            terminalLines.add("hello")
////                        } else if (inputText == "cut") {
////                            if (terminalLines.isNotEmpty()) {
////                                clipboardManager.setText(AnnotatedString(terminalLines.last()))
////                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
////                                    terminalLines.removeLast()
////                                }
////                            }
////                        }
////
////                        currentInputText = ""
////                    }
////                ),
////                modifier = Modifier
////                    .focusRequester(focusRequester)
////                    .alpha(0f),
////                cursorBrush = SolidColor(Color.Transparent)
////            )
////        }
////    }
////}
//
//
//
//
//@Composable
//fun TerminalScreen(
//    modifier: Modifier = Modifier,
//    backgroundColor: Color = Color.Black,
//    textColor: Color = Color.White,
//    promptColor: Color = Color.Green,
//    cursorColor: Color = Color.White,
//    fontFamily: FontFamily = FontFamily.Monospace,
//    fontSize: Int = 14
//) {
//    val context = LocalContext.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val scrollState = rememberLazyListState()
//    val scope = rememberCoroutineScope()
//    val systemTextToolbar = LocalTextToolbar.current
//
//    data class TerminalLine(
//        val id: Int,
//        val prompt: String,
//        val input: String = "",
//        val output: String = "",
//        val isActive: Boolean = false,
//        val focusRequester: FocusRequester = FocusRequester()
//    )
//
//    var terminalLines by remember {
//        mutableStateOf(
//            listOf(
//                TerminalLine(id = 0, prompt = "$ ", isActive = true)
//            )
//        )
//    }
//    var currentInput by remember { mutableStateOf("") }
//
//    val terminal = remember {
//        TermuxAidlTerminal(context) { output, isError ->
//            terminalLines = terminalLines.mapIndexed { index, line ->
//                if (index == terminalLines.size - 1) {
//                    line.copy(output = line.output + output)
//                } else line
//            }
//        }
//    }
//
//    // 初始化终端连接
//    LaunchedEffect(Unit) {
//        terminal.connect()
//        delay(100)
//        terminalLines.last().focusRequester.requestFocus()
//    }
//
//    // 自动滚动和焦点管理
//    LaunchedEffect(terminalLines.size) {
//        if (terminalLines.isNotEmpty()) {
//            scrollState.animateScrollToItem(terminalLines.size - 1)
//            // 等待布局更新完成后请求焦点
//            delay(50)
//            terminalLines.last().takeIf { it.isActive }?.focusRequester?.requestFocus()
//        }
//    }
//
//    // 清理资源
//    DisposableEffect(Unit) {
//        onDispose {
//            terminal.disconnect()
//        }
//    }
//
//    fun submitCommand(command: String) {
//        if (command.isNotBlank()) {
//            terminal.sendInput(command)
//
//            // 保持键盘显示状态
////            keyboardController?.show()
//
//            // 更新终端行并创建新行
//            val newLines = terminalLines.map { line ->
//                if (line.isActive) line.copy(
//                    input = command,
//                    isActive = false
//                ) else line
//            } + TerminalLine(
//                id = terminalLines.size,
//                prompt = "$ ",
//                isActive = true
//            )
//
//            terminalLines = newLines
//            currentInput = ""
//        }
//    }
//
//    // 点击空白区域的处理
//    val clickModifier = Modifier.pointerInput(Unit) {
//        detectTapGestures(
//            onPress = {
//                systemTextToolbar.hide()
//                terminalLines.last().takeIf { it.isActive }?.focusRequester?.requestFocus()
//            }
//        )
//    }
//
//    // 自定义文本工具栏代理
//    val proxyTextToolbar = remember {
//        object : TextToolbar {
//            override fun showMenu(
//                rect: androidx.compose.ui.geometry.Rect,
//                onCopyRequested: (() -> Unit)?,
//                onPasteRequested: (() -> Unit)?,
//                onCutRequested: (() -> Unit)?,
//                onSelectAllRequested: (() -> Unit)?
//            ) {
//                systemTextToolbar.showMenu(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
//            }
//
//            override fun hide() = systemTextToolbar.hide()
//            override val status get() = systemTextToolbar.status
//        }
//    }
//
//    CompositionLocalProvider(LocalTextToolbar provides proxyTextToolbar) {
//        Column(
//            modifier = modifier
//                .background(backgroundColor)
//                .padding(8.dp)
//                .fillMaxSize()
//                .then(clickModifier),
//            verticalArrangement = Arrangement.Top
//        ) {
//            SelectionContainer(
//                modifier = Modifier
//                    .weight(1f)
//                    .navigationBarsPadding()
//                    .imePadding()
//            ) {
//                LazyColumn(
//                    modifier = Modifier.fillMaxWidth(),
//                    state = scrollState,
//                    contentPadding = PaddingValues(bottom = 56.dp)
//                ) {
//                    itemsIndexed(terminalLines) { index, line ->
//                        Column {
//                            if (line.output.isNotEmpty()) {
//                                Text(
//                                    text = line.output,
//                                    color = textColor,
//                                    fontFamily = fontFamily,
//                                    fontSize = fontSize.sp,
//                                    modifier = Modifier.padding(vertical = 2.dp)
//                                )
//                            }
//
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.padding(vertical = 2.dp)
//                            ) {
//                                Text(
//                                    text = line.prompt,
//                                    color = promptColor,
//                                    fontFamily = fontFamily,
//                                    fontSize = fontSize.sp
//                                )
//
//                                if (line.isActive) {
//                                    BasicTextField(
//                                        value = currentInput,
//                                        onValueChange = { currentInput = it },
//                                        textStyle = TextStyle(
//                                            color = textColor,
//                                            fontFamily = fontFamily,
//                                            fontSize = fontSize.sp
//                                        ),
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .focusRequester(line.focusRequester)
//                                            .onFocusChanged {
//                                                if (it.isFocused) {
////                                                    keyboardController?.show()
//                                                    scope.launch {
//                                                        scrollState.animateScrollToItem(index)
//                                                    }
//                                                }
//                                            }
//                                            .onKeyEvent { event ->
//                                                if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
//                                                    submitCommand(currentInput)
//                                                    true
//                                                } else false
//                                            },
//                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                                        keyboardActions = KeyboardActions(
//                                            onDone = { submitCommand(currentInput) }
//                                        ),
//                                        cursorBrush = SolidColor(cursorColor),
//                                        decorationBox = { innerTextField ->
//                                            Box(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                contentAlignment = Alignment.CenterStart
//                                            ) {
//                                                innerTextField()
//                                            }
//                                        }
//                                    )
//                                } else {
//                                    Text(
//                                        text = line.input,
//                                        color = textColor,
//                                        fontFamily = fontFamily,
//                                        fontSize = fontSize.sp
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}