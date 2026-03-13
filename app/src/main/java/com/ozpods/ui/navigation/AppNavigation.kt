package com.ozpods.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ozpods.ui.screens.DeviceDetailScreen
import com.ozpods.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object DeviceDetail : Screen("device/{address}") {
        fun createRoute(address: String) = "device/$address"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onDeviceClick = { address ->
                    navController.navigate(Screen.DeviceDetail.createRoute(address))
                }
            )
        }
        composable(
            route = Screen.DeviceDetail.route,
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: return@composable
            DeviceDetailScreen(
                address = address,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
