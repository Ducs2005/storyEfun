package com.example.storyefun.admin.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.storyefun.data.models.Volume
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.VolumeViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AddVolumeScreen(navController: NavController, bookId: String) {
    val theme = LocalAppColors.current
    val context = LocalContext.current
    val volumeViewModel: VolumeViewModel = viewModel()

    // Load danh sách volumes
    LaunchedEffect(bookId) {
        volumeViewModel.loadVolumes(bookId)
    }

    val volumesState by volumeViewModel.volumes.collectAsState()
    val isLoading by volumeViewModel.isLoading.collectAsState()
    val toastMessage by volumeViewModel.toastMessage.collectAsState()

    // Hiển thị toast nếu có message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            volumeViewModel.setToastMessage("") // Reset toast message
        }
    }

    // State để giữ order tiếp theo
    var nextOrder by remember { mutableStateOf(1L) }

    // State cho tiêu đề volume
    var volumeTitle by remember { mutableStateOf(TextFieldValue("")) }

    // Lấy order tiếp theo và set giá trị mặc định cho title
    LaunchedEffect(bookId) {
        volumeViewModel.fetchNextVolumeOrder(bookId) { fetchedOrder ->
            nextOrder = fetchedOrder
            volumeTitle = TextFieldValue("Volume $fetchedOrder")
        }
    }

    // UI layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Add Volume",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Input tiêu đề volume
        OutlinedTextField(
            value = volumeTitle,
            onValueChange = { volumeTitle = it },
            label = { Text("Volume Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        // Button thêm volume
        Button(
            onClick = {
                val title = if (volumeTitle.text.isNotBlank()) {
                    volumeTitle.text
                } else {
                    "Volume $nextOrder"
                }

                val newVolume = Volume(title = title, order = nextOrder)
                volumeViewModel.addVolume(bookId, newVolume) { volumeId ->
                    navController.navigate("addChapter/$bookId/$volumeId")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = theme.buttonOrange),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            Text("Add Volume", color = Color.Black)
        }

        Divider(color = Color.LightGray, thickness = 1.dp)

        Text("Existing Volumes", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(volumesState, key = { it.id }) { volume ->
                SwipeToDeleteVolume(
                    volume = volume,
                    bookId = bookId,
                    volumeViewModel = volumeViewModel,
                    onClick = {
                        Log.d("Navigate", "Navigating to addChapter/$bookId/${volume.id}")
                        navController.navigate("addChapter/$bookId/${volume.id}")
                    }
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun SwipeToDeleteVolume(
    volume: Volume,
    bookId: String,
    volumeViewModel: VolumeViewModel,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val maxSwipeDistance = with(density) { 80.dp.toPx() } // Khoảng cách tối đa để hiển thị nút xóa
    val deleteThreshold = maxSwipeDistance * 0.6f // Ngưỡng để hiển thị dialog (60% maxSwipeDistance)

    // Dialog xác nhận xóa
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                coroutineScope.launch {
                    offsetX = 0f // Reset vị trí khi đóng dialog
                }
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa volume '${volume.title}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        volumeViewModel.deleteVolume(bookId, volume.id)
                        showConfirmDialog = false
                        coroutineScope.launch {
                            offsetX = 0f // Reset vị trí sau khi xóa
                        }
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        coroutineScope.launch {
                            offsetX = 0f // Reset vị trí khi hủy
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
        // Nút xóa ở phía sau với nền bo tròn
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = Color.Red,
                    shape = RoundedCornerShape(12.dp) // Bo tròn nền đỏ
                )
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        }

        // Nội dung volume
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .clickable(onClick = onClick)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Khi kết thúc lướt
                            if (-offsetX > deleteThreshold) {
                                showConfirmDialog = true // Hiển thị dialog xác nhận
                            } else {
                                offsetX = 0f // Reset nếu không đủ ngưỡng
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f // Reset nếu hủy
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Cập nhật vị trí khi lướt
                            val newOffset = offsetX + dragAmount
                            // Giới hạn lướt trái (âm) và không cho lướt phải (dương)
                            offsetX = newOffset.coerceIn(-maxSwipeDistance, 0f)
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = volume.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = LocalAppColors.current.textPrimary
                )
            }
        }
    }
}