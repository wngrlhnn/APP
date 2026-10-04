package com.wngrlhnn.jeepexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var favoriteIds by remember { mutableStateOf(setOf<Int>()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }

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
        (category == Category.ALL || jeep.category == category) &&
        (query.isBlank() || jeep.name.contains(query, true) || jeep.subtitle.contains(query, true)) &&
        (!favoritesOnly || jeep.id in favoriteIds)
    }

    Scaffold(
        containerColor = Color(0xFF07090B),
        bottomBar = {
            Surface(color = Color(0xFF0E1215), modifier = Modifier.navigationBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OFFLINE • " + filtered.size + " ג׳יפים", color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelMedium)
                    Text("JEEP EXPLORER", color = Color(0xFFD9F36A), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HeroHeader()
                    Spacer(Modifier.height(14.dp))
                    SearchBar(query, { query = it }, favoritesOnly, { favoritesOnly = !favoritesOnly })
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Category.values().forEach { item ->
                            FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item.title) })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(if (favoritesOnly) "המועדפים שלי" else "כל הדגמים", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            items(filtered, key = { it.id }) { jeep ->
                JeepCard(
                    jeep = jeep,
                    favorite = jeep.id in favoriteIds,
                    onFavorite = { favoriteIds = if (jeep.id in favoriteIds) favoriteIds - jeep.id else favoriteIds + jeep.id },
                    onClick = { selected = jeep.id }
                )
            }

            if (filtered.isEmpty()) {
                item(span = { GridItemSpan(2) }) { EmptyState() }
            }
        }
    }
}

@Composable
private fun HeroHeader() {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(30.dp), color = Color(0xFF11171A)) {
        Box(
            Modifier.fillMaxWidth().height(190.dp).background(
                Brush.linearGradient(listOf(Color(0xFF26302A), Color(0xFF0D1113), Color(0xFF171C1F)))
            )
        ) {
            JeepIllustration(
                body = 0xFF39433A,
                accent = 0xFFD9F36A,
                pickup = false,
                modifier = Modifier.padding(start = 110.dp, top = 8.dp, end = 8.dp, bottom = 4.dp)
            )
            Column(Modifier.padding(22.dp).width(190.dp)) {
                Text("JEEP", color = Color(0xFFD9F36A), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text("תבחר את ההרפתקה הבאה שלך", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Surface(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp)) {
                    Text("30 דגמים • 100% אופליין", color = Color.White.copy(alpha = 0.78f), modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, favoritesOnly: Boolean, onToggleFavorites: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
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
            onClick = onToggleFavorites,
            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(if (favoritesOnly) Color(0xFFD9F36A) else Color(0xFF151A1D))
        ) {
            Icon(if (favoritesOnly) Icons.Default.Favorite else Icons.Default.Tune, contentDescription = "סינון", tint = if (favoritesOnly) Color(0xFF172000) else Color.White)
        }
    }
}

@Composable
private fun JeepCard(jeep: Jeep, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(24.dp), color = Color(0xFF12171A)) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(156.dp).background(Brush.linearGradient(listOf(Color(jeep.color), Color(0xFF0D1012)))).padding(8.dp)
            ) {
                JeepIllustration(body = jeep.color, accent = jeep.accent, pickup = jeep.category == Category.PICKUP)
                Surface(color = Color.Black.copy(alpha = 0.35f), shape = CircleShape, modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(onClick = onFavorite, modifier = Modifier.size(38.dp)) {
                        Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "מועדף", tint = if (favorite) Color(0xFFFF6D7A) else Color.White)
                    }
                }
            }
            Column(Modifier.padding(13.dp)) {
                Text(jeep.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(jeep.subtitle, color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Surface(color = Color(jeep.accent).copy(alpha = 0.13f), shape = RoundedCornerShape(10.dp)) {
                    Text(jeep.category.title, color = Color(jeep.accent), modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(Modifier.fillMaxWidth().padding(top = 24.dp), color = Color(0xFF12171A), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("לא מצאתי ג׳יפ כזה", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text("נסה חיפוש אחר או אפס את הסינון.", color = Color.White.copy(alpha = 0.55f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun JeepDetails(jeep: Jeep, favorite: Boolean, onFavorite: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF07090B))) {
        Box(
            Modifier.fillMaxWidth().height(390.dp).background(Brush.verticalGradient(listOf(Color(jeep.color), Color(0xFF07090B))))
        ) {
            JeepIllustration(body = jeep.color, accent = jeep.accent, pickup = jeep.category == Category.PICKUP, modifier = Modifier.padding(12.dp))
            Surface(color = Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
                Text("חזרה", color = Color.White, modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 16.dp, vertical = 10.dp))
            }
            Surface(color = Color.Black.copy(alpha = 0.35f), shape = CircleShape, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                IconButton(onClick = onFavorite) {
                    Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "מועדף", tint = if (favorite) Color(0xFFFF6D7A) else Color.White)
                }
            }
        }
        Column(Modifier.padding(22.dp)) {
            Text(jeep.name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(jeep.subtitle, color = Color(jeep.accent), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(18.dp))
            Surface(color = Color(0xFF12171A), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("פרופיל דגם", color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Text("קטגוריה: " + jeep.category.title + "\n\nהגלריה, החיפוש, המועדפים והאיורים עובדים ללא חיבור לאינטרנט.", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
