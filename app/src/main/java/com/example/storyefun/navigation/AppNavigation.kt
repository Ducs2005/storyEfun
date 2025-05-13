package com.example.storyefun.navigation


import UploadScreen
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.profileui.ProfileScreen
import com.example.storyefun.ui.screens.*

import com.example.storyefun.admin.ui.*
import com.example.storyefun.ui.UserManageScreen
import com.example.storyefun.viewModel.PostViewModel


//import com.example.storyefun.ui.AddChapterScreen
import com.example.storyefun.viewModel.ThemeViewModel
import com.google.common.base.Objects
import com.google.firebase.auth.FirebaseAuth


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object BookDetail : Screen("bookDetail/{bookId}")

    object ProductOrder : Screen("productOrder")


    object Login : Screen("login")
    object Upload : Screen("upload")
    object Reading : Screen("reading/{bookId}/{volumeOrder}/{chapterOrder}")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object Favourite : Screen("favourite")
    object Setting : Screen("setting")
    object CategoryList : Screen("category")
    object Search : Screen("search")
    object AllBook : Screen("allbook")


    object AdminMenu : Screen("menuScreen")
    object AdminUpload : Screen("uploadBook")
    object ManageBook : Screen("manageBook")
    //    object AddChapter : Screen("addCChapter/{bookId}")
    object EditBook : Screen("editBook/{bookId}")
    object AddVolume : Screen("addVolume/{bookId}")
    object ListChapter : Screen("listChapter/{bookId}/{volumeId}")
    object AddChapter : Screen("addChapter/{bookId}/{volumeId}")
    object AddCategory : Screen("addCategory")

    object Desposite : Screen("desposite")
    object Coin : Screen("coin")
    object ManageUser : Screen("manageUser")
    object PaymentNotification : Screen("paymentNotification")
    object HistoricalTransaction : Screen("historicalTransaction")
    object RevenueStatistics : Screen("revenueStatistics")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController, themeViewModel: ThemeViewModel, postViewModel: PostViewModel) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val start : String
    if (currentUser != null) {
        start = Screen.AdminMenu.route
    } else {
        // Người dùng chưa đăng nhập, yêu cầu đăng nhập
        start = Screen.Login.route
    }
//    if (currentUser != null) {
//        start = Screen.AdminMenu.route
//    } else {
//        // Người dùng chưa đăng nhập, yêu cầu đăng nhập
//        start = Screen.Login.route
//    }
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
//        startDestination = start
//        startDestination = Screen.Upload.route
    ) {
//        composable(Screen.Home.route) { HomeScreen(navController, themeViewModel) }
        composable(Screen.Home.route) { HomeBookScreen(navController, postViewModel) }
        composable("bookDetail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: "Unknown"
            BookDetailScreen(navController, bookId, themeViewModel)
        }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Reading.route) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: "unknown"
            val volumeOrderStr = backStackEntry.arguments?.getString("volumeOrder") ?: "1"
            val chapterOrderStr = backStackEntry.arguments?.getString("chapterOrder") ?: "1"

            // Parse strings to Int
            val volumeOrder = volumeOrderStr.toLongOrNull() ?: 1
            val chapterOrder = chapterOrderStr.toLongOrNull() ?: 1

            //Log.d("chapter and voluume ", "chapter $chapterOrder  volume $volumeOrder")
            ReaderScreen(navController, bookId, volumeOrder, chapterOrder, themeViewModel)
        }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController, themeViewModel) }
        composable(Screen.Upload.route) { UploadScreen(navController) }
        composable(Screen.Favourite.route) { FavouriteScreen(themeViewModel, navController) }
        composable(Screen.Setting.route) { SettingScreen(navController, themeViewModel) }
        composable(Screen.CategoryList.route) { CategoriesScreen(navController) }
        composable(Screen.Desposite.route) { DespositeScreen() }
        composable(Screen.Coin.route) { CoinScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.AllBook.route) { AllBookScreen(navController) }
        composable(Screen.HistoricalTransaction.route) { HistoricalTransaction(navController) }


        composable(Screen.ManageUser.route) { UserManageScreen(navController) }
        composable(Screen.AdminMenu.route) {MenuScreen(navController)}
        composable(Screen.AdminUpload.route) {AdminUploadScreen(navController)}
        composable(Screen.ManageBook.route) { ManageBooksScreen(navController) }
        composable(Screen.AddCategory.route) { AddCategory(navController, onCategoryAdded = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.AddCategory.route) { inclusive = true }
            }
        }) }

        composable("editBook/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: "Unknown"
            EditBook(navController, bookId)
        }
        composable("addVolume/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: "Unknown"
            AddVolumeScreen(navController, bookId)
        }
        composable("chapter_list_screen/{bookId}/{volumeId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: "Unknown"
            val volumeId = backStackEntry.arguments?.getString("volumeId") ?: "Unknown"
            ListChapterScreen(navController, bookId, volumeId)
        }
        composable("addChapter/{bookId}/{volumeId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: "Unknown"
            val volumeId = backStackEntry.arguments?.getString("volumeId") ?: "Unknown"
            AddChapterScreen(navController, bookId, volumeId)
        }


        composable(Screen.ProductOrder.route) {
            ProductOrderScreen()
        }
        composable(Screen.Desposite.route) {
            DespositeScreen()
        }
        composable(Screen.RevenueStatistics.route) {
            RevenueStatisticsScreen(navController)
        }


    }
}