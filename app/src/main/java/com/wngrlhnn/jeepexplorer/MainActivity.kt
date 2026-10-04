package com.wngrlhnn.jeepexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFD9F36A),
            onPrimary = Color(0xFF172000),
            background = Color(0xFF090B0D),
            surface = Color(0xFF14181B)
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

    val selectedJeep = jeeps.firstOrNull { it.id == selected }
    if (selectedJeep != null) {
        JeepDetails(
            jeep = selectedJeep,
            favorite = selectedJeep.id in favoriteIds,
            onFavorite = {
                favoriteIds = if (selectedJeep.id in favoriteIds) {
                    favoriteIds - selectedJeep.id
                } else {
                    favoriteIds + selectedJeep.id
                }
            },
            onBack = { selected = null }
        )
        return
    }

    val filtered = jeeps.filter { jeep ->
        val categoryMatch = category == Category.ALL || jeep.category == category
        val queryMatch = query.isBlank() ||
            jeep.name.contains(query, ignoreCase = true) ||
            jeep.subtitle.contains(query, ignoreCase = true)
        categoryMatch && queryMatch
    }

    Scaffold(
        containerColor = Color(0xFF090B0D),
        bottomBar = {
            Surface(color = Color(0xFF101316), modifier = Modifier.navigationBarsPadding()) {
                Text(
                    text = "Jeep Explorer • ${filtered.size} ג׳יפים",
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(14.dp)
                )
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(Color(0xFF090B0D)).padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("JEEP", color = Color(0xFFD9F36A), style = MaterialTheme.typography.labelLarge)
                        Text(
                            "עולם של ג׳יפים",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Surface(color = Color(0xFFD9F36A), shape = CircleShape) {
                        Text(
                            "4×4",
                            color = Color(0xFF172000),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("חפש Wrangler, Gladiator...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF15191C),
                        unfocusedContainerColor = Color(0xFF15191C),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.values().forEach { item ->
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
                items(filtered, key = { it.id }) { jeep ->
                    JeepCard(
                        jeep = jeep,
                        favorite = jeep.id in favoriteIds,
                        onFavorite = {
                            favoriteIds = if (jeep.id in favoriteIds) {
                                favoriteIds - jeep.id
                            } else {
                                favoriteIds + jeep.id
                            }
                        },
                        onClick = { selected = jeep.id }
                    )
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
        color = Color(0xFF14181B)
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(150.dp).background(
                    Brush.linearGradient(listOf(Color(jeep.color), Color(0xFF0E1012)))
                ).padding(10.dp)
            ) {
                JeepIllustration(
                    body = jeep.color,
                    accent = jeep.accent,
                    pickup = jeep.category == Category.PICKUP
                )
                IconButton(
                    onClick = onFavorite,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "מועדף",
                        tint = if (favorite) Color(0xFFFF6D7A) else Color.White
                    )
                }
            }
            Column(Modifier.padding(14.dp)) {
                Text(jeep.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(jeep.subtitle, color = Color.White.copy(alpha = 0.58f))
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(jeep.accent).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        jeep.category.title,
                        color = Color(jeep.accent),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun JeepDetails(
    jeep: Jeep,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF090B0D))
    ) {
        Box(
            Modifier.fillMaxWidth().height(380.dp).background(
                Brush.verticalGradient(listOf(Color(jeep.color), Color(0xFF090B0D)))
            )
        ) {
            JeepIllustration(
                body = jeep.color,
                accent = jeep.accent,
                pickup = jeep.category == Category.PICKUP
            )
            Button(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                Text("חזרה")
            }
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "מועדף",
                    tint = if (favorite) Color(0xFFFF6D7A) else Color.White
                )
            }
        }

        Column(Modifier.padding(22.dp)) {
            Text(jeep.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(jeep.subtitle, color = Color(jeep.accent), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Surface(color = Color(0xFF14181B), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("ג׳יפ שנבחר", color = Color.White.copy(alpha = 0.55f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "גלול בין הדגמים, חפש לפי שם, סנן לפי קטגוריה וסמן את הג׳יפים שאתה הכי אוהב.",
                        color = Color.White
                    )
                }
            }
        }
    }
}
