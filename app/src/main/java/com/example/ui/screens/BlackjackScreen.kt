package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoCard
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun BlackjackScreen(
    viewModel: CasinoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = viewModel.selectedGame.collectAsState().value ?: return
    val session by viewModel.userSession.collectAsState()
    val wager by viewModel.blackjackWager.collectAsState()
    val state by viewModel.blackjackState.collectAsState()
    val playerHand = viewModel.playerHand
    val dealerHand = viewModel.dealerHand
    val playerScore by viewModel.playerScore.collectAsState()
    val dealerScore by viewModel.dealerScore.collectAsState()
    val result by viewModel.blackjackResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkFeltBg)
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
                    fontSize = 18.sp
                )
                Text(
                    text = "Table Limit: ${game.minBet}-${game.maxBet} • RTP: ${game.rtp}%",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            
            Box(modifier = Modifier.size(48.dp)) // Spacer for alignment
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MY CHIPS", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 16.sp)
                    Text(
                        text = String.format("%,d", session?.chips ?: 0),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("CURRENT BET", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%,d", wager),
                    color = GoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DEALER SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state == "playing") "DEALER: ?" else "DEALER: $dealerScore",
                color = TextLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(120.dp)
            ) {
                itemsIndexed(dealerHand) { idx, card ->
                    // Hide dealer first card when playing
                    val hide = state == "playing" && idx == 0
                    CardItem(card = card, isHidden = hide)
                }
            }
        }

        // CONSOLE / BANNER RESULT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (result.isNotEmpty()) {
                Surface(
                    color = if (result.contains("Win")) EmeraldGreen.copy(alpha = 0.15f) 
                            else if (result.contains("Bust") || result.contains("Dealer Wins")) CrimsonRed.copy(alpha = 0.15f)
                            else SurfaceCharcoal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp, 
                        if (result.contains("Win")) EmeraldGreen 
                        else if (result.contains("Bust") || result.contains("Dealer Wins")) CrimsonRed
                        else SurfaceLighter
                    )
                ) {
                    Text(
                        text = result,
                        color = if (result.contains("Win")) EmeraldGreen else if (result.contains("Bust") || result.contains("Dealer Wins")) CrimsonRed else TextGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // PLAYER SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(120.dp)
            ) {
                itemsIndexed(playerHand) { _, card ->
                    CardItem(card = card, isHidden = false)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PLAYER SCORE: $playerScore",
                color = GoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE PANEL BASED ON STATE
        when (state) {
            "betting" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "ADJUST BET", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.blackjackWager.value = (wager - game.minBet).coerceAtLeast(game.minBet) },
                                enabled = wager > game.minBet
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
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.blackjackWager.value = (wager + game.minBet).coerceIn(game.minBet, game.maxBet) },
                                enabled = wager < game.maxBet
                            ) {
                                Text("+", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.startBlackjack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (session?.chips ?: 0L) >= wager
                ) {
                    Text("PLACE BET & DEAL", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }

            "playing" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.hitBlackjack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("HIT", color = MatteBlack, fontWeight = FontWeight.Black)
                    }

                    val canDouble = (session?.chips ?: 0L) >= wager
                    Button(
                        onClick = { viewModel.doubleBlackjack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canDouble
                    ) {
                        Text("DOUBLE", color = MatteBlack, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { viewModel.standBlackjack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("STAND", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }

            "resolved" -> {
                Button(
                    onClick = { viewModel.resetBlackjackToBetting() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("PLAY AGAIN", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }
            
            else -> {
                // Resolving dealers turns
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            }
        }
    }
}

@Composable
fun CardItem(card: CasinoCard, isHidden: Boolean) {
    Surface(
        modifier = Modifier
            .width(76.dp)
            .height(110.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .border(
                1.dp, 
                if (isHidden) GoldPrimary.copy(alpha = 0.5f) else Color.LightGray, 
                RoundedCornerShape(8.dp)
            ),
        color = if (isHidden) SurfaceCharcoal else Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isHidden) {
            // Card back
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(SurfaceLighter, RoundedCornerShape(4.dp))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👑",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Card front
            val textColor = if (card.isRed) CrimsonRed else Color.Black
            
            Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                // Top-Left rank + suit
                Column(
                    modifier = Modifier.align(Alignment.TopStart),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.value,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = card.suit,
                        color = textColor,
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }

                // Center big suit symbol
                Text(
                    text = card.suit,
                    color = textColor,
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Bottom-Right upside down/normal rank + suit
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.suit,
                        color = textColor,
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                    Text(
                        text = card.value,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
