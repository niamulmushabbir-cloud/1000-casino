package com.example.data

data class CasinoGame(
    val id: Int,
    val name: String,
    val category: String, // "slots", "blackjack", "roulette", "poker", "crash", "plinko"
    val theme: String, // "egypt", "space", "neon", "west", "sweet", "pirate", "atlantis", "classic", "dragon", "gold", "spooky", "safari"
    val themeLabel: String,
    val rtp: Double,
    val minBet: Long,
    val maxBet: Long,
    val isJackpotActive: Boolean,
    val jackpotAmount: Long,
    val description: String
)

object GameCatalog {
    private val THEMES = listOf(
        "egypt" to "Ancient Egypt",
        "space" to "Cosmic Space",
        "neon" to "Cyberpunk Neon",
        "west" to "Wild West",
        "sweet" to "Candy Sweet",
        "pirate" to "Pirate Loot",
        "atlantis" to "Sunken Atlantis",
        "classic" to "Retro Classic",
        "dragon" to "Dragon Dynasty",
        "gold" to "Vegas Gold",
        "spooky" to "Haunted Spooky",
        "safari" to "Serengeti Safari"
    )

    private val THEME_ADJECTIVES = mapOf(
        "egypt" to listOf("Cleopatra's", "Anubis'", "Pharaoh's", "Sacred Nile", "Pyramid", "Golden Scarab", "Osiris", "Sphinx's Hidden"),
        "space" to listOf("Cosmic", "Interstellar", "Starlight", "Nebula", "Supernova", "Alien Invasion", "Orbiting", "Solar Flare"),
        "neon" to listOf("Cyberpunk", "Synthwave", "Retro Glow", "Laser Grid", "Neon", "Volt", "Retro-Future", "Pixelated"),
        "west" to listOf("Outlaw", "Gold Rush", "Sheriff's", "Bandit", "Canyon", "Trigger Happy", "Saloon", "Spur-Clad"),
        "sweet" to listOf("Sugar Rush", "Choco-Drop", "Cookie", "Fruity Splash", "Lollipop", "Sweet Berry", "Marshmallow", "Donut"),
        "pirate" to listOf("Blackbeard's", "Release Kraken", "Golden Galleon", "Skull Cove", "Treasure", "Dubloon", "Corsair", "Jolly Roger"),
        "atlantis" to listOf("Poseidon's", "Mermaid", "Deep Sea Abyss", "Atlantis", "Trident", "Sunken Pearl", "Neptune's", "Coral"),
        "classic" to listOf("Lucky Cherry", "Sparkling Diamond", "Flaming 777", "Golden Bell", "Retro Fruit", "Double Spin", "Jester's", "Vegas Bar"),
        "dragon" to listOf("Dynasty", "Shogun's Temple", "Jade Dragon", "Lotus Bloom", "Emperor's", "Bonsai Wilds", "Katana", "Samurai"),
        "gold" to listOf("Vegas VIP", "Billionaire's", "Grand Royal", "Fortune Spin", "Super Rich", "Diamond Club", "Cash Flow", "Imperial"),
        "spooky" to listOf("Count Dracula's", "Zombie Graveyard", "Witchcraft", "Haunted Mansion", "Phantom", "Full Moon", "Creepy", "Eerie"),
        "safari" to listOf("Serengeti", "Lion King's", "Zebra Stripe", "Jungle Quest", "Savannah", "Wild Cheetah", "Elephant Herd", "Safari")
    )

