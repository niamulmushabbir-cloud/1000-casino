package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun StatsScreen(
    viewModel: CasinoViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.allGameStats.collectAsState()
    val transactions by viewModel.recentTransactions.collectAsState()
    val session by viewModel.userSession.collectAsState()

    var activeTab by remember { mutableStateOf("ledger") } // "ledger", "achievements"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(16.dp)
    ) {
        // Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Achievements",
                tint = PurplePrimary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "STATS & ACHIEVEMENTS",
                color = TextLight,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
        }

        // Summary Cards Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalGamesPlayed = remember(stats) { stats.sumOf { it.playCount } }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TOTAL PLAYS", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%,d", totalGamesPlayed),
                        color = PurplePrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            val maxWin = remember(stats) { stats.maxOfOrNull { it.biggestWin } ?: 0L }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("BIGGEST WIN", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%,d", maxWin),
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Tab Selector Row
        TabRow(
            selectedTabIndex = if (activeTab == "ledger") 0 else 1,
            containerColor = Color.Transparent,
            contentColor = PurplePrimary,
            divider = {}
        ) {
            Tab(
                selected = activeTab == "ledger",
                onClick = { activeTab = "ledger" },
                text = { Text("RECENT LEDGER", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeTab == "achievements",
                onClick = { activeTab = "achievements" },
                text = { Text("MILESTONES", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Body Content
        if (activeTab == "ledger") {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recorded game wagers yet.\nHead over to the Lobby and start spinning!",
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(transactions) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = log.gameName,
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = log.gameCategory.uppercase(),
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val isGain = log.netGain >= 0
                                    Text(
                                        text = (if (isGain) "+" else "") + String.format("%,d", log.netGain),
                                        color = if (isGain) EmeraldGreen else CrimsonRed,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "BET: ${String.format("%,d", log.wager)}",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Achievements Tab
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Determine completion state
                val chipsAccumulated = session?.chips ?: 0L
                val totalPlays = stats.sumOf { it.playCount }
                val maxSingleWin = stats.maxOfOrNull { it.biggestWin } ?: 0L

                val achs = listOf(
                    Achievement("Vegas Monarch", "Accumulate a balance of 100,000 chips.", chipsAccumulated >= 100000L, "👑"),
                    Achievement("Spins Apprentice", "Play any Slots machine 5 times.", stats.filter { it.gameId in 1..600 }.sumOf { it.playCount } >= 5, "🎰"),
                    Achievement("Card Counter", "Resolve a Blackjack hand.", stats.filter { it.gameId in 601..750 }.sumOf { it.playCount } >= 1, "🃏"),
                    Achievement("Double Zero Hero", "Resolve a Roulette spin.", stats.filter { it.gameId in 751..850 }.sumOf { it.playCount } >= 1, "🎡"),
                    Achievement("Apollo Flight", "Reach 2.0x multiplier in Crash.", maxSingleWin >= 200, "🚀"),
                    Achievement("Gravity Drop", "Drop a ball on Plinko.", stats.filter { it.gameId in 971..1000 }.sumOf { it.playCount } >= 1, "🎯"),
                    Achievement("Casino Whaler", "Unlock Level 5 by earning XP.", (session?.level ?: 1) >= 5, "💎")
                )

                items(achs) { ach ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ach.completed) EmeraldGreen.copy(alpha = 0.08f) else SurfaceCharcoal
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (ach.completed) EmeraldGreen.copy(alpha = 0.4f) else SurfaceLighter
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ach.badge,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.title,
                                    color = if (ach.completed) EmeraldGreen else PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = ach.requirement,
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            
                            Surface(
                                color = if (ach.completed) EmeraldGreen else SurfaceLighter,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (ach.completed) "CLAIMED" else "LOCKED",
                                    color = if (ach.completed) MatteBlack else TextLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Achievement(
    val title: String,
    val requirement: String,
    val completed: Boolean,
    val badge: String
)
