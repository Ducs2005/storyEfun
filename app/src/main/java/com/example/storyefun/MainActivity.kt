package com.example.storyefun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue

import androidx.navigation.compose.*
import com.example.storyefun.navigation.AppNavigation
//import com.example.storyefun.ui.screens.AudioBooksContent
import com.example.storyefun.ui.screens.HomeBookScreen
import com.example.storyefun.ui.screens.PostScreen
import com.example.storyefun.ui.theme.AppTheme
import com.example.storyefun.viewModel.PostViewModel
import com.example.storyefun.viewModel.ThemeViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            val navController = rememberNavController()
            val postViewModel: PostViewModel = PostViewModel()
            val themeViewModel: ThemeViewModel = viewModel()  // Tạo ViewModel
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

//            // Wrap the entire app in AppTheme with the latest isDarkTheme
            AppTheme(darkTheme = isDarkTheme) {
                AppNavigation(navController, themeViewModel, postViewModel)
            }
        }
    }
}


