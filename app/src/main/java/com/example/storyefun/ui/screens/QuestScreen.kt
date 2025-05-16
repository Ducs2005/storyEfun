
package com.example.storyefun.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.storyefun.data.repository.QuestRepository
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.QuestViewModel
import com.example.storyefun.viewModel.ThemeViewModel

@Composable
fun QuestScreen(navController: NavController, userId: String) {
    val theme = LocalAppColors.current
    val context = LocalContext.current
    val repository = remember { QuestRepository() }
    val viewModel: QuestViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return QuestViewModel(repository, userId) as T
            }
        }
    )

    // Lấy trạng thái từ ViewModel
    val quests by viewModel.quests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Hiển thị toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            //viewModel.clearToastMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.header)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }
            Text(
                text = "Nhiệm vụ hàng ngày",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Danh sách nhiệm vụ
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(theme.background)
        ) {
            if (quests.isEmpty() && !isLoading) {
                item {
                    Text(
                        text = "Không có nhiệm vụ nào",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = theme.textSecondary
                    )
                }
            }
            items(quests, key = { it.id }) { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(5.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.backgroundColor.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (quest.type) {
                                    "daily_login" -> "Đăng nhập hàng ngày"
                                    "online_one_minute" -> "Online 1 phút"
                                    "online_two_minute" -> "Online 2 phút"
                                    "online_twenty_minute" -> "Online 20 phút"
                                    else -> "Nhiệm vụ"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (quest.completed) theme.questCompletedColor else theme.textPrimary
                            )
                            Text(
                                text = "Tiến độ: ${quest.progress}/${quest.requiredProgress}",
                                fontSize = 14.sp,
                                color = if (quest.completed) theme.questCompletedColor else theme.textSecondary
                            )
                            Text(
                                text = "Thưởng: ${quest.reward} coin",
                                fontSize = 14.sp,
                                color = theme.buttonOrange
                            )
                        }
                        Button(
                            onClick = {
                                if (!quest.completed && quest.progress >= quest.requiredProgress) {
                                    viewModel.completeQuest(quest.id, quest.reward)
                                }
                            },
                            enabled = !quest.completed && quest.progress >= quest.requiredProgress,
                            modifier = Modifier
                                .height(36.dp)
                                .shadow(3.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (quest.completed) theme.questCompletedColor else theme.buttonOrange,
                                disabledContainerColor = theme.textSecondary
                            )
                        ) {
                            Text(
                                text = if (quest.completed) "Xong" else "Nhận",
                                fontSize = 14.sp,
                                color = theme.buttonText
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                color = theme.textPrimary
            )
        }
    }
}