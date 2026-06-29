package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun SlotsScreen(
    viewModel: CasinoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = viewModel.selectedGame.collectAsState().value ?: return
    val session by viewModel.userSession.collectAsState()
    val isSpinning by viewModel.isSlotSpinning.collectAsState()
    val reels by viewModel.slotReels.collectAsState()
    val payout by viewModel.slotPayout.collectAsState()
    val winMessage by viewModel.slotWinMessage.collectAsState()
    val wager by viewModel.slotWager.collectAsState()

    // Setup theme-based colors for slot background
    val themeGradient = remember(game.theme) {
        when (game.theme) {
            "egypt" -> Brush.verticalGradient(listOf(Color(0xFF2E1C05), Color(0xFF5B3908), MatteBlack))
            "space" -> Brush.verticalGradient(listOf(Color(0xFF0F052E), Color(0xFF051C3B), MatteBlack))
            "neon" -> Brush.verticalGradient(listOf(Color(0xFF1F002C), Color(0xFF00152C), MatteBlack))
            "sweet" -> Brush.verticalGradient(listOf(Color(0xFF330514), Color(0xFF2C0212), MatteBlack))
            "pirate" -> Brush.verticalGradient(listOf(Color(0xFF141923), Color(0xFF0B0F17), MatteBlack))
            "atlantis" -> Brush.verticalGradient(listOf(Color(0xFF001C31), Color(0xFF003D5B), MatteBlack))
            "dragon" -> Brush.verticalGradient(listOf(Color(0xFF2E0202), Color(0xFF500303), MatteBlack))
            "spooky" -> Brush.verticalGradient(listOf(Color(0xFF171324), Color(0xFF0D0D14), MatteBlack))
            "safari" -> Brush.verticalGradient(listOf(Color(0xFF231F11), Color(0xFF3E331A), MatteBlack))
            else -> Brush.verticalGradient(listOf(SurfaceCharcoal, MatteBlack))
        }
    }

    var showPaytable by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeGradient)
            .padding(16.dp)
    ) {
        // Top Back & Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextLight)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = game.name.uppercase(),
                    color = GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Theme: ${game.themeLabel} • RTP: ${game.rtp}%",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = { showPaytable = !showPaytable }) {
                Icon(Icons.Default.Info, contentDescription = "Paytable", tint = TextGold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance & Jackpot Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MatteBlack.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MY BALANCE", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = String.format("%,d", session?.chips ?: 0),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }

                if (game.isJackpotActive) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PROGRESSIVE JACKPOT", color = CrimsonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%,d", game.jackpotAmount),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // THE SLOT REELS BOARD (3x3 GRID)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(3.dp, GoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Outer gold highlight board
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3 columns of reels
                    for (colIndex in 0..2) {
                        val colSymbols = reels.getOrNull(colIndex) ?: listOf("", "", "")
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MatteBlack)
                                .border(1.dp, SurfaceLighter, RoundedCornerShape(12.dp)),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (rowIndex in 0..2) {
                                val symbol = colSymbols.getOrNull(rowIndex) ?: ""
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Custom style for reels
                                    Text(
                                        text = symbol,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLight,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                                
                                if (rowIndex < 2) {
                                    HorizontalDivider(color = SurfaceCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Payout Banner
        AnimatedVisibility(
            visible = winMessage.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (payout > 0) EmeraldGreen.copy(alpha = 0.15f) else SurfaceCharcoal
                ),
                border = BorderStroke(
                    1.dp, 
                    if (payout > 0) EmeraldGreen else SurfaceLighter
                )
            ) {
                Text(
                    text = winMessage,
                    color = if (payout > 0) EmeraldGreen else TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }

        // Betting control, stake adjustments
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WAGER AMOUNT",
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.slotWager.value = (wager - game.minBet).coerceAtLeast(game.minBet) },
                        enabled = !isSpinning && wager > game.minBet
                    ) {
                        Text("-", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        color = MatteBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            text = String.format("%,d", wager),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.slotWager.value = (wager + game.minBet).coerceIn(game.minBet, game.maxBet) },
                        enabled = !isSpinning && wager < game.maxBet
                    ) {
                        Text("+", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(
                        onClick = { viewModel.slotWager.value = game.maxBet },
                        enabled = !isSpinning
                    ) {
                        Text("MAX", color = CrimsonRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Giant Spin Button
        Button(
            onClick = { viewModel.spinSlots() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(16.dp),
            enabled = !isSpinning && (session?.chips ?: 0L) >= wager
        ) {
            if (isSpinning) {
                CircularProgressIndicator(color = MatteBlack, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "SPIN REELS",
                    color = MatteBlack,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    // Paytable Dialog
    if (showPaytable) {
        AlertDialog(
            onDismissRequest = { showPaytable = false },
            title = { Text("Symbol Paytable", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Matching 3 of any symbol yields big multipliers:", color = TextLight, fontSize = 12.sp)
                    Divider(color = SurfaceLighter)
                    
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("👑 High-tier (Themed, 777, Gold):", color = GoldPrimary, fontSize = 11.sp)
                        Text("12x Wager", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("💎 Medium-tier (Diamonds, Scarabs):", color = TextLight, fontSize = 11.sp)
                        Text("4x Wager", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("🔔 Low-tier (Bells, Cherries):", color = TextLight, fontSize = 11.sp)
                        Text("1.5x Wager", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("🔥 Diagonals Match:", color = TextLight, fontSize = 11.sp)
                        Text("Extra 5x Multiplier", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaytable = false }) {
                    Text("GOT IT", color = GoldPrimary)
                }
            },
            containerColor = SurfaceCharcoal
        )
    }
}