    private val CATEGORY_NOUNS = mapOf(
        "slots" to listOf("Reels", "Spin", "Jackpot", "Bonanza", "Frenzy", "Gold", "Fortune", "Deluxe", "Cascades", "Treasures", "Wilds", "Payouts"),
        "blackjack" to listOf("Table", "Dealer's Lounge", "Pro Deck", "Classic", "Royale", "VIP Table", "High Stakes", "Double Exposure", "Vegas Deck", "Blackjack"),
        "roulette" to listOf("Wheel", "Royale", "VIP Wheel", "Classic Spin", "Live Wheel", "Double Zero", "3D Spin", "Masterclass", "Lucky Pocket"),
        "poker" to listOf("Jacks or Better", "Deuces Wild", "Draw Poker", "Hold'em Lounge", "Poker Showdown", "Aces High", "Joker Wild", "All-In", "Triple Draw"),
        "crash" to listOf("Rocket Flight", "To the Moon", "Multiplier Chase", "Apex Lift", "Jet Escape", "Asteroid Rush", "Hyperspeed", "Altitude Peak"),
        "plinko" to listOf("Peg Board", "Gravity Drop", "Plinko Cascade", "Plink King", "Pachinko Pegs", "Bucket Win", "Chip Drop", "Pyramid Plinko")
    )

    fun getGame(id: Int): CasinoGame {
        // Enforce boundary
        val safeId = id.coerceIn(1, 1000)

        // 1. Determine Category
        val category = when (safeId) {
            in 1..600 -> "slots"
            in 601..750 -> "blackjack"
            in 751..850 -> "roulette"
            in 851..930 -> "poker"
            in 931..970 -> "crash"
            else -> "plinko"
        }

        // 2. Determine Theme
        val themePair = THEMES[(safeId * 7) % THEMES.size]
        val theme = themePair.first
        val themeLabel = themePair.second

        // 3. Formulate Name
        val adjectives = THEME_ADJECTIVES[theme] ?: listOf("Golden")
        val nouns = CATEGORY_NOUNS[category] ?: listOf("Game")

        val adj = adjectives[(safeId * 13) % adjectives.size]
        val noun = nouns[(safeId * 17) % nouns.size]

        // Ensure blackjack, roulette, poker, crash, plinko names are explicitly clear
        val suffix = when (category) {
            "blackjack" -> if (noun.contains("Blackjack", ignoreCase = true)) "" else " Blackjack"
            "roulette" -> if (noun.contains("Roulette", ignoreCase = true)) "" else " Roulette"
            "poker" -> if (noun.contains("Poker", ignoreCase = true)) "" else " Video Poker"
            "crash" -> if (noun.contains("Crash", ignoreCase = true)) "" else " Crash"
            "plinko" -> if (noun.contains("Plinko", ignoreCase = true)) "" else " Plinko"
            else -> ""
        }

        val name = "$adj $noun$suffix"

        // 4. Calculate RTP (88.0% to 99.4%)
        val rtpBase = 88.0 + (safeId * 11) % 115 / 10.0
        val rtp = Math.round(rtpBase * 10) / 10.0

        // 5. Betting Stakes
        val stakesMultiplier = when {
            safeId % 200 == 0 -> 100 // High VIP High-roller
            safeId % 50 == 0 -> 10  // Mid roller
            else -> 1               // Normal
        }
        val minBet = 10L * stakesMultiplier
        val maxBet = minBet * 100

        // 6. Progressive Jackpots (About 4.3% of games have active jackpots)
        val isJackpotActive = safeId % 23 == 0
        val jackpotAmount = if (isJackpotActive) {
            50000L + (safeId * 1237L) % 250000L
        } else {
            0L
        }

        val description = "Experience $name, a custom $themeLabel $category classic. " +
                "Featuring an optimized RTP of $rtp% and a maximum bet of $maxBet chips. " +
                if (isJackpotActive) "Includes an active PROGRESSIVE JACKPOT of $jackpotAmount chips!" else "Enjoy massive multipliers, free spins, and high-fidelity visuals."

        return CasinoGame(
            id = safeId,
            name = name,
            category = category,
            theme = theme,
            themeLabel = themeLabel,
            rtp = rtp,
            minBet = minBet,
            maxBet = maxBet,
            isJackpotActive = isJackpotActive,
            jackpotAmount = jackpotAmount,
            description = description
        )
    }

    /**
     * Lazily returns all 1000 games.
     */
    fun getAllGames(): List<CasinoGame> {
        val list = ArrayList<CasinoGame>(1000)
        for (i in 1..1000) {
            list.add(getGame(i))
        }
        return list
    }
}
