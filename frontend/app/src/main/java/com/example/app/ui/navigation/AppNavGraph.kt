package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.ui.screens.CreateItemScreen
import com.example.app.ui.screens.FilterScreen
import com.example.app.ui.screens.ItemDetailScreen
import com.example.app.ui.screens.ItemListScreen
import com.example.app.ui.screens.LoginScreen
import com.example.app.ui.screens.ProfileScreen
import com.example.app.ui.screens.RegisterScreen
import com.example.app.ui.screens.ScannerScreen
import com.example.app.viewmodel.AuthViewModel
import com.example.app.viewmodel.ItemListViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val LIST = "list"
    const val CREATE = "create"
    const val DETAIL = "detail"
    const val SCANNER = "scanner"
    const val FILTERS = "filters"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(LocalContext.current)
    )

    val authState by authViewModel.state.collectAsState()

    val itemListViewModel: ItemListViewModel = viewModel()

    LaunchedEffect(authState.isAuthenticated) {

        if (!authState.isAuthenticated) {

            val currentRoute =
                navController.currentDestination?.route

            if (
                currentRoute != Routes.LOGIN &&
                currentRoute != Routes.REGISTER
            ) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {

            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {

            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

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
                    navController.navigate(Routes.FILTERS)
                },

                onProfileClick = {
                    navController.navigate(Routes.PROFILE) {
                        launchSingleTop = true
                    }
                },

                onItemClick = { itemId ->
                    navController.navigate(
                        "${Routes.DETAIL}/$itemId"
                    )
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                state = authState,
                onInventoryClick = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LIST) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onLogoutClick = authViewModel::logout
            )
        }

        composable(Routes.FILTERS) {

            FilterScreen(
                viewModel = itemListViewModel,
                onBack = {
                    navController.popBackStack()
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

            CreateItemScreen(
                initialEan = ean,

                onBack = {
                    navController.popBackStack()
                },

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
                    ?.toInt()
                    ?: 0

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
