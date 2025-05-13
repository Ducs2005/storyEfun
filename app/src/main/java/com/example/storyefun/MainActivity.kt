package com.example.storyefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue

import androidx.navigation.compose.*
import com.example.storyefun.navigation.AppNavigation
import com.example.storyefun.ui.theme.AppTheme
import com.example.storyefun.viewModel.ThemeViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
//        setContent {
//            val navController = rememberNavController()
//            val themeViewModel: ThemeViewModel = viewModel()
//            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
//            val isAdmin = themeViewModel.isAdmin.collectAsState(initial = false).value
//            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//
//            AppTheme(darkTheme = isDarkTheme) {
//                Scaffold(
//                    modifier = Modifier.fillMaxSize(),
//                    bottomBar = {
//                        if (!isAdmin) {
//                            BottomBar(
//                                navController = navController,
//                                currentRoute = currentRoute ?: ""
//                            )
//                        }
//                    }
//                ) { innerPadding ->
//                    if (isAdmin) {
//                        AppNavigationAdmin(navController = navController, themeViewModel)
//                    } else {
//                        AppNavigationUser(navController = navController, themeViewModel)
//                    }
//                }
//            }
//        }
        setContent {

            val navController = rememberNavController()
            val themeViewModel: ThemeViewModel = viewModel()  // Tạo ViewModel

            // Apply the theme dynamically based on the collected state

            // Observe the isDarkTheme state
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            // Wrap the entire app in AppTheme with the latest isDarkTheme
            AppTheme(darkTheme = isDarkTheme) {
                AppNavigation(navController, themeViewModel)
            }
        }
    }
}


