package com.example.storyefun.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.storyefun.R
import com.example.storyefun.data.models.Book
import com.example.storyefun.ui.theme.LocalAppColors
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

@Composable
fun PopularBookScreen(navController: NavController) {
    val firestore = Firebase.firestore
    val books = remember { mutableStateListOf<Book>() }
    val context = LocalContext.current
    val theme = LocalAppColors.current // Access theme colors

    LaunchedEffect(Unit) {
        firestore.collection("books")
            .orderBy("likes", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                books.clear()
                for (document in result) {
                    val book = document.toObject(Book::class.java)
                    if (book.likes >= 100) {
                        books.add(book)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load books: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(theme.background) // Use theme.background
    ) {
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular books",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary // Use theme.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Books List
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books.size) { index ->
                val book = books[index]
                PopularCard(
                    title = book.name,
                    author = book.author,
                    imageUrl = book.imageUrl ?: "",
                    onClick = {
                        navController.navigate("bookDetail/${book.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun PopularCard(title: String, author: String, imageUrl: String, onClick: () -> Unit) {
    val theme = LocalAppColors.current // Access theme colors

    Card(
        modifier = Modifier
            .width(120.dp)
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor), // Use theme.backgroundColor
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Add slight elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp) // Add padding inside card
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.error)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = theme.textPrimary, // Use theme.textPrimary
                maxLines = 1
            )
            Text(
                text = author,
                color = theme.textSecondary, // Use theme.textSecondary
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}