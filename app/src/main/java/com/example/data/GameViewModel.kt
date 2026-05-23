package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundSynthesizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sin
import kotlin.math.cos

enum class GameState {
    START, PLAYING, GAME_OVER, ENCYCLOPEDIA, LEADERBOARDS
}

enum class GameDifficulty {
    EASY, MEDIUM, HARD
}

enum class ItemType {
    GRAPE, KVEVRI, COIN, SCROLL, STONE, ARROW
}

data class GameObject(
    val id: Long,
    var x: Float, // Relative width (e.g. 0 to 1000)
    var y: Float, // Relative height above floor (e.g. 0 is ground)
    val type: ItemType,
    var isCollected: Boolean = false,
    val size: Float, // Collision radius
    var rotation: Float = 0f,
    val speedY: Float = 0f // For falling items
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = GameDatabase.getDatabase(application)
    private val repository = GameRepository(db.gameDao())

    // SharedPreferences for Coins and Skins
    private val sharedPrefs = application.getSharedPreferences("svaneti_game_prefs", android.content.Context.MODE_PRIVATE)

    private val _coinsBalance = MutableStateFlow(sharedPrefs.getInt("coins_balance", 40)) // Welcome gift of 40 coins
    val coinsBalance: StateFlow<Int> = _coinsBalance.asStateFlow()

    private val _unlockedSkins = MutableStateFlow(sharedPrefs.getStringSet("unlocked_skins", setOf("default")) ?: setOf("default"))
    val unlockedSkins: StateFlow<Set<String>> = _unlockedSkins.asStateFlow()

    private val _equippedSkin = MutableStateFlow(sharedPrefs.getString("equipped_skin", "default") ?: "default")
    val equippedSkin: StateFlow<String> = _equippedSkin.asStateFlow()

    private val _lastRunCoinsBonus = MutableStateFlow(0)
    val lastRunCoinsBonus: StateFlow<Int> = _lastRunCoinsBonus.asStateFlow()

    // Online Leaderboard simulation
    data class OnlineLeaderboardEntry(
        val playerName: String,
        val score: Int,
        val level: Int,
        val countryFlag: String,
        val isUser: Boolean = false,
        val dateFormatted: String
    )

    private val _isUploadingScore = MutableStateFlow(false)
    val isUploadingScore: StateFlow<Boolean> = _isUploadingScore.asStateFlow()

    private val _onlineLeaderboard = MutableStateFlow<List<OnlineLeaderboardEntry>>(emptyList())
    val onlineLeaderboard: StateFlow<List<OnlineLeaderboardEntry>> = _onlineLeaderboard.asStateFlow()

    // UI States
    private val _gameState = MutableStateFlow(GameState.START)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _encyclopediaTab = MutableStateFlow(0)
    val encyclopediaTab: StateFlow<Int> = _encyclopediaTab.asStateFlow()

    fun setEncyclopediaTab(tab: Int) {
        _encyclopediaTab.value = tab
    }

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _hearts = MutableStateFlow(3)
    val hearts: StateFlow<Int> = _hearts.asStateFlow()

    private val _difficulty = MutableStateFlow(GameDifficulty.MEDIUM)
    val difficulty: StateFlow<GameDifficulty> = _difficulty.asStateFlow()

    fun setDifficulty(diff: GameDifficulty) {
        _difficulty.value = diff
    }

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _gameTicks = MutableStateFlow(0L)
    val gameTicks: StateFlow<Long> = _gameTicks.asStateFlow()

    // Parallax scrolling backgrounds (offsets 0f to 3600f)
    private val _parallaxOffsetFar = MutableStateFlow(0f)
    val parallaxOffsetFar: StateFlow<Float> = _parallaxOffsetFar.asStateFlow()

    private val _parallaxOffsetMid = MutableStateFlow(0f)
    val parallaxOffsetMid: StateFlow<Float> = _parallaxOffsetMid.asStateFlow()

    private val _parallaxOffsetNear = MutableStateFlow(0f)
    val parallaxOffsetNear: StateFlow<Float> = _parallaxOffsetNear.asStateFlow()

    // Hero Position State
    private val _heroY = MutableStateFlow(0f) // 0 is ground
    val heroY: StateFlow<Float> = _heroY.asStateFlow()

    private val _isCrouching = MutableStateFlow(false)
    val isCrouching: StateFlow<Boolean> = _isCrouching.asStateFlow()

    private val _heroInvincibleTicks = MutableStateFlow(0)
    val heroInvincibleTicks: StateFlow<Int> = _heroInvincibleTicks.asStateFlow()

    private val _canDoubleJump = MutableStateFlow(true)
    val canDoubleJump: StateFlow<Boolean> = _canDoubleJump.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _comboMultiplier = MutableStateFlow(1)
    val comboMultiplier: StateFlow<Int> = _comboMultiplier.asStateFlow()

    // Game Objects
    val gameObjects = CopyOnWriteArrayList<GameObject>()
    private var objectIdCounter = 0L

    // Notification Banner for Unlocked History Items during Play
    private val _unlockNotification = MutableStateFlow<HistoryFact?>(null)
    val unlockNotification: StateFlow<HistoryFact?> = _unlockNotification.asStateFlow()

    // Database flow streams
    val highScores: StateFlow<List<ScoreEntry>> = repository.highScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbUnlockedFacts: StateFlow<List<UnlockedHistory>> = repository.unlockedFacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Physics constants
    private val gravity = -1.5f
    private var heroVelocityY = 0f
    private val groundY = 0f

    // Current Game stats
    private var isPlaying = false
    private var gameJob: Job? = null
    private var ticks = 0L

    // For historical scroll trackers
    private var scrollsCollectedInRun = 0

    init {
        // Core initialization: unlock the introduction fact automatically
        viewModelScope.launch {
            repository.unlockFact("intro")
        }
        refreshOnlineLeaderboard()
    }

    // Coins & Skin Shop helper methods
    fun addCoins(amount: Int) {
        val newBalance = _coinsBalance.value + amount
        _coinsBalance.value = newBalance
        sharedPrefs.edit().putInt("coins_balance", newBalance).apply()
    }

    fun canAffordSkin(skinId: String): Boolean {
        val costs = mapOf("default" to 0, "svan_scout" to 15, "khevsur_warrior" to 30, "golden" to 50, "royal_tamar" to 100, "amirani_fire" to 200)
        val cost = costs[skinId] ?: 0
        return _coinsBalance.value >= cost
    }

    fun purchaseSkin(skinId: String): Boolean {
        if (_unlockedSkins.value.contains(skinId)) return false
        val costs = mapOf("default" to 0, "svan_scout" to 15, "khevsur_warrior" to 30, "golden" to 50, "royal_tamar" to 100, "amirani_fire" to 200)
        val cost = costs[skinId] ?: 0
        if (_coinsBalance.value >= cost) {
            // Deduct coins
            val newCoins = _coinsBalance.value - cost
            _coinsBalance.value = newCoins
            sharedPrefs.edit().putInt("coins_balance", newCoins).apply()

            // Unlock skin
            val updatedSkins = _unlockedSkins.value.toMutableSet()
            updatedSkins.add(skinId)
            _unlockedSkins.value = updatedSkins
            sharedPrefs.edit().putStringSet("unlocked_skins", updatedSkins).apply()

            // Equip skin
            equipSkin(skinId)
            SoundSynthesizer.playUnlockFanfare()
            return true
        }
        return false
    }

    fun equipSkin(skinId: String) {
        if (_unlockedSkins.value.contains(skinId)) {
            _equippedSkin.value = skinId
            sharedPrefs.edit().putString("equipped_skin", skinId).apply()
        }
    }

    // Online Leaderboard helpers
    fun refreshOnlineLeaderboard() {
        val userSavedScoresJson = sharedPrefs.getString("user_online_scores", "") ?: ""
        val userScores = mutableListOf<OnlineLeaderboardEntry>()
        if (userSavedScoresJson.isNotEmpty()) {
            userSavedScoresJson.split(";").forEach {
                val parts = it.split(",")
                if (parts.size >= 4) {
                    val name = parts[0]
                    val sc = parts[1].toIntOrNull() ?: 0
                    val lvl = parts[2].toIntOrNull() ?: 1
                    val flag = parts[3]
                    userScores.add(OnlineLeaderboardEntry(name, sc, lvl, flag, true, "Just Now"))
                }
            }
        }

        val baseOnlineList = listOf(
            OnlineLeaderboardEntry("Lasha • Ushguli Rider", 3450, 5, "🇬🇪", false, "2 hours ago"),
            OnlineLeaderboardEntry("Tariel • Lion Spirit", 2820, 4, "🇬🇪", false, "5 hours ago"),
            OnlineLeaderboardEntry("Sophia • Alpine Queen", 2310, 4, "🇦🇹", false, "Yesterday"),
            OnlineLeaderboardEntry("Alex • TrailBlazer", 1950, 3, "🇺🇸", false, "Yesterday"),
            OnlineLeaderboardEntry("Nino • Queen Tamar Fan", 1740, 3, "🇬🇪", false, "2 days ago"),
            OnlineLeaderboardEntry("Dmitri • Svan Mountaineer", 1520, 3, "🇺🇦", false, "3 days ago"),
            OnlineLeaderboardEntry("Yuki • Zen Runner", 1310, 2, "🇯🇵", false, "4 days ago"),
            OnlineLeaderboardEntry("David • Svan Tower Builder", 1120, 2, "🇬🇧", false, "5 days ago")
        )

        val combined = (userScores + baseOnlineList).sortedByDescending { it.score }
        _onlineLeaderboard.value = combined
    }

    fun uploadScoreToOnlineLeaderboard(playerName: String, countryFlag: String = "🇬🇪") {
        val finalScore = _score.value
        val finalLevel = _level.value
        viewModelScope.launch {
            _isUploadingScore.value = true
            // Satisfying server upload delay
            delay(1500)

            val currentListStr = sharedPrefs.getString("user_online_scores", "") ?: ""
            val cleanName = playerName.trim().ifEmpty { "კეთილი მგზავრი" }
            val newEntryStr = "$cleanName,$finalScore,$finalLevel,$countryFlag"
            val updated = if (currentListStr.isEmpty()) newEntryStr else "$currentListStr;$newEntryStr"

            sharedPrefs.edit().putString("user_online_scores", updated).apply()
            _isUploadingScore.value = false
            refreshOnlineLeaderboard()

            // Open Leaderboards
            _gameState.value = GameState.LEADERBOARDS
        }
    }

    fun startGame(playerName: String? = null) {
        ticks = 0L
        _lastRunCoinsBonus.value = 0
        _gameTicks.value = 0L
        _score.value = 0
        _hearts.value = 3
        _level.value = 1
        _heroY.value = 0f
        heroVelocityY = 0f
        scrollsCollectedInRun = 0
        _isCrouching.value = false
        _heroInvincibleTicks.value = 0
        _canDoubleJump.value = true
        _comboCount.value = 0
        _comboMultiplier.value = 1
        gameObjects.clear()
        isPlaying = true
        _gameState.value = GameState.PLAYING

        // Start 2D Frame Loop
        gameJob?.cancel()
        gameJob = viewModelScope.launch(Dispatchers.Default) {
            val tickRateMs = 16L // ~60 FPS
            while (isPlaying) {
                val startTime = System.currentTimeMillis()
                updateGameEngineState()
                val elapsed = System.currentTimeMillis() - startTime
                val sleepTime = maxOf(2L, tickRateMs - elapsed)
                delay(sleepTime)
            }
        }
    }

    fun jump() {
        if (_gameState.value != GameState.PLAYING) return
        if (_heroY.value == groundY && !_isCrouching.value) {
            heroVelocityY = 25f // Upward thrust force
            _heroY.value = 1f   // Exit ground slightly to avoid stickiness
            _canDoubleJump.value = true
            SoundSynthesizer.playJump()
        } else if (_heroY.value > groundY && _canDoubleJump.value) {
            heroVelocityY = 22f // Mid-air jump boost
            _canDoubleJump.value = false
            SoundSynthesizer.playJump()
        }
    }

    fun setCrouch(crouch: Boolean) {
        if (_gameState.value == GameState.PLAYING) {
            _isCrouching.value = crouch
        }
    }

    private suspend fun updateGameEngineState() {
        ticks++
        _gameTicks.value = ticks

        // 1. Parallax Scroll updates
        val scrollSpeed = 6f + (_level.value * 0.8f)
        _parallaxOffsetFar.value = (_parallaxOffsetFar.value + scrollSpeed * 0.15f) % 2000f
        _parallaxOffsetMid.value = (_parallaxOffsetMid.value + scrollSpeed * 0.4f) % 2000f
        _parallaxOffsetNear.value = (_parallaxOffsetNear.value + scrollSpeed) % 2000f

        // 2. Hero Jump physics
        val currentY = _heroY.value
        if (currentY > groundY) {
            heroVelocityY += gravity
            val nextY = currentY + heroVelocityY
            if (nextY <= groundY) {
                _heroY.value = groundY
                heroVelocityY = 0f
                _canDoubleJump.value = true // Reset on landing securely
            } else {
                _heroY.value = nextY
            }
        }

        // Hero Invincibility decr
        if (_heroInvincibleTicks.value > 0) {
            _heroInvincibleTicks.value--
        }

        // 3. GameObject generator
        if (ticks % maxOf(30, 80 - _level.value * 5) == 0L) {
            spawnRandomObject()
        }

        // 4. Update existing game objects
        val iterator = gameObjects.iterator()
        while (iterator.hasNext()) {
            val obj = iterator.next()
            obj.x -= scrollSpeed // Scroll objects from Right to Left

            // Falling object gravity (Stones from sky)
            if (obj.type == ItemType.STONE && obj.y > 0) {
                obj.y = maxOf(0f, obj.y + obj.speedY)
            }

            // Arrow movement speed increase
            if (obj.type == ItemType.ARROW) {
                obj.x -= scrollSpeed * 0.5f // Fly faster!
            }

            // Spin items
            obj.rotation = (obj.rotation + 4f) % 360f

            // Collision check
            if (!obj.isCollected && obj.x < 240f && obj.x > 80f) {
                val playerCoreY = _heroY.value + if (_isCrouching.value) 20f else 60f
                val playerCoreX = 140f
                val distanceSq = (obj.x - playerCoreX) * (obj.x - playerCoreX) + (obj.y - playerCoreY) * (obj.y - playerCoreY)
                val hitDistance = obj.size + 40f
                if (distanceSq < hitDistance * hitDistance) {
                    handleCollision(obj)
                }
            }

            // Purge out of bound items
            if (obj.x < -100f) {
                gameObjects.remove(obj)
            }
        }

        // 5. Harder as score grows (Progressive escalating system up to Level 10)
        val currentScore = _score.value
        val targetLevel = when {
            currentScore < 500 -> 1
            currentScore < 1200 -> 2
            currentScore < 2200 -> 3
            currentScore < 3500 -> 4
            currentScore < 5000 -> 5
            currentScore < 7000 -> 6
            currentScore < 9500 -> 7
            currentScore < 12500 -> 8
            currentScore < 16000 -> 9
            else -> 10
        }
        if (targetLevel != _level.value && targetLevel <= 10) {
            _level.value = targetLevel
            SoundSynthesizer.playUnlockFanfare()
            // Try to unlock level specific history facts
            viewModelScope.launch {
                when (targetLevel) {
                    2 -> unlockFactById("svan_towers")
                    3 -> unlockFactById("alphabet")
                    4 -> unlockFactById("chakrulo")
                    5 -> unlockFactById("amirani")
                }
            }
        }
    }

    private fun spawnRandomObject() {
        val rand = (0..100).random()
        val type: ItemType
        val startY: Float
        val speedY: Float
        var size = 30f

        val lv = _level.value
        when (lv) {
            2 -> {
                // Kakheti Vineyards: Grapes and Wine jars (Kvevri) are extremely common!
                when {
                    rand < 55 -> { // 55% Grapes
                        type = ItemType.GRAPE
                        startY = (45..175).random().toFloat()
                        speedY = 0f
                        size = 28f
                    }
                    rand < 75 -> { // 20% Coins
                        type = ItemType.COIN
                        startY = (65..225).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    rand < 90 -> { // 15% Kvevri
                        type = ItemType.KVEVRI
                        startY = 0f
                        speedY = 0f
                        size = 35f
                    }
                    else -> { // Only 10% Stones
                        type = ItemType.STONE
                        startY = (280..350).random().toFloat()
                        speedY = -4f - (lv * 0.5f)
                        size = 35f
                    }
                }
            }
            3 -> {
                // Tbilisi Defense: Archer arrows and brick debris from fort are very common!
                when {
                    rand < 30 -> {
                        type = ItemType.ARROW
                        startY = 100f
                        speedY = 0f
                        size = 20f
                    }
                    rand < 60 -> {
                        type = ItemType.STONE
                        startY = (280..350).random().toFloat()
                        speedY = -5f - (lv * 0.5f)
                        size = 35f
                    }
                    rand < 85 -> {
                        type = ItemType.COIN
                        startY = (50..200).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    else -> {
                        type = ItemType.SCROLL
                        startY = 140f
                        speedY = 0f
                        size = 32f
                    }
                }
            }
            4 -> {
                // Adjara Coast: Coins floating in golden ocean waves!
                when {
                    rand < 58 -> {
                        type = ItemType.COIN
                        // Dynamic Sine-wave starting trajectory
                        startY = 80f + (sin(ticks.toFloat() * 0.28f).toFloat() * 60f)
                        speedY = 0f
                        size = 25f
                    }
                    rand < 78 -> {
                        type = ItemType.GRAPE
                        startY = (60..180).random().toFloat()
                        speedY = 0f
                        size = 28f
                    }
                    else -> {
                        type = ItemType.STONE
                        startY = 0f // Stationary driftwood obstacles to jump over on the beach!
                        speedY = 0f
                        size = 35f
                    }
                }
            }
            5 -> {
                // Kazbegi Freezing Blizzard: Snowy heavy ice boulders fall with high velocity!
                when {
                    rand < 42 -> {
                        type = ItemType.STONE
                        startY = 350f
                        speedY = -8f - (lv * 0.6f) // Blizzard hyper speed drop!
                        size = 36f
                    }
                    rand < 68 -> {
                        type = ItemType.GRAPE
                        startY = (60..210).random().toFloat()
                        speedY = 0f
                        size = 28f
                    }
                    rand < 88 -> {
                        type = ItemType.COIN
                        startY = (80..230).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    else -> {
                        type = ItemType.KVEVRI
                        startY = 0f
                        speedY = 0f
                        size = 35f
                    }
                }
            }
            6 -> {
                // Imereti Caves: Falling stalactites and magical scrolls navigating in zigzags!
                when {
                    rand < 40 -> {
                        type = ItemType.STONE
                        startY = 350f
                        speedY = -6f - (lv * 0.4f)
                        size = 32f
                    }
                    rand < 65 -> {
                        type = ItemType.SCROLL
                        // Zigzag flying scroll!
                        startY = 120f + (cos(ticks.toFloat() * 0.18f).toFloat() * 50f)
                        speedY = 0f
                        size = 32f
                    }
                    rand < 90 -> {
                        type = ItemType.COIN
                        startY = (50..160).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    else -> {
                        type = ItemType.GRAPE
                        startY = (40..150).random().toFloat()
                        speedY = 0f
                        size = 28f
                    }
                }
            }
            8 -> {
                // Tusheti Watchtowers: Double arrow fly zones (cliff defenders!)
                when {
                    rand < 35 -> {
                        type = ItemType.ARROW
                        startY = 85f // low archer arrow (slide under!)
                        speedY = 0f
                        size = 20f
                    }
                    rand < 55 -> {
                        type = ItemType.ARROW
                        startY = 165f // High altitude arrow (jump carefully!)
                        speedY = 0f
                        size = 20f
                    }
                    rand < 80 -> {
                        type = ItemType.COIN
                        startY = (60..220).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    else -> {
                        type = ItemType.STONE
                        startY = (280..350).random().toFloat()
                        speedY = -6f - (lv * 0.5f)
                        size = 35f
                    }
                }
            }
            9 -> {
                // Shida Kartli Cave Archives: Scrolls are highly abundant!
                when {
                    rand < 36 -> {
                        type = ItemType.SCROLL
                        startY = (60..220).random().toFloat()
                        speedY = 0f
                        size = 32f
                    }
                    rand < 70 -> {
                        type = ItemType.COIN
                        startY = (45..195).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    rand < 90 -> {
                        type = ItemType.STONE
                        startY = (280..350).random().toFloat()
                        speedY = -4f - (lv * 0.5f)
                        size = 35f
                    }
                    else -> {
                        type = ItemType.GRAPE
                        startY = (40..180).random().toFloat()
                        speedY = 0f
                        size = 28f
                    }
                }
            }
            else -> {
                // Balanced classic distribution for Svaneti (1), Samegrelo (7), Racha (10)
                when {
                    rand < 35 -> {
                        type = ItemType.GRAPE
                        startY = (40..160).random().toFloat()
                        speedY = 0f
                        size = 28f
                    }
                    rand < 60 -> {
                        type = ItemType.COIN
                        startY = (60..220).random().toFloat()
                        speedY = 0f
                        size = 25f
                    }
                    rand < 75 -> {
                        type = ItemType.STONE
                        startY = (280..350).random().toFloat()
                        speedY = -4f - (lv * 0.5f)
                        size = 35f
                    }
                    rand < 85 -> {
                        type = ItemType.ARROW
                        startY = 100f
                        speedY = 0f
                        size = 20f
                    }
                    rand < 95 -> {
                        type = ItemType.KVEVRI
                        startY = 0f
                        speedY = 0f
                        size = 35f
                    }
                    else -> {
                        type = ItemType.SCROLL
                        startY = (100..200).random().toFloat()
                        speedY = 0f
                        size = 32f
                    }
                }
            }
        }

        gameObjects.add(
            GameObject(
                id = objectIdCounter++,
                x = 1100f, // Offscreen right
                y = startY,
                type = type,
                size = size,
                speedY = speedY
            )
        )
    }

    private suspend fun handleCollision(obj: GameObject) {
        // Collectibles
        if (obj.type != ItemType.STONE && obj.type != ItemType.ARROW) {
            obj.isCollected = true
            gameObjects.remove(obj)

            _comboCount.value++
            _comboMultiplier.value = minOf(5, (_comboCount.value / 3) + 1)
            val currentMultiplier = _comboMultiplier.value

            when (obj.type) {
                ItemType.GRAPE -> {
                    _score.value += 10 * currentMultiplier
                    SoundSynthesizer.playCollectCoin()
                }
                ItemType.COIN -> {
                    _score.value += 25 * currentMultiplier
                    addCoins(1 * currentMultiplier) // Gather shop coins! multiplier-boosted
                    SoundSynthesizer.playCollectCoin()
                    if (_score.value >= 200) {
                        unlockFactById("kvevri")
                    }
                    if (_score.value >= 500) {
                        unlockFactById("tamar")
                    }
                    if (_score.value >= 1000) {
                        unlockFactById("rustaveli")
                    }
                }
                ItemType.KVEVRI -> {
                    _score.value += 40 * currentMultiplier
                    SoundSynthesizer.playCollectWine()
                    // Restore health if damaged
                    if (_hearts.value < 3) {
                        _hearts.value++
                    }
                    unlockFactById("kvevri")
                }
                ItemType.SCROLL -> {
                    _score.value += 50 * currentMultiplier
                    scrollsCollectedInRun++
                    SoundSynthesizer.playUnlockFanfare()

                    // Unlock facts sequentially
                    if (scrollsCollectedInRun == 1) {
                        unlockFactById("david")
                    } else if (scrollsCollectedInRun >= 3) {
                        unlockFactById("vardzia")
                    }
                }
                else -> {}
            }
        } else {
            // Obstacles
            // Crouch protects from ARROW (archer arrows fly high, crouching slides under them)
            if (obj.type == ItemType.ARROW && _isCrouching.value) {
                // Slipped cleanly under arrow!
                return
            }

            // Normal damage if not invincible
            if (_heroInvincibleTicks.value == 0) {
                _comboCount.value = 0
                _comboMultiplier.value = 1

                _hearts.value--
                _heroInvincibleTicks.value = 60 // 1 second invincibility
                SoundSynthesizer.playHurt()

                // Destroy obstacle
                gameObjects.remove(obj)

                if (_hearts.value <= 0) {
                    triggerGameOver()
                }
            }
        }
    }

    private suspend fun triggerGameOver() {
        isPlaying = false
        _gameState.value = GameState.GAME_OVER
        gameJob?.cancel()

        // Award bonus coins for finishing a run (Score/10)
        val finalScore = _score.value
        val coinBonus = finalScore / 10
        _lastRunCoinsBonus.value = coinBonus
        addCoins(coinBonus)

        // Submit highscore to local SQLite database via repository in a detached IO scope so it does not cancel!
        val finalLevel = _level.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertScore("", finalScore, finalLevel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitPlayerScore(customName: String) {
        val finalScore = _score.value
        val finalLevel = _level.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertScore(customName, finalScore, finalLevel)
            // Go to leaderboards screen
            _gameState.value = GameState.LEADERBOARDS
        }
    }

    private suspend fun unlockFactById(factId: String) {
        // Find existing facts in local db of unlocked
        val alreadyUnlocked = dbUnlockedFacts.value.any { it.factId == factId }
        if (!alreadyUnlocked) {
            repository.unlockFact(factId)
            // Show local visual celebration banner on screen!
            val matchedFact = HistoryFactsProvider.facts.find { it.id == factId }
            if (matchedFact != null) {
                _unlockNotification.value = matchedFact
                // Hide banner after 3.5 seconds in a separate launched coroutine to avoid suspending the main game loop!
                viewModelScope.launch {
                    delay(3500)
                    _unlockNotification.value = null
                }
            }
        }
    }

    fun setScreen(state: GameState) {
        isPlaying = false
        gameJob?.cancel()
        _gameState.value = state
    }

    fun clearHistoricalRecords() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
            // Reset intro
            repository.unlockFact("intro")
            // Reset shop prefs
            sharedPrefs.edit().putInt("coins_balance", 40).putStringSet("unlocked_skins", setOf("default")).putString("equipped_skin", "default").apply()
            _coinsBalance.value = 40
            _unlockedSkins.value = setOf("default")
            _equippedSkin.value = "default"
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPlaying = false
        gameJob?.cancel()
    }
}
