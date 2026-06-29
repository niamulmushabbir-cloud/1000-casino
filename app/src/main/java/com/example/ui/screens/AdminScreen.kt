package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import com.example.data.PlayerAccount
import com.example.ui.CasinoViewModel
import com.example.ui.SubAdmin
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: CasinoViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by viewModel.isAdminUnlocked.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val session by viewModel.userSession.collectAsState()
    val subAdmins by viewModel.subAdmins.collectAsState()
    val allPlayers by viewModel.allPlayers.collectAsState()
    val loggedInSubAdmin by viewModel.loggedInSubAdmin.collectAsState()

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    // Dialog state for Main Admin configuring Sub-Admins
    var editingSubAdmin by remember { mutableStateOf<SubAdmin?>(null) }
    var subUsernameInput by remember { mutableStateOf("") }
    var subPasscodeInput by remember { mutableStateOf("") }
    var subTransferAmountInput by remember { mutableStateOf("") }

    // Dialog state for Main Admin managing Player Wallets directly
    var editingPlayer by remember { mutableStateOf<PlayerAccount?>(null) }
    var playerTransferAmountInput by remember { mutableStateOf("") }
    var playerSearchQuery by remember { mutableStateOf("") }

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
                    text = if (activeRole == "sub_admin") "SUB-ADMIN PORTAL" else "ADMIN DASHBOARD",
                    color = TextLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
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
            // Role Login gateway
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
                            text = "SYSTEM GATEWAY",
                            color = TextLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Access secure control simulation & distributed transaction terminals.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = {
                                usernameInput = it
                                loginError = false
                            },
                            label = { Text("System Username") },
                            placeholder = { Text("admin / sub1") },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                loginError = false
                            },
                            label = { Text("Security Key (Passcode)") },
                            placeholder = { Text("admin123 / sub123") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray)
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Warning else Icons.Default.Face
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = "Toggle key visibility", tint = TextGray)
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
                                text = "❌ Invalid Credentials. Try again.",
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Default keys: Main is 'admin' (admin123). Sub is 'sub1' (sub123).",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val role = viewModel.unlockAdminOrSubAdmin(usernameInput, passwordInput)
                                if (role != "none") {
                                    passwordInput = ""
                                    usernameInput = ""
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
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else if (activeRole == "sub_admin") {
            // Restricted Sub-Admin Layout
            val sub = loggedInSubAdmin ?: SubAdmin(1, "sub1", "sub123", 0L)
            var targetUser by remember { mutableStateOf("") }
            var transferAmount by remember { mutableStateOf("") }
            var subAdminMessage by remember { mutableStateOf<String?>(null) }
            var isSuccessMessage by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connection Status & Balance
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "SUB-ADMIN TERMINAL ACTIVE",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Operator: ${sub.username.uppercase()}",
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.lockAdmin() },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceLighter)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock Terminal",
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = SurfaceLighter, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Your Wallet Balance:", color = TextGray, fontSize = 12.sp)
                                Text(
                                    "${String.format("%,d", sub.balance)} 🪙",
                                    color = GoldPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Balance Transfer Module
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "BALANCE DISTRIBUTION TERMINAL",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = targetUser,
                                onValueChange = { targetUser = it; subAdminMessage = null },
                                label = { Text("Recipient Player Username") },
                                placeholder = { Text("player1") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = SurfaceLighter,
                                    focusedContainerColor = SurfaceCharcoal,
                                    unfocusedContainerColor = SurfaceCharcoal
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = transferAmount,
                                onValueChange = { transferAmount = it.filter { c -> c.isDigit() }; subAdminMessage = null },
                                label = { Text("Chips Amount to Transfer") },
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
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (subAdminMessage != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = subAdminMessage!!,
                                    color = if (isSuccessMessage) EmeraldGreen else CrimsonRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val amt = transferAmount.toLongOrNull() ?: 0L
                                    if (targetUser.isBlank() || amt <= 0) {
                                        subAdminMessage = "❌ Please specify valid username and amount"
                                        isSuccessMessage = false
                                        return@Button
                                    }
                                    val res = viewModel.subAdminTransferToUser(targetUser, amt)
                                    if (res == "Success") {
                                        subAdminMessage = "✅ Sent ${String.format("%,d", amt)} chips to $targetUser"
                                        isSuccessMessage = true
                                        targetUser = ""
                                        transferAmount = ""
                                    } else {
                                        subAdminMessage = "❌ $res"
                                        isSuccessMessage = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("INITIATE BALANCE TRANSFER", color = MatteBlack, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Quick selector list
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "QUICK PLAYER SELECT",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (allPlayers.isEmpty()) {
                                Text("No registered system players found.", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 12.dp))
                            } else {
                                allPlayers.forEach { player ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceLighter)
                                            .clickable { targetUser = player.username }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccountBox, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(player.username, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Text(
                                            "${String.format("%,d", player.chips)} 🪙",
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Main Admin Screen Controllers
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
                // Connection Status
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
                                        text = "Role: MAIN SYSTEM ADMINISTRATOR",
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

                // NEW: SUB-ADMIN SETUP EDITOR PANEL (Up to 20 sub-admins)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. SUB-ADMIN WALLET ENGINE (20 SLOTS)",
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Surface(
                                    color = PurplePrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "20 ACTIVE",
                                        color = PurplePrimary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Scrollable list of 20 SubAdmins
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                            ) {
                                subAdmins.forEach { sub ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceLighter)
                                            .clickable {
                                                subUsernameInput = sub.username
                                                subPasscodeInput = sub.passcode
                                                subTransferAmountInput = ""
                                                editingSubAdmin = sub
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "#${sub.id}",
                                                    color = PurplePrimary,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                Text(
                                                    text = sub.username.uppercase(),
                                                    color = TextLight,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "Key: ${sub.passcode}",
                                                color = TextGray,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${String.format("%,d", sub.balance)} 🪙",
                                                color = GoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Sub",
                                                tint = TextGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // NEW: REGISTERED PLAYER ACCOUNT WALLETS Direct Injector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "2. PLAYER WALLET MANAGER (ADD / CUT CHIPS)",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = playerSearchQuery,
                                onValueChange = { playerSearchQuery = it },
                                label = { Text("Filter Player Accounts") },
                                placeholder = { Text("Search username") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = SurfaceLighter,
                                    focusedContainerColor = SurfaceCharcoal,
                                    unfocusedContainerColor = SurfaceCharcoal
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val filteredPlayers = allPlayers.filter {
                                it.username.lowercase().contains(playerSearchQuery.lowercase())
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                            ) {
                                if (filteredPlayers.isEmpty()) {
                                    Text(
                                        "No players matching filters found.",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    filteredPlayers.forEach { player ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceLighter)
                                                .clickable {
                                                    playerTransferAmountInput = ""
                                                    editingPlayer = player
                                                }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(player.username, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${String.format("%,d", player.chips)} 🪙",
                                                    color = GoldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Icon(
                                                    Icons.Default.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = TextGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Luck override engine
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "3. HOUSE LUCK OVERRIDE ENGINE",
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
                                "player_wins" -> "🟢 Active: Live session hitting Jackpots in Slots, Royal Flush in Video Poker, drift extremes in Plinko, dealer low cards in Blackjack, high rocket fly in Crash, and wins in Roulette."
                                "house_wins" -> "🔴 Active: Live session guaranteed to lose. Slots get zero lines, Blackjack dealer gets natural 21, Roulette hits worst pocket, Crash rocket explodes instantly at 1.00x, Plinko center drop."
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

                // Section 4: Live Session Chip Injector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "4. CHIPS SIMULATION INJECTOR",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Live Balance: ${String.format("%,d", session?.chips ?: 0)} Chips",
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

                // Section 5: Passcode Management
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "5. PASSCODE MANAGEMENT",
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

                // Section 6: DB Cold system recovery
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "6. COLD SYSTEM RECOVERY WIPE",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Resets user profiles, sub-admins, transaction logs, and favorited states back to original factory defaults.",
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
                                        text = "This deletes all player accounts, daily streaks, play session history, and custom wallet data permanently.",
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

    // 1. DIALOG: Main Admin editing SubAdmin
    if (editingSubAdmin != null) {
        val sub = editingSubAdmin!!
        Dialog(onDismissRequest = { editingSubAdmin = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "CONFIGURE SUB-ADMIN #${sub.id}",
                        color = TextLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = subUsernameInput,
                        onValueChange = { subUsernameInput = it },
                        label = { Text("Sub-Admin Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = SurfaceLighter,
                            focusedContainerColor = SurfaceCharcoal,
                            unfocusedContainerColor = SurfaceCharcoal
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = subPasscodeInput,
                        onValueChange = { subPasscodeInput = it },
                        label = { Text("Sub-Admin Passcode") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = SurfaceLighter,
                            focusedContainerColor = SurfaceCharcoal,
                            unfocusedContainerColor = SurfaceCharcoal
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Wallet Balance: ${String.format("%,d", sub.balance)} 🪙", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = subTransferAmountInput,
                        onValueChange = { subTransferAmountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Add/Cut Wallet Balance") },
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val amt = subTransferAmountInput.toLongOrNull() ?: 0L
                                if (amt > 0) {
                                    viewModel.mainAdminAddCutSubAdmin(sub.id, amt, isAdd = false)
                                    editingSubAdmin = viewModel.subAdmins.value.find { it.id == sub.id }
                                    subTransferAmountInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CUT BAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amt = subTransferAmountInput.toLongOrNull() ?: 0L
                                if (amt > 0) {
                                    viewModel.mainAdminAddCutSubAdmin(sub.id, amt, isAdd = true)
                                    editingSubAdmin = viewModel.subAdmins.value.find { it.id == sub.id }
                                    subTransferAmountInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ADD BAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { editingSubAdmin = null },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLighter),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CLOSE", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (subUsernameInput.isNotBlank() && subPasscodeInput.isNotBlank()) {
                                    viewModel.saveSubAdmin(sub.copy(username = subUsernameInput, passcode = subPasscodeInput))
                                    editingSubAdmin = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("SAVE ACCESS", color = PurpleActiveOn, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    // 2. DIALOG: Main Admin editing Player Wallet directly
    if (editingPlayer != null) {
        val player = editingPlayer!!
        Dialog(onDismissRequest = { editingPlayer = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "MANAGE PLAYER WALLET",
                        color = TextLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Player: ${player.username.uppercase()}",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Wallet Balance: ${String.format("%,d", player.chips)} 🪙", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = playerTransferAmountInput,
                        onValueChange = { playerTransferAmountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Inject/Cut Chips Amount") },
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val amt = playerTransferAmountInput.toLongOrNull() ?: 0L
                                if (amt > 0) {
                                    viewModel.mainAdminAddCutUserBalance(player.username, amt, isAdd = false)
                                    // Live refresh player object from Room db background thread safely
                                    viewModel.viewModelScope.launch {
                                        val db = com.example.data.CasinoDatabase.getDatabase(viewModel.getApplication())
                                        val fresh = db.casinoDao().getPlayerAccount(player.username)
                                        if (fresh != null) { editingPlayer = fresh }
                                    }
                                    playerTransferAmountInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CUT CHIPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amt = playerTransferAmountInput.toLongOrNull() ?: 0L
                                if (amt > 0) {
                                    viewModel.mainAdminAddCutUserBalance(player.username, amt, isAdd = true)
                                    // Live refresh player object from Room db background thread safely
                                    viewModel.viewModelScope.launch {
                                        val db = com.example.data.CasinoDatabase.getDatabase(viewModel.getApplication())
                                        val fresh = db.casinoDao().getPlayerAccount(player.username)
                                        if (fresh != null) { editingPlayer = fresh }
                                    }
                                    playerTransferAmountInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ADD CHIPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { editingPlayer = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLighter),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", color = TextLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
