package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// Card structures for table games
data class CasinoCard(val suit: String, val value: String, val rank: Int) {
    val isRed: Boolean = suit == "♥" || suit == "♦"
    override fun toString(): String = "$value$suit"
}

data class PlinkoBall(
    var x: Float, // horizontal position (0.0 to 1.0)
    var y: Float, // vertical level (0 to maxRows)
    var velocityX: Float = 0f,
    val id: Int = Random.nextInt()
)

class CasinoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CasinoRepository

    // Global state flows from database
    val userSession: StateFlow<UserSession?>
    val allGameStats: StateFlow<List<GameStat>>
    val recentTransactions: StateFlow<List<TransactionHistory>>

    init {
        val database = CasinoDatabase.getDatabase(application)
        repository = CasinoRepository(database.casinoDao())
        
        userSession = repository.userSession.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        
        allGameStats = repository.allGameStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        recentTransactions = repository.recentTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize user session on launch
        viewModelScope.launch {
            repository.getOrCreateUserSession()
        }
    }

    // Lobby UI Navigation & Filters
    val currentTab = MutableStateFlow("lobby") // "lobby", "shop", "stats", "history"
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("all") // "all", "slots", "blackjack", "roulette", "poker", "crash", "plinko", "favorites"
    val selectedGame = MutableStateFlow<CasinoGame?>(null)

    // Derived filtered 1000 game list
    private val fullGameList = GameCatalog.getAllGames()
    
    val filteredGames: StateFlow<List<CasinoGame>> = combine(
        searchQuery,
        selectedCategory,
        allGameStats
    ) { query, category, stats ->
        val favorites = stats.filter { it.isFavorite }.map { it.gameId }.toSet()
        
        fullGameList.filter { game ->
            val matchesQuery = game.name.contains(query, ignoreCase = true) ||
                    game.themeLabel.contains(query, ignoreCase = true)
            
            val matchesCategory = when (category) {
                "all" -> true
                "favorites" -> favorites.contains(game.id)
                else -> game.category.equals(category, ignoreCase = true)
            }
            
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Favorite Helper
    fun toggleFavorite(gameId: Int, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(gameId, isCurrentlyFavorite)
        }
    }

    // Shop actions
    fun claimDailyReward() {
        val session = userSession.value ?: return
        val now = System.currentTimeMillis()
        // 24 hour check
        if (now - session.lastSpinTimestamp >= 24 * 60 * 60 * 1000 || session.lastSpinTimestamp == 0L) {
            val streak = if (now - session.lastSpinTimestamp < 48 * 60 * 60 * 1000) session.dailyRewardStreak + 1 else 1
            val chipsWon = 2000L * streak.coerceAtMost(7)
            viewModelScope.launch {
                repository.updateDailySpin(now, streak)
                repository.recordGamePlay(
                    gameId = 1001, // Special ID for reward spin
                    gameName = "Daily Reward Spin",
                    category = "lobby",
                    wager = 0L,
                    payout = chipsWon
                )
            }
        }
    }

    fun purchaseChipsInShop(costXP: Long, chipsAwarded: Long) {
        val session = userSession.value ?: return
        if (session.xp >= costXP) {
            viewModelScope.launch {
                repository.updateChipsAndXp(chipsAwarded, -costXP)
                repository.recordGamePlay(
                    gameId = 1002, // Special ID for shop
                    gameName = "Shop Purchase",
                    category = "shop",
                    wager = 0,
                    payout = chipsAwarded
                )
            }
        }
    }

    fun claimFreeHourlyChips() {
        viewModelScope.launch {
            repository.recordGamePlay(
                gameId = 1003,
                gameName = "Hourly Free Chips",
                category = "lobby",
                wager = 0,
                payout = 1000
            )
        }
    }

    // ==========================================
    // 1. SLOT MACHINE ENGINE
    // ==========================================
    val slotWager = MutableStateFlow(100L)
    val isSlotSpinning = MutableStateFlow(false)
    val slotReels = MutableStateFlow(listOf(listOf("777", "777", "777"), listOf("777", "777", "777"), listOf("777", "777", "777")))
    val slotPayout = MutableStateFlow(0L)
    val slotWinMessage = MutableStateFlow("")

    // Map theme to slot symbols
    private fun getThemeSymbols(theme: String): List<String> {
        return when (theme) {
            "egypt" -> listOf("𓀾 Pharaoh", "𓆣 Scarab", "𓃠 Anubis", "𓉴 Pyramid", "𓋹 Ankh", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "space" -> listOf("🛸 UFO", "🪐 Saturn", "🚀 Rocket", "⭐ Star", "👽 Alien", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "neon" -> listOf("🕶️ Visor", "👾 Synth", "🌆 City", "⚡ Laser", "💿 Disc", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "west" -> listOf("🤠 Sheriff", "🐎 Horse", "🌵 Cactus", "💰 Loot", "🔫 Colt", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "sweet" -> listOf("🍬 Candy", "🍫 Choco", "🍭 Lollipop", "🍓 Berry", "🍩 Donut", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "pirate" -> listOf("🏴‍☠️ Jolly", "🐙 Kraken", "🪙 Dubloon", "🗺️ Map", "⚔️ Cutlass", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "atlantis" -> listOf("🔱 Trident", "🧜 Mermaid", "🐚 Shell", "🐠 Fish", "🏛️ Sunken", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "classic" -> listOf("777", "💎 Diamond", "👑 Crown", "🔔 Bell", "🍉 Melon", "🍋 Lemon", "🍇 Grape", "🍒 Cherry")
            "dragon" -> listOf("🐲 Dragon", "🏮 Lantern", "🎋 Bonsai", "🪭 Fan", "🪙 Coin", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "gold" -> listOf("👑 Gold", "💰 Bag", "🍾 Bubbly", "💎 Carat", "💳 VIP", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "spooky" -> listOf("🧛 Dracula", "🧟 Zombie", "🎃 Pumpkin", "👻 Ghost", "🦇 Bat", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            "safari" -> listOf("🦁 Lion", "🦓 Zebra", "🐆 Cheetah", "🐘 Elephant", "🌳 Acacia", "🔔 Bell", "💎 Diamond", "🍒 Cherry")
            else -> listOf("777", "💎 Diamond", "👑 Crown", "🔔 Bell", "🍉 Melon", "🍋 Lemon", "🍇 Grape", "🍒 Cherry")
        }
    }

    fun spinSlots() {
        val game = selectedGame.value ?: return
        val currentChips = userSession.value?.chips ?: 0
        val wager = slotWager.value
        
        if (currentChips < wager || isSlotSpinning.value) return

        isSlotSpinning.value = true
        slotPayout.value = 0
        slotWinMessage.value = ""

        viewModelScope.launch {
            // Deduct wager initially
            repository.updateChipsAndXp(-wager, 0)
            
            val symbols = getThemeSymbols(game.theme)
            
            // Spin loop animation
            repeat(12) { step ->
                slotReels.value = List(3) {
                    List(3) { symbols.random() }
                }
                delay(80L + step * 20L)
            }

            // Final outcome
            val finalReels = List(3) {
                List(3) { symbols.random() }
            }
            slotReels.value = finalReels

            // Calculate wins
            var totalWinFactor = 0.0
            val winningLines = mutableListOf<String>()

            // 1. Horizontal paylines
            for (row in 0..2) {
                if (finalReels[0][row] == finalReels[1][row] && finalReels[1][row] == finalReels[2][row]) {
                    val sym = finalReels[0][row]
                    val factor = getSymbolMultiplier(sym) * 3
                    totalWinFactor += factor
                    winningLines.add("Line ${row + 1} ($sym Match)")
                } else if (finalReels[0][row] == finalReels[1][row] || finalReels[1][row] == finalReels[2][row]) {
                    // 2 matches
                    val sym = finalReels[1][row]
                    totalWinFactor += getSymbolMultiplier(sym) * 0.5
                }
            }

            // 2. Diagonals
            if (finalReels[0][0] == finalReels[1][1] && finalReels[1][1] == finalReels[2][2]) {
                val sym = finalReels[1][1]
                val factor = getSymbolMultiplier(sym) * 5
                totalWinFactor += factor
                winningLines.add("Diagonal Down ($sym Match)")
            }
            if (finalReels[0][2] == finalReels[1][1] && finalReels[1][1] == finalReels[2][0]) {
                val sym = finalReels[1][1]
                val factor = getSymbolMultiplier(sym) * 5
                totalWinFactor += factor
                winningLines.add("Diagonal Up ($sym Match)")
            }

            val winAmount = (wager * totalWinFactor).toLong()

            // Handle progressive jackpot hit! (Very rare 0.5% chance)
            val hitJackpot = game.isJackpotActive && Random.nextInt(200) == 77
            val finalPayout = if (hitJackpot) winAmount + game.jackpotAmount else winAmount

            slotPayout.value = finalPayout
            
            if (finalPayout > 0) {
                slotWinMessage.value = if (hitJackpot) {
                    "🎰 MEGA JACKPOT HIT! Won $finalPayout Chips!"
                } else {
                    "Payout: $finalPayout! Matches on " + winningLines.joinToString(", ").ifEmpty { "Mixed Symbols" }
                }
            } else {
                slotWinMessage.value = "Better luck next spin!"
            }

            // Commit outcome to database
            repository.recordGamePlay(
                gameId = game.id,
                gameName = game.name,
                category = "slots",
                wager = wager,
                payout = finalPayout
            )
            
            isSlotSpinning.value = false
        }
    }

    private fun getSymbolMultiplier(sym: String): Double {
        return when {
            sym.contains("Pharaoh") || sym.contains("UFO") || sym.contains("Visor") ||
            sym.contains("Sheriff") || sym.contains("Candy") || sym.contains("Jolly") ||
            sym.contains("Trident") || sym.startsWith("777") || sym.contains("Dragon") ||
            sym.contains("Gold") || sym.contains("Dracula") || sym.contains("Lion") -> 12.0
            
            sym.contains("Diamond") -> 6.0
            sym.contains("Scarab") || sym.contains("Saturn") || sym.contains("Synth") ||
            sym.contains("Horse") || sym.contains("Choco") || sym.contains("Kraken") ||
            sym.contains("Mermaid") || sym.contains("Crown") || sym.contains("Lantern") ||
            sym.contains("Bag") || sym.contains("Zombie") || sym.contains("Zebra") -> 4.0
            
            sym.contains("Bell") || sym.contains("Cherry") -> 1.5
            else -> 1.0
        }
    }

    // ==========================================
    // 2. BLACKJACK ENGINE
    // ==========================================
    val blackjackWager = MutableStateFlow(100L)
    val blackjackState = MutableStateFlow("betting") // "betting", "playing", "resolved"
    val playerHand = mutableStateListOf<CasinoCard>()
    val dealerHand = mutableStateListOf<CasinoCard>()
    val playerScore = MutableStateFlow(0)
    val dealerScore = MutableStateFlow(0)
    val blackjackResult = MutableStateFlow("")

    private var deck = mutableListOf<CasinoCard>()

    private fun generateDeck() {
        val suits = listOf("♥", "♦", "♣", "♠")
        val values = listOf(
            "2" to 2, "3" to 3, "4" to 4, "5" to 5, "6" to 6, "7" to 7, "8" to 8, "9" to 9, "10" to 10,
            "J" to 10, "Q" to 10, "K" to 10, "A" to 11
        )
        deck.clear()
        for (suit in suits) {
            for ((value, rank) in values) {
                deck.add(CasinoCard(suit, value, rank))
            }
        }
        deck.shuffle()
    }

    fun startBlackjack() {
        val game = selectedGame.value ?: return
        val currentChips = userSession.value?.chips ?: 0
        val wager = blackjackWager.value

        if (currentChips < wager || blackjackState.value != "betting") return

        viewModelScope.launch {
            repository.updateChipsAndXp(-wager, 0)
            
            generateDeck()
            playerHand.clear()
            dealerHand.clear()

            // Deal first cards
            playerHand.add(deck.removeAt(0))
            dealerHand.add(deck.removeAt(0))
            playerHand.add(deck.removeAt(0))
            dealerHand.add(deck.removeAt(0))

            recalcBlackjackScores()
            
            // Check natural Blackjack
            if (playerScore.value == 21) {
                resolveBlackjackGame("Natural Blackjack!")
            } else {
                blackjackState.value = "playing"
                blackjackResult.value = "Your turn. Hit or Stand?"
            }
        }
    }

    private fun recalcBlackjackScores() {
        playerScore.value = calculateHandScore(playerHand)
        dealerScore.value = calculateHandScore(dealerHand)
    }

    private fun calculateHandScore(hand: List<CasinoCard>): Int {
        var sum = hand.sumOf { it.rank }
        var aces = hand.count { it.value == "A" }
        while (sum > 21 && aces > 0) {
            sum -= 10
            aces -= 1
        }
        return sum
    }

    fun hitBlackjack() {
        if (blackjackState.value != "playing") return
        viewModelScope.launch {
            playerHand.add(deck.removeAt(0))
            recalcBlackjackScores()

            if (playerScore.value > 21) {
                resolveBlackjackGame("Bust! Dealer Wins.")
            } else if (playerScore.value == 21) {
                standBlackjack()
            }
        }
    }

    fun doubleBlackjack() {
        if (blackjackState.value != "playing") return
        val currentChips = userSession.value?.chips ?: 0
        val wager = blackjackWager.value
        if (currentChips < wager) return

        viewModelScope.launch {
            repository.updateChipsAndXp(-wager, 0)
            blackjackWager.value = wager * 2
            
            playerHand.add(deck.removeAt(0))
            recalcBlackjackScores()

            if (playerScore.value > 21) {
                resolveBlackjackGame("Bust on Double! Dealer Wins.")
            } else {
                standBlackjack()
            }
        }
    }

    fun standBlackjack() {
        if (blackjackState.value != "playing") return
        blackjackState.value = "resolving"
        
        viewModelScope.launch {
            blackjackResult.value = "Dealer is drawing..."
            delay(1000L)
            
            // Dealer draws on soft 17
            while (dealerScore.value < 17) {
                dealerHand.add(deck.removeAt(0))
                recalcBlackjackScores()
                delay(800L)
            }

            val finalPlayer = playerScore.value
            val finalDealer = dealerScore.value

            val message = when {
                finalDealer > 21 -> "Dealer Busts! You Win."
                finalPlayer > finalDealer -> "You Win!"
                finalPlayer < finalDealer -> "Dealer Wins."
                else -> "Push (Tie)."
            }
            resolveBlackjackGame(message)
        }
    }

    private suspend fun resolveBlackjackGame(outcome: String) {
        val game = selectedGame.value ?: return
        val wager = blackjackWager.value
        blackjackResult.value = outcome
        blackjackState.value = "resolved"

        val payout = when {
            outcome.contains("Bust") || outcome.contains("Dealer Wins") -> 0L
            outcome.contains("Push") -> wager
            outcome.contains("Natural") -> (wager * 2.5).toLong() // 3:2 payout
            else -> wager * 2
        }

        repository.recordGamePlay(
            gameId = game.id,
            gameName = game.name,
            category = "blackjack",
            wager = wager,
            payout = payout
        )

        // Reset wager to base
        delay(2000L)
    }

    fun resetBlackjackToBetting() {
        blackjackState.value = "betting"
        blackjackResult.value = ""
        playerHand.clear()
        dealerHand.clear()
        playerScore.value = 0
        dealerScore.value = 0
        // Reset blackjack wager if it was doubled
        blackjackWager.value = selectedGame.value?.minBet ?: 100L
    }

    // ==========================================
    // 3. ROULETTE ENGINE
    // ==========================================
    val rouletteWager = MutableStateFlow(100L)
    val selectedRouletteBets = mutableStateMapOf<String, Long>() // e.g. "Red" -> 100, "17" -> 50
    val isRouletteSpinning = MutableStateFlow(false)
    val rouletteWinningNumber = MutableStateFlow<Int?>(null)
    val rouletteWinningColor = MutableStateFlow<String?>(null)
    val rouletteResultLabel = MutableStateFlow("")
    val rouletteHistory = mutableStateListOf<Int>()

    fun toggleRouletteBet(betType: String) {
        if (isRouletteSpinning.value) return
        val betAmount = rouletteWager.value
        val currentBet = selectedRouletteBets[betType] ?: 0L
        if (currentBet > 0) {
            selectedRouletteBets.remove(betType)
        } else {
            selectedRouletteBets[betType] = betAmount
        }
    }

    fun clearRouletteBets() {
        if (!isRouletteSpinning.value) {
            selectedRouletteBets.clear()
        }
    }

    fun spinRoulette() {
        val game = selectedGame.value ?: return
        val currentChips = userSession.value?.chips ?: 0
        val totalWager = selectedRouletteBets.values.sum()

        if (totalWager <= 0 || currentChips < totalWager || isRouletteSpinning.value) return

        isRouletteSpinning.value = true
        rouletteResultLabel.value = "Spinning the Wheel..."

        viewModelScope.launch {
            // Deduct total bet
            repository.updateChipsAndXp(-totalWager, 0)

            // Simulate spinning
            repeat(15) { step ->
                val randNum = Random.nextInt(37)
                rouletteWinningNumber.value = randNum
                rouletteWinningColor.value = getRouletteColor(randNum)
                delay(60L + step * 25L)
            }

            // Final number
            val finalNum = Random.nextInt(37)
            val finalColor = getRouletteColor(finalNum)
            rouletteWinningNumber.value = finalNum
            rouletteWinningColor.value = finalColor
            rouletteHistory.add(0, finalNum)
            if (rouletteHistory.size > 10) rouletteHistory.removeLast()

            // Calculate payouts
            var totalPayout = 0L
            for ((betType, betWager) in selectedRouletteBets) {
                totalPayout += calculateRouletteBetPayout(betType, betWager, finalNum, finalColor)
            }

            val netGain = totalPayout - totalWager
            rouletteResultLabel.value = if (totalPayout > 0) {
                "Number $finalNum ($finalColor) hit! You won $totalPayout Chips!"
            } else {
                "Number $finalNum ($finalColor) hit. Better luck next spin!"
            }

            // Log
            repository.recordGamePlay(
                gameId = game.id,
                gameName = game.name,
                category = "roulette",
                wager = totalWager,
                payout = totalPayout
            )

            isRouletteSpinning.value = false
        }
    }

    private fun getRouletteColor(num: Int): String {
        val redNumbers = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        return when {
            num == 0 -> "Green"
            redNumbers.contains(num) -> "Red"
            else -> "Black"
        }
    }

    private fun calculateRouletteBetPayout(betType: String, wager: Long, winningNum: Int, winningColor: String): Long {
        return when (betType) {
            "Red" -> if (winningColor == "Red") wager * 2 else 0L
            "Black" -> if (winningColor == "Black") wager * 2 else 0L
            "Even" -> if (winningNum > 0 && winningNum % 2 == 0) wager * 2 else 0L
            "Odd" -> if (winningNum > 0 && winningNum % 2 != 0) wager * 2 else 0L
            "1st Dozen" -> if (winningNum in 1..12) wager * 3 else 0L
            "2nd Dozen" -> if (winningNum in 13..24) wager * 3 else 0L
            "3rd Dozen" -> if (winningNum in 25..36) wager * 3 else 0L
            else -> {
                // Specific number bet
                val numBet = betType.toIntOrNull()
                if (numBet != null && numBet == winningNum) wager * 36 else 0L
            }
        }
    }

    // ==========================================
    // 4. RETRO VIDEO POKER ENGINE
    // ==========================================
    val pokerWager = MutableStateFlow(100L)
    val pokerState = MutableStateFlow("betting") // "betting", "deal", "draw"
    val pokerHand = mutableStateListOf<CasinoCard>()
    val pokerHeld = mutableStateListOf(false, false, false, false, false)
    val pokerPayout = MutableStateFlow(0L)
    val pokerResultLabel = MutableStateFlow("")

    fun startVideoPoker() {
        val game = selectedGame.value ?: return
        val currentChips = userSession.value?.chips ?: 0
        val wager = pokerWager.value

        if (currentChips < wager || pokerState.value != "betting") return

        viewModelScope.launch {
            repository.updateChipsAndXp(-wager, 0)
            generateDeck()
            pokerHand.clear()
            pokerHeld.fill(false)
            pokerResultLabel.value = ""
            pokerPayout.value = 0

            // Deal 5 cards
            repeat(5) { pokerHand.add(deck.removeAt(0)) }
            pokerState.value = "deal"
            pokerResultLabel.value = "Select cards to HOLD and click DRAW!"
        }
    }

    fun togglePokerHold(index: Int) {
        if (pokerState.value == "deal") {
            pokerHeld[index] = !pokerHeld[index]
        }
    }

    fun drawVideoPoker() {
        if (pokerState.value != "deal") return
        val game = selectedGame.value ?: return
        val wager = pokerWager.value

        viewModelScope.launch {
            // Replace non-held cards
            for (i in 0..4) {
                if (!pokerHeld[i]) {
                    pokerHand[i] = deck.removeAt(0)
                }
            }

            pokerState.value = "draw"

            // Evaluate Poker Hand
            val handRank = evaluatePokerHand(pokerHand)
            val multiplier = getPokerMultiplier(handRank)
            val winAmount = wager * multiplier
            pokerPayout.value = winAmount

            pokerResultLabel.value = if (winAmount > 0) {
                "🎉 $handRank! Won $winAmount Chips!"
            } else {
                "No Pair or Low Card. Better luck next deal."
            }

            repository.recordGamePlay(
                gameId = game.id,
                gameName = game.name,
                category = "poker",
                wager = wager,
                payout = winAmount
            )
        }
    }

    fun resetPokerToBetting() {
        pokerState.value = "betting"
        pokerHand.clear()
        pokerHeld.fill(false)
        pokerPayout.value = 0
        pokerResultLabel.value = ""
    }

    private fun evaluatePokerHand(hand: List<CasinoCard>): String {
        val ranks = hand.map { it.rank }.sorted()
        val values = hand.map { it.value }
        val suits = hand.map { it.suit }

        val isFlush = suits.distinct().size == 1
        
        // Evaluate straight
        val straightRanks = hand.map { card ->
            when (card.value) {
                "J" -> 11
                "Q" -> 12
                "K" -> 13
                "A" -> 14
                else -> card.value.toInt()
            }
        }.sorted()

        var isStraight = false
        if (straightRanks[4] - straightRanks[0] == 4 && straightRanks.distinct().size == 5) {
            isStraight = true
        } else if (straightRanks == listOf(2, 3, 4, 5, 14)) { // Wheel straight (A-2-3-4-5)
            isStraight = true
        }

        // Count duplicates
        val rankCounts = straightRanks.groupingBy { it }.eachCount()
        val maxDuplicate = rankCounts.values.maxOrNull() ?: 1

        return when {
            isFlush && isStraight && straightRanks.contains(14) && straightRanks.contains(10) -> "Royal Flush"
            isFlush && isStraight -> "Straight Flush"
            maxDuplicate == 4 -> "Four of a Kind"
            rankCounts.values.contains(3) && rankCounts.values.contains(2) -> "Full House"
            isFlush -> "Flush"
            isStraight -> "Straight"
            maxDuplicate == 3 -> "Three of a Kind"
            rankCounts.values.count { it == 2 } == 2 -> "Two Pair"
            // Jacks or Better
            rankCounts.filter { it.key >= 11 && it.value == 2 }.isNotEmpty() -> "Jacks or Better"
            else -> "High Card"
        }
    }

    private fun getPokerMultiplier(rank: String): Int {
        return when (rank) {
            "Royal Flush" -> 250
            "Straight Flush" -> 50
            "Four of a Kind" -> 25
            "Full House" -> 9
            "Flush" -> 6
            "Straight" -> 4
            "Three of a Kind" -> 3
            "Two Pair" -> 2
            "Jacks or Better" -> 1
            else -> 0
        }
    }

    // ==========================================
    // 5. CRASH ROCKET ENGINE
    // ==========================================
    val crashWager = MutableStateFlow(100L)
    val crashState = MutableStateFlow("betting") // "betting", "flying", "crashed"
    val crashMultiplier = MutableStateFlow(1.0f)
    val cashOutMultiplier = MutableStateFlow<Float?>(null)
    val crashResultLabel = MutableStateFlow("")

    private var crashJob: Job? = null

    fun startCrashGame() {
        val game = selectedGame.value ?: return
        val currentChips = userSession.value?.chips ?: 0
        val wager = crashWager.value

        if (currentChips < wager || crashState.value == "flying") return

        crashState.value = "flying"
        crashMultiplier.value = 1.0f
        cashOutMultiplier.value = null
        crashResultLabel.value = "Ascending..."

        viewModelScope.launch {
            repository.updateChipsAndXp(-wager, 0)
        }

        crashJob = viewModelScope.launch {
            // Determine crash point: weighted towards low values, occasionally very high
            val rand = Random.nextFloat()
            val crashPoint = when {
                rand < 0.15f -> 1.0f + Random.nextFloat() * 0.2f // 15% instant low crash
                rand < 0.70f -> 1.2f + Random.nextFloat() * 2.5f // 55% average run (1.2 to 3.7)
                rand < 0.95f -> 3.7f + Random.nextFloat() * 10.0f // 25% super run (3.7 to 13.7)
                else -> 15.0f + Random.nextFloat() * 85.0f // 5% legendary high fly up to 100x!
            }

            var tick = 1.0f
            while (tick < crashPoint) {
                delay(60L)
                tick += if (tick < 2.0f) 0.02f else if (tick < 5.0f) 0.05f else 0.15f
                crashMultiplier.value = tick
            }

            // Crashed!
            crashState.value = "crashed"
            crashResultLabel.value = "💥 EXPLODED at ${String.format("%.2f", crashPoint)}x!"

            // Record outcome if didn't cash out
            if (cashOutMultiplier.value == null) {
                repository.recordGamePlay(
                    gameId = game.id,
                    gameName = game.name,
                    category = "crash",
                    wager = wager,
                    payout = 0L
                )
            }
        }
    }

    fun cashOutCrash() {
        if (crashState.value != "flying" || cashOutMultiplier.value != null) return
        val game = selectedGame.value ?: return
        val wager = crashWager.value
        val mult = crashMultiplier.value
        
        cashOutMultiplier.value = mult
        val winAmount = (wager * mult).toLong()
        crashResultLabel.value = "💰 Cashed out at ${String.format("%.2f", mult)}x! Won $winAmount Chips!"

        viewModelScope.launch {
            repository.recordGamePlay(
                gameId = game.id,
                gameName = game.name,
                category = "crash",
                wager = wager,
                payout = winAmount
            )
        }
    }

    fun resetCrashGame() {
        crashJob?.cancel()
        crashState.value = "betting"
        crashMultiplier.value = 1.0f
        cashOutMultiplier.value = null
        crashResultLabel.value = ""
    }

    // ==========================================
    // 6. PLINKO BOARD ENGINE
    // ==========================================
    val plinkoWager = MutableStateFlow(100L)
    val plinkoBalls = mutableStateListOf<PlinkoBall>()
    val plinkoPayouts = listOf(5.0f, 2.0f, 1.2f, 0.6f, 0.2f, 0.6f, 1.2f, 2.0f, 5.0f) // 9 buckets corresponding to 8 rows
    private val maxRows = 8
    private var plinkoJob: Job? = null

    fun dropPlinkoBall() {
        val game = selectedGame.value ?: return
        val currentChips = userSession.value?.chips ?: 0
        val wager = plinkoWager.value

        if (currentChips < wager) return

        viewModelScope.launch {
            repository.updateChipsAndXp(-wager, 0)
        }

        val ball = PlinkoBall(x = 0.5f, y = 0f)
        plinkoBalls.add(ball)

        // Launch peg-bouncing physics simulation
        viewModelScope.launch {
            while (ball.y < maxRows) {
                delay(80L)
                ball.y += 0.5f
                
                // Add tiny bounce physics noise left or right when encountering a peg row
                val direction = if (Random.nextBoolean()) 0.05f else -0.05f
                ball.x = (ball.x + direction).coerceIn(0.02f, 0.98f)
            }

            // Ball reached bottom - resolve landing bucket
            val index = (ball.x * plinkoPayouts.size).toInt().coerceIn(0, plinkoPayouts.size - 1)
            val payoutFactor = plinkoPayouts[index]
            val payoutAmount = (wager * payoutFactor).toLong()

            // Flash visual bounce of ball landing
            delay(100L)
            plinkoBalls.remove(ball)

            repository.recordGamePlay(
                gameId = game.id,
                gameName = game.name,
                category = "plinko",
                wager = wager,
                payout = payoutAmount
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        crashJob?.cancel()
        plinkoJob?.cancel()
    }
}
