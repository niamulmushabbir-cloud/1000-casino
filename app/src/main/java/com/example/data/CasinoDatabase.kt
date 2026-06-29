package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ------------------ ENTITIES ------------------

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val id: Int = 1,
    val chips: Long = 10000, // Starts with 10,000 free chips!
    val xp: Long = 0,
    val level: Int = 1,
    val lastSpinTimestamp: Long = 0,
    val dailyRewardStreak: Int = 0,
    val selectedTheme: String = "VIP Gold"
)

@Entity(tableName = "game_stats")
data class GameStat(
    @PrimaryKey val gameId: Int,
    val playCount: Int = 0,
    val totalWagered: Long = 0,
    val totalPayout: Long = 0,
    val biggestWin: Long = 0,
    val isFavorite: Boolean = false
)

@Entity(tableName = "transaction_history")
data class TransactionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameName: String,
    val gameCategory: String,
    val wager: Long,
    val payout: Long,
    val netGain: Long
)

// ------------------ DAO ------------------

@Dao
interface CasinoDao {
    // User Session Queries
    @Query("SELECT * FROM user_sessions WHERE id = 1")
    fun getUserSessionFlow(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions WHERE id = 1")
    suspend fun getUserSession(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(session: UserSession)

    @Update
    suspend fun updateUserSession(session: UserSession)

    // Game Stats Queries
    @Query("SELECT * FROM game_stats")
    fun getAllGameStatsFlow(): Flow<List<GameStat>>

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    suspend fun getGameStat(gameId: Int): GameStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameStat(stat: GameStat)

    @Update
    suspend fun updateGameStat(stat: GameStat)

    @Query("UPDATE game_stats SET isFavorite = :isFav WHERE gameId = :gameId")
    suspend fun updateFavorite(gameId: Int, isFav: Boolean)

    // Transaction Queries
    @Query("SELECT * FROM transaction_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactionsFlow(): Flow<List<TransactionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionHistory)
}

// ------------------ DATABASE ------------------

@Database(
    entities = [UserSession::class, GameStat::class, TransactionHistory::class],
    version = 1,
    exportSchema = false
)
abstract class CasinoDatabase : RoomDatabase() {
    abstract fun casinoDao(): CasinoDao

    companion object {
        @Volatile
        private var INSTANCE: CasinoDatabase? = null

        fun getDatabase(context: Context): CasinoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CasinoDatabase::class.java,
                    "casino_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ------------------ REPOSITORY ------------------

class CasinoRepository(private val casinoDao: CasinoDao) {
    val userSession: Flow<UserSession?> = casinoDao.getUserSessionFlow()
    val allGameStats: Flow<List<GameStat>> = casinoDao.getAllGameStatsFlow()
    val recentTransactions: Flow<List<TransactionHistory>> = casinoDao.getRecentTransactionsFlow()

    suspend fun getOrCreateUserSession(): UserSession {
        var session = casinoDao.getUserSession()
        if (session == null) {
            session = UserSession()
            casinoDao.insertUserSession(session)
        }
        return session
    }

    suspend fun updateChipsAndXp(chipsDelta: Long, xpDelta: Long) {
        val current = getOrCreateUserSession()
        val newChips = (current.chips + chipsDelta).coerceAtLeast(0)
        val newXp = current.xp + xpDelta
        // Simple level logic: 500 XP per level
        val newLevel = 1 + (newXp / 500).toInt()
        
        casinoDao.updateUserSession(
            current.copy(
                chips = newChips,
                xp = newXp,
                level = newLevel
            )
        )
    }

    suspend fun updateDailySpin(newTimestamp: Long, streak: Int) {
        val current = getOrCreateUserSession()
        casinoDao.updateUserSession(
            current.copy(
                lastSpinTimestamp = newTimestamp,
                dailyRewardStreak = streak
            )
        )
    }

    suspend fun updateTheme(themeName: String) {
        val current = getOrCreateUserSession()
        casinoDao.updateUserSession(current.copy(selectedTheme = themeName))
    }

    suspend fun getGameStatOrCreate(gameId: Int): GameStat {
        return casinoDao.getGameStat(gameId) ?: GameStat(gameId = gameId)
    }

    suspend fun recordGamePlay(
        gameId: Int,
        gameName: String,
        category: String,
        wager: Long,
        payout: Long
    ) {
        // Update user's chips (net change)
        val netGain = payout - wager
        // Experience points earned is proportional to the bet (10% of wager, min 10 XP)
        val xpEarned = (wager / 10).coerceAtLeast(10)
        updateChipsAndXp(netGain, xpEarned)

        // Update specific game stats
        val currentStat = getGameStatOrCreate(gameId)
        val updatedStat = currentStat.copy(
            playCount = currentStat.playCount + 1,
            totalWagered = currentStat.totalWagered + wager,
            totalPayout = currentStat.totalPayout + payout,
            biggestWin = currentStat.biggestWin.coerceAtLeast(payout)
        )
        casinoDao.insertGameStat(updatedStat)

        // Log transaction history
        if (wager > 0 || payout > 0) {
            casinoDao.insertTransaction(
                TransactionHistory(
                    gameName = gameName,
                    gameCategory = category,
                    wager = wager,
                    payout = payout,
                    netGain = netGain
                )
            )
        }
    }

    suspend fun toggleFavorite(gameId: Int, currentFavState: Boolean) {
        val stat = getGameStatOrCreate(gameId)
        casinoDao.insertGameStat(stat.copy(isFavorite = !currentFavState))
    }
}
