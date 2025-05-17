package net.codeocean.cheese.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hjq.toast.Toaster
import net.codeocean.cheese.backend.impl.PermissionsImpl.ACCESSIBILITY
import net.codeocean.cheese.backend.impl.PermissionsImpl.FLOATING
import net.codeocean.cheese.backend.impl.PermissionsImpl.RECORDSCREEN
import net.codeocean.cheese.core.CoreFactory
import kotlin.concurrent.thread

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val  elevation: Dp = 12.dp
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "常用设置",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "\uD83D\uDC1D",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Text(
                text = "年年岁岁花相似 岁岁年年人不同",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionButton(
                icon = Icons.Default.AddCircleOutline,
                title = "开启悬浮控制台",
                description = "快速访问常用功能的悬浮控制台",
                onClick = {

                    if (!CoreFactory.getPermissions().checkPermission(FLOATING)) {
                        Toaster.show("悬浮窗权限不正常")
                    }else{
                        CoreFactory.getToolWindow().floatingConsole().show()
                    }

                }
            )

            PermissionButton(
                icon = Icons.Default.VolunteerActivism,
                title = "无障碍权限",
                description = "启用辅助功能服务",
                onClick = {
                    thread {
                        if (!CoreFactory.getPermissions().checkPermission(ACCESSIBILITY)) {
                            CoreFactory.getPermissions().requestPermission(ACCESSIBILITY,5)
                        }else{
                            Toaster.show("无障碍权限正常")
                        }

                    }



                }
            )

            PermissionButton(
                icon = Icons.Default.Window,
                title = "悬浮窗权限",
                description = "允许应用显示悬浮窗口",
                onClick = {
                    if (!CoreFactory.getPermissions().checkPermission(FLOATING)) {
                        CoreFactory.getPermissions().requestPermission(FLOATING,5)
                    }else{
                        Toaster.show("悬浮窗权限正常")
                    }

                }
            )

            PermissionButton(
                icon = Icons.Default.Visibility,
                title = "录屏权限",
                description = "允许应用录制屏幕内容",
                onClick = {

                    thread {
                        if (!CoreFactory.getPermissions().checkPermission(RECORDSCREEN)) {
                            CoreFactory.getPermissions().requestPermission(RECORDSCREEN,5)
                        }else{
                            Toaster.show("录屏权限正常")
                        }

                    }

                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionButton(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "前往设置",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}