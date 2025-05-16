package com.example.storyefun.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.storyefun.ui.theme.LocalAppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CoinScreen(navController: NavController) {
    val theme = LocalAppColors.current
    val options = listOf(
        "Chương hiện tại" to 0,
        "10 chương sau\nGiảm 10%" to 10,
        "20 chương sau\nGiảm 15%" to 15,
        "30 chương sau\nGiảm 20%" to 20,
        "710 chương sau\nGiảm 40%" to 40
    )

    var selectedOption by remember { mutableStateOf(10) }

    val originalPrice = 1001
    val discount = options.firstOrNull { it.second == selectedOption }?.second ?: 0
    val total = originalPrice - (originalPrice * discount / 100)

    // Lấy coin từ Firestore
    var coinBalance by remember { mutableStateOf<Int?>(null) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    coinBalance = document.getLong("coin")?.toInt()
                }
                .addOnFailureListener {
                    coinBalance = 0 // hoặc hiển thị lỗi
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Đọc vĩnh viễn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Chương bắt đầu: Chapter 67",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = theme.textPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Options grid
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.take(2).forEach { option ->
                    OptionButton(option, selectedOption) { selectedOption = option.second }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.subList(2, 4).forEach { option ->
                    OptionButton(option, selectedOption) { selectedOption = option.second }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OptionButton(options[4], selectedOption) { selectedOption = options[4].second }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Giá gốc", fontSize = 14.sp, color = theme.textSecondary)
        Text("$originalPrice xu", fontWeight = FontWeight.Bold, color = theme.textPrimary)

        Spacer(modifier = Modifier.height(4.dp))

        Text("Hàng loạt ưu đãi", fontSize = 14.sp, color = theme.textSecondary)
        Text("-$originalPrice * discount / 100 xu", fontWeight = FontWeight.Bold, color = theme.buttonOrange)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Tự động mua chương sau",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = theme.textPrimary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            "Chương miễn phí và chương đã mở khóa sẽ không bị mua lại",
            fontSize = 12.sp,
            color = theme.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tổng: $total xu",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.buttonOrange
        )

        // ✅ Hiển thị coin lấy từ Firestore
        Text(
            text = "Số dư: ${coinBalance?.toString() ?: "Đang tải..."} xu",
            fontSize = 14.sp,
            color = theme.textSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (coinBalance != null && coinBalance!! < total) {
            Button(
                onClick = {
                    navController.navigate("desposite")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.buttonOrange)
            ) {
                Text("Nạp tiền", color = theme.buttonText)
            }
        } else {
            Button(
                onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val db = FirebaseFirestore.getInstance()
                        val userDoc = db.collection("users").document(uid)

                        db.runTransaction { transaction ->
                            val snapshot = transaction.get(userDoc)
                            val currentCoin = snapshot.getLong("coin") ?: 0
                            if (currentCoin >= total) {
                                transaction.update(userDoc, "coin", currentCoin - total)
                                // TODO: Lưu trạng thái chương đã mở khóa tại đây nếu cần
                            }
                        }.addOnSuccessListener {
                            // Hiển thị thông báo hoặc chuyển màn hình
                            println("Mở khóa thành công")
                        }.addOnFailureListener {
                            println("Mở khóa thất bại: ${it.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.questCompletedColor)
            ) {
                Text("Mở khóa", color = theme.buttonText)
            }
        }
    }
}

@Composable
fun OptionButton(
    option: Pair<String, Int>,
    selected: Int,
    onClick: () -> Unit
) {
    val theme = LocalAppColors.current
    val isSelected = option.second == selected
    val borderColor = if (isSelected) theme.buttonOrange else theme.textSecondary
    val textColor = if (isSelected) theme.buttonOrange else theme.textPrimary

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(60.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.first,
            fontSize = 14.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

