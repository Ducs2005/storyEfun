package com.example.storyefun.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.storyefun.viewModel.PostViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.storyefun.data.models.Post
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun PostScreen(
    viewModel: PostViewModel = PostViewModel()
) {
    val posts by viewModel.post

    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No posts available or error occurred")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
        ) {
            items(posts) { post ->
                PostCard(post = post) {}
            }
        }
    }
}

@Composable
fun PostCard(post: Post,  openLink: (String) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Unspecified),
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            if (!post.image.isNullOrEmpty()) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(post.image),
                    contentDescription = "Audiobook Cover",
                    modifier = Modifier
                        .size(100.dp)
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = post.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Author: ${post.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (!post.audio.isNullOrEmpty()) {
                    Text(
                        text = "Audio: ${post.audio}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            openSpotifyLink(context,post.audio)
                        }
                    )
                }
            }
        }
    }
}

fun openSpotifyLink(context: android.content.Context, url: String) {
    val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        `package` = "com.spotify.music"
    }

    // Kiểm tra nếu ứng dụng Spotify đã cài đặt
    if (spotifyIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(spotifyIntent) // Mở bằng ứng dụng Spotify
    } else {
        // Nếu không có Spotify, mở bằng trình duyệt
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}
