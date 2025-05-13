package com.example.profileui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.storyefun.R
import com.example.storyefun.ui.theme.AppColors
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ThemeViewModel
import com.example.storyefun.viewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, themeViewModel: ThemeViewModel) {
    val theme = LocalAppColors.current
    val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current

    // Firebase current user
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    // Bottom sheet state for avatar
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Dialog states for username and password
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(firebaseUser?.displayName ?: "") }
    var newPassword by remember { mutableStateOf("") }

    // Image picker launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            userViewModel.updateAvatar(
                imageUri = it,
                onSuccess = {
                    Toast.makeText(context, "Avatar updated successfully", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e ->
                    Toast.makeText(context, "Failed to update avatar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Toast.makeText(context, "Camera capture not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for camera
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(Uri.EMPTY)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Username dialog
    if (showUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showUsernameDialog = false },
            modifier = Modifier.background(theme.backgroundColor, RoundedCornerShape(12.dp)),
            title = {
                Text(
                    "Change Username",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("New Username", color = theme.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.textPrimary,
                        unfocusedBorderColor = theme.textSecondary,
                        focusedLabelColor = theme.textPrimary,
                        unfocusedLabelColor = theme.textSecondary,
                        cursorColor = theme.textPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.updateUsername(
                            newName = newUsername,
                            onSuccess = {
                                showUsernameDialog = false
                                Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                showUsernameDialog = false
                                Toast.makeText(context, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = newUsername.isNotBlank(),
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.textPrimary)
                ) {
                    Text("Save", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUsernameDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.textSecondary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // Password dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            modifier = Modifier.background(theme.backgroundColor, RoundedCornerShape(12.dp)),
            title = {
                Text(
                    "Change Password",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password", color = theme.textSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.textPrimary,
                        unfocusedBorderColor = theme.textSecondary,
                        focusedLabelColor = theme.textPrimary,
                        unfocusedLabelColor = theme.textSecondary,
                        cursorColor = theme.textPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.updatePassword(
                            newPassword = newPassword,
                            onSuccess = {
                                showPasswordDialog = false
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                showPasswordDialog = false
                                Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = newPassword.length >= 6,
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.textPrimary)
                ) {
                    Text("Save", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.textSecondary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // Bottom sheet for avatar selection
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = theme.backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Avatar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.textPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showBottomSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.textPrimary,
                        contentColor = theme.backgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pick from Gallery", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(Uri.EMPTY)
                        } else {
                            permissionLauncher.launch(permission)
                        }
                        showBottomSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.textPrimary,
                        contentColor = theme.backgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Take a Photo", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
    ) {
        ProfileHeader(
            navController = navController,
            displayName = firebaseUser?.displayName ?: "Guest",
            email = firebaseUser?.email ?: "No email",
            photoUrl = firebaseUser?.photoUrl?.toString(),
            onEditAvatarClick = { showBottomSheet = true },
            onEditUsernameClick = { showUsernameDialog = true },
            theme = theme,
            isDarkMode = isDarkMode
        )
        SettingsSection(
            navController = navController,
            darkMode = isDarkMode,
            theme = theme,
            onDarkModeToggle = { themeViewModel.toggleTheme() },
            onChangePasswordClick = { showPasswordDialog = true },
            onItemClick = { destination ->
                when (destination) {
                    "home" -> navController.navigate("login")
                    "favourite" -> navController.navigate("login")
                    "account" -> navController.navigate("login")
                    "contact" -> navController.navigate("login")
                    "out" -> {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(
    navController: NavController,
    displayName: String,
    email: String,
    photoUrl: String?,
    onEditAvatarClick: () -> Unit,
    onEditUsernameClick: () -> Unit,
    theme: AppColors,
    isDarkMode: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.linearGradient(
                    colors = if (isDarkMode) {
                        listOf(theme.textPrimary.copy(alpha = 0.8f), theme.backgroundContrast2)
                    } else {
                        listOf(theme.textPrimary.copy(alpha = 0.9f), theme.backgroundContrast2.copy(alpha = 0.7f))
                    },
                    start = Offset(0f, 0f),
                    end = Offset(0f, 400f)
                ),
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = if (photoUrl != null) {
                        rememberAsyncImagePainter(
                            model = photoUrl,
                            placeholder = painterResource(id = R.drawable.ava),
                            error = painterResource(id = R.drawable.ava)
                        )
                    } else {
                        painterResource(id = R.drawable.ava)
                    },
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, theme.backgroundColor, CircleShape)
                        .shadow(6.dp, CircleShape)
                )
                IconButton(
                    onClick = onEditAvatarClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(theme.backgroundColor, CircleShape)
                        .border(1.dp, theme.textPrimary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Avatar",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.backgroundColor
                )
                IconButton(
                    onClick = onEditUsernameClick,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Username",
                        tint = theme.backgroundColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = email,
                fontSize = 14.sp,
                color = theme.backgroundColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileStats(theme = theme)
        }
    }
}

@Composable
fun ProfileStats(theme: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("122", "Followers", theme)
        StatItem("67", "Following", theme)
        StatItem("37K", "Likes", theme)
    }
}

@Composable
fun StatItem(number: String, label: String, theme: AppColors) {
    var isPressed by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        if (isPressed) theme.textPrimary.copy(alpha = 0.1f) else theme.backgroundColor
    )

    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(90.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isPressed = !isPressed },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = theme.textSecondary
            )
        }
    }
}

@Composable
fun SettingsSection(
    navController: NavController,
    darkMode: Boolean,
    theme: AppColors,
    onDarkModeToggle: (Boolean) -> Unit,
    onChangePasswordClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Settings",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = theme.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                title = "Chế độ tối",
                icon = Icons.Default.Face,
                switch = true,
                darkMode = darkMode,
                theme = theme,
                onDarkModeToggle = { onDarkModeToggle(it) }
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Tài khoản",
                icon = Icons.Default.Person,
                theme = theme,
                onClick = { onItemClick("account") }
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Đổi mật khẩu",
                icon = Icons.Default.Lock,
                theme = theme,
                onClick = onChangePasswordClick
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Truyện yêu thích",
                icon = Icons.Default.Favorite,
                theme = theme,
                onClick = { navController.navigate("mystory") }
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Truyện đã đăng",
                icon = Icons.Default.Add,
                theme = theme,
                onClick = { navController.navigate("mystory") }
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Thêm truyện",
                icon = Icons.Default.Add,
                theme = theme,
                onClick = { navController.navigate("upload") }
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Liên hệ",
                icon = Icons.Default.Call,
                theme = theme,
                onClick = { onItemClick("contact") }
            )
            Divider(color = theme.textSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            SettingItem(
                title = "Log out",
                icon = Icons.Default.ExitToApp,
                theme = theme,
                onClick = { onItemClick("out") }
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    switch: Boolean = false,
    darkMode: Boolean = false,
    theme: AppColors,
    onDarkModeToggle: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        if (isPressed) theme.textPrimary.copy(alpha = 0.05f) else Color.Transparent
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick?.invoke()
                    isPressed = false
                }
            )
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.textPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = theme.textPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        if (switch) {
            Switch(
                checked = darkMode,
                onCheckedChange = { onDarkModeToggle?.invoke(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = theme.textPrimary,
                    checkedTrackColor = theme.textPrimary.copy(alpha = 0.5f),
                    uncheckedThumbColor = theme.backgroundColor,
                    uncheckedTrackColor = theme.textSecondary.copy(alpha = 0.5f)
                )
            )
        }
    }
}