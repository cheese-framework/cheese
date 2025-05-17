package net.codeocean.cheese.bottomnav

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.codeocean.cheese.screen.CodeDetailScreen
import net.codeocean.cheese.screen.DebugScreen
import net.codeocean.cheese.screen.EditorScreen
import net.codeocean.cheese.screen.HomeScreen

import net.codeocean.cheese.screen.SettingsScreen


@ExperimentalMaterial3Api
@Composable
fun BottomNavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = BottomBarScreen.Home.route) {
        composable(route = BottomBarScreen.Home.route)
        {
            HomeScreen()
        }
        composable(route = BottomBarScreen.Debug.route)
        {
            DebugScreen()
        }
        composable(route = BottomBarScreen.Project.route)
        {


            EditorScreen(navController)
        }
        composable(route = BottomBarScreen.Settings.route)
        {
            SettingsScreen()
        }

        composable(
            route = "codeDetail/{exampleId}/{name}/{path}",
            arguments = listOf(
                navArgument("exampleId") {
                    type = NavType.IntType
                },
                navArgument("name") {
                    type = NavType.StringType
                    // 可选：设置默认值
                    nullable = true
                    defaultValue = null
                },
                navArgument("path") {
                    type = NavType.StringType
                    // 可选：设置默认值
                    nullable = true
                    defaultValue = null
                }

            )
        ) { backStackEntry ->
            val exampleId = backStackEntry.arguments?.getInt("exampleId") ?: 0
            val name = backStackEntry.arguments?.getString("name")
            val path = backStackEntry.arguments?.getString("path")
            CodeDetailScreen(
                exampleId = exampleId,
                name = name.toString(),  // 传递给屏幕
                path = path.toString(),  // 传递给屏幕
                navController = navController
            )
        }


//        composable(
//            route = "codeDetail/{exampleId}",
//            arguments = listOf(
//                navArgument("exampleId") {
//                    type = NavType.IntType
//                }
//            )
//        ) { backStackEntry ->
//            val exampleId = backStackEntry.arguments?.getInt("exampleId") ?: 0
//            CodeDetailScreen(
//                exampleId = exampleId,
//                navController = navController
//            )
//        }

    }


}