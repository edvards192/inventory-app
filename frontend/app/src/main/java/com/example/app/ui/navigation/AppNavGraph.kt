package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.ui.screens.CreateItemScreen
import com.example.app.ui.screens.ItemDetailScreen
import com.example.app.ui.screens.ItemListScreen
import com.example.app.ui.screens.ScannerScreen
import com.example.app.ui.screens.FilterScreen
import com.example.app.viewmodel.ItemListViewModel


object Routes {
    const val LIST = "list"
    const val CREATE = "create"
    const val DETAIL = "detail"
    const val SCANNER = "scanner"
    const val FILTERS = "filters"
}

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val itemListViewModel: ItemListViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {

        composable(Routes.LIST) {

            ItemListScreen(
                viewModel = itemListViewModel,

                onCreateClick = {
                    navController.navigate(Routes.CREATE)
                },
                onScanClick = {
                    navController.navigate(Routes.SCANNER)
                },
                onFilterClick = {
                    navController.navigate(
                        Routes.FILTERS
                    )
                },
                onItemClick = { itemId ->
                    navController.navigate(
                        "${Routes.DETAIL}/$itemId"
                    )
                }
            )
        }
        composable(
            Routes.FILTERS
        ) {
            FilterScreen(
                viewModel = itemListViewModel,
                onBack = { navController.popBackStack() }
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

            CreateItemScreen(
                initialEan = ean,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    itemListViewModel.refresh()
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LIST) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            "${Routes.DETAIL}/{itemId}"
        ) { backStackEntry ->

            val itemId =
                backStackEntry.arguments
                    ?.getString("itemId")
                    ?.toInt() ?: 0

            ItemDetailScreen(
                itemId = itemId,
                onBack = {
                    navController.popBackStack()
                },

                onDeleteSuccess = {
                    itemListViewModel.refresh()
                    navController.navigate(Routes.LIST) {
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
                    navController.navigate(
                        "${Routes.CREATE}?ean=$ean"
                    )
                }
            )
        }
    }
}