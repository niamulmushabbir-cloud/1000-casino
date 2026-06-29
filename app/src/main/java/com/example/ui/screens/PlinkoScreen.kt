package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun PlinkoScreen(
    viewModel: CasinoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = viewModel.selectedGame.collectAsState().value ?: return
    val session by viewModel.userSession.collectAsState()
    val wager by viewModel.plinkoWager.collectAsState()
    val balls = viewModel.plinkoBalls
    val payouts = viewModel.plinkoPayouts

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(16.dp)
    ) {
        // Header
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
                    text = "Physics Drop Peg Board • RTP: ${game.rtp}%",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Balance & Stats Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal.copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
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
                    Text("DROP WAGER", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

        // PLINKO TRIANGULAR PEG BOARD CANVAS WITH LIVE BALLS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f)
                .border(1.dp, SurfaceLighter, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                
                // Canvas board drawing
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw triangular peg rows (8 rows)
                    val rowsCount = 8
                    val startY = 20f
                    val spacingY = (h - 60f) / rowsCount

                    for (row in 0 until rowsCount) {
                        val pegsInRow = row + 3
                        val spacingX = w / (pegsInRow - 1)
                        val rowY = startY + row * spacingY

                        for (col in 0 until pegsInRow) {
                            val pegX = col * spacingX
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = 4f,
                                center = Offset(pegX, rowY)
                            )
                        }
                    }

                    // 2. Draw live balls bouncing
                    synchronized(balls) {
                        for (ball in balls) {
                            val curRow = ball.y
                            val ballRowY = startY + curRow * spacingY
                            
                            // Map normalized x (0 to 1) across the row width
                            val pegsInRow = curRow.toInt() + 3
                            val ballX = ball.x * w

                            drawCircle(
                                color = AccentCyan,
                                radius = 8f,
                                center = Offset(ballX, ballRowY)
                            )
                            
                            // Underglow shadow
                            drawCircle(
                                color = AccentCyan.copy(alpha = 0.3f),
                                radius = 14f,
                                center = Offset(ballX, ballRowY)
                            )
                        }
                    }
                }

                // Bucket Layout display at the absolute bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    payouts.forEachIndexed { idx, factor ->
                        val binColor = when {
                            factor >= 2.0f -> CrimsonRed
                            factor >= 1.0f -> GoldDark
                            else -> Color(0xFF2C2D35)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(binColor)
                                .border(0.5.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${factor}x",
                                color = if (factor >= 1.0f) Color.White else TextGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CHIP DROP CONTROL BOARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "DROP STAKE", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.plinkoWager.value = (wager - game.minBet).coerceAtLeast(game.minBet) }
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
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.plinkoWager.value = (wager + game.minBet).coerceIn(game.minBet, game.maxBet) }
                    ) {
                        Text("+", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DROP CHIP TRIGGER BUTTON
        Button(
            onClick = { viewModel.dropPlinkoBall() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(14.dp),
            enabled = (session?.chips ?: 0L) >= wager
        ) {
            Text(
                text = "DROP CHIP 🪙",
                color = MatteBlack,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
