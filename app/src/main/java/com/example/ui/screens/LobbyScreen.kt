package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CasinoGame
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun LobbyScreen(
    viewModel: CasinoViewModel,
    onGameSelected: (CasinoGame) -> Unit,
    modifier: Modifier = Modifier
) {
    val session by viewModel.userSession.collectAsState()
    val games by viewModel.filteredGames.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val stats by viewModel.allGameStats.collectAsState()
    
    val favoriteIds = remember(stats) { stats.filter { it.isFavorite }.map { it.gameId }.toSet() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
    ) {
        // 1. TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User profile with Avatar Circle & XP bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(SurfaceLighter)
                        .border(1.5.dp, PurplePrimary, RoundedCornerShape(21.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VP",
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text(
                        text = "WELCOME BACK",
                        color = TextGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vip Player",
                            color = TextLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Surface(
                            color = GoldPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Lvl ${session?.level ?: 1}",
                                color = MatteBlack,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // XP bar
                    val xp = session?.xp ?: 0
                    val currentLevelXp = xp % 500
                    val progress = currentLevelXp.toFloat() / 500f
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SurfaceLighter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(PurplePrimary)
                            )
                        }
                        Text(
                            text = "${currentLevelXp}/500 XP",
                            color = TextGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Balance Pill Box
            Row(
                modifier = Modifier
                    .background(SurfaceCharcoal, RoundedCornerShape(20.dp))
                    .border(1.dp, SurfaceLighter, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "★",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%,d", session?.chips ?: 0),
                    color = PurplePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                // Plus button
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(PurplePrimary)
                        .clickable { viewModel.claimDailyReward() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = PurpleDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. JACKPOT DROPS HERO BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(PurplePrimary, PurpleDark)
                    )
                )
        ) {
            // Giant faded Slot Emoji
            Text(
                text = "🎰",
                fontSize = 72.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .alpha(0.2f)
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Jackpot Drops",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Win your share of 🪙 5,000,000!",
                        color = TextLight.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = { viewModel.claimDailyReward() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Play Now",
                        color = PurpleDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // DAILY REWARD SPIN CARD
        val lastSpin = session?.lastSpinTimestamp ?: 0L
        val canSpin = System.currentTimeMillis() - lastSpin >= 24 * 60 * 60 * 1000 || lastSpin == 0L
        
        if (canSpin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎡", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(
                                text = "Daily Spin Wheel Ready!",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Multiply reward with streaks!",
                                color = EmeraldGreen,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.claimDailyReward() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("SPIN", color = MatteBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. SEARCH & CATEGORIES BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Search 1,000 themed rooms...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = SurfaceLighter,
                focusedContainerColor = SurfaceCharcoal,
                unfocusedContainerColor = SurfaceCharcoal
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            shape = RoundedCornerShape(10.dp)
        )

        // Category scrollable filters
        val categories = listOf(
            "all" to "🌐 All 1000",
            "slots" to "🎰 Slots",
            "blackjack" to "🃏 BJ 21",
            "roulette" to "🎡 Roulette",
            "poker" to "👑 Poker",
            "crash" to "🚀 Crash",
            "plinko" to "🎯 Plinko",
            "favorites" to "⭐ Favs"
        )
        
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = PurplePrimary,
            indicator = {},
            divider = {}
        ) {
            categories.forEach { (catId, catLabel) ->
                val selected = selectedCategory == catId
                Tab(
                    selected = selected,
                    onClick = { viewModel.selectedCategory.value = catId },
                    text = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (selected) PurplePrimary else SurfaceCharcoal)
                                .border(
                                    1.dp,
                                    if (selected) Color.Transparent else SurfaceLighter,
                                    RoundedCornerShape(30.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = catLabel,
                                color = if (selected) PurpleActiveOn else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }
        }

        // 4. GAME GRID (1000 GAMES LAZY LOADED)
        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No games match your filters.",
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(games, key = { it.id }) { game ->
                    val isFav = favoriteIds.contains(game.id)
                    GameItemCard(
                        game = game,
                        isFavorite = isFav,
                        onFavToggle = { viewModel.toggleFavorite(game.id, isFav) },
                        onClick = { onGameSelected(game) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameItemCard(
    game: CasinoGame,
    isFavorite: Boolean,
    onFavToggle: () -> Unit,
    onClick: () -> Unit
) {
    // Determine card styling based on category
    val (icon, badgeColor) = when (game.category) {
        "slots" -> "🎰" to CrimsonRed
        "blackjack" -> "🃏" to EmeraldGreen
        "roulette" -> "🎡" to AccentCyan
        "poker" -> "👑" to GoldPrimary
        "crash" -> "🚀" to Color(0xFFFF8F00)
        else -> "🎯" to Color(0xFF9C27B0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            0.5.dp, 
            if (game.isJackpotActive) GoldPrimary.copy(alpha = 0.5f) else SurfaceLighter
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Icon, Category Badge & Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 24.sp)
                    
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = game.category.uppercase(),
                            color = badgeColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Game Name
                Text(
                    text = game.name,
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Theme Sub-label
                Text(
                    text = game.themeLabel,
                    color = TextGray,
                    fontSize = 10.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progressive Jackpot label if active
                if (game.isJackpotActive) {
                    Surface(
                        color = GoldPrimary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "🔥 JCKPT: ${String.format("%,d", game.jackpotAmount)}",
                            color = MatteBlack,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }

                // RTP and Stakes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RTP: ${game.rtp}%",
                        color = EmeraldGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BET: ${game.minBet}-${game.maxBet}",
                        color = TextGray,
                        fontSize = 9.sp
                    )
                }
            }

            // Favorite Overlay Button
            IconButton(
                onClick = onFavToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) CrimsonRed else TextGray.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Game ID watermarked faintly
            Text(
                text = "#${game.id}",
                color = TextGray.copy(alpha = 0.12f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}
