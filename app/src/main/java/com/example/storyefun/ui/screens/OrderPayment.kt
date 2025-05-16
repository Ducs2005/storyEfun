package com.example.storyefun.ui.screens

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storyefun.R
import com.example.storyefun.admin.ui.PaymentNotification
import com.example.storyefun.data.repository.TransactionRepository
import com.example.storyefun.ui.theme.AppTheme
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.TransactionViewModel
import com.example.storyefun.zaloPay.Api.CreateOrder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

class OrderPayment : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cho phép gọi mạng trên main thread (chỉ dùng khi test)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        )

        // Khởi tạo ZaloPay SDK
        ZaloPaySDK.init(2553, Environment.SANDBOX)

        val amount = intent.getIntExtra("amount", 0)
        val coin = intent.getIntExtra("coin", 0)

        setContent {
            AppTheme(darkTheme = isSystemInDarkTheme()) {
                OrderPaymentScreen(amount, coin)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            ZaloPaySDK.getInstance().onResult(it)
        }
    }
}

@Composable
fun OrderPaymentScreen(amount: Int, coin: Int) {
    val theme = LocalAppColors.current
    val context = LocalContext.current
    val amountFormatted = String.format("%.0f", amount.toDouble())
    var paymentStatus by remember { mutableStateOf<String?>(null) }

    // Dữ liệu người dùng
    var userName by remember { mutableStateOf<String?>(null) }
    var coinBalance by remember { mutableStateOf<Int?>(null) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    val viewModel: TransactionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionViewModel(TransactionRepository(), uid ?: "") as T
            }
        }
    )
    val transactionStatus by viewModel.transactionStatus.collectAsState()

    // Lấy thông tin người dùng
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("username") ?: "Người dùng không xác định"
                    coinBalance = document.getLong("coin")?.toInt()
                }
                .addOnFailureListener {
                    userName = "Không thể lấy thông tin người dùng"
                    coinBalance = 0
                }
        }
    }

    if (userName == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = theme.buttonOrange)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(theme.buttonOrange, theme.backOrange)
                        )
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Đơn hàng",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.buttonText
                    )
                }
            }

            // Nội dung
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Số dư hiện tại
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Số dư hiện tại:",
                        fontSize = 20.sp,
                        color = theme.textPrimary
                    )
                    Row {
                        Text(
                            "${coinBalance ?: "Đang tải"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.buttonOrange
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Coin Balance",
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Top),
                            tint = theme.textSecondary
                        )
                    }
                }

                // Thông tin nạp tiền
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.backgroundColor)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Số tiền nạp: $amountFormatted VNĐ",
                        fontSize = 20.sp,
                        color = theme.textPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Số coin: +$coin",
                            fontSize = 20.sp,
                            color = theme.textPrimary
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Coin",
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.Top),
                            tint = theme.textSecondary
                        )
                    }
                }

                // Trạng thái thanh toán
                paymentStatus?.let {
                    Text(
                        text = it,
                        color = theme.buttonOrange,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                transactionStatus?.let {
                    Text(
                        text = it,
                        color = theme.buttonOrange,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                // Nút thanh toán
                Button(
                    onClick = {
                        val orderApi = CreateOrder()
                        try {
                            val data: JSONObject = orderApi.createOrder(amountFormatted)
                            if (data.getString("return_code") == "1") {
                                val token = data.getString("zp_trans_token")
                                ZaloPaySDK.getInstance().payOrder(
                                    context as ComponentActivity,
                                    token,
                                    "demozpdk://app",
                                    object : PayOrderListener {
                                        override fun onPaymentSucceeded(s: String?, s1: String?, s2: String?) {
                                            paymentStatus = "Thanh toán thành công"
                                            if (uid != null && coinBalance != null) {
                                                val newCoin = coinBalance!! + coin
                                                viewModel.addTransaction(coin, amount.toDouble())
                                                FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(uid)
                                                    .update("coin", newCoin)
                                                    .addOnSuccessListener {
                                                        Log.d("OrderPayment", "Cập nhật coin thành công: $newCoin")
                                                        context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                            putExtra("result", "Thanh toán thành công")
                                                        })
                                                    }
                                                    .addOnFailureListener {
                                                        context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                            putExtra("result", "Cập nhật coin thất bại")
                                                        })
                                                    }
                                            } else {
                                                paymentStatus = "Lỗi khi lấy thông tin người dùng"
                                                context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                    putExtra("result", "Lỗi khi lấy thông tin người dùng")
                                                })
                                            }
                                        }

                                        override fun onPaymentCanceled(s: String?, s1: String?) {
                                            paymentStatus = "Hủy thanh toán"
                                            context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                putExtra("result", "Hủy thanh toán")
                                            })
                                        }

                                        override fun onPaymentError(zaloPayError: ZaloPayError?, s: String?, s1: String?) {
                                            paymentStatus = "Lỗi thanh toán"
                                            context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                putExtra("result", "Lỗi thanh toán")
                                            })
                                        }
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            paymentStatus = "Lỗi tạo đơn hàng"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(64.dp)
                        .shadow(5.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(theme.buttonOrange, theme.backOrange)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Thanh toán bằng Zalo Pay",
                            color = theme.buttonText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}