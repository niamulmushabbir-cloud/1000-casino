package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
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
fun ShopScreen(
    viewModel: CasinoViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.userSession.collectAsState()

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
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Shop",
                tint = PurplePrimary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "CASINO SHOP",
                color = TextLight,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
        }

        // Profile details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TOTAL CHIPS", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 18.sp, modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = String.format("%,d", session?.chips ?: 0),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL XP ACCUMULATED", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = String.format("%,d", session?.xp ?: 0),
                            color = AccentCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Free Hourly Chips
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Free Hourly Chips",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Claim 1,000 chips instantly!",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                        
                        Button(
                            onClick = { viewModel.claimFreeHourlyChips() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("CLAIM", color = MatteBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // XP Exchange Options Header
            item {
                Text(
                    text = "REDEEM YOUR XP FOR CHIPS:",
                    color = PurplePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Packages list
            val packages = listOf(
                XpPackage(500L, 5000L, "Starter Loot", "🪙 Standard entry package"),
                XpPackage(2000L, 25000L, "High-Roller Stack", "🔥 Great value boost"),
                XpPackage(10000L, 150000L, "VIP Gold Briefcase", "✨ Premium wealth chest"),
                XpPackage(50000L, 1000000L, "Billionaire vault", "🏆 Ultimate casino treasure")
            )

            items(packages) { pkg ->
                val canAfford = (session?.xp ?: 0L) >= pkg.costXp
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = pkg.name,
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = pkg.desc,
                                color = TextGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "COST: ${String.format("%,d", pkg.costXp)} XP",
                                    color = if (canAfford) PurplePrimary else CrimsonRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.purchaseChipsInShop(pkg.costXp, pkg.rewardChips) },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            enabled = canAfford
                        ) {
                            Text(
                                text = "+${String.format("%,d", pkg.rewardChips)}",
                                color = PurpleActiveOn,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class XpPackage(
    val costXp: Long,
    val rewardChips: Long,
    val name: String,
    val desc: String
)
