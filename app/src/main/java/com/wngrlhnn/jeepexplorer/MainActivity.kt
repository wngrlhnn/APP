package com.wngrlhnn.jeepexplorer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Movie
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

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
    ALL("הכול"), NEON("ניאון"), FUN("כיף"), SPACE("חלל"), NATURE("טבע")
}

private data class GifItem(
    val id: Int,
    val name: String,
    val subtitle: String,
    val category: GifCategory,
    val resName: String
)

private val gifs = listOf(
    GifItem(1, "Neon Rings", "טבעות ניאון מסתובבות", GifCategory.NEON, "neon"),
    GifItem(2, "Bouncing Ball", "כדור קופץ בלופ", GifCategory.FUN, "ball"),
    GifItem(3, "Star Field", "כוכבים מנצנצים", GifCategory.SPACE, "stars"),
    GifItem(4, "Pixel Fire", "אש פיקסלים זוהרת", GifCategory.FUN, "fire"),
    GifItem(5, "Ocean Waves", "גלים בתנועה", GifCategory.NATURE, "waves"),
    GifItem(6, "Rotating Sun", "שמש מסתובבת", GifCategory.NATURE, "sun"),
    GifItem(7, "Glitch Burst", "אפקט גליץ׳ צבעוני", GifCategory.NEON, "glitch"),
    GifItem(8, "Orbit", "כדור במסלול", GifCategory.SPACE, "orbit")
)

@Composable
private fun GifGalleryApp() {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GifCategory.ALL) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }
    var selected by remember { mutableStateOf<GifItem?>(null) }

    selected?.let { item ->
        GifDetails(item, item.id in favorites, {
            favorites = if (item.id in favorites) favorites - item.id else favorites + item.id
        }) { selected = null }
        return
    }

    val filtered = gifs.filter {
        val text = query.isBlank() || it.name.contains(query, true) || it.subtitle.contains(query, true)
        val cat = category == GifCategory.ALL || it.category == category
        val fav = !favoritesOnly || it.id in favorites
        text && cat && fav
    }

    Scaffold(
        containerColor = Color(0xFF080910),
        bottomBar = {
            Text(
                "GIF Gallery • ${filtered.size} GIFים • 100% אופליין",
                color = Color.White.copy(alpha = .55f),
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0E1018)).padding(14.dp)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(Color(0xFF080910)).padding(padding)) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("GIF GALLERY", color = Color(0xFF7CFFCB), style = MaterialTheme.typography.labelLarge)
                Text("GIFים שזזים באמת", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("גלריה מגניבה • עובדת גם בלי אינטרנט", color = Color.White.copy(alpha = .55f))
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
                    GifCard(item, item.id in favorites, {
                        favorites = if (item.id in favorites) favorites - item.id else favorites + item.id
                    }) { selected = item }
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
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .size(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = .55f))
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
            Spacer(Modifier.height(16.dp))
            Text(
                "GIF מונפש שנמצא בתוך האפליקציה. אין צורך באינטרנט כדי להפעיל אותו.",
                color = Color.White.copy(alpha = .65f)
            )
        }
    }
}

@Composable
private fun GifPlayer(name: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            GifView(context).apply {
                gifResId = context.resources.getIdentifier(name, "drawable", context.packageName)
            }
        },
        update = { view ->
            view.gifResId = view.context.resources.getIdentifier(name, "drawable", view.context.packageName)
            view.loadMovie()
        }
    )
}

private class GifView(context: Context) : View(context) {
    var gifResId: Int = 0
    private var movie: Movie? = null
    private var started = 0L
    private var loadedId = 0

    fun loadMovie() {
        if (gifResId == 0 || gifResId == loadedId) return
        loadedId = gifResId
        movie = Movie.decodeStream(resources.openRawResource(gifResId))
        started = SystemClock.uptimeMillis()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val m = movie ?: run { loadMovie(); return }
        val duration = if (m.duration() > 0) m.duration() else 1000
        m.setTime(((SystemClock.uptimeMillis() - started) % duration).toInt())
        val scale = minOf(width.toFloat() / m.width(), height.toFloat() / m.height())
        val dx = (width - m.width() * scale) / 2f
        val dy = (height - m.height() * scale) / 2f
        canvas.save()
        canvas.translate(dx, dy)
        canvas.scale(scale, scale)
        m.draw(canvas, 0f, 0f)
        canvas.restore()
        postInvalidateOnAnimation()
    }
}
