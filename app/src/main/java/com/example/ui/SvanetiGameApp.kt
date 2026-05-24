package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.SoundSynthesizer
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun SvanetiGameApp(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel()
) {
    val gameState by gameViewModel.gameState.collectAsStateWithLifecycle()
    val score by gameViewModel.score.collectAsStateWithLifecycle()
    val level by gameViewModel.level.collectAsStateWithLifecycle()
    val hearts by gameViewModel.hearts.collectAsStateWithLifecycle()
    val unlockNotification by gameViewModel.unlockNotification.collectAsStateWithLifecycle()
    val highScores by gameViewModel.highScores.collectAsStateWithLifecycle()
    val dbUnlockedFacts by gameViewModel.dbUnlockedFacts.collectAsStateWithLifecycle()
    val coinsBalance by gameViewModel.coinsBalance.collectAsStateWithLifecycle()
    val equippedSkin by gameViewModel.equippedSkin.collectAsStateWithLifecycle()

    var isMutedState by remember { mutableStateOf(SoundSynthesizer.isMuted) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SvanObsidian)
        ) {
            // Screen router
            when (gameState) {
                GameState.START -> StartScreen(
                    unlockedCount = dbUnlockedFacts.size,
                    coinsBalance = coinsBalance,
                    highScores = highScores,
                    equippedSkin = equippedSkin,
                    gameViewModel = gameViewModel,
                    onPlayClick = { gameViewModel.startGame() },
                    onEncyclopediaClick = { 
                        gameViewModel.setEncyclopediaTab(0)
                        gameViewModel.setScreen(GameState.ENCYCLOPEDIA) 
                    },
                    onLeaderboardClick = { gameViewModel.setScreen(GameState.LEADERBOARDS) },
                    isMuted = isMutedState,
                    onMuteToggle = {
                        isMutedState = !isMutedState
                        SoundSynthesizer.isMuted = isMutedState
                    }
                )

                GameState.PLAYING -> PlayingScreen(
                    gameViewModel = gameViewModel,
                    score = score,
                    level = level,
                    hearts = hearts,
                    isMuted = isMutedState,
                    onMuteToggle = {
                        isMutedState = !isMutedState
                        SoundSynthesizer.isMuted = isMutedState
                    },
                    onExitClick = { gameViewModel.setScreen(GameState.START) }
                )

                GameState.GAME_OVER -> GameOverScreen(
                    score = score,
                    level = level,
                    onSubmitScore = { name -> gameViewModel.submitPlayerScore(name) },
                    onRestart = { gameViewModel.startGame() },
                    onExit = { gameViewModel.setScreen(GameState.START) }
                )

                GameState.ENCYCLOPEDIA -> EncyclopediaScreen(
                    unlockedIds = dbUnlockedFacts.map { it.factId }.toSet(),
                    gameViewModel = gameViewModel,
                    onBackClick = { gameViewModel.setScreen(GameState.START) }
                )

                GameState.LEADERBOARDS -> LeaderboardsScreen(
                    highScores = highScores,
                    gameViewModel = gameViewModel,
                    onBackClick = { gameViewModel.setScreen(GameState.START) },
                    onClearRecords = { gameViewModel.clearHistoricalRecords() }
                )
            }

            // Real-time Unlock Celebration Banner Popup Slide-Down Overlay
            // Placed as the last element inside the Box to naturally render on top.
            AnimatedVisibility(
                visible = unlockNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                unlockNotification?.let { fact ->
                    UnlockBanner(fact = fact)
                }
            }
        }
    }
}

