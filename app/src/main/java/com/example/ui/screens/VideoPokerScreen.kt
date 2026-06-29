package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun VideoPokerScreen(
    viewModel: CasinoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = viewModel.selectedGame.collectAsState().value ?: return
    val session by viewModel.userSession.collectAsState()
    val wager by viewModel.pokerWager.collectAsState()
    val state by viewModel.pokerState.collectAsState() // "betting", "deal", "draw"
    val hand = viewModel.pokerHand
    val held = viewModel.pokerHeld
    val resultLabel by viewModel.pokerResultLabel.collectAsState()
    val payout by viewModel.pokerPayout.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0066)) // Deep retro arcade blue
            .padding(12.dp)
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
                    fontSize = 18.sp
                )
                Text(
                    text = "Retro Jacks or Better • RTP: ${game.rtp}%",
                    color = TextLight.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // RETRO ARCADE PAYTABLE BOARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .border(2.dp, GoldPrimary, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = MatteBlack),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "ROYAL FLUSH................. 250X",
                    color = GoldPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "STRAIGHT FLUSH..............  50X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "FOUR OF A KIND..............  25X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "FULL HOUSE..................   9X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "FLUSH.......................   6X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "STRAIGHT....................   4X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "THREE OF A KIND.............   3X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "TWO PAIR....................   2X",
                    color = TextLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = "JACKS OR BETTER.............   1X",
                    color = EmeraldGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MY BALANCE", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 14.sp)
                        Text(
                            text = String.format("%,d", session?.chips ?: 0),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("GAME WAGER", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%,d", wager),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PLAYER HAND ROW
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f),
            contentAlignment = Alignment.Center
        ) {
            if (hand.isEmpty()) {
                Text(
                    text = "CHOOSE WAGER & PRESS DEAL!",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    hand.forEachIndexed { idx, card ->
                        val isHeld = held[idx]
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.togglePokerHold(idx) }
                        ) {
                            // Held Indicator overlay
                            Surface(
                                color = if (isHeld) GoldPrimary else Color.Transparent,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = "HELD",
                                    color = if (isHeld) MatteBlack else Color.Transparent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            CardItem(card = card, isHidden = false)
                        }
                    }
                }
            }
        }

        // Output results notification
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (resultLabel.isNotEmpty()) {
                Surface(
                    color = if (payout > 0) EmeraldGreen.copy(alpha = 0.15f) else SurfaceCharcoal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (payout > 0) EmeraldGreen else SurfaceLighter)
                ) {
                    Text(
                        text = resultLabel,
                        color = if (payout > 0) EmeraldGreen else TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PANEL CONTROLS
        when (state) {
            "betting" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "BET SETTING", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.pokerWager.value = (wager - game.minBet).coerceAtLeast(game.minBet) },
                                enabled = wager > game.minBet
                            ) {
                                Text("-", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Surface(
                                color = MatteBlack,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(90.dp)
                            ) {
                                Text(
                                    text = String.format("%,d", wager),
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.pokerWager.value = (wager + game.minBet).coerceIn(game.minBet, game.maxBet) },
                                enabled = wager < game.maxBet
                            ) {
                                Text("+", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.startVideoPoker() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (session?.chips ?: 0L) >= wager
                ) {
                    Text("DEAL HAND", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }

            "deal" -> {
                Button(
                    onClick = { viewModel.drawVideoPoker() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DRAW CARDS", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }

            "draw" -> {
                Button(
                    onClick = { viewModel.resetPokerToBetting() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("NEXT DEAL", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
