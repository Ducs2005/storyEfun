package com.example.storyefun.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.storyefun.data.models.Book
import com.example.storyefun.ui.components.BottomBar
import com.example.storyefun.ui.components.Header
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ThemeViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllBookScreen(navController: NavController, themeViewModel: ThemeViewModel) {
    val firestore = Firebase.firestore
    val books = remember { mutableStateListOf<Book>() }
    val context = LocalContext.current
    val theme = LocalAppColors.current // Access theme colors

    LaunchedEffect(Unit) {
        firestore.collection("books").get()
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

    Scaffold(
        topBar = {
            Column {
                Header(navController = navController, themeViewModel = themeViewModel)
                Divider(
                    color = theme.textSecondary.copy(alpha = 0.5f), // Use theme.textSecondary for divider
                    thickness = 1.dp
                )
            }
        },
        bottomBar = { BottomBar(navController, "home", themeViewModel = themeViewModel) },

        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.background) // Apply theme background
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                items(books) { book ->
                    AllBookCard(navController, book = book)
                }
            }
        }
    }
}

@Composable
fun AllBookCard(navController: NavController, book: Book) {
    val theme = LocalAppColors.current // Access theme colors

    Card(
        modifier = Modifier
            .clickable { navController.navigate("bookDetail/${book.id}") }
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor), // Use theme.backgroundColor
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Slight elevation for depth
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(4.dp) // Add padding inside card
        ) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = "Book Cover",
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = book.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = theme.textPrimary, // Use theme.textPrimary
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = book.author,
                color = theme.textSecondary, // Use theme.textSecondary
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}