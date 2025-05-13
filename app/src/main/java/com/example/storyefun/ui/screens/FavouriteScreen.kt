package com.example.storyefun.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storyefun.R
import com.example.storyefun.ui.theme.AppColors
import com.example.storyefun.data.models.Book
import com.example.storyefun.ui.components.BottomBar
import com.example.storyefun.ui.theme.AppTheme
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.BookViewModel
import com.example.storyefun.viewModel.ThemeViewModel
import com.example.storyefun.viewModel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    themeViewModel: ThemeViewModel,
    navController: NavController,
    viewModel: BookViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
    val favoriteBooks by viewModel.favoriteBooks.collectAsState()
    val colors = LocalAppColors.current

    AppTheme(darkTheme = isDarkMode) {
        Scaffold(
            topBar = {
//                Header(
//                    text = searchText,
//                    active = active,
//                    onQueryChange = { searchText = it },
//                    onActiveChange = { active = it },
//                    navController = navController
//                )
            },
            bottomBar = {
                BottomBar(navController, "favourite", themeViewModel)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (!isDarkMode) {
                    // Nếu muốn thêm hình nền trong light mode thì thêm lại ở đây
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(colors.backgroundColor)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Lịch sử yêu thích",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(favoriteBooks) { book ->
                        FavoriteBookItem(
                            navController = navController,
                            book = book,
                            theme = colors,
                            onClick = { navController.navigate("bookDetail/${book.id}") },
                            onRemove = { scope.launch{
                                userViewModel.unlikingBook(it.id)
                                viewModel.loadFavorites() // <- cập nhật lại danh sách sau khi xoá

                            }} // hoặc viewModel.removeFavorite(book.id)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteBookItem(
    navController: NavController,
    book: Book,
    theme: AppColors,
    onClick: () -> Unit,
    onRemove: (Book) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
        ,
        verticalAlignment = Alignment.CenterVertically

    ) {
        AsyncImage(
            model = book.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.error),
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))

        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = book.name,
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Cập nhật mới:",
                fontSize = 14.sp,
                color = theme.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = (book.getLatestVolume()?.title ?: "") + " - " + (book.getLatestChapter()?.title ?: ""),
                fontSize = 14.sp,
                color = theme.textPrimary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable {
                        navController.navigate("reading/${book.id}/${book.getLatestVolume()?.order}/${book.getLatestChapter()?.order}")
                    }
            )
        }

        // Nút xóa
        IconButton(
            onClick = { onRemove(book) },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }
}

