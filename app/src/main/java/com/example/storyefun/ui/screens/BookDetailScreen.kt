package com.example.storyefun.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.profileui.StatItem
import com.example.storyefun.R
import com.example.storyefun.data.models.Book
import com.example.storyefun.data.models.Chapter
import com.example.storyefun.ui.components.CommentSection
import com.example.storyefun.ui.components.Header
import com.example.storyefun.ui.theme.AppColors
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.BookViewModel
import com.example.storyefun.viewModel.ThemeViewModel
import com.example.storyefun.viewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String,
    themeViewModel: ThemeViewModel,
    viewModel: BookViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    var theme = LocalAppColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
    val book by viewModel.book.observeAsState()

    LaunchedEffect(bookId) {
        viewModel.fetchBook(bookId)
        Log.e("info of launched book: ", book.toString())
    }

    Scaffold(
        topBar = {
            Header(
                navController = navController,
                themeViewModel = themeViewModel
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isDarkMode) {
                // Add light mode background if needed
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(theme.backgroundColor)
                )
            }

            if (book != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { MangaInfo(theme, book!!, navController, userViewModel, viewModel) }

                    item {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = theme.backgroundContrast2
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = { Text("Thông tin", color = theme.backgroundColor) }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = { Text("Chapter", color = theme.backgroundColor) }
                            )
                        }
                    }

                    item {
                        when (selectedTabIndex) {
                            0 -> InformationSection(navController, theme, book!!)
                            1 -> ChapterListSection(theme, book!!, navController, viewModel)
                        }
                        CommentSection(theme, book!!.id, book!!.comments)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = theme.textPrimary)
                }
            }
        }
    }
}

