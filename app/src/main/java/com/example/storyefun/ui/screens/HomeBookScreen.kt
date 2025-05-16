package com.example.storyefun.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.storyefun.ui.components.BottomBar
import com.example.storyefun.ui.components.Header
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@ExperimentalMaterial3Api
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeBookScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val theme = LocalAppColors.current // Access theme colors

    Scaffold(
        topBar = {
            Header(navController = navController, themeViewModel = themeViewModel)
        },
        bottomBar = { BottomBar(navController, "home", themeViewModel = themeViewModel) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background) // Use theme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { BookTabScreen(navController) }
            }
        }
    }
}

@Composable
fun BookTabScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Books", "Audio books")
    val theme = LocalAppColors.current // Access theme colors

    Column(modifier = Modifier) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = theme.backgroundColor, // Use theme.backgroundColor
            contentColor = theme.textPrimary, // Use theme.textPrimary
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = theme.buttonOrange // Use theme.buttonOrange for indicator
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) theme.textPrimary else theme.textSecondary // Use theme colors
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTabIndex) {
            0 -> {
                NewBookScreen(navController)
                Divider(
                    color = Color.Gray.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 30.dp)
                )
                RecommendedBookScreen(navController)
                Divider(
                    color = Color.Gray.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 30.dp)
                )
                PopularBookScreen(navController)
                Spacer(modifier = Modifier.height(16.dp))
            }
            1 -> PostScreen()
        }
    }
}