// ==========================================
// 1. START MENU SCREEN
// ==========================================
@Composable
fun StartScreen(
    unlockedCount: Int,
    coinsBalance: Int,
    highScores: List<ScoreEntry>,
    equippedSkin: String,
    gameViewModel: GameViewModel,
    onPlayClick: () -> Unit,
    onEncyclopediaClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    isMuted: Boolean,
    onMuteToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val appLanguage by gameViewModel.language.collectAsStateWithLifecycle()
    val appTheme by gameViewModel.theme.collectAsStateWithLifecycle()

    val skyGradient = if (appTheme == AppTheme.FREEZING) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Dark slate
                Color(0xFF0C4A6E), // Deep sky blue
                Color(0xFF0284C7), // Light ice-blue glow
                SvanObsidian
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1B0711), // Midnight plum
                Color(0xFF4C0519), // Rich Georgian Burgundy
                Color(0xFF1E1B4B), // Royal Indigo Night
                SvanObsidian
            )
        )
    }

    val bestScore = highScores.maxOfOrNull { it.score } ?: 0
    val rank = calculatePlayerRank(bestScore)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skyGradient)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val centerGlowColor = if (appTheme == AppTheme.FREEZING) {
            Color(0xFF22D3EE).copy(alpha = glowAlpha)
        } else {
            GeorgianGold.copy(alpha = glowAlpha)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = centerGlowColor,
                radius = 165.dp.toPx() * scalePulse,
                center = Offset(size.width / 2f, size.height / 2f - 100.dp.toPx())
            )

            // Dynamic background snowflake rendering for Freezing theme
            if (appTheme == AppTheme.FREEZING) {
                val t = scalePulse * 15f
                val pRand = java.util.Random(12345L)
                for (i in 0..25) {
                    val startX = pRand.nextFloat() * size.width
                    val startY = pRand.nextFloat() * size.height
                    val driftX = kotlin.math.sin((t + i).toDouble()).toFloat() * 25f
                    val driftY = (t * 30f + startY) % size.height
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f + 0.5f * kotlin.math.sin((t + i).toDouble()).toFloat() * 0.5f),
                        radius = 2f + pRand.nextFloat() * 4f,
                        center = Offset((startX + driftX) % size.width, driftY)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Upper Profile Header: Rank Badge & Coins Wallet (Row 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge -> Navigates to Leaderboard/Records
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .border(1.dp, rank.color.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .clickable { onLeaderboardClick() },
                    colors = CardDefaults.cardColors(containerColor = SvanCharcoal.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = rank.icon, fontSize = 16.sp)
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) rank.title else rank.enTitle,
                                color = rank.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) {
                                    if (rank.nextThreshold == "MAX RANK ACHIEVED") "მაქსიმალური წოდება" else rank.nextThreshold.replace("Next:", "შემდეგი:")
                                } else rank.nextThreshold,
                                color = CaucasusSnow.copy(alpha = 0.6f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Coins Wallet -> Direct deep link to Skin Shop
                Card(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(44.dp)
                        .border(1.dp, GeorgianGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            gameViewModel.setEncyclopediaTab(1)
                            gameViewModel.setScreen(GameState.ENCYCLOPEDIA)
                        },
                    colors = CardDefaults.cardColors(containerColor = SvanCharcoal.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 16.sp)
                        Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "$coinsBalance",
                                color = GeorgianGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) "მაღაზია" else "PAINT SHOP",
                                color = CaucasusSnow.copy(alpha = 0.5f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Global settings row (Language, Theme, Sound Control) (Row 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher button (Pill style)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(SvanCharcoal.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
                        .border(1.dp, GeorgianGold.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        .clickable {
                            SoundSynthesizer.playCollectCoin()
                            gameViewModel.setLanguage(if (appLanguage == AppLanguage.GEORGIAN) AppLanguage.ENGLISH else AppLanguage.GEORGIAN)
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val flag = if (appLanguage == AppLanguage.GEORGIAN) "🇬🇪" else "🇬🇧"
                        val label = if (appLanguage == AppLanguage.GEORGIAN) "ქართული" else "English"
                        Text(text = flag, fontSize = 13.sp)
                        Text(
                            text = label,
                            color = CaucasusSnow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Theme Switcher button (Pill style)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(SvanCharcoal.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
                        .border(
                            1.dp,
                            if (appTheme == AppTheme.FREEZING) Color(0xFF22D3EE).copy(alpha = 0.5f) else GeorgianGold.copy(alpha = 0.4f),
                            RoundedCornerShape(22.dp)
                        )
                        .clickable {
                            SoundSynthesizer.playCollectCoin()
                            gameViewModel.setTheme(if (appTheme == AppTheme.BURGUNDY) AppTheme.FREEZING else AppTheme.BURGUNDY)
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val indicator = if (appTheme == AppTheme.BURGUNDY) "🍷" else "❄️"
                        val label = if (appLanguage == AppLanguage.GEORGIAN) {
                            if (appTheme == AppTheme.BURGUNDY) "კოსმიური" else "ყინულოვანი"
                        } else {
                            if (appTheme == AppTheme.BURGUNDY) "Cosmic" else "Freezing"
                        }
                        Text(text = indicator, fontSize = 13.sp)
                        Text(
                            text = label,
                            color = if (appTheme == AppTheme.FREEZING) Color(0xFF22D3EE) else GeorgianGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Volume / Mute button
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(SvanCharcoal.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .testTag("mute_button")
                ) {
                    Text(
                        text = if (isMuted) "🔇" else "🔊",
                        fontSize = 18.sp
                    )
                }
            }

            // Title & Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp * scalePulse)
                        .shadow(12.dp, RoundedCornerShape(45.dp))
                        .background(
                            Brush.sweepGradient(listOf(GeorgianGold, Color(0xFFD4AF37), GeorgianGold)),
                            RoundedCornerShape(45.dp)
                        )
                        .border(3.dp, CaucasusSnow, RoundedCornerShape(45.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appTheme == AppTheme.FREEZING) "❄️" else "🛡️",
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) "სვანური ლეგენდები" else "SVANETI LEGENDS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (appTheme == AppTheme.FREEZING) Color(0xFF22D3EE) else GeorgianGold,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) "SVANETI LEGENDS" else "სვანური ლეგენდები",
                    fontSize = 16.sp,
                    letterSpacing = 5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CaucasusSnow.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) {
                        "2D სათავგადასავლო რანერი ქართული კულტურისა და ისტორიის შესახებ"
                    } else {
                        "A 2D Action Game celebrating Georgian Culture & History"
                    },
                    fontSize = 13.sp,
                    color = CaucasusSnow.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Central Actions Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                val difficultyState = gameViewModel.difficulty.collectAsStateWithLifecycle()
                val currentDifficulty = difficultyState.value

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Button (priority size)
                    Button(
                        onClick = {
                            SoundSynthesizer.playUnlockFanfare()
                            onPlayClick()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .shadow(8.dp, RoundedCornerShape(34.dp))
                            .border(3.dp, GeorgianGold, RoundedCornerShape(34.dp))
                            .testTag("play_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeorgianCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = if (appTheme == AppTheme.FREEZING) "❄️" else "⚔️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) "ლეგენდის დაწყება" else "START RUN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Difficulty Selector Row
                    Row(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .background(SvanCharcoal.copy(alpha = 0.95f), RoundedCornerShape(34.dp))
                            .border(2.dp, GeorgianGold.copy(alpha = 0.7f), RoundedCornerShape(34.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(
                            GameDifficulty.EASY to if (appLanguage == AppLanguage.GEORGIAN) "მარტივი" else "Easy",
                            GameDifficulty.MEDIUM to if (appLanguage == AppLanguage.GEORGIAN) "საშუალო" else "Normal",
                            GameDifficulty.HARD to if (appLanguage == AppLanguage.GEORGIAN) "რთული" else "Hard"
                        ).forEach { (diff, label) ->
                            val isSelected = currentDifficulty == diff
                            val bg = if (isSelected) {
                                when (diff) {
                                    GameDifficulty.EASY -> Color(0xFF047857) // Dark Emerald
                                    GameDifficulty.MEDIUM -> Color(0xFFD97706) // Dark Amber
                                    GameDifficulty.HARD -> Color(0xFFB91C1C) // Dark Crimson
                                }
                            } else Color.Transparent

                            val txtCol = if (isSelected) Color.White else CaucasusSnow.copy(alpha = 0.5f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(bg, RoundedCornerShape(30.dp))
                                    .clickable {
                                        SoundSynthesizer.playCollectCoin()
                                        gameViewModel.setDifficulty(diff)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val emoji = when (diff) {
                                        GameDifficulty.EASY -> "🍃"
                                        GameDifficulty.MEDIUM -> "⚡"
                                        GameDifficulty.HARD -> "💀"
                                    }
                                    Text(text = emoji, fontSize = 11.sp)
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = txtCol
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            SoundSynthesizer.playCollectCoin()
                            onEncyclopediaClick()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .border(1.5.dp, GeorgianGold, RoundedCornerShape(12.dp))
                            .testTag("encyclopedia_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SvanCharcoal,
                            contentColor = GeorgianGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) "კულტურა • HUB" else "HERITAGE HUB",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (unlockedCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(GeorgianCrimson, RoundedCornerShape(9.dp))
                                        .align(Alignment.TopEnd)
                                        .offset(x = 18.dp, y = (-12).dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unlockedCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            SoundSynthesizer.playCollectCoin()
                            onLeaderboardClick()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .testTag("leaderboards_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SvanCharcoal,
                            contentColor = CaucasusSnow
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Stars Icon",
                                tint = GeorgianGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) "რეკორდები • LIST" else "LEADERBOARD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ACTIVE 2D GAME PLAYING SCREEN
// ==========================================
@Composable
fun PlayingScreen(
    gameViewModel: GameViewModel,
    score: Int,
    level: Int,
    hearts: Int,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onExitClick: () -> Unit
) {
    val offsetFar by gameViewModel.parallaxOffsetFar.collectAsStateWithLifecycle()
    val offsetMid by gameViewModel.parallaxOffsetMid.collectAsStateWithLifecycle()
    val offsetNear by gameViewModel.parallaxOffsetNear.collectAsStateWithLifecycle()

    val heroY by gameViewModel.heroY.collectAsStateWithLifecycle()
    val isCrouching by gameViewModel.isCrouching.collectAsStateWithLifecycle()
    val invincibleTicks by gameViewModel.heroInvincibleTicks.collectAsStateWithLifecycle()
    val comboMultiplier by gameViewModel.comboMultiplier.collectAsStateWithLifecycle()
    val canDoubleJump by gameViewModel.canDoubleJump.collectAsStateWithLifecycle()
    val currentTicks by gameViewModel.gameTicks.collectAsStateWithLifecycle()
    val equippedSkin by gameViewModel.equippedSkin.collectAsStateWithLifecycle()

    val isSlidePressed = remember { mutableStateOf(false) }
    val isJumpPressed = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvanObsidian)
    ) {
        // Game HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SvanCharcoal)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(GeorgianCrimson, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LEVEL $level",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (comboMultiplier > 1) {
                    Box(
                        modifier = Modifier
                            .background(GeorgianGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMBO x$comboMultiplier 🔥",
                            color = SvanObsidian,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..3) {
                    val tint = if (i <= hearts) HealthRed else Color.DarkGray
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Life $i",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🪙", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$score PTS",
                    color = CaucasusSnow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onMuteToggle, modifier = Modifier.size(36.dp)) {
                    Text(
                        text = if (isMuted) "🔇" else "🔊",
                        fontSize = 16.sp,
                        color = CaucasusSnow
                    )
                }
                IconButton(onClick = onExitClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit run",
                        tint = CaucasusSnow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Region travel display under the main HUD Bar
        val regionPair = when (level) {
            1 -> "სვანეთი • SVANETI" to "🏔️ Peak Shkhara & Svan Towers"
            2 -> "კახეთი • KAKHETI" to "🍇 Alazani Valley & Vineyards"
            3 -> "თბილისი • TBILISI" to "🏰 Narikala Fortress & sulfur baths"
            4 -> "აჭარა • ADJARA" to "🏖️ Coastal Batumi & Black Sea"
            5 -> "ყაზბეგი • KAZBEGI" to "❄️ Gergeti Trinity & Mount Kazbek"
            6 -> "იმერეთი • IMERETI" to "🔮 Prometheus Caves & Stalactites"
            7 -> "სამეგრელო • SAMEGRELO" to "🌲 Martvili Canyon Waterfalls"
            8 -> "თუშეთი • TUSHETI" to "🦅 Keselo Forts & Wild Cliffs"
            9 -> "შიდა ქართლი • SHIDA KARTLI" to "🏛️ Uplistsikhe Rock-hewn Town"
            10 -> "რაჭა • RACHA" to "🍂 Shaori Reservoir & Autumn Forest"
            else -> "საქართველო • GEORGIA" to "👑 Eternal Horizon"
        }
        val (regionKote, regionDesc) = regionPair
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📍 $regionKote",
                color = GeorgianGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = regionDesc,
                color = CaucasusSnow.copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Active 2D Canvas Scene
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    gameViewModel.jump()
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                GameCanvasRenderer.drawParallaxMountains(this, offsetFar, offsetMid, offsetNear, level)

                gameViewModel.gameObjects.forEach { obj ->
                    if (!obj.isCollected) {
                        GameCanvasRenderer.drawGameObject(this, obj.x, obj.y, obj.type, obj.rotation)
                    }
                }

                GameCanvasRenderer.drawHero(
                    this,
                    heroY,
                    isCrouching,
                    invincibleTicks > 0,
                    currentTicks,
                    equippedSkin
                )
            }

            if (currentTicks < 120) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "TAP TO JUMP • JUMP OVER ROCKS\nSLIDE UNDER FLYING ARROWS!",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Tactile Controls Board - highly compatible retro action triggers (Bigger, Colorful, Tactile Glow!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SvanObsidian)
                .padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. SLIDE Button (HOLD) - Bigger and vibrant gold-fire gradient
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp)
                    .graphicsLayer {
                        scaleX = if (isSlidePressed.value) 0.92f else 1.0f
                        scaleY = if (isSlidePressed.value) 0.92f else 1.0f
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .border(3.5.dp, GeorgianGold, RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFB300), Color(0xFFFF3D00))
                        )
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            isSlidePressed.value = true
                            gameViewModel.setCrouch(true)
                            waitForUpOrCancellation()
                            gameViewModel.setCrouch(false)
                            isSlidePressed.value = false
                        }
                    }
                    .testTag("slide_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "▼ 🛡️",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SLIDE (HOLD)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Center Status indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(64.dp)
            ) {
                Text(
                    text = if (isCrouching) "🛡️" else "🏃",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isCrouching) "SLIDING" else "RUNNING",
                    color = if (isCrouching) GeorgianGold else CaucasusSnow.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            // 2. JUMP Button - Majestic glowing neon crimson red to violet rose gradient with Double-Jump support
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp)
                    .graphicsLayer {
                        scaleX = if (isJumpPressed.value) 0.92f else 1.0f
                        scaleY = if (isJumpPressed.value) 0.92f else 1.0f
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .border(3.5.dp, CaucasusSnow, RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF1744), Color(0xFFD500F9))
                        )
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            isJumpPressed.value = true
                            gameViewModel.jump()
                            waitForUpOrCancellation()
                            isJumpPressed.value = false
                        }
                    }
                    .testTag("jump_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (canDoubleJump) "▲ ⚡ ▲" else "▲",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (canDoubleJump) "DOUBLE JUMP" else "JUMP",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. GAME OVER - CHRONICLES SCROLL SCREEN
// ==========================================
@Composable
fun GameOverScreen(
    score: Int,
    level: Int,
    onSubmitScore: (String) -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    var nameText by remember { mutableStateOf(TextFieldValue("")) }

    val backgroundScrollBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1B070A),
            Color(0xFF2E0A10),
            SvanObsidian
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundScrollBrush)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(GeorgianCrimson.copy(alpha = 0.2f), RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⌛",
                fontSize = 42.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ქრონიკები დასრულდა",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = GeorgianCrimson,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "RUN COMPLETED",
            fontSize = 14.sp,
            color = CaucasusSnow,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SvanCharcoal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "THE TRAVELER'S LOG",
                    fontSize = 12.sp,
                    color = GeorgianGold,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SCORE", fontSize = 11.sp, color = CaucasusSnow.copy(alpha = 0.6f))
                        Text(text = "$score", fontSize = 28.sp, fontWeight = FontWeight.Black, color = CaucasusSnow)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SECTOR", fontSize = 11.sp, color = CaucasusSnow.copy(alpha = 0.6f))
                        Text(text = "Lvl $level", fontSize = 28.sp, fontWeight = FontWeight.Black, color = GeorgianGold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "შეიყვანეთ თქვენი სახელი ქრონიკებში შესატანად:",
            color = CaucasusSnow,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = nameText,
            onValueChange = { nameText = it },
            placeholder = { Text("კეთილი მგზავრი / Noble Knight") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CaucasusSnow,
                unfocusedTextColor = CaucasusSnow,
                focusedBorderColor = GeorgianGold,
                unfocusedBorderColor = SvanTowerGrey,
                focusedContainerColor = SvanCharcoal,
                unfocusedContainerColor = SvanCharcoal
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onExit,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("exit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SvanCharcoal,
                    contentColor = CaucasusSnow
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "სოფელში დაბრუნება", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Button(
                onClick = {
                    val name = nameText.text.trim()
                    onSubmitScore(name)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeorgianGold,
                    contentColor = SvanObsidian
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "შენახვა • RECORD", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onRestart,
            modifier = Modifier.testTag("restart_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                tint = GeorgianGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "თავიდან ცდა • TRY AGAIN", color = GeorgianGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ==========================================
// 4. HISTORICAL ENCYCLOPEDIA & PREMIUM SKY SHOP
// ==========================================
data class SkinItem(
    val id: String,
    val nameGe: String,
    val nameEn: String,
    val descriptionGe: String,
    val descriptionEn: String,
    val cost: Int,
    val badge: String,
    val badgeColor: Color,
    val previewSymbol: String
)

@Composable
fun EncyclopediaScreen(
    unlockedIds: Set<String>,
    gameViewModel: GameViewModel,
    onBackClick: () -> Unit
) {
    var selectedFact by remember { mutableStateOf<HistoryFact?>(null) }
    val activeTab by gameViewModel.encyclopediaTab.collectAsStateWithLifecycle()

    val coinsByVM by gameViewModel.coinsBalance.collectAsStateWithLifecycle()
    val unlockedSkins by gameViewModel.unlockedSkins.collectAsStateWithLifecycle()
    val equippedSkin by gameViewModel.equippedSkin.collectAsStateWithLifecycle()

    val skins = listOf(
        SkinItem("default", "ჩვეულებრივი სვანი", "Default Svan Builder", "ტრადიციული შავ-თეთრი ჩოხითა და თეთრი ფაფახით.", "Classic obsidian chokha and white wool papakha.", 0, "უფასო / Free", GeorgianGold, "🛡️"),
        SkinItem("svan_scout", "სვანი მზვერავი", "Svan Scout Hunter", "მწვანე ჩოხითა და ტყის თბილი სამოსით მზვერავისათვის.", "Rustic forest-green chokha crafted for alpine scouts.", 15, "ბიუჯეტური • 15 Coins", Color(0xFF2E6F40), "🌲"),
        SkinItem("khevsur_warrior", "ხევსური მცველი", "Khevsur Bold Knight", "ხევსურული ორნამენტებით, ჯვრებითა და მორთული ფარით.", "Decorated with custom cross tunic and native shield embroidery.", 30, "ბიუჯეტური • 30 Coins", Color(0xFF1565C0), "🛡️"),
        SkinItem("golden", "ოქროს მეომარი", "Golden Svan Warrior", "ღვთაებრივი ოქროს აურითა და მბზინავი ქართული მუზარადით.", "Glow with a divine golden aura and shiny warrior helmet.", 50, "ოქრო • 50 Coins", Color(0xFFFFD700), "👑"),
        SkinItem("royal_tamar", "თამარის გვარდიელი", "Tamar's Royal Guard", "დიდებული მუქი ლურჯი ჩოხითა და სამეფო წითელი მფრინავი მოსასხამით.", "Royal blue tunic accompanied by a red flapping imperial cape.", 100, "სამეფო • 100 Coins", Color(0xFF00E5FF), "⚔️"),
        SkinItem("amirani_fire", "ამირანის ცეცხლი", "Amirani's Flame Spirit", "კავკასიონის მითიური ცეცხლოვანი სული, რომელიც ცეცხლის ნაკვალევს ტოვებს.", "Fiery warrior leaving magma trials behind in Svaneti.", 200, "ლეგენდარული • 200 Coins", Color(0xFFFF3D00), "🔥")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvanObsidian)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = GeorgianGold,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                val appLanguage by gameViewModel.language.collectAsStateWithLifecycle()
                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) "კულტურული ჰაბი" else "HERITAGE HUB",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeorgianGold
                )
                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) "ისტორიული გრაგნილები, ვიკიპედიის ბმულები და მებრძოლები" else "Chronicles, Wikipedia Links & Outfits",
                    fontSize = 11.sp,
                    color = CaucasusSnow.copy(alpha = 0.6f)
                )
            }
        }

        val appLanguage by gameViewModel.language.collectAsStateWithLifecycle()

        // Custom M3 Segmented Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(SvanCharcoal, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { gameViewModel.setEncyclopediaTab(0) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) GeorgianGold else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) "ენციკლოპედია" else "ENCYCLOPEDIA",
                    color = if (activeTab == 0) SvanObsidian else CaucasusSnow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { gameViewModel.setEncyclopediaTab(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) GeorgianGold else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (appLanguage == AppLanguage.GEORGIAN) "სკინების მაღაზია" else "SKINS SHOP",
                    color = if (activeTab == 1) SvanObsidian else CaucasusSnow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        if (activeTab == 0) {
            // TAB 1: FACT CLOUD ENCYCLOPEDIA
            if (selectedFact == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (appLanguage == AppLanguage.GEORGIAN) {
                                "შეაგროვეთ ისტორიის გრაგნილები და ქვევრები თამაშში ახალი თავების გასახსნელად!"
                            } else {
                                "Collect historical scrolls and kvevris in the game to unlock new heritage chapters!"
                            },
                            fontSize = 11.sp,
                            color = CaucasusSnow.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )
                    }

                    itemsIndexed(HistoryFactsProvider.facts) { index, fact ->
                        val isUnlocked = unlockedIds.contains(fact.id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isUnlocked) {
                                        selectedFact = fact
                                    }
                                }
                                .testTag("fact_item_${fact.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) SvanCharcoal else Color.Black.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUnlocked) fact.iconSymbol else "🔒",
                                    fontSize = 28.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isUnlocked) fact.titleGe else "დაბლოკილია / Locked Item",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) GeorgianGold else Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isUnlocked) fact.titleEn else fact.unlockRequirementEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isUnlocked) CaucasusSnow.copy(alpha = 0.7f) else GeorgianCrimson,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Read details",
                                        tint = GeorgianGold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                selectedFact?.let { fact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(4.dp),
                        colors = CardDefaults.cardColors(containerColor = SvanCharcoal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = fact.iconSymbol, fontSize = 40.sp)
                                IconButton(onClick = { selectedFact = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close detailed view",
                                        tint = CaucasusSnow
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) fact.eraGe else fact.eraEn,
                                color = GeorgianGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) fact.titleGe else fact.titleEn,
                                color = CaucasusSnow,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (appLanguage == AppLanguage.GEORGIAN) fact.titleEn else fact.titleGe,
                                color = CaucasusSnow.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f)
                             ) {
                                item {
                                    Text(
                                        text = if (appLanguage == AppLanguage.GEORGIAN) fact.textGe else fact.textEn,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp,
                                        color = CaucasusSnow,
                                        textAlign = TextAlign.Left
                                    )
                                }
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(GeorgianCrimson.copy(alpha = 0.2f))
                                    )
                                }
                                item {
                                    Text(
                                        text = if (appLanguage == AppLanguage.GEORGIAN) fact.textEn else fact.textGe,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = CaucasusSnow.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Left
                                    )
                                }
                                item {
                                    // Clickable source external intent button linking to academic material source
                                    val context = LocalContext.current
                                    Button(
                                        onClick = {
                                            try {
                                                val uri = android.net.Uri.parse(fact.sourceUrl)
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Graceful
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GeorgianGold),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .testTag("wikipedia_button_${fact.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Read Fact Online",
                                            tint = SvanObsidian,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (appLanguage == AppLanguage.GEORGIAN) "წყარო • ვიკიპედიის ბმული" else "RESEARCH • WIKIPEDIA SOURCE",
                                            color = SvanObsidian,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: SKIN SHOP PANEL
            // Wallet Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SvanCharcoal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ხელმისაწვდომი ბიუჯეტი • Purse",
                            color = CaucasusSnow.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "🪙 $coinsByVM ოქროს მონეტა",
                            color = GeorgianGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Earn coins during gameplay and scoring bonuses!",
                            color = CaucasusSnow.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                    Text(text = "🎒", fontSize = 34.sp)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(skins) { _, skin ->
                    val isUnlocked = unlockedSkins.contains(skin.id)
                    val isEquipped = equippedSkin == skin.id
                    val canAfford = coinsByVM >= skin.cost

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skin_item_${skin.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) GeorgianCrimson.copy(alpha = 0.2f) else SvanCharcoal
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isEquipped) androidx.compose.foundation.BorderStroke(1.5.dp, GeorgianGold) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(SvanObsidian, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = skin.previewSymbol, fontSize = 28.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = skin.nameGe,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeorgianGold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = skin.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = skin.badgeColor,
                                        modifier = Modifier
                                            .background(skin.badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = skin.nameEn,
                                    fontSize = 11.sp,
                                    color = CaucasusSnow.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = skin.descriptionEn,
                                    fontSize = 10.sp,
                                    color = CaucasusSnow.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (isUnlocked) {
                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .background(GeorgianGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "აქტიური",
                                            color = GeorgianGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { gameViewModel.equipSkin(skin.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SvanObsidian),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = "EQUIP",
                                            color = CaucasusSnow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { gameViewModel.purchaseSkin(skin.id) },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canAfford) GeorgianGold else Color.DarkGray,
                                        disabledContainerColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "BUY",
                                        color = if (canAfford) SvanObsidian else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ANCIENT LEADERBOARDS RECORDS SCREEN
// ==========================================
@Composable
fun LeaderboardsScreen(
    highScores: List<ScoreEntry>,
    gameViewModel: GameViewModel,
    onBackClick: () -> Unit,
    onClearRecords: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Local Highscores, 1 = Online Leaderboard

    val onlineScores by gameViewModel.onlineLeaderboard.collectAsStateWithLifecycle()

    // Synchronize online scores
    LaunchedEffect(Unit) {
        gameViewModel.refreshOnlineLeaderboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvanObsidian)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GeorgianGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ლიდერბორდი • RANKS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeorgianGold
                    )
                    Text(
                        text = "Eternal Hall of Fame & Online Rivals",
                        fontSize = 11.sp,
                        color = CaucasusSnow.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(onClick = onClearRecords) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Wipe logs",
                    tint = GeorgianCrimson
                )
            }
        }

        // Custom M3 Segmented Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(SvanCharcoal, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { activeTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) GeorgianGold else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ლოკალური • LOCAL",
                    color = if (activeTab == 0) SvanObsidian else CaucasusSnow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { activeTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) GeorgianGold else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "გლობალური • ONLINE",
                    color = if (activeTab == 1) SvanObsidian else CaucasusSnow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // User Name entry & Online Score Submission
        if (activeTab == 1 && highScores.isNotEmpty()) {
            var customName by remember { mutableStateOf("") }
            val highestLocalScore = highScores.maxByOrNull { it.score }

            highestLocalScore?.let { bestLocal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, GeorgianGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SvanCharcoal)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "რეკორდის ატვირთვა • PUBLISH BEST SCORE",
                            color = GeorgianGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sync highest score: ${bestLocal.score} PTS under a secure username!",
                            color = CaucasusSnow.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                placeholder = { Text("Enter your name...", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GeorgianGold,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("online_username_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    if (customName.isNotBlank()) {
                                        gameViewModel.uploadScoreToOnlineLeaderboard(customName)
                                        customName = ""
                                    }
                                },
                                enabled = customName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeorgianGold,
                                    disabledContainerColor = Color.DarkGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text("SUBMIT", color = SvanObsidian, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        if (activeTab == 0) {
            // TAB 1: LOCAL ROOM HISTORICAL HIGHSCORES
            if (highScores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📜", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ქრონიკები ცარიელია.",
                            color = CaucasusSnow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "No local runs recorded. Start a legend on the field!",
                            color = CaucasusSnow.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SvanCharcoal)
                        .padding(8.dp)
                ) {
                    itemsIndexed(highScores) { index, entry ->
                        val rank = index + 1
                        val dateFormatted = try {
                            dateFormatter.format(Date(entry.dateMillis))
                        } catch (e: Exception) {
                            "Ancient times"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = when (rank) {
                                                1 -> GeorgianGold
                                                2 -> Color(0xFFC0C0C0)
                                                3 -> Color(0xFFCD7F32)
                                                else -> Color.DarkGray
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rank.toString(),
                                        color = if (rank <= 3) SvanObsidian else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (entry.playerName.isBlank()) "უცნობი გმირი / Svan Defender" else entry.playerName,
                                        color = CaucasusSnow,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$dateFormatted • Lvl ${entry.level}",
                                        color = CaucasusSnow.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                }

                                Text(
                                    text = "${entry.score} PTS",
                                    color = GeorgianGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        } else {
            // TAB 2: ONLINE SIMULATED GLOBAL LEADERBOARD
            if (onlineScores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GeorgianGold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SvanCharcoal)
                        .padding(8.dp)
                ) {
                    itemsIndexed(onlineScores) { index, entry ->
                        val rank = index + 1
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = when (rank) {
                                                1 -> GeorgianGold
                                                2 -> Color(0xFFC0C0C0)
                                                3 -> Color(0xFFCD7F32)
                                                else -> Color.DarkGray
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rank.toString(),
                                        color = if (rank <= 3) SvanObsidian else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.countryFlag + " " + entry.playerName,
                                            color = CaucasusSnow,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (entry.isUser) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "YOU",
                                                color = GeorgianGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .background(GeorgianGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "გლობალური სერვერი • Live Record",
                                        color = CaucasusSnow.copy(alpha = 0.4f),
                                        fontSize = 9.sp
                                    )
                                }

                                Text(
                                    text = "${entry.score} PTS",
                                    color = GeorgianGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// REAL-TIME CELEBRATION UNLOCK SLIDE BANNER
// ==========================================
@Composable
fun UnlockBanner(fact: HistoryFact) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .border(2.dp, GeorgianGold, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = GeorgianCrimson),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(GeorgianGold, RoundedCornerShape(23.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📜", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ისტორია გაიხსნა! NEW FACT UNLOCKED",
                    fontSize = 11.sp,
                    color = GeorgianGold,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = fact.titleGe,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = fact.titleEn,
                    fontSize = 11.sp,
                    color = CaucasusSnow.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// SVAN DYNAMIC RANK SYSTEM ("maked rank")
// ==========================================
data class PlayerRankInfo(
    val title: String,
    val enTitle: String,
    val icon: String,
    val color: Color,
    val nextThreshold: String
)

fun calculatePlayerRank(highestScore: Int): PlayerRankInfo {
    return when {
        highestScore >= 5000 -> PlayerRankInfo("კავკასიონის ლეგენდა", "LEGEND", "🌋", Color(0xFFFF3D00), "MAX RANK ACHIEVED")
        highestScore >= 3500 -> PlayerRankInfo("დიდგორი ჩემპიონი", "ROYAL CHAMPION", "👑", Color(0xFFFFD700), "Next: 5000 PTS")
        highestScore >= 2000 -> PlayerRankInfo("ხევსური რაინდი", "KHEVSUR KNIGHT", "⚔️", Color(0xFF00E5FF), "Next: 3500 PTS")
        highestScore >= 1000 -> PlayerRankInfo("კოშკის მცველი", "TOWER GUARD", "🛡️", GeorgianGold, "Next: 2000 PTS")
        highestScore >= 500  -> PlayerRankInfo("არქივის მცველი", "ARCHIVE KEEPER", "📜", Color(0xFF81C784), "Next: 1000 PTS")
        highestScore >= 200  -> PlayerRankInfo("მთის მზვერავი", "MOUNTAIN SCOUT", "🌲", Color(0xFFAED581), "Next: 500 PTS")
        else -> PlayerRankInfo("ახალბედა გმირი", "NOVICE INITIATE", "🆕", Color(0xFFB0BEC5), "Next: 200 PTS")
    }
}
