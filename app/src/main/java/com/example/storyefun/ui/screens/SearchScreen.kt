package com.example.storyefun.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.storyefun.data.models.Book
import com.example.storyefun.ui.theme.LocalAppColors
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val appColors = LocalAppColors.current
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var active by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    var searchMode by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
    ) {
        SearchTextField(
            query = query,
            onQueryChange = { newQuery ->
                query = newQuery
                searchMode = true
                searchResults = emptyList()
                if (newQuery.isNotEmpty()) {
                    performSearch(db, newQuery) { results ->
                        searchResults = results.sortedBy { it.first }
                        isLoading = false
                    }
                } else {
                    searchResults = emptyList()
                }
            },
            onSearch = {
                if (query.isNotEmpty()) {
                    scope.launch {
                        searchMode = false
                        selectedBooks = emptyList()
                        isLoading = true
                        searchAndFetchBooks(db, query) { books ->
                            selectedBooks = books
                            isLoading = false
                        }
                    }
                    keyboardController?.hide()
                }
            },
            active = active,
            onActiveChange = { newActive ->
                active = newActive
                if (!newActive && query.isNotEmpty()) {
                    performSearch(db, query) { results ->
                        searchResults = results.sortedBy { it.first }
                    }
                } else if (query.isEmpty()) {
                    searchResults = emptyList()
                }
            }
        )

        when {
            isLoading -> {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = appColors.textSecondary
                )
            }
            !searchMode && selectedBooks.isEmpty() -> {
                Text(
                    text = "No results found",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = appColors.textSecondary
                )
            }
            !searchMode -> {
                BookList(
                    books = selectedBooks,
                    onBookClick = { book ->
                        navController.navigate("bookDetail/${book.id}")
                    }
                )
            }
            searchMode && searchResults.isEmpty() -> {
                Text(
                    text = "No results found",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = appColors.textSecondary
                )
            }
            searchMode -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appColors.background)
                ) {
                    items(searchResults) { (name, id) ->
                        ListItem(
                            modifier = Modifier.clickable {
                                navController.navigate("bookDetail/$id")
                            },
                            headlineContent = {
                                Text(
                                    text = name,
                                    color = appColors.textPrimary
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

fun searchAndFetchBooks(
    db: FirebaseFirestore,
    query: String,
    onResult: (List<Book>) -> Unit
) {
    db.collection("books")
        .whereGreaterThanOrEqualTo("name", query)
        .whereLessThan("name", query + '\uf8ff')
        .get()
        .addOnSuccessListener { documents ->
            val books = documents.map { doc ->
                Book(
                    id = doc.id,
                    name = doc.getString("name") ?: "Unknown",
                    author = doc.getString("author") ?: "Unknown",
                    description = doc.getString("description") ?: "No Description",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            println("Books fetched: $books")
            onResult(books)
        }
        .addOnFailureListener { exception ->
            println("Error fetching books: ${exception.message}")
            onResult(emptyList())
        }
}

fun performSearch(db: FirebaseFirestore, query: String, onResult: (List<Pair<String, String>>) -> Unit) {
    db.collection("books")
        .whereGreaterThanOrEqualTo("name", query)
        .whereLessThan("name", query + '\uf8ff')
        .get()
        .addOnSuccessListener { documents ->
            val results = documents.mapNotNull { doc ->
                val name = doc.getString("name")
                val id = doc.id
                if (name != null) name to id else null
            }
            println("Search results: $results")
            onResult(results)
        }
        .addOnFailureListener { exception ->
            println("Error: ${exception.message}")
            onResult(emptyList())
        }
}

@Composable
fun SearchHistoryChip(keyword: String, onClick: (String) -> Unit) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.tagColor.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick(keyword) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = keyword,
                style = MaterialTheme.typography.headlineSmall,
                color = appColors.textPrimary
            )
        }
    }
}

@Composable
fun BookList(
    books: List<Book>,
    onBookClick: (Book) -> Unit = {}
) {
    val appColors = LocalAppColors.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.background)
    ) {
        items(books.size) { index ->
            val book = books[index]
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onBookClick(book) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.background),
                border = BorderStroke(1.dp, appColors.textSecondary)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = "Book Cover",
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                            .border(1.dp, appColors.textSecondary),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = book.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = appColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Text(
                        text = book.author,
                        color = appColors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit
) {
    val appColors = LocalAppColors.current
    OutlinedTextField(
        value = query,
        onValueChange = { newQuery ->
            onQueryChange(newQuery.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(appColors.background, shape = RoundedCornerShape(8.dp)),
        placeholder = {
            Text(
                "Search for books...",
                color = appColors.textSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search Icon",
                tint = appColors.textSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear Search",
                        tint = appColors.textSecondary
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = appColors.textPrimary,
            unfocusedTextColor = appColors.textPrimary,
            focusedContainerColor = appColors.background,
            unfocusedContainerColor = appColors.background,
            focusedIndicatorColor = appColors.buttonOrange,
            unfocusedIndicatorColor = appColors.textSecondary,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onActiveChange(false)
                onSearch()
            }
        )
    )
}