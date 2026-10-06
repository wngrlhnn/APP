package com.wngrlhnn.jeepexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import pl.droidsonroids.gif.GifImageView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GifGalleryTheme { GifGalleryApp() } }
    }
}

@Composable
private fun GifGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF7CFFCB),
            secondary = Color(0xFF9B7CFF),
            background = Color(0xFF080910),
            surface = Color(0xFF121522)
        ),
        content = content
    )
}

private enum class GifCategory(val title: String) {
    ALL("הכול"), WAVE("גלים"), CIRCLE("מעגלים")
}

private data class GifItem(
    val id: Int,
    val name: String,
    val subtitle: String,
    val category: GifCategory,
    val resName: String
)

private val gifs = List(5000) { index ->
    val id = index + 1
    val category = when { id <= 500 -> GifCategory.WAVE; id <= 1000 -> GifCategory.CIRCLE; id <= 2300 -> GifCategory.WAVE; else -> GifCategory.CIRCLE }
    GifItem(
        id = id,
        name = "GIF ${id.toString().padStart(4, '0')}",
        subtitle = if (category == GifCategory.WAVE) "דגל מונפש • אפקט גלים" else "דגל מונפש • אפקט מעגל",
        category = category,
        resName = "gif%04d".format(id)
    )
}

@Composable
private fun GifGalleryApp() {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GifCategory.ALL) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }
    var selected by remember { mutableStateOf<GifItem?>(null) }

    BackHandler(enabled = selected != null) {
        selected = null
    }

    selected?.let { item ->
        GifDetails(
            item = item,
            favorite = item.id in favorites,
            onFavorite = {
                favorites = if (item.id in favorites) favorites - item.id else favorites + item.id
            },
            onBack = { selected = null }
        )
        return
    }

    val filtered = gifs.filter {
        val text = query.isBlank() ||
            it.name.contains(query, true) ||
            it.subtitle.contains(query, true)
        val cat = category == GifCategory.ALL || it.category == category
        val fav = !favoritesOnly || it.id in favorites
        text && cat && fav
    }

    Scaffold(
        containerColor = Color(0xFF080910),
        bottomBar = {
            Text(
                "GIF Gallery • ${filtered.size} GIFים • אופליין",
                color = Color.White.copy(alpha = .55f),
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0E1018)).padding(14.dp)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(Color(0xFF080910)).padding(padding)) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("GIF GALLERY", color = Color(0xFF7CFFCB), style = MaterialTheme.typography.labelLarge)
                Text("5,000 GIFים מונפשים", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("המון GIFים מונפשים • חיפוש • קטגוריות • מועדפים", color = Color.White.copy(alpha = .55f))
                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("חפש GIF...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF151824),
                            unfocusedContainerColor = Color(0xFF151824),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { favoritesOnly = !favoritesOnly },
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp))
                            .background(if (favoritesOnly) Color(0xFF7CFFCB) else Color(0xFF151824))
                    ) {
                        Icon(
                            if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "מועדפים",
                            tint = if (favoritesOnly) Color(0xFF08110D) else Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GifCategory.values().toList()) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item.title) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { item ->
                    GifCard(
                        item = item,
                        favorite = item.id in favorites,
                        onFavorite = {
                            favorites = if (item.id in favorites) favorites - item.id else favorites + item.id
                        },
                        onClick = { selected = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun GifCard(item: GifItem, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF121522)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(175.dp)) {
                GifPlayer(item.resName, Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)))
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(40.dp)
                        .clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = .55f))
                ) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (favorite) Color(0xFFFF6D8B) else Color.White
                    )
                }
            }
            Column(Modifier.padding(13.dp)) {
                Text(item.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(item.subtitle, color = Color.White.copy(alpha = .55f), maxLines = 2)
            }
        }
    }
}

@Composable
private fun GifDetails(item: GifItem, favorite: Boolean, onFavorite: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF080910))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("← חזרה", color = Color.White, modifier = Modifier.clickable(onClick = onBack).padding(8.dp))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onFavorite) {
                Icon(
                    if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (favorite) Color(0xFFFF6D8B) else Color.White
                )
            }
        }
        Box(
            Modifier.fillMaxWidth().height(390.dp).padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(28.dp)).background(Color(0xFF111522))
        ) {
            GifPlayer(item.resName, Modifier.fillMaxSize())
        }
        Column(Modifier.padding(22.dp)) {
            Text(item.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(item.subtitle, color = Color(0xFF7CFFCB))
        }
    }
}

@Composable
private fun GifPlayer(name: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            GifImageView(context).apply {
                val resId = resources.getIdentifier(name, "drawable", context.packageName)
                if (resId != 0) setImageResource(resId)
            }
        },
        update = { view ->
            val resId = view.resources.getIdentifier(name, "drawable", view.context.packageName)
            if (resId != 0 && view.drawable == null) view.setImageResource(resId)
        }
    )
}
