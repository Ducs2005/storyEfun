package com.example.storyefun.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.storyefun.data.models.Book
import com.example.storyefun.ui.theme.LocalAppColors
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

@Composable
fun NewBookScreen(navController: NavController) {
    val firestore = Firebase.firestore
    val books =  remember { mutableStateListOf<Book>() }
    val context = LocalContext.current
    val theme = LocalAppColors.current // Access theme colors

    LaunchedEffect(Unit) {
        firestore.collection("books")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(4)
            .get()
            .addOnSuccessListener { result ->
                books.clear()
                for (document in result) {
                    val book = document.toObject(Book::class.java)
                    books.add(book)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load books: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background) // Use theme.background
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New arrivals",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = theme.textPrimary // Use theme.textPrimary
                )
                Text(
                    text = "Select all",
                    color = theme.buttonOrange, // Use theme.buttonOrange
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("allbook")
                    }
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books.size) { index ->
                    val book = books[index]
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                navController.navigate("bookDetail/${book.id}")
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor), // Use theme.backgroundColor
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Add slight elevation
                    ) {
                        Row(
                            modifier = Modifier
                                .background(theme.backgroundColor) // Use theme.backgroundColor
                                .padding(8.dp) // Add padding inside card
                        ) {
                            Image(
                                painter = rememberImagePainter(book.imageUrl),
                                contentDescription = book.name,
                                modifier = Modifier
                                    .height(150.dp)
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = book.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = theme.textPrimary, // Use theme.textPrimary
                                    maxLines = 1
                                )
                                Text(
                                    text = book.author,
                                    color = theme.textSecondary, // Use theme.textSecondary
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${book.follows} Followers",
                                        fontSize = 12.sp,
                                        color = theme.textSecondary // Use theme.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${book.likes} Likes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = theme.textPrimary // Use theme.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}