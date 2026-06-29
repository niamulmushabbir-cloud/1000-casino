package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CasinoViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CasinoViewModel = viewModel()
                val selectedGame by viewModel.selectedGame.collectAsState()
                val currentTab by viewModel.currentTab.collectAsState()

                if (selectedGame != null) {
                    // Open full-screen active game screen with back navigation
                    val activeGame = selectedGame!!
                    when (activeGame.category) {
                        "slots" -> SlotsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.selectedGame.value = null }
                        )
                        "blackjack" -> BlackjackScreen(
                            viewModel = viewModel,
                            onBack = { 
                                viewModel.resetBlackjackToBetting()
                                viewModel.selectedGame.value = null 
                            }
                        )
                        "roulette" -> RouletteScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearRouletteBets()
                                viewModel.selectedGame.value = null
                            }
                        )
                        "poker" -> VideoPokerScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.resetPokerToBetting()
                                viewModel.selectedGame.value = null
                            }
                        )
                        "crash" -> CrashScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.resetCrashGame()
                                viewModel.selectedGame.value = null
                            }
                        )
                        "plinko" -> PlinkoScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.selectedGame.value = null }
                        )
                    }
                } else {
                    // Main Lobby / Shell Layout
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                containerColor = SurfaceCharcoal,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == "lobby",
                                    onClick = { viewModel.currentTab.value = "lobby" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Lobby") },
                                    label = { Text("Lobby") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PurpleActiveOn,
                                        selectedTextColor = TextLight,
                                        indicatorColor = PurpleActiveBg,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentTab == "shop",
                                    onClick = { viewModel.currentTab.value = "shop" },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Shop") },
                                    label = { Text("Shop") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PurpleActiveOn,
                                        selectedTextColor = TextLight,
                                        indicatorColor = PurpleActiveBg,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentTab == "stats",
                                    onClick = { viewModel.currentTab.value = "stats" },
                                    icon = { Icon(Icons.Default.Star, contentDescription = "Stats") },
                                    label = { Text("Stats") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PurpleActiveOn,
                                        selectedTextColor = TextLight,
                                        indicatorColor = PurpleActiveBg,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        val modifierWithPadding = Modifier.padding(innerPadding)
                        when (currentTab) {
                            "lobby" -> LobbyScreen(
                                viewModel = viewModel,
                                onGameSelected = { game -> viewModel.selectedGame.value = game },
                                modifier = modifierWithPadding
                            )
                            "shop" -> ShopScreen(
                                viewModel = viewModel,
                                modifier = modifierWithPadding
                            )
                            "stats" -> StatsScreen(
                                viewModel = viewModel,
                                modifier = modifierWithPadding
                            )
                        }
                    }
                }
            }
        }
    }
}

