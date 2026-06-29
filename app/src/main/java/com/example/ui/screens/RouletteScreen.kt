package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@Composable
fun RouletteScreen(
    viewModel: CasinoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = viewModel.selectedGame.collectAsState().value ?: return
    val session by viewModel.userSession.collectAsState()
    val wager by viewModel.rouletteWager.collectAsState()
    val selectedBets = viewModel.selectedRouletteBets
    val isSpinning by viewModel.isRouletteSpinning.collectAsState()
    val winningNumber by viewModel.rouletteWinningNumber.collectAsState()
    val winningColor by viewModel.rouletteWinningColor.collectAsState()
    val resultLabel by viewModel.rouletteResultLabel.collectAsState()
    val history = viewModel.rouletteHistory

    // Color definitions for roulette numbers
    val redNumbers = remember { setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36) }
    fun getNumColor(num: Int): Color {
        return when {
            num == 0 -> EmeraldGreen
            redNumbers.contains(num) -> CrimsonRed
            else -> Color(0xFF1E1E1E) // Slate Black
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkFeltBg)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title Header
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
                    text = "RTP: ${game.rtp}% • Single Zero Board",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            Box(modifier = Modifier.size(48.dp))
        }

        // Stats and Bets Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                    Text("WALLET", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

                val totalActiveBet = selectedBets.values.sum()
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL WAGER", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%,d", totalActiveBet),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // ROLLING WHEEL ANIMATION BLOCK (Tick Scrolling)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MatteBlack),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (winningNumber != null) {
                    val num = winningNumber!!
                    val colorName = winningColor ?: "Red"
                    val col = getNumColor(num)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(50.dp)
                                .border(2.dp, GoldPrimary, CircleShape),
                            color = col,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = num.toString(),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "POCKET HIT!",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$colorName $num".uppercase(),
                                color = GoldPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    Text(
                        text = "PLACE YOUR CHIPS BELOW",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Vertical pointer overlay lines
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(GoldPrimary)
                        .align(Alignment.Center)
                )
            }
        }

        // Spinner / Winner Result Banner
        AnimatedVisibility(visible = resultLabel.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (resultLabel.contains("won")) EmeraldGreen.copy(alpha = 0.12f) else SurfaceCharcoal
                ),
                border = BorderStroke(
                    1.dp, 
                    if (resultLabel.contains("won")) EmeraldGreen else SurfaceLighter
                )
            ) {
                Text(
                    text = resultLabel,
                    color = if (resultLabel.contains("won")) EmeraldGreen else TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BET CHIP SIZER SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "CHIP VALUE:", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                
                val chipsSizers = listOf(10L, 50L, 100L, 500L, 1000L)
                chipsSizers.forEach { valAmt ->
                    val selected = valAmt == wager
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { viewModel.rouletteWager.value = valAmt }
                            .border(
                                2.dp,
                                if (selected) GoldPrimary else Color.Transparent,
                                CircleShape
                            ),
                        color = when (valAmt) {
                            10L -> Color(0xFFE0E0E0)
                            50L -> CrimsonRed
                            100L -> Color(0xFF0D47A1)
                            500L -> EmeraldGreen
                            else -> MatteBlack
                        },
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (valAmt >= 1000L) "${valAmt / 1000}K" else valAmt.toString(),
                                color = if (valAmt == 10L) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // THE ROULETTE BOARD BETTING GRID
        Text(
            text = "BETTING GRID (Tap sections to bet active chip):",
            color = TextGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Inside numbers grid (3 columns, 12 rows + 0)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceLighter, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCharcoal)
        ) {
            // ZERO SLOT (Felt Green)
            val zeroBet = selectedBets["0"] ?: 0L
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleRouletteBet("0") }
                    .background(if (zeroBet > 0) EmeraldGreen else DarkFeltBg)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "0  (Green Single Zero)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (zeroBet > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        ChipBadge(amount = zeroBet)
                    }
                }
            }

            HorizontalDivider(color = SurfaceLighter)

            // Numbers 1 to 36 (represented compact for scrolling/playing layout)
            Column {
                for (row in 0..11) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 1..3) {
                            val num = row * 3 + col
                            val numStr = num.toString()
                            val betOnNum = selectedBets[numStr] ?: 0L
                            val bgNumCol = getNumColor(num)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .border(0.5.dp, SurfaceLighter)
                                    .background(if (betOnNum > 0) GoldPrimary.copy(alpha = 0.25f) else bgNumCol)
                                    .clickable { viewModel.toggleRouletteBet(numStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = numStr,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (betOnNum > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        ChipBadge(amount = betOnNum)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Outside Bets (Colors, Evens, Odds, Dozens)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Colors, Evens/Odds Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val redBet = selectedBets["Red"] ?: 0L
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (redBet > 0) CrimsonRed.copy(alpha = 0.4f) else CrimsonRed)
                        .clickable { viewModel.toggleRouletteBet("Red") }
                        .border(1.dp, if (redBet > 0) GoldPrimary else Color.Transparent, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (redBet > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            ChipBadge(amount = redBet)
                        }
                    }
                }

                val blackBet = selectedBets["Black"] ?: 0L
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (blackBet > 0) Color(0xFF3E3F4B) else Color(0xFF0F0F14))
                        .clickable { viewModel.toggleRouletteBet("Black") }
                        .border(1.dp, if (blackBet > 0) GoldPrimary else Color.Transparent, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("BLACK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (blackBet > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            ChipBadge(amount = blackBet)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val evenBet = selectedBets["Even"] ?: 0L
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCharcoal)
                        .clickable { viewModel.toggleRouletteBet("Even") }
                        .border(1.dp, if (evenBet > 0) GoldPrimary else SurfaceLighter, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("EVEN (2x)", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        if (evenBet > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            ChipBadge(amount = evenBet)
                        }
                    }
                }

                val oddBet = selectedBets["Odd"] ?: 0L
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCharcoal)
                        .clickable { viewModel.toggleRouletteBet("Odd") }
                        .border(1.dp, if (oddBet > 0) GoldPrimary else SurfaceLighter, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ODD (2x)", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        if (oddBet > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            ChipBadge(amount = oddBet)
                        }
                    }
                }
            }

            // Dozens Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1st Dozen", "2nd Dozen", "3rd Dozen").forEach { dozen ->
                    val dozBet = selectedBets[dozen] ?: 0L
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCharcoal)
                            .clickable { viewModel.toggleRouletteBet(dozen) }
                            .border(1.dp, if (dozBet > 0) GoldPrimary else SurfaceLighter, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dozen.uppercase(), color = TextLight, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            if (dozBet > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                ChipBadge(amount = dozBet)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SPIN CONTROL PANEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearRouletteBets() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSpinning && selectedBets.isNotEmpty()
            ) {
                Text("CLEAR CHIPS", fontWeight = FontWeight.Bold)
            }

            val totalActiveBet = selectedBets.values.sum()
            Button(
                onClick = { viewModel.spinRoulette() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSpinning && totalActiveBet > 0L && (session?.chips ?: 0L) >= totalActiveBet
            ) {
                if (isSpinning) {
                    CircularProgressIndicator(color = MatteBlack, modifier = Modifier.size(24.dp))
                } else {
                    Text("SPIN WHEEL", color = MatteBlack, fontWeight = FontWeight.Black)
                }
            }
        }

        // Recent result history
        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("RECENT RESULTS:", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(history) { num ->
                    Surface(
                        modifier = Modifier.size(28.dp),
                        color = getNumColor(num),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = num.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChipBadge(amount: Long) {
    Surface(
        color = GoldPrimary,
        shape = CircleShape,
        modifier = Modifier.size(18.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (amount >= 1000L) "${amount / 1000}K" else amount.toString(),
                color = MatteBlack,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
