package com.wngrlhnn.jeepexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JeepExplorerTheme { JeepExplorerApp() } }
    }
}

@Composable
private fun JeepExplorerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFD9F36A),
            onPrimary = Color(0xFF172000),
            background = Color(0xFF07090B),
            surface = Color(0xFF12171A)
        ),
        content = content
    )
}

@Composable
private fun JeepExplorerApp() {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.ALL) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var favoriteIds by remember { mutableStateOf(setOf<Int>()) }
    var selected by remember { mutableStateOf<Int?>(null) }

    val selectedJeep = jeeps.firstOrNull { it.id == selected }
    if (selectedJeep != null) {
        JeepDetails(
            jeep = selectedJeep,
            favorite = selectedJeep.id in favoriteIds,
            onFavorite = {
                favoriteIds = if (selectedJeep.id in favoriteIds) favoriteIds - selectedJeep.id else favoriteIds + selectedJeep.id
            },
            onBack = { selected = null }
        )
        return
    }

    val filtered = jeeps.filter { jeep ->
        val textMatch = query.isBlank() ||
            jeep.name.contains(query, true) ||
            jeep.subtitle.contains(query, true)
        val categoryMatch = category == Category.ALL || jeep.category == category
        val favoritesMatch = !favoritesOnly || jeep.id in favoriteIds
        textMatch && categoryMatch && favoritesMatch
    }

    Scaffold(
        containerColor = Color(0xFF07090B),
        bottomBar = {
            Surface(color = Color(0xFF0E1215)) {
                Text(
                    "Jeep Explorer • " + filtered.size + " ג׳יפים",
                    color = Color.White.copy(alpha = 0.58f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(Color(0xFF07090B)).padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("JEEP EXPLORER", color = Color(0xFFD9F36A), fontWeight = FontWeight.Black)
                Spacer(Modifier.height(3.dp))
                Text("גלריית ג׳יפים אמיתית", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("30 דגמים • תמונות מקומיות • 100% אופליין", color = Color.White.copy(alpha = 0.55f))
                Spacer(Modifier.height(15.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("חפש Wrangler, Gladiator...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF151A1D),
                            unfocusedContainerColor = Color(0xFF151A1D),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { favoritesOnly = !favoritesOnly },
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(
                            if (favoritesOnly) Color(0xFFD9F36A) else Color(0xFF151A1D)
                        )
                    ) {
                        Icon(
                            if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "מועדפים",
                            tint = if (favoritesOnly) Color(0xFF172000) else Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Category.values().toList()) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item.title) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (filtered.isEmpty()) {
                Text(
                    "לא מצאתי ג׳יפ כזה",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { jeep ->
                        JeepCard(
                            jeep = jeep,
                            favorite = jeep.id in favoriteIds,
                            onFavorite = {
                                favoriteIds = if (jeep.id in favoriteIds) favoriteIds - jeep.id else favoriteIds + jeep.id
                            },
                            onClick = { selected = jeep.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JeepCard(
    jeep: Jeep,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF12171A)
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(165.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Image(
                    painter = painterResource(id = jeep.photoRes),
                    contentDescription = jeep.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD07090B)))
                    )
                )
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).padding(9.dp)
                        .size(39.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "מועדף",
                        tint = if (favorite) Color(0xFFFF6D7A) else Color.White
                    )
                }
                Surface(
                    color = Color(0xDD101417),
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                ) {
                    Text(
                        jeep.category.title,
                        color = Color(jeep.accent),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
            Column(Modifier.padding(13.dp)) {
                Text(jeep.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(jeep.subtitle, color = Color.White.copy(alpha = 0.55f), maxLines = 2)
            }
        }
    }
}

@Composable
private fun JeepDetails(jeep: Jeep, favorite: Boolean, onFavorite: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF07090B))) {
        Box(Modifier.fillMaxWidth().height(410.dp)) {
            Image(
                painter = painterResource(id = jeep.photoRes),
                contentDescription = jeep.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.08f), Color(0xF707090B))
                    )
                )
            )
            Surface(
                color = Color.Black.copy(alpha = 0.46f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp).clickable(onClick = onBack)
            ) {
                Text("← חזרה", color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "מועדף",
                    tint = if (favorite) Color(0xFFFF6D7A) else Color.White
                )
            }
        }

        Column(Modifier.padding(22.dp)) {
            Text(jeep.name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(jeep.subtitle, color = Color(jeep.accent), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(18.dp))
            Surface(color = Color(0xFF12171A), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("מידע על הדגם", color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "קטגוריה: " + jeep.category.title + "\n\nהצילום ארוז בתוך האפליקציה ולכן הגלריה זמינה גם בלי אינטרנט.",
                        color = Color.White
                    )
                }
            }
        }
    }
}
