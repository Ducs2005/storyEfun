package com.example.storyefun.admin.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.storyefun.data.models.Chapter
import com.example.storyefun.data.repository.BookRepository
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.utils.extractTextFromDocx
import com.example.storyefun.viewModel.ChapterViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AddChapterScreen(
    navController: NavController,
    bookId: String,
    volumeId: String,
    repository: BookRepository = BookRepository()
) {
    val theme = LocalAppColors.current
    val context = LocalContext.current
    val viewModel: ChapterViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ChapterViewModel(repository, bookId, volumeId) as T
            }
        }
    )

    // Lấy trạng thái từ ViewModel
    val chapters by viewModel.chapters.collectAsState()
    val book by viewModel.book.collectAsState()
    val imageUris = viewModel.imageUris
    val isUploading = viewModel.isUploading
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // State cho novel content
    var novelContent by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    // Hiển thị toast nếu có message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.setToastMessage("") // Reset toast message
        }
    }

    // Launcher để chọn ảnh (cho manga)
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        viewModel.updateImageUris(uris)
    }

    // Launcher để chọn file (cho novel)
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedFileUri = uri
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val fileName = uri.path?.substringAfterLast("/")?.lowercase() ?: ""
                    if (fileName.endsWith(".txt")) {
                        novelContent = TextFieldValue(inputStream.bufferedReader().readText())
                    } else if (fileName.endsWith(".docx")) {
                        val extractedText = extractTextFromDocx(inputStream)
                        if (extractedText.startsWith("Error")) {
                            Toast.makeText(context, extractedText, Toast.LENGTH_LONG).show()
                            novelContent = TextFieldValue("")
                        } else {
                            novelContent = TextFieldValue(extractedText)
                        }
                    } else {
                        Toast.makeText(context, "Chỉ hỗ trợ file .txt hoặc .docx", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi đọc file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Add Chapter",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Danh sách chapters đã có
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            items(chapters, key = { it.id }) { chapter ->
                SwipeToDeleteChapter(
                    chapter = chapter,
                    bookId = bookId,
                    volumeId = volumeId,
                    chapterViewModel = viewModel,
                    isNovel = book?.isNovel() ?: false
                )
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            color = Color(0xFFE0E0E0)
        )

        // Phần thêm chapter mới
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("📌 Chapter Title", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                shape = RoundedCornerShape(12.dp)
            )

            ChapterPriceSelector(
                selectedPrice = viewModel.price,
                onPriceChange = { viewModel.updatePrice(it ?: 0) }
            )

            if (book?.isNovel() == true) {
                // UI cho novel
                Button(
                    onClick = { filePickerLauncher.launch("text/plain,application/vnd.openxmlformats-officedocument.wordprocessingml.document") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(5.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.buttonOrange),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Chọn file .txt hoặc .docx",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                selectedFileUri?.let {
                    Text(
                        text = "File đã chọn: ${it.path?.substringAfterLast("/")}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = novelContent,
                    onValueChange = { novelContent = it },
                    label = { Text("Nội dung chương", fontSize = 16.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 10
                )
            } else {
                // UI cho manga
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(5.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.buttonOrange),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Chọn ảnh minh họa",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hiển thị các ảnh đã chọn
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    items(imageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (book?.isNovel() == true) {
                        viewModel.uploadNovelChapter(novelContent.text) {
                            novelContent = TextFieldValue("")
                            selectedFileUri = null
                        }
                    } else {
                        viewModel.uploadChapter {}
                    }
                },
                enabled = viewModel.title.isNotBlank() &&
                        (if (book?.isNovel() == true) novelContent.text.isNotBlank() else imageUris.isNotEmpty()) &&
                        !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(5.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.buttonOrange,
                    disabledContainerColor = Color(0xFFD3D3D3)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    if (isUploading) "Đang tải lên..." else "⬆️ Tải lên chương mới",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun SwipeToDeleteChapter(
    chapter: Chapter,
    bookId: String,
    volumeId: String,
    chapterViewModel: ChapterViewModel,
    isNovel: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val maxSwipeDistance = with(density) { 80.dp.toPx() }
    val deleteThreshold = maxSwipeDistance * 0.6f

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                coroutineScope.launch {
                    offsetX = 0f
                }
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa chapter '${chapter.title}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        chapterViewModel.deleteChapter(chapter.id)
                        showConfirmDialog = false
                        coroutineScope.launch {
                            offsetX = 0f
                        }
                    }
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        coroutineScope.launch {
                            offsetX = 0f
                        }
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(5.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
        ) {
            Column(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (-offsetX > deleteThreshold) {
                                    showConfirmDialog = true
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onDragCancel = {
                                offsetX = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newOffset = offsetX + dragAmount
                                offsetX = newOffset.coerceIn(-maxSwipeDistance, 0f)
                            }
                        )
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = chapter.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isNovel) {
                    Text(
                        text = chapter.content.firstOrNull()?.take(100) ?: "No content",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2
                    )
                } else {
                    chapter.content.forEach { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = "Chapter Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterPriceSelector(
    selectedPrice: Int?,
    onPriceChange: (Int?) -> Unit
) {
    val options = listOf(0, 100, 200, 500, 1000)
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedPrice?.toString() ?: "") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onPriceChange(it.toIntOrNull())
            },
            label = { Text("💰 Giá chương", fontSize = 16.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { price ->
                DropdownMenuItem(
                    text = { Text(if (price == 0) "Miễn phí" else "$price đ") },
                    onClick = {
                        text = price.toString()
                        onPriceChange(price)
                        expanded = false
                    }
                )
            }
        }
    }
}