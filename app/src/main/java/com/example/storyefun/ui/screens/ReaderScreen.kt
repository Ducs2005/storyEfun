package com.example.storyefun.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.storyefun.R
import com.example.storyefun.data.models.Book
import com.example.storyefun.data.models.Chapter
import com.example.storyefun.ui.theme.AppColors
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.utils.downloadAndExtractDocx
import com.example.storyefun.utils.downloadTextFile
import com.example.storyefun.utils.splitParagraphIntoChunks
import com.example.storyefun.viewModel.BookViewModel
import com.example.storyefun.viewModel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.storyefun.utils.rememberTextToSpeech
import androidx.compose.material.icons.filled.Pause

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    bookId: String,
    volumeOrder: Long,
    chapterOrder: Long,
    themeViewModel: ThemeViewModel,
    viewModel: BookViewModel = viewModel(),
    isPlaying : Boolean = false
) {
    var isUIVisible by remember { mutableStateOf(true) }
    val book by viewModel.book.observeAsState()
    val theme = LocalAppColors.current
    var coinBalance by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var showUnlockDialog by remember { mutableStateOf(false) }
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var selectedVolumeOrder by remember { mutableStateOf<Long?>(null) }
    var showChapterList by remember { mutableStateOf(false) }

    var currentVolumeOrder by remember { mutableStateOf(volumeOrder) }
    var currentChapterOrder by remember { mutableStateOf(chapterOrder) }


    val textToSpeech = rememberTextToSpeech(context)


    val isPlaying by viewModel.isPlaying.observeAsState(false)
    val currentParagraphIndex by viewModel.currentParagraphIndex.observeAsState(0)

    // Giải phóng TextToSpeech khi Composable bị hủy
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    LaunchedEffect(bookId) {
        viewModel.fetchBook(bookId)
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
        viewModel.setIsPlaying(isPlaying)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = { isUIVisible = !isUIVisible },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Column {
            val chapter = viewModel.getChapterContent(currentVolumeOrder, currentChapterOrder)
            CustomTopBar(
                isVisible = isUIVisible,
                chapterName = chapter?.title ?: "Loading...",
                onBack = { navController.popBackStack() }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(theme.backgroundColor)
            ) {
                if (book != null) {
                    val isManga = !book!!.isNovel()
                    val chapterContent = viewModel.getChapterContent(currentVolumeOrder, currentChapterOrder)

                    if (chapterContent != null) {
                        if (isManga) {
                            MangaContent(chapterContent.content)
                        } else {
                            NovelContent(
                                fileUrls = chapterContent.content,
                                fontSize = viewModel.fontSize.value,
                                lineSpacing = viewModel.lineSpacing.value,
                                currentParagraphIndex = currentParagraphIndex,
                                isPlaying = isPlaying,
                                onPlayParagraph = { index, text ->
                                    viewModel.updateParagraphIndex(index)
                                    Log.e("info before play: ",  " " + text)
                                    textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chapter not found", color = theme.textPrimary)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = theme.textPrimary)
                    }
                }
            }

            if (book != null) {
                CustomBottomBar(
                    isVisible = isUIVisible,
                    viewModel = viewModel,
                    volumeOrder = currentVolumeOrder,
                    chapterOrder = currentChapterOrder,
                    book = book,
                    isPlaying = isPlaying,
                    onPlayToggle = {

                        if (!isPlaying) {
                            textToSpeech?.stop()
                        }
                        viewModel.setIsPlaying(!isPlaying)
                    },
                    onPreviousChapter = {
                        goToPreviousChapter(
                            currentVolumeOrder,
                            currentChapterOrder,
                            viewModel,
                            textToSpeech,
                            onChapterChanged = { vol, chap ->
                                currentVolumeOrder = vol
                                currentChapterOrder = chap
                            },
                            onShowUnlockDialog = { chapter, vol ->
                                selectedChapter = chapter
                                selectedVolumeOrder = vol
                                showUnlockDialog = true
                            }
                        )
                    },

                    onNextChapter = {
                        goToNextChapter(
                            currentVolumeOrder,
                            currentChapterOrder,
                            viewModel,
                            textToSpeech,
                            onChapterChanged = { vol, chap ->
                                currentVolumeOrder = vol
                                currentChapterOrder = chap
                            },
                            onShowUnlockDialog = { chapter, vol ->
                                selectedChapter = chapter
                                selectedVolumeOrder = vol
                                showUnlockDialog = true
                            }
                        )
                    },
                    onChapterListClick = { showChapterList = true },
                    themeViewModel = themeViewModel
                )
            }
        }

        if (showUnlockDialog && selectedChapter != null && selectedVolumeOrder != null && uid != null) {
            AlertDialog(
                onDismissRequest = { showUnlockDialog = false },
                title = { Text("Mở khóa chương") },
                text = {
                    Text("Bạn có $coinBalance coin. Mở khóa chương này với ${selectedChapter?.price} coin?")
                },
                confirmButton = {
                    TextButton(onClick = {
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
                                currentVolumeOrder = volumeOrder
                                currentChapterOrder = chapter.order
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
                    }) {
                        Text("Mở khóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnlockDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        if (showChapterList && book != null) {
            ModalBottomSheet(
                onDismissRequest = { showChapterList = false },
                containerColor = theme.backgroundColor
            ) {
                ChapterListContent(
                    book = book!!,
                    viewModel = viewModel,
                    onChapterSelected = { chapter, volumeOrder ->
                        if (viewModel.isChapterUnlocked(chapter.id) || chapter.price == 0) {
                            currentVolumeOrder = volumeOrder
                            currentChapterOrder = chapter.order
                            viewModel.setIsPlaying(false)
                            textToSpeech?.stop()
                            showChapterList = false
                        } else {
                            selectedChapter = chapter
                            selectedVolumeOrder = volumeOrder
                            showUnlockDialog = true
                            showChapterList = false
                        }
                    },
                    theme = theme
                )
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    textToSpeech?.setSpeechRate(1.5F)
    val chunkToParagraphIndex = mutableListOf<Int>()

    DisposableEffect(isPlaying) {
        var allChunks: List<String> = emptyList()
        var currentChunkIndex = 0

        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("TTS", "🔊 onStart: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d("TTS", "✅ onDone: $utteranceId")

                if (!isPlaying) {
                    Log.d("TTS", "⏹️ Dừng do isPlaying = false")
                    textToSpeech?.stop()
                    return
                }

                coroutineScope.launch {
                    if (currentChunkIndex + 1 < allChunks.size) {
                        currentChunkIndex++
                        val nextChunk = allChunks[currentChunkIndex]
                        Log.d("TTS", "▶️ Phát chunk $currentChunkIndex: $nextChunk")

                        val result = textToSpeech?.speak(
                            nextChunk,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "utterance_$currentChunkIndex"
                        )
                        if (result == TextToSpeech.ERROR) {
                            Log.e("TTS", "❌ speak() lỗi chunk $currentChunkIndex")
                        }

                        val currentParagraphIndex = chunkToParagraphIndex[currentChunkIndex]
                        viewModel.updateParagraphIndex(currentParagraphIndex)

                    } else {
                        Log.d("TTS", "🏁 Hết tất cả chunks")

                        goToNextChapter(
                            currentVolumeOrder,
                            currentChapterOrder,
                            viewModel,
                            textToSpeech,
                            onChapterChanged = { nextVol, nextChap ->
                                currentVolumeOrder = nextVol
                                currentChapterOrder = nextChap
                            },
                            onShowUnlockDialog = { chapter, vol ->
                                selectedChapter = chapter
                                selectedVolumeOrder = vol
                                showUnlockDialog = true
                            },
                            continuePlay = true
                        )
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                Log.e("TTS", "❌ onError: $utteranceId")
                Toast.makeText(context, "Lỗi phát âm thanh", Toast.LENGTH_SHORT).show()
                viewModel.setIsPlaying(false)
            }
        }

        if (isPlaying) {
            Log.d("TTS", "⏯️ Bắt đầu phát")
            coroutineScope.launch {
                val chapterContent = viewModel.getChapterContent(currentVolumeOrder, currentChapterOrder)

                if (chapterContent != null) {
                    val paragraphs = viewModel.getParagraphs(chapterContent.content)

                    val chunkList = mutableListOf<String>()
                    chunkToParagraphIndex.clear()
                    paragraphs.forEachIndexed { index, paragraph ->
                        val chunks = splitParagraphIntoChunks(paragraph)
                        chunks.forEach { chunk ->
                            chunkList.add(chunk)
                            chunkToParagraphIndex.add(index)
                        }
                    }

                    allChunks = chunkList
                    currentChunkIndex = 0

                    if (allChunks.isNotEmpty()) {
                        viewModel.updateParagraphIndex(chunkToParagraphIndex[currentChunkIndex])
                        textToSpeech?.setOnUtteranceProgressListener(listener)

                        val firstChunk = allChunks[currentChunkIndex]
                        val result = textToSpeech?.speak(
                            firstChunk,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "utterance_$currentChunkIndex"
                        )
                        if (result == TextToSpeech.ERROR) {
                            Log.e("TTS", "❌ speak() lỗi tại chunk đầu tiên")
                        }
                    } else {
                        Log.w("TTS", "⚠️ Không có chunk nào để phát")
                    }
                } else {
                    Log.e("TTS", "❌ Không thể tải nội dung chương")
                    viewModel.setIsPlaying(false)
                }
            }
        }


        onDispose {
            Log.d("TTS", "🧹 onDispose: dừng TTS")
            textToSpeech?.stop()
            textToSpeech?.setOnUtteranceProgressListener(null)
        }
    }


}

@Composable
fun ChapterListContent(
    book: Book,
    viewModel: BookViewModel,
    onChapterSelected: (Chapter, Long) -> Unit,
    theme: AppColors
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 400.dp)
    ) {
        book.volume.sortedBy { it.order }.forEach { volume ->
            item {
                Text(
                    text = "Tập ${volume.title}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(volume.chapters.sortedBy { it.order }) { chapter ->
                ChapterListItem(
                    chapter = chapter,
                    volumeOrder = volume.order,
                    viewModel = viewModel,
                    onClick = { onChapterSelected(chapter, volume.order) },
                    theme = theme
                )
            }
        }
    }
}

@Composable
fun ChapterListItem(
    chapter: Chapter,
    volumeOrder: Long,
    viewModel: BookViewModel,
    onClick: () -> Unit,
    theme: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = chapter.title,
                fontSize = 14.sp,
                color = if (viewModel.isChapterUnlocked(chapter.id) || chapter.price == 0) theme.textPrimary else theme.textSecondary,
                maxLines = 1
            )
            if (viewModel.isChapterUnlocked(chapter.id) || chapter.price == 0) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_unlocked),
                    contentDescription = "Unlocked",
                    modifier = Modifier.size(14.dp),
                    tint = theme.textSecondary
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Locked",
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

@Composable
fun CustomTopBar(
    isVisible: Boolean,
    chapterName: String,
    onBack: () -> Unit
) {
    val theme = LocalAppColors.current
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    val baseHeight = 56.dp
    val adjustedHeight = baseHeight / fontScale

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adjustedHeight)
                .background(theme.background)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }
                Text(
                    text = chapterName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = theme.textPrimary
                )
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    isVisible: Boolean,
    viewModel: BookViewModel,
    volumeOrder: Long,
    chapterOrder: Long,
    book: Book?,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onChapterListClick: () -> Unit,
    themeViewModel: ThemeViewModel,
) {
    val theme = LocalAppColors.current
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    val baseHeight = 64.dp
    val adjustedHeight = baseHeight / fontScale
    val fontSize by viewModel.fontSize
    val lineSpacing by viewModel.lineSpacing

    var darkMode by remember { mutableStateOf(false) }
    var fontSelectorExpanded by remember { mutableStateOf(false) }
    var lineSpacingSelectorExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.background)
                .shadow(4.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adjustedHeight)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChapterNavigationButton(
                    enabled = viewModel.getPreviousChapter(volumeOrder, chapterOrder).first,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    text = "",
                    theme = theme,
                    onClick = onPreviousChapter
                )

                IconButton(
                    onClick = {
                        darkMode = !darkMode
                        themeViewModel.toggleTheme()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            theme.backgroundColor.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = if (darkMode) R.drawable.dark else R.drawable.light),
                        contentDescription = "Toggle Dark Mode",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (book != null && book.isNovel()) {
                    // Nút Play/Pause cho TTS
                    IconButton(
                        onClick = onPlayToggle,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                theme.backgroundColor.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )

                    }

                    FontSizeSelector(
                        fontSize = fontSize,
                        expanded = fontSelectorExpanded,
                        theme = theme,
                        onExpandedChange = { fontSelectorExpanded = it },
                        onFontSizeSelected = { size ->
                            viewModel.setFontSize(size)
                            fontSelectorExpanded = false
                        }
                    )
                    LineSpacingSelector(
                        lineSpacing = lineSpacing,
                        expanded = lineSpacingSelectorExpanded,
                        theme = theme,
                        onExpandedChange = { lineSpacingSelectorExpanded = it },
                        onLineSpacingSelected = { spacing ->
                            viewModel.setLineSpacing(spacing)
                            lineSpacingSelectorExpanded = false
                        }
                    )
                }

                IconButton(
                    onClick = onChapterListClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            theme.backgroundColor.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Chapter List",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                ChapterNavigationButton(
                    enabled = viewModel.getNextChapter(volumeOrder, chapterOrder).first,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    text = "",
                    theme = theme,
                    onClick = onNextChapter
                )
            }
        }
    }
}

@Composable
private fun LineSpacingSelector(
    lineSpacing: Float,
    expanded: Boolean,
    theme: AppColors,
    onExpandedChange: (Boolean) -> Unit,
    onLineSpacingSelected: (Float) -> Unit,
) {
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(theme.background.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "↕",
                fontSize = 14.sp,
                color = theme.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format("%.1f", lineSpacing)}x",
                fontSize = 14.sp,
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(theme.background)
                .border(1.dp, theme.textSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            listOf(1.0f, 1.5f, 2.0f, 2.5f).forEach { spacing ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${String.format("%.1f", spacing)}x",
                            fontSize = 14.sp,
                            color = theme.textPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    },
                    onClick = { onLineSpacingSelected(spacing) }
                )
            }
        }
    }
}

