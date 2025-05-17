package com.example.storyefun.ui.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.storyefun.R
import com.example.storyefun.ui.theme.LocalAppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun SplashScreen(navController: NavController) {
    val appColors = LocalAppColors.current
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val gradientOffset = remember { Animatable(0f) }
    val shimmerOffset = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    var isLoading by remember { mutableStateOf(false) }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFE4B5), // moccasin
            Color(0xFFFFFACD), // lemon chiffon
            Color(0xFFFFE4B5)
        ),
        start = androidx.compose.ui.geometry.Offset(0f, shimmerOffset.value),
        end = androidx.compose.ui.geometry.Offset(shimmerOffset.value, 1000f)
    )

    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        alpha.animateTo(1f, tween(1200, easing = LinearOutSlowInEasing))
        rotation.animateTo(360f, tween(1000, easing = FastOutSlowInEasing))
        gradientOffset.animateTo(1f, tween(2000, easing = LinearEasing))

        delay(1000)
        isLoading = true

        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            Log.d("SplashScreen", "Current user: ${currentUser?.uid ?: "null"}")

            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val document = db.collection("users").document(currentUser.uid).get().await()
                val role = document.getString("role")
                Log.d("SplashScreen", "User role: ${role ?: "null"}")

                val destination = if (role == "admin") "menuScreen" else "home"
                navController.navigate(destination) {
                    popUpTo("splashScreen") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("splashScreen") { inclusive = true }
                }
            }
        } catch (e: Exception) {
            Log.e("SplashScreen", "Navigation error: ${e.message}", e)
            navController.navigate("home") {
                popUpTo("splashScreen") { inclusive = true }
            }
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(130.dp)
                    .clip(MaterialTheme.shapes.large)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        alpha = alpha.value,
                        rotationZ = rotation.value * 0.2f
                    )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Story E Fun",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3E2723),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        alpha = alpha.value
                    )
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your Adventure Awaits!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6D4C41),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(
                        alpha = alpha.value * 0.85f
                    )
                    .padding(horizontal = 16.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(32.dp),
                color = Color(0xFF6D4C41)
            )
        }
    }
}