package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun CrashScreen(
    viewModel: CasinoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = viewModel.selectedGame.collectAsState().value ?: return
    val session by viewModel.userSession.collectAsState()
    val wager by viewModel.crashWager.collectAsState()
    val state by viewModel.crashState.collectAsState() // "betting", "flying", "crashed"
    val multiplier by viewModel.crashMultiplier.collectAsState()
    val cashedOutAt by viewModel.cashOutMultiplier.collectAsState()
    val resultLabel by viewModel.crashResultLabel.collectAsState()

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
                    text = "High Volatility Arcade • RTP: ${game.rtp}%",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Balance section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("WALLET CHIPS", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                Text("ACTIVE MULTIPLIER", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${String.format("%.2f", multiplier)}x",
                    color = if (state == "crashed") CrimsonRed else EmeraldGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REAL-TIME GRAPH FLIGHT PATH CANVAS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f)
                .border(1.dp, SurfaceLighter, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                
                // Trajectory drawing
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw glowing grid lines
                    val gridLinesCount = 6
                    for (i in 1..gridLinesCount) {
                        val x = (w / gridLinesCount) * i
                        val y = (h / gridLinesCount) * i
                        drawLine(Color.Gray.copy(alpha = 0.15f), Offset(x, 0f), Offset(x, h))
                        drawLine(Color.Gray.copy(alpha = 0.15f), Offset(0f, y), Offset(w, y))
                    }

                    if (state == "flying" || state == "crashed") {
                        // Plot flight path
                        val path = Path()
                        path.moveTo(40f, h - 40f)
                        
                        // Limit progression representation to max 10x scale
                        val progressX = (multiplier - 1f).coerceIn(0f, 9f) / 9f
                        val endX = 40f + progressX * (w - 120f)
                        val endY = (h - 40f) - progressX * progressX * (h - 120f)

                        path.quadraticTo(
                            (40f + endX) / 2f,
                            h - 40f,
                            endX,
                            endY
                        )

                        // Draw path line
                        drawPath(
                            path = path,
                            color = if (state == "crashed") CrimsonRed else AccentCyan,
                            style = Stroke(width = 4f)
                        )

                        // Draw rocket head
                        if (state != "crashed") {
                            drawCircle(
                                color = GoldPrimary,
                                radius = 10f,
                                center = Offset(endX, endY)
                            )
                        }
                    }
                }

                // Rocket Emojis Floating along trajectory or static
                if (state == "flying" && multiplier > 1.0f) {
                    val progressX = (multiplier - 1f).coerceIn(0f, 9f) / 9f
                    val offsetX = (progressX * 72).coerceIn(0f, 82f)
                    val offsetY = (progressX * progressX * 65).coerceIn(0f, 75f)

                    Text(
                        text = "🚀",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(
                                start = (30 + offsetX).dp,
                                bottom = (30 + offsetY).dp
                            )
                    )
                } else if (state == "crashed") {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "💥", fontSize = 56.sp)
                            Text(
                                text = "CRASHED!",
                                color = CrimsonRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    // Ready to Launch
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "READY FOR TELEMETRY LAUNCH",
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Floating telemetry text overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TELEMETRY STATUS: " + if (state == "flying") "FLYING" else if (state == "crashed") "CRASHED" else "STANDBY",
                        color = if (state == "flying") AccentCyan else if (state == "crashed") CrimsonRed else TextGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MULT: ${String.format("%.2f", multiplier)}X",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RESULT DISPLAY BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (resultLabel.isNotEmpty()) {
                Surface(
                    color = if (cashedOutAt != null) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (cashedOutAt != null) EmeraldGreen else CrimsonRed)
                ) {
                    Text(
                        text = resultLabel,
                        color = if (cashedOutAt != null) EmeraldGreen else CrimsonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ACTIONS BASED ON CRASH STATE
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
                        Text(text = "LAUNCH WAGER", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.crashWager.value = (wager - game.minBet).coerceAtLeast(game.minBet) },
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
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.crashWager.value = (wager + game.minBet).coerceIn(game.minBet, game.maxBet) },
                                enabled = wager < game.maxBet
                            ) {
                                Text("+", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.startCrashGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (session?.chips ?: 0L) >= wager
                ) {
                    Text("LAUNCH ROCKET", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }

            "flying" -> {
                val cashed = cashedOutAt != null
                Button(
                    onClick = { viewModel.cashOutCrash() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cashed) EmeraldGreen else GoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !cashed
                ) {
                    Text(
                        text = if (cashed) "CASHED OUT!" else "CASH OUT NOW (${String.format("%,d", (wager * multiplier).toLong())} chips)",
                        color = MatteBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }

            "crashed" -> {
                Button(
                    onClick = { viewModel.resetCrashGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("LAUNCH NEXT MISSION", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
