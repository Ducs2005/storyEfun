package com.example.storyefun.admin.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyefun.ui.screens.OrderPayment

class ProductOrder : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProductOrderScreen()
        }
    }
}

@Composable
fun ProductOrderScreen() {
    var soLuong by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(148.dp))

        Text(
            text = "Chú chim ba quen",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "5.000VNĐ",
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = soLuong,
            onValueChange = { soLuong = it },
            label = { Text("Số lượng") },
            modifier = Modifier
                .align(Alignment.End)
                .width(146.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (soLuong.isBlank()) {
                    Toast.makeText(context, "Nhập số lượng muốn mua", Toast.LENGTH_SHORT).show()
                } else {
                    val total = soLuong.toDoubleOrNull()?.times(5_000) ?: 0.0
                    val intent = Intent(context, OrderPayment::class.java)
                    intent.putExtra("soluong", soLuong)
                    intent.putExtra("total", total)
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .width(136.dp)
                .height(49.dp)
        ) {
            Text("Xác nhận")
        }
    }
}
