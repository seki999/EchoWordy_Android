package com.seki999.echowordy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seki999.echowordy.EchoWordyApplication
import com.seki999.echowordy.ui.ViewModelFactory
import com.seki999.echowordy.ui.edit.PasteCardsScreen
import com.seki999.echowordy.ui.edit.PasteCardsViewModel
import com.seki999.echowordy.ui.home.HomeScreen
import com.seki999.echowordy.ui.home.HomeViewModel
import com.seki999.echowordy.ui.listdetail.ListDetailScreen
import com.seki999.echowordy.ui.listdetail.ListDetailViewModel
import com.seki999.echowordy.ui.review.ReviewScreen
import com.seki999.echowordy.ui.review.ReviewViewModel
import com.seki999.echowordy.ui.settings.SettingsScreen
import com.seki999.echowordy.ui.settings.SettingsViewModel

@Composable
fun EchoWordyNavHost() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as EchoWordyApplication

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(factory = ViewModelFactory(app))
            HomeScreen(
                viewModel = viewModel,
                onOpenList = { listId -> navController.navigate(Routes.listDetail(listId)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.LIST_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_LIST_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(Routes.ARG_LIST_ID) ?: -1L
            val viewModel: ListDetailViewModel = viewModel(
                key = "list_detail_$listId",
                factory = ViewModelFactory(app, listId),
            )
            ListDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditCards = { navController.navigate(Routes.pasteCards(listId)) },
                onStartReview = { navController.navigate(Routes.review(listId)) },
                onDeleted = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }

        composable(
            route = Routes.PASTE_CARDS,
            arguments = listOf(navArgument(Routes.ARG_LIST_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(Routes.ARG_LIST_ID) ?: -1L
            val viewModel: PasteCardsViewModel = viewModel(
                key = "paste_cards_$listId",
                factory = ViewModelFactory(app, listId),
            )
            PasteCardsScreen(
                viewModel = viewModel,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.REVIEW,
            arguments = listOf(navArgument(Routes.ARG_LIST_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(Routes.ARG_LIST_ID) ?: -1L
            val viewModel: ReviewViewModel = viewModel(
                key = "review_$listId",
                factory = ViewModelFactory(app, listId),
            )
            ReviewScreen(
                viewModel = viewModel,
                onBackToLists = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onOpenUnknownList = { unknownListId ->
                    navController.navigate(Routes.listDetail(unknownListId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory(app))
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
