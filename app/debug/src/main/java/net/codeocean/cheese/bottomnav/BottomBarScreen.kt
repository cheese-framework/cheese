package net.codeocean.cheese.bottomnav

import androidx.annotation.DrawableRes
import net.codeocean.cheese.R


sealed class BottomBarScreen(
    val route:String,
    val title:String,
    @DrawableRes val icon: Int,
    @DrawableRes val icon_focused: Int,
    val badgeCount: Int = 0 // 添加默认值为0的badgeCount
) {


    object Home: BottomBarScreen(
        route = "home",
        title = "主页",
        icon = R.drawable.home,
        icon_focused = R.drawable.home,
        badgeCount = 0
    )

    object Debug: BottomBarScreen(
        route = "debug",
        title = "日志",
        icon = R.drawable.debug,
        icon_focused = R.drawable.debug,
        badgeCount = 0
    )

    object Project: BottomBarScreen(
        route = "project",
        title = "项目",
        icon = R.drawable.project,
        icon_focused = R.drawable.project,
        badgeCount = 0
    )

    object Settings: BottomBarScreen(
        route = "settings",
        title = "设置",
        icon = R.drawable.permissions,
        icon_focused = R.drawable.permissions,
        badgeCount = 0
    )


}