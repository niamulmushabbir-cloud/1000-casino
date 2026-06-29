package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CasinoViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: CasinoViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by viewModel.isAdminUnlocked.collectAsState()
    val session by viewModel.userSession.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatteBlack)
            .padding(16.dp)
    ) {
        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Admin Panel",
                    tint = PurplePrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "ADMIN DASHBOARD",
                    color = TextLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
            IconButton(
                onClick = { viewModel.currentTab.value = "lobby" }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Admin Panel",
                    tint = TextLight
                )
            }
        }

        if (!isUnlocked) {
            // Password Verification Gateway Screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(SurfaceLighter),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Gateway",
                                tint = PurplePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "SECURITY GATEWAY",
                            color = TextLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter secure security key to access backend simulation controllers.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                loginError = false
                            },
                            label = { Text("Enter Security Key") },
                            placeholder = { Text("admin123") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray)
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Warning else Icons.Default.Face
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = "Toggle password visibility", tint = TextGray)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = SurfaceLighter,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = TextGray,
                                focusedContainerColor = SurfaceCharcoal,
                                unfocusedContainerColor = SurfaceCharcoal
                            ),
                            singleLine = true,
                            isError = loginError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (loginError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "❌ Invalid Security Passcode. Try again.",
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Dev Tip: Default access key is admin123",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val success = viewModel.unlockAdmin(passwordInput)
                                if (success) {
                                    passwordInput = ""
                                    loginError = false
                                } else {
                                    loginError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                "AUTHORIZE ACCESS",
                                color = PurpleActiveOn,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Admin Panel Logged In Controllers
            val luckMode by viewModel.adminLuckMode.collectAsState()
            var showResetConfirm by remember { mutableStateOf(false) }
            
            var customChipsInput by remember { mutableStateOf("") }
            var customLevelInput by remember { mutableStateOf("") }
            var customPasswordInput by remember { mutableStateOf("") }
            var passwordChangeSuccess by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Active Connection Status Indicator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(EmeraldGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "SIMULATION CONTROL ACTIVE",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Live modifying local session data",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.lockAdmin() },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceLighter)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Admin",
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Section 2: Luck skewer controller (Rigged settings)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "1. HOUSE LUCK OVERRIDE ENGINE",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val modes = listOf(
                                    Triple("normal", "FAIR", PurplePrimary),
                                    Triple("player_wins", "GOD MODE", EmeraldGreen),
                                    Triple("house_wins", "RIGGED LOSS", CrimsonRed)
                                )

                                modes.forEach { (modeKey, modeName, color) ->
                                    val isSelected = luckMode == modeKey
                                    Button(
                                        onClick = { viewModel.setAdminLuckMode(modeKey) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) color else SurfaceLighter
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = modeName,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = if (isSelected) MatteBlack else TextLight
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val explanation = when (luckMode) {
                                "player_wins" -> "🟢 Active: Player hitting Jackpots in Slots, Royal Flush in Video Poker, drift extremes in Plinko, dealer low cards in Blackjack, high rocket fly in Crash, and wins in Roulette."
                                "house_wins" -> "🔴 Active: Player guaranteed to lose. Slots get zero lines, Blackjack dealer gets natural 21, Roulette hits worst pocket, Crash rocket explodes instantly at 1.00x, Plinko center drop."
                                else -> "🟡 Active: Pure normal odds using standard verified Random Number Generator."
                            }

                            Text(
                                text = explanation,
                                color = TextGray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Section 3: Balance / Currency modifier
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "2. CHIPS SIMULATION INJECTOR",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Balance: ${String.format("%,d", session?.chips ?: 0)} Chips",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf(
                                    "+50K" to 50000L,
                                    "+500K" to 500000L,
                                    "+5M" to 5000000L,
                                    "SET 0" to 0L
                                )
                                presets.forEach { (label, amount) ->
                                    Button(
                                        onClick = {
                                            if (amount == 0L) {
                                                viewModel.adminSetChips(0L)
                                            } else {
                                                viewModel.adminAddChips(amount)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLighter),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Text(label, fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customChipsInput,
                                    onValueChange = { customChipsInput = it.filter { c -> c.isDigit() } },
                                    label = { Text("Custom Amount") },
                                    placeholder = { Text("100000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight,
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = SurfaceLighter,
                                        focusedContainerColor = SurfaceCharcoal,
                                        unfocusedContainerColor = SurfaceCharcoal
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val amt = customChipsInput.toLongOrNull() ?: 0L
                                        viewModel.adminSetChips(amt)
                                        customChipsInput = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Text("SET", fontWeight = FontWeight.Bold, color = PurpleActiveOn)
                                }
                            }
                        }
                    }
                }

                // Section 4: Level / XP Modifier
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "3. LEVEL & XP CALIBRATOR",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current: Level ${session?.level ?: 1} (${String.format("%,d", session?.xp ?: 0)} XP)",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val levelPresets = listOf(
                                    "LEVEL 5" to 5 to 2000L,
                                    "LEVEL 20" to 20 to 9500L,
                                    "LEVEL 100" to 100 to 49500L
                                )
                                levelPresets.forEach { (pair, xp) ->
                                    val (label, lvl) = pair
                                    Button(
                                        onClick = { viewModel.adminSetLevelAndXp(lvl, xp) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLighter),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Text(label, fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customLevelInput,
                                    onValueChange = { customLevelInput = it.filter { c -> c.isDigit() } },
                                    label = { Text("Custom Level") },
                                    placeholder = { Text("50") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight,
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = SurfaceLighter,
                                        focusedContainerColor = SurfaceCharcoal,
                                        unfocusedContainerColor = SurfaceCharcoal
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val lvl = customLevelInput.toIntOrNull() ?: 1
                                        val reqXp = (lvl - 1) * 500L
                                        viewModel.adminSetLevelAndXp(lvl, reqXp)
                                        customLevelInput = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Text("SET", fontWeight = FontWeight.Bold, color = PurpleActiveOn)
                                }
                            }
                        }
                    }
                }

                // Section 5: Security gate pass modifiers
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "4. PASSCODE MANAGEMENT",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customPasswordInput,
                                    onValueChange = {
                                        customPasswordInput = it
                                        passwordChangeSuccess = false
                                    },
                                    label = { Text("New Admin Passcode") },
                                    placeholder = { Text("admin123") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight,
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = SurfaceLighter,
                                        focusedContainerColor = SurfaceCharcoal,
                                        unfocusedContainerColor = SurfaceCharcoal
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        if (customPasswordInput.isNotEmpty()) {
                                            viewModel.changeAdminPassword(customPasswordInput)
                                            customPasswordInput = ""
                                            passwordChangeSuccess = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Text("UPDATE", fontWeight = FontWeight.Bold, color = PurpleActiveOn)
                                }
                            }

                            if (passwordChangeSuccess) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✅ Passcode successfully updated!",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Section 6: DB wiping / hard reset
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "5. COLD SYSTEM RECOVERY WIPE",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Resets user profile, transactions log, game favoriting, and all historical records back to factory defaults.",
                                color = TextGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (!showResetConfirm) {
                                Button(
                                    onClick = { showResetConfirm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("HARD SANITIZE DATABASE", color = Color.White, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(BorderStroke(1.dp, CrimsonRed), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "⚠️ ARE YOU ABSOLUTELY SURE?",
                                        color = CrimsonRed,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "This deletes all achievements progress, play hours, custom currencies, and custom settings permanently.",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showResetConfirm = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLighter),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("CANCEL", color = TextLight, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.adminResetDatabase()
                                                showResetConfirm = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("YES, WIPE ALL", color = Color.White, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
