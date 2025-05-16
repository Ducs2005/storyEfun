package com.example.storyefun.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyefun.data.models.Comment
import com.example.storyefun.data.repository.CommentRepository
import com.example.storyefun.ui.theme.AppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSection(theme: AppColors, bookId: String, initialComments: List<Comment>) {
    var newComment by remember { mutableStateOf("") }
    val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val commentRepository: CommentRepository = CommentRepository()
    val context = LocalContext.current
    // Local mutable state to hold comments
    var comments by remember { mutableStateOf(initialComments) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Bình luận (${comments.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Danh sách comment
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            items(comments) { comment ->
                CommentItem(
                    username = comment.userName,
                    date = comment.date,
                    comment = comment.content,
                    theme = theme,
                    userImageUrl = comment.userImageUrl
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ô nhập bình luận
        OutlinedTextField(
            value = newComment,
            onValueChange = { newComment = it },
            label = { Text("Viết bình luận...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = theme.textPrimary,
                focusedBorderColor = theme.textPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (newComment.isNotBlank()) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val currentDate = getCurrentDate()

                    if (currentUser != null) {
                        val newCommentData = Comment(
                            userId = currentUser.uid,
                            userName = currentUser.displayName ?: "Bạn",
                            date = currentDate,
                            content = newComment.trim(),
                            type = "text",
                            userImageUrl = currentUser.photoUrl?.toString().toString()
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                commentRepository.addComment(bookId, newCommentData)
                                // Add the new comment to the local list
                                comments = comments + newCommentData
                                newComment = "" // Reset the text field
                                Toast.makeText(context, "Bình luận đã được gửi", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("CommentSection", "Failed to add comment: ${e.message}")
                                Toast.makeText(context, "Không thể gửi bình luận", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("CommentSection", "User is not authenticated")
                        Toast.makeText(context, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = theme.backOrange,
                contentColor = theme.textPrimary
            )
        ) {
            Text("Gửi")
        }
    }
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}