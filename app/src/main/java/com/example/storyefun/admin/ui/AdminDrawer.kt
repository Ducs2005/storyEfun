package com.example.storyefun.admin.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.storyefun.R
import com.example.storyefun.data.DrawerItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminDrawer(
    navController: NavController,
    drawerState: DrawerState,
    selectedItem: MutableState<String>,
    content: @Composable () -> Unit // Thay đổi để không nhận PaddingValues
) {
    val scope = rememberCoroutineScope()

    // Icons for drawer items
    val bookIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.librarybook)
    val uploadBook: ImageVector = ImageVector.vectorResource(id = R.drawable.upload)
    val categoryIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.category)

    // Drawer items list
    val drawerItems = listOf(
        DrawerItem("Manage Users", "ManageUser", Icons.Default.AccountBox, 3),
        DrawerItem("Manage Books", "ManageBook", bookIcon, 12),
        DrawerItem("Upload Book", "uploadBook", uploadBook),
        DrawerItem("Statistics", "revenueStatistics", Icons.Default.List),
        DrawerItem("Manage Categories", "addCategory", categoryIcon),
        DrawerItem("Settings", "settings", Icons.Default.Settings)
    )

    // Gradient màu cam đẹp
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF7043),  // Cam đậm
            Color(0xFFFFA726)   // Cam nhạt
//                    Color(0xFFFFA500),  // Cam sáng (orange)
//            Color(0xFFFFD700)   // Vàng nhạt (gold)
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(gradient)
                    .padding(vertical = 24.dp)
            ) {
                // Header với avatar và thông tin
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://i.pinimg.com/474x/94/7e/db/947edbbf6ced34863fc8702ed29ef79f.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .shadow(8.dp, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "StoryeFun",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Admin",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Danh sách menu items
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(drawerItems) { item ->
                        val isSelected = selectedItem.value == item.route
                        val elevation = animateDpAsState(if (isSelected) 8.dp else 0.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.2f)
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedItem.value = item.route
                                    scope.launch { drawerState.close() }
                                    navController.navigate(item.route)
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .animateItemPlacement()
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = item.title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            if (item.badgeCount > 0) {
                                Spacer(modifier = Modifier.weight(1f))
                                Badge(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFFFF7043)
                                ) {
                                    Text(item.badgeCount.toString())
                                }
                            }
                        }
                    }
                }

                // Footer
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Handle logout */ }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Logout",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Logout",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) {
        content() // Gọi content mà không truyền paddingValues
    }
}