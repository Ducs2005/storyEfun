package com.example.storyefun.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.storyefun.R
import com.example.storyefun.data.models.Book
import com.example.storyefun.data.models.Category
import com.example.storyefun.ui.components.BottomBar
import com.example.storyefun.ui.components.Header
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ThemeViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CategoriesScreen(navController: NavController, themeViewModel: ThemeViewModel) {
    val firestore = Firebase.firestore
    val categories = remember { mutableStateListOf<Category>() }
    val books = remember { mutableStateListOf<Book>() }
    val selectedCategoryId = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val theme = LocalAppColors.current // Access theme colors

    LaunchedEffect(Unit) {
        firestore.collection("categories").get()
            .addOnSuccessListener { result ->
                categories.clear()
                for (document in result) {
                    val category = document.toObject(Category::class.java)
                    categories.add(category)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load categories: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(selectedCategoryId.value) {
        selectedCategoryId.value?.let { categoryId ->
            firestore.collection("books")
                .whereArrayContains("categoryIDs", categoryId)
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
    }

    Scaffold(
        topBar = {
            Column {
                Header(navController = navController, themeViewModel = themeViewModel)
                Divider(
                    color = theme.textSecondary.copy(alpha = 0.5f), // Use theme.textSecondary
                    thickness = 1.dp
                )
            }
        },
        bottomBar = { BottomBar(navController, "category", themeViewModel = themeViewModel) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.background) // Use theme.background
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryId.value == category.id

                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .border(
                                width = 1.dp,
                                color = theme.textSecondary, // Use theme.textSecondary
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategoryId.value = category.id },
                        //.background(if (isSelected) theme.backgroundContrast2 else theme.backgroundColor),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) theme.backgroundContrast2.copy(0.7f) else theme.backgroundColor // Use theme colors
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Add slight elevation
                    ) {
                        Text(
                            text = category.name,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = theme.textPrimary, // Use theme.textPrimary
                            modifier = Modifier.padding(8.dp),

                            )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = theme.textSecondary // Use theme.textSecondary
            )

            if (selectedCategoryId.value != null) {
                BookList1(navController, books = books)
            }
        }
    }
}

@Composable
fun BookList1(navController: NavController, books: List<Book>) {
    val theme = LocalAppColors.current // Access theme colors

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        items(books) { book ->
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .wrapContentHeight()
                    .clickable { navController.navigate("bookDetail/${book.id}") },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = theme.backgroundColor), // Use theme.backgroundColor
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Add slight elevation
            ) {
                Column(
                    modifier = Modifier.padding(8.dp) // Add padding inside card
                ) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = book.name,
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.error)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = book.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = theme.textPrimary, // Use theme.textPrimary
                        maxLines = 1
                    )
                    Text(
                        text = book.author,
                        color = theme.textSecondary, // Use theme.textSecondary
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}