@Composable
fun MangaInfo(theme: AppColors, book: Book, navController: NavController, userViewModel: UserViewModel, bookViewModel: BookViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLiked by remember { mutableStateOf(false) }
    var isFollowed by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var selectedVolumeOrder by remember { mutableStateOf<Long?>(null) }
    var coinBalance by remember { mutableStateOf<Int?>(null) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(book.id, uid) {
        isLiked = withContext(Dispatchers.IO) { userViewModel.isLikedBook(book.id) }
        isFollowed = withContext(Dispatchers.IO) { userViewModel.isFollowedBook(book.id) }
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    coinBalance = document.getLong("coin")?.toInt() ?: 0
                }
                .addOnFailureListener {
                    coinBalance = 0
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        ) {
            AsyncImage(
                model = book.posterUrl,
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.error),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 40.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = theme.backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = "Poster",
                        contentScale = ContentScale.FillHeight,
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.error),
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = book.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book.author,
                            fontSize = 14.sp,
                            color = theme.textSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem(
                                icon = R.drawable.ic_follows,
                                text = if (isFollowed) "${book.follows}" else "Follow ${book.follows}",
                                color = if (isFollowed) Color.Red else theme.textSecondary
                            ) {
                                scope.launch {
                                    if (isFollowed) {
                                        userViewModel.unfollowingBook(book.id)
                                        book.follows--
                                        isFollowed = false
                                    } else {
                                        userViewModel.followingBook(book.id)
                                        book.follows++
                                        isFollowed = true
                                    }
                                }
                            }

                            StatItem(
                                icon = R.drawable.ic_likes,
                                text = if (isLiked) "${book.likes}" else "Like ${book.likes}",
                                color = if (isLiked) Color.Red else theme.textSecondary
                            ) {
                                scope.launch {
                                    if (isLiked) {
                                        userViewModel.unlikingBook(book.id)
                                        book.likes--
                                        isLiked = false
                                    } else {
                                        userViewModel.likingBook(book.id)
                                        book.likes++
                                        isLiked = true
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val (volumeOrder, chapterOrder) = getFirstChapter(book) ?: (1L to 1L)
                                val firstVolume = book.volume.sortedBy { it.order }.firstOrNull()
                                val firstChapter = firstVolume?.chapters?.sortedBy { it.order }?.firstOrNull()
                                if (firstChapter != null && (bookViewModel.isChapterUnlocked(firstChapter.id) || firstChapter.price == 0)) {
                                    navController.navigate("reading/${book.id}/$volumeOrder/$chapterOrder")
                                } else if (firstChapter != null) {
                                    selectedChapter = firstChapter
                                    selectedVolumeOrder = volumeOrder
                                    showUnlockDialog = true
                                } else {
                                    Toast.makeText(context, "Không có chương nào để đọc", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Bắt đầu đọc",
                                fontSize = 16.sp,
                                color = theme.textPrimary
                            )
                        }
                    }
                }
            }
        }

        if (showUnlockDialog && selectedChapter != null && selectedVolumeOrder != null && uid != null) {
            AlertDialog(

                onDismissRequest = { showUnlockDialog = false },
                modifier = Modifier.background(theme.backgroundContrast2, RoundedCornerShape(12.dp)),
                title = {
                    Text(
                        "Mở khóa chương",
                        color = theme.backgroundColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        "Bạn có $coinBalance coin. Mở khóa chương này với ${selectedChapter?.price} coin?",
                        color = theme.backgroundColor,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val chapter = selectedChapter ?: return@TextButton
                            val volumeOrder = selectedVolumeOrder ?: return@TextButton
                            val userDocRef = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)

                            if (coinBalance != null && coinBalance!! >= (selectedChapter?.price ?: 0)) {
                                val newCoin = coinBalance!! - (selectedChapter?.price?.toInt() ?: 0)

                                userDocRef.update(
                                    mapOf(
                                        "coin" to newCoin,
                                        "unlockedChapterIds" to FieldValue.arrayUnion(chapter.id)
                                    )
                                ).addOnSuccessListener {
                                    coinBalance = newCoin
                                    Toast.makeText(context, "Chương đã được mở khóa", Toast.LENGTH_LONG).show()
                                    navController.navigate("reading/${book.id}/${volumeOrder}/${chapter.order}")
                                    showUnlockDialog = false
                                    bookViewModel.refreshUnlockedChapterIds()
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi mở khóa chương: ${e.message}")
                                    Toast.makeText(context, "Không thể mở khóa chương", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                showUnlockDialog = false
                                navController.navigate("desposite")
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.backgroundColor)
                    ) {
                        Text("Mở khóa", fontWeight = FontWeight.Medium)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUnlockDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.backgroundColor)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    }
}

/**
 * Retrieves the order of the first chapter of the first volume of the book.
 * @param book The book to extract the first chapter from.
 * @return A Pair of (volumeOrder, chapterOrder) or null if no volumes/chapters exist.
 */
private fun getFirstChapter(book: Book): Pair<Long, Long>? {
    // Sort volumes by order to get the first volume
    val firstVolume = book.volume.sortedBy { it.order }.firstOrNull() ?: return null
    // Sort chapters by order to get the first chapter
    val firstChapter = firstVolume.chapters.sortedBy { it.order }.firstOrNull() ?: return null
    return firstVolume.order to firstChapter.order
}

@Composable
fun GenreTag(text: String, theme: AppColors) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Gray.copy(alpha = 0.2f),
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = theme.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InformationSection(navController: NavController, theme: AppColors, book: Book) {
    val genres = book.category

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Thể loại",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.forEach { genre ->
                GenreTag(text = genre.name, theme = theme)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Giới thiệu",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var expanded by remember { mutableStateOf(false) }
        val maxLines = if (expanded) Int.MAX_VALUE else 3

        Text(
            text = book.description,
            fontSize = 14.sp,
            color = theme.textSecondary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        if (book.description.length > 100) {
            Text(
                text = if (expanded) "Thu gọn" else "Xem thêm",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ChapterListSection(theme: AppColors, book: Book, navController: NavController, viewModel: BookViewModel) {
    var showUnlockDialog by remember { mutableStateOf(false) }
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var selectedVolumeOrder by remember { mutableStateOf<Long?>(null) }
    var coinBalance by remember { mutableStateOf<Int?>(null) }

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    coinBalance = document.getLong("coin")?.toInt() ?: 0
                }
                .addOnFailureListener {
                    coinBalance = 0
                }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Volumes (${book.volume.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )

        Text(
            text = "Cập nhật mới nhất 29/02/2024",
            fontSize = 14.sp,
            color = theme.textSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        book.volume.forEach { volume ->
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = theme.backgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tập ${volume.title}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    volume.chapters.forEach { chapter ->
                        ChapterItem(
                            bookId = book.id,
                            volumeOrder = volume.order,
                            chapter = chapter,
                            viewModel = viewModel,
                            navController = navController,
                            onUnlockRequest = { chap, volOrder ->
                                selectedChapter = chap
                                selectedVolumeOrder = volOrder
                                showUnlockDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showUnlockDialog && selectedChapter != null && selectedVolumeOrder != null && uid != null) {
            AlertDialog(
                onDismissRequest = { showUnlockDialog = false },
                modifier = Modifier.background(theme.backgroundColor, RoundedCornerShape(12.dp)),
                title = {
                    Text(
                        "Mở khóa chương",
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        "Bạn có $coinBalance coin. Mở khóa chương này với ${selectedChapter?.price} coin?",
                        color = theme.textSecondary,
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val chapter = selectedChapter ?: return@TextButton
                            val volumeOrder = selectedVolumeOrder ?: return@TextButton
                            val userDocRef = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)

                            if (coinBalance != null && coinBalance!! >= (selectedChapter?.price ?: 0)) {
                                val newCoin = coinBalance!! - (selectedChapter?.price?.toInt() ?: 0)

                                userDocRef.update(
                                    mapOf(
                                        "coin" to newCoin,
                                        "unlockedChapterIds" to FieldValue.arrayUnion(chapter.id)
                                    )
                                ).addOnSuccessListener {
                                    coinBalance = newCoin
                                    Toast.makeText(context, "Chương đã được mở khóa", Toast.LENGTH_LONG).show()
                                    navController.navigate("reading/${book.id}/${volumeOrder}/${chapter.order}")
                                    showUnlockDialog = false
                                    viewModel.refreshUnlockedChapterIds()
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi mở khóa chương: ${e.message}")
                                    Toast.makeText(context, "Không thể mở khóa chương", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                showUnlockDialog = false
                                navController.navigate("desposite")
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.textPrimary)
                    ) {
                        Text("Mở khóa", fontWeight = FontWeight.Medium)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUnlockDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.textSecondary)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Medium)
                    }
                }
            )
        }
    }
}

@Composable
fun ChapterItem(
    bookId: String,
    chapter: Chapter,
    viewModel: BookViewModel,
    navController: NavController,
    volumeOrder: Long,
    onUnlockRequest: (Chapter, Long) -> Unit
) {
    val theme = LocalAppColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                if (viewModel.isChapterUnlocked(chapter.id) || chapter.price == 0) {
                    navController.navigate("reading/${bookId}/${volumeOrder}/${chapter.order}")
                } else {
                    onUnlockRequest(chapter, volumeOrder)
                }
            },
        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = chapter.title,
                    color = if (viewModel.isChapterUnlocked(chapter.id)) theme.textPrimary else theme.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                if (viewModel.isChapterUnlocked(chapter.id) || chapter.price == 0) {
                    Log.e("unlock", "${chapter.id} la id")
                    Icon(
                        painter = painterResource(id = R.drawable.ic_unlocked),
                        contentDescription = "Coin",
                        modifier = Modifier.size(14.dp),
                        tint = theme.textSecondary
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Coin",
                        modifier = Modifier.size(14.dp),
                        tint = theme.textSecondary
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (chapter.price == 0) "Free" else chapter.price.toString(),
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
                if (chapter.price != 0) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_coin),
                        contentDescription = "Coin",
                        modifier = Modifier.size(12.dp),
                        tint = theme.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: Int,
    text: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .padding(horizontal = 8.dp)
        .then(
            if (onClick != null) Modifier.clickable { onClick() }
            else Modifier
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}