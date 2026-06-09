package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.screens.ItemListScreen
import com.example.app.ui.screens.CreateItemScreen
import com.example.app.ui.screens.ItemDetailScreen
import com.example.app.ui.screens.ScannerScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument


object Routes {
    const val LIST = "list"
    const val CREATE = "create"
    const val DETAIL = "detail"

    const val SCANNER = "scanner"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "${Routes.LIST}?message="
    ) {
        composable(
            route = "${Routes.LIST}?message={message}",
            arguments = listOf(
                navArgument("message") {
                    type = NavType.StringType
                    defaultValue = ""
                    }
                )
            ) { backStackEntry ->
            val message =
                backStackEntry.arguments
                    ?.getString("message")
                    ?: ""

            ItemListScreen(
                message = message,
                onCreateClick = {
                    navController.navigate(Routes.CREATE)
                },
                onScanClick = {
                    navController.navigate(Routes.SCANNER)
                },
                onItemClick = { itemId ->
                    navController.navigate("${Routes.DETAIL}/$itemId")
                }
            )
        }

        composable(
            route = "${Routes.CREATE}?ean={ean}",
            arguments = listOf(
                navArgument("ean") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val ean =
                backStackEntry.arguments
                    ?.getString("ean")
                    ?: ""

            println("NAV EAN: $ean")

            CreateItemScreen(
                initialEan = ean,

                onBack = {
                    navController.popBackStack()
                },

                onSuccess = {

                    navController.navigate("${Routes.LIST}?message=create") {

                        popUpTo(Routes.LIST) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
        composable("${Routes.DETAIL}/{itemId}") { backStackEntry ->

            val itemId =
                backStackEntry.arguments?.getString("itemId")?.toInt() ?: 0
            ItemDetailScreen(

                itemId = itemId,

                onBack = {
                    navController.popBackStack()
                },

                onUpdateSuccess = {

                    navController.navigate(
                        "${Routes.LIST}?message=update"
                    ) {

                        popUpTo(Routes.LIST) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },

                onDeleteSuccess = {

                    navController.navigate(
                        "${Routes.LIST}?message=delete"
                    ) {

                        popUpTo(Routes.LIST) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.SCANNER) {
            ScannerScreen(
                onItemFound = { itemId ->

                    navController.navigate(
                        "${Routes.DETAIL}/$itemId"
                    )
                },
                onCreateItem = { ean ->
                    navController.navigate("${Routes.CREATE}?ean=$ean")
                }
            )
        }
    }
}