@Composable
private fun ChapterNavigationButton(
    enabled: Boolean,
    icon: ImageVector,
    text: String,
    theme: AppColors,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (enabled) theme.textPrimary else theme.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (enabled) theme.textPrimary else theme.textSecondary.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FontSizeSelector(
    fontSize: Float,
    expanded: Boolean,
    theme: AppColors,
    onExpandedChange: (Boolean) -> Unit,
    onFontSizeSelected: (Float) -> Unit
) {
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(theme.background.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Aa",
                fontSize = 14.sp,
                color = theme.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${fontSize.toInt()}sp",
                fontSize = 14.sp,
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(theme.background)
                .border(1.dp, theme.textSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            listOf(12, 14, 16, 18, 20, 24, 26, 28, 30, 32, 34, 36, 38, 40).forEach { size ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$size sp",
                            fontSize = 14.sp,
                            color = theme.textPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    },
                    onClick = { onFontSizeSelected(size.toFloat()) }
                )
            }
        }
    }
}
@Composable
fun NovelContent(
    fileUrls: List<String>,
    fontSize: Float,
    lineSpacing: Float,
    currentParagraphIndex: Int,
    isPlaying: Boolean,
    onPlayParagraph: (Int, String) -> Unit
) {


    val theme = LocalAppColors.current
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        fileUrls.forEach { url ->
            item {
                var paragraphs by remember(url) { mutableStateOf<List<String>>(listOf("Đang tải...")) }

                LaunchedEffect(url) {
                    paragraphs = try {
                        val rawText = when {
                            url.endsWith(".txt") -> downloadTextFile(url)
                            url.endsWith(".docx") -> downloadAndExtractDocx(url)
                            else -> url // Trường hợp nội dung được truyền trực tiếp
                        }

                        rawText
                            .split(Regex("\r?\n"))
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                    } catch (e: Exception) {
                        listOf("Lỗi tải nội dung: ${e.message}")
                    }
                }
                Log.e("paragrap", paragraphs.toString())


                Column {
                    paragraphs.forEachIndexed { index, paragraph ->
                        Text(
                            text = paragraph,
                            fontSize = fontSize.sp,
                            color =  theme.textPrimary.copy(alpha = 0.7f),

                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .clickable {
                                    //onPlayParagraph(index, paragraph)
                                },
                            lineHeight = (fontSize * lineSpacing).sp,
//                            fontWeight = if (index == currentParagraphIndex && isPlaying)
//                                FontWeight.Bold
//                            else
//                                FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MangaContent(imageUrls: List<String>) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(LocalDensity.current) { screenWidthDp.toPx() }

    val isZoomed = scale.value > 1f
    val verticalScrollState = rememberScrollState()

    var totalContentHeightDp by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    coroutineScope.launch {
                        val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                        scale.snapTo(newScale)

                        if (newScale > 1f) {
                            val contentWidth = screenWidthPx * newScale
                            val contentHeight = with(density) { totalContentHeightDp.toPx() } * newScale

                            val maxOffsetX = ((contentWidth - screenWidthPx) / 2f).coerceAtLeast(0f)
                            val maxOffsetY = ((contentHeight - screenWidthPx * 2f) / 2f).coerceAtLeast(0f)

                            val newOffsetX = (offsetX.value + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                            val newOffsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)

                            offsetX.snapTo(newOffsetX)
                            offsetY = newOffsetY
                        } else {
                            scale.snapTo(1f)
                            offsetX.snapTo(0f)
                            offsetY = 0f
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            scale.animateTo(1f)
                            offsetX.animateTo(0f)
                            offsetY = 0f
                        }
                    }
                )
            }
            .clipToBounds()
    ) {
        val scrollModifier = if (!isZoomed) {
            Modifier.verticalScroll(verticalScrollState)
        } else {
            Modifier
        }

        var accumulatedHeight = 0.dp

        Column(
            modifier = scrollModifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
        ) {
            imageUrls.forEach { imageUrl ->
                var imageHeight by remember { mutableStateOf(500.dp) }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Manga Page",
                    contentScale = ContentScale.FillWidth,
                    onSuccess = { state ->
                        val width = state.painter.intrinsicSize.width
                        val height = state.painter.intrinsicSize.height
                        if (width > 0) {
                            val ratio = height / width
                            imageHeight = screenWidthDp * ratio
                            accumulatedHeight += imageHeight + 8.dp
                            totalContentHeightDp = accumulatedHeight
                        }
                    },
                    modifier = Modifier
                        .width(screenWidthDp)
                        .height(imageHeight)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }



}

fun goToPreviousChapter(
    currentVolumeOrder: Long,
    currentChapterOrder: Long,
    viewModel: BookViewModel,
    textToSpeech: TextToSpeech?,
    onChapterChanged: (volumeOrder: Long, chapterOrder: Long) -> Unit,
    onShowUnlockDialog: (chapter: Chapter?, volumeOrder: Long) -> Unit
) {
    val (hasPrevious, prevVolumeOrder, prevChapterOrder) = viewModel.getPreviousChapter(currentVolumeOrder, currentChapterOrder)
    if (hasPrevious && prevVolumeOrder != null && prevChapterOrder != null) {
        val prevChapter = viewModel.getChapterContent(prevVolumeOrder, prevChapterOrder)
        if (prevChapter != null && (viewModel.isChapterUnlocked(prevChapter.id) || prevChapter.price == 0)) {
            onChapterChanged(prevVolumeOrder, prevChapterOrder)
            viewModel.updateParagraphIndex(0)
            viewModel.setIsPlaying(false)
            textToSpeech?.stop()
        } else {
            onShowUnlockDialog(prevChapter, prevVolumeOrder)
        }
    }
}
fun goToNextChapter(
    currentVolumeOrder: Long,
    currentChapterOrder: Long,
    viewModel: BookViewModel,
    textToSpeech: TextToSpeech?,
    onChapterChanged: (volumeOrder: Long, chapterOrder: Long) -> Unit,
    onShowUnlockDialog: (chapter: Chapter?, volumeOrder: Long) -> Unit,
    continuePlay: Boolean = false
) {
    val (hasNext, nextVolumeOrder, nextChapterOrder) = viewModel.getNextChapter(currentVolumeOrder, currentChapterOrder)
    if (hasNext && nextVolumeOrder != null && nextChapterOrder != null) {
        val nextChapter = viewModel.getChapterContent(nextVolumeOrder, nextChapterOrder)
        if (nextChapter != null && (viewModel.isChapterUnlocked(nextChapter.id) || nextChapter.price == 0)) {
            onChapterChanged(nextVolumeOrder, nextChapterOrder)
            viewModel.updateParagraphIndex(0)
            viewModel.setIsPlaying(false) // 👈 tiếp tục phát nếu được yêu cầu
            textToSpeech?.stop()
        } else {
            onShowUnlockDialog(nextChapter, nextVolumeOrder)
        }
    }
}
