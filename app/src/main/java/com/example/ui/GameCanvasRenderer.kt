package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.ItemType
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

object GameCanvasRenderer {

    fun drawParallaxMountains(
        scope: DrawScope,
        offsetFar: Float,
        offsetMid: Float,
        offsetNear: Float,
        level: Int = 1
    ) {
        val width = scope.size.width
        val height = scope.size.height
        val groundHeight = 120f // Height from bottom

        // 1. Level-dependent sky color background gradient representing Georgia's corners
        val skyBrush = when (level) {
            1 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617)) // Svaneti deep alpine night
            )
            2 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF581C87), Color(0xFFD946EF), Color(0xFF0F052D)) // Kakheti sunset purple-magenta
            )
            3 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF7C2D12), Color(0xFFF97316), Color(0xFF1C0D02)) // Tbilisi deep sunrise orange glow
            )
            4 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF1E1B4B), Color(0xFF4F46E5), Color(0xFF030712)) // Adjara deep purple/indigo oceanic evening
            )
            5 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF042F1A), Color(0xFF10B981), Color(0xFF02170D)) // Kazbegi mystical cold mint sky
            )
            6 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF1E1B4B), Color(0xFF86198F), Color(0xFF05051E)) // Imereti subterranean cave lights
            )
            7 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF022C22)) // Samegrelo jade canyon mist
            )
            8 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF450A0A), Color(0xFFEF4444), Color(0xFF1A0505)) // Tusheti extreme blood-red dusk
            )
            9 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF4338CA), Color(0xFFFACC15), Color(0xFF0B132B)) // Shida Kartli cave-light gold-cyan
            )
            10 -> Brush.verticalGradient(
                colors = listOf(Color(0xFF1E3A8A), Color(0xFFF59E0B), Color(0xFF020617)) // Racha royal blue-auroral autumn sunset
            )
            else -> Brush.verticalGradient(
                colors = listOf(Color(0xFF172554), Color(0xFF3B82F6), Color(0xFF022C22))
            )
        }
        scope.drawRect(
            brush = skyBrush,
            topLeft = Offset.Zero,
            size = Size(width, height)
        )

        // 1.1 Cosmic Aurora Borealis drifting bands (for Svaneti (1), Kazbegi (5), Tusheti (8), Racha (10))
        if (level == 1 || level == 5 || level == 8 || level == 10) {
            val auroraPath = Path().apply {
                var curX = 0f
                val step = 45f
                moveTo(0f, height * 0.25f)
                while (curX < width + step) {
                    val waveHeight = sin(offsetFar * 0.012f + curX * 0.005f).toFloat() * 38f
                    val y = height * 0.22f + waveHeight
                    lineTo(curX, y)
                    curX += step
                }
                lineTo(width, height * 0.45f)
                curX = width
                while (curX >= -step) {
                    val waveHeight = sin(offsetFar * 0.012f + curX * 0.005f + 1.6f).toFloat() * 42f
                    val y = height * 0.38f + waveHeight
                    lineTo(curX, y)
                    curX -= step
                }
                close()
            }
            val auroraColor = when (level) {
                1 -> Color(0xFF0D9488) // Warm emerald
                5 -> Color(0xFF22D3EE) // Cool glacier cyan
                8 -> Color(0xFFEF4444) // Wild red energy
                10 -> Color(0xFFE2E8F0) // Golden celestial dust
                else -> Color(0xFF10B981)
            }
            scope.drawPath(
                path = auroraPath,
                color = auroraColor.copy(alpha = 0.12f + sin(offsetFar * 0.02f).toFloat() * 0.04f)
            )
        }

        // 1.2 Twinkling sky stars
        val starSeed = 42L
        val random = java.util.Random(starSeed)
        val maxStars = 25 + level * 5
        for (i in 0..maxStars) {
            val starX = (random.nextFloat() * width + offsetFar * 0.2f) % width
            val starY = random.nextFloat() * (height * 0.58f)
            val starSize = 2.5f + random.nextFloat() * (4.5f + level * 0.5f)
            val twinkleFreq = 0.04f + (i % 3) * 0.02f
            val starAlpha = 0.25f + 0.75f * sin((offsetFar * twinkleFreq + i).toDouble()).toFloat()
            scope.drawCircle(
                color = Color.White.copy(alpha = maxOf(0f, minOf(1f, starAlpha))),
                radius = starSize,
                center = Offset(starX, starY)
            )
        }

        // 1.2.2 Fast Shooting Stars sweeping across the nighttime skies (levels 1, 4, 6, 8, 10)
        if (level == 1 || level == 4 || level == 6 || level == 8 || level == 10) {
            val loopDist = 450f
            val sProgress = (offsetFar % loopDist) / loopDist
            if (sProgress < 0.45f) {
                val sAlpha = sin(sProgress * (3.14159f / 0.45f)) * 0.85f
                val starStartX = width * 0.88f - (offsetFar % 1200f) * 0.4f
                val starStartY = height * 0.08f + (offsetFar % 1200f) * 0.2f
                val dx = -220f * sProgress
                val dy = 140f * sProgress
                scope.drawLine(
                    color = Color.White.copy(alpha = sAlpha),
                    start = Offset(starStartX + dx, starStartY + dy),
                    end = Offset(starStartX + dx - 60f, starStartY + dy + 38f),
                    strokeWidth = 3f
                )
            }
        }

        // 1.2.3 Drifting soft atmospheric clouds (levels 2, 3, 7, 9)
        if (level == 2 || level == 3 || level == 7 || level == 9) {
            val numClouds = 4
            for (c in 0 until numClouds) {
                val cloudS = 180f + c * 60f
                val driftDir = if (c % 2 == 0) 1f else -1f
                val cloudX = (offsetFar * 0.25f * driftDir + c * 380f) % (width + 350f) - 175f
                val cloudY = height * 0.1f + c * 38f
                scope.drawOval(
                    color = Color.White.copy(alpha = 0.08f + sin(offsetFar * 0.015f + c).toFloat() * 0.02f),
                    topLeft = Offset(cloudX, cloudY),
                    size = Size(cloudS, cloudS * 0.35f)
                )
            }
        }

        // 1.3 Celestial light body (Crescent or Sunrise Sun) or Cave crystals
        if (level != 6) { // Caves don't have moon
            val moonColor = when (level) {
                1 -> Color(0xFFFFD54F) // warm gold
                2 -> Color(0xFF00E676) // neon green
                3 -> Color(0xFFFFF176) // glowing sun
                4 -> Color(0xFFEA80FC) // magenta neon
                5 -> Color(0xFF69F0AE) // mint electric
                7 -> Color(0xFFFFD700) // royal pure gold
                8 -> Color(0xFFFF4081) // vivid hot pink
                9 -> Color(0xFFA7FFEB) // shimmering aurora teal
                else -> Color(0xFFFFFFFF) // divine white star glow
            }
            val pulseRadius = 115f + sin(offsetFar * 0.04f).toFloat() * 12f
            scope.drawCircle(
                color = moonColor.copy(alpha = 0.14f),
                radius = pulseRadius,
                center = Offset(width * 0.82f, height * 0.22f)
            )
            scope.drawCircle(
                color = moonColor,
                radius = 35f,
                center = Offset(width * 0.82f, height * 0.22f)
            )
            // Crop for crescent except on sunrise levels
            if (level != 3 && level != 10) {
                val cropColor = when (level) {
                    1 -> Color(0xFF0F172A)
                    2 -> Color(0xFF581C87)
                    4 -> Color(0xFF1E1B4B)
                    5 -> Color(0xFF042F1A)
                    7 -> Color(0xFF064E3B)
                    8 -> Color(0xFF450A0A)
                    9 -> Color(0xFF4338CA)
                    else -> Color(0xFF1A1A1A)
                }
                scope.drawCircle(
                    color = cropColor,
                    radius = 32f,
                    center = Offset(width * 0.79f, height * 0.19f)
                )
            }
        }

        // 1.4 Dynamic Weather/Atmospheric Effects per region of Georgia
        if (level == 5) {
            // Kazbegi Blizzard: Fast snowy blizzard lines blowing across the screen
            val windSeed = 100L
            val windRand = java.util.Random(windSeed)
            for (w in 0..12) {
                val windY = windRand.nextFloat() * height
                val windLen = 180f + windRand.nextFloat() * 200f
                val windX = ((offsetNear * 4.5f) + windRand.nextFloat() * width) % (width + windLen) - windLen
                scope.drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(windX, windY),
                    end = Offset(windX + windLen, windY - 20f),
                    strokeWidth = 3f
                )
            }
        } else if (level == 4) {
            // Adjara: Soft glowing ocean wave foam lines scrolling across the bottom sea and moon reflections
            val foamBase = height - groundHeight - 110f
            val reflectionWidth = 140f + sin(offsetNear * 0.05f).toFloat() * 20f
            val reflectionRPath = Path().apply {
                moveTo(width * 0.82f - reflectionWidth * 0.5f, foamBase)
                lineTo(width * 0.82f + reflectionWidth * 0.5f, foamBase)
                lineTo(width * 0.82f + reflectionWidth * 1.5f, height - groundHeight)
                lineTo(width * 0.82f - reflectionWidth * 1.5f, height - groundHeight)
                close()
            }
            scope.drawPath(
                path = reflectionRPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFEA80FC).copy(alpha = 0.15f), Color(0xFF00E5FF).copy(alpha = 0.4f))
                )
            )

            for (wi in 0..5) {
                val waveX = (offsetNear * 0.6f + wi * 260f) % (width + 200f) - 100f
                scope.drawArc(
                    color = Color(0xFFCCFFFA).copy(alpha = 0.45f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(waveX, foamBase + (wi * 10f)),
                    size = Size(90f, 18f)
                )
            }
        } else if (level == 6) {
            // Imereti Caves Subterranean Cave Roof
            val ceilingPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, 120f)
                var curX = 0f
                val step = 100f
                while (curX < width + step) {
                    // Dangling stalactites from above
                    lineTo(curX + step * 0.3f, 150f + sin(curX).toFloat() * 30f)
                    lineTo(curX + step * 0.5f, 50f)
                    lineTo(curX + step * 0.8f, 190f + cos(curX).toFloat() * 40f)
                    lineTo(curX + step, 100f)
                    curX += step
                }
                lineTo(width, 0f)
                close()
            }
            scope.drawPath(ceilingPath, color = Color(0xFF100B22))
        }

        // 2. Distant Mountains (Far): Custom mountain/coastline silhouette per region - extremely slow parallax
        val pathFar = Path().apply {
            moveTo(0f, height - groundHeight)
            when (level) {
                4 -> {
                    // Adjara - Flat Sea horizon
                    lineTo(0f, height - groundHeight - 120f)
                    lineTo(width, height - groundHeight - 120f)
                }
                6 -> {
                    // Imereti Caves - Subterranean Cave bottom outline
                    lineTo(0f, height - groundHeight - 60f)
                    var curX = 0f
                    val step = 120f
                    while (curX < width + step) {
                        lineTo(curX + step * 0.3f, height - groundHeight - 90f)
                        lineTo(curX + step * 0.7f, height - groundHeight - 50f)
                        lineTo(curX + step, height - groundHeight - 60f)
                        curX += step
                    }
                }
                2, 7 -> {
                    // Kakheti / Samegrelo - Gentle rolling hills
                    lineTo(0f, height - groundHeight - 80f)
                    var curX = 0f
                    val step = 300f
                    while (curX < width + step) {
                        quadraticTo(
                            curX + step * 0.5f, height - groundHeight - 160f,
                            curX + step, height - groundHeight - 80f
                        )
                        curX += step
                    }
                }
                3 -> {
                    // Tbilisi - Ridges of Narikala and Mtatsminda
                    lineTo(0f, height - groundHeight - 100f)
                    lineTo(width * 0.3f, height - groundHeight - 180f)
                    lineTo(width * 0.6f, height - groundHeight - 130f)
                    lineTo(width, height - groundHeight - 220f)
                }
                9 -> {
                    // Shida Kartli - Uplistsikhe plateaus and square rocky formations
                    lineTo(0f, height - groundHeight - 100f)
                    var curX = 0f
                    val step = 400f
                    while (curX < width + step) {
                        lineTo(curX + 80f, height - groundHeight - 110f)
                        lineTo(curX + 110f, height - groundHeight - 220f)
                        lineTo(curX + 280f, height - groundHeight - 220f)
                        lineTo(curX + 310f, height - groundHeight - 110f)
                        curX += step
                    }
                }
                else -> {
                    // Svaneti (1), Kazbegi (5), Tusheti (8), Racha (10) - Alpine Giants
                    val spacing = 500f
                    val startX = -(offsetFar % spacing)
                    var currentX = startX
                    lineTo(currentX, height - groundHeight)
                    while (currentX < width + spacing) {
                        lineTo(currentX + 150f, height - 350f)
                        lineTo(currentX + 220f, height - 280f)
                        lineTo(currentX + 300f, height - 425f) // Tallest peak
                        lineTo(currentX + 450f, height - groundHeight)
                        currentX += spacing
                    }
                }
            }
            lineTo(width, height - groundHeight)
            close()
        }

        val farColor = when (level) {
            1 -> Color(0xFF232535)
            2 -> Color(0xFF2A1C3C)
            3 -> Color(0xFF40282C)
            4 -> Color(0xFF0A163B)
            5 -> Color(0xFF163C2E)
            6 -> Color(0xFF161026)
            7 -> Color(0xFF0F3225)
            8 -> Color(0xFF411213)
            9 -> Color(0xFF4E3725)
            10 -> Color(0xFF1E2E4E)
            else -> Color(0xFF282530)
        }
        scope.drawPath(pathFar, color = farColor)

        // Tallest peak snowcaps for alpine regions
        if (level == 1 || level == 5 || level == 8 || level == 10) {
            val pathSnow = Path().apply {
                val spacing = 500f
                val startX = -(offsetFar % spacing)
                var currentX = startX
                while (currentX < width + spacing) {
                    moveTo(currentX + 110f, height - 300f)
                    lineTo(currentX + 150f, height - 350f)
                    lineTo(currentX + 180f, height - 310f)
                    close()

                    moveTo(currentX + 260f, height - 350f)
                    lineTo(currentX + 300f, height - 425f)
                    lineTo(currentX + 340f, height - 340f)
                    close()
                    currentX += spacing
                }
            }
            scope.drawPath(pathSnow, color = CaucasusSnow.copy(alpha = 0.5f))
        }

        // 3. Middle range: Region specific landscape and landmarks - mid-speed parallax
        val pathMid = Path().apply {
            moveTo(0f, height - groundHeight)
            val spacing = 350f
            val startX = -(offsetMid % spacing)
            var currentX = startX
            lineTo(startX, height - groundHeight)
            while (currentX < width + spacing) {
                when (level) {
                    2, 7 -> {
                        // Kakheti / Samegrelo - lower hills
                        lineTo(currentX + 100f, height - groundHeight - 40f)
                        lineTo(currentX + 200f, height - groundHeight - 60f)
                        lineTo(currentX + 300f, height - groundHeight)
                    }
                    4 -> {
                        // Adjara - Gentle beach dunes or wave crests
                        lineTo(currentX + 100f, height - groundHeight - 15f)
                        lineTo(currentX + 200f, height - groundHeight - 20f)
                        lineTo(currentX + 300f, height - groundHeight)
                    }
                    else -> {
                        // Default mountain ridges
                        lineTo(currentX + 100f, height - 210f)
                        lineTo(currentX + 200f, height - 260f)
                        lineTo(currentX + 300f, height - groundHeight)
                    }
                }
                currentX += spacing
            }
            lineTo(width, height - groundHeight)
            close()
        }

        val midColor = when (level) {
            1 -> Color(0xFF161019)
            2 -> Color(0xFF1E102F)
            3 -> Color(0xFF2D181C)
            4 -> Color(0xFF030D25)
            5 -> Color(0xFF0C241B)
            6 -> Color(0xFF0F0B1E)
            7 -> Color(0xFF072118)
            8 -> Color(0xFF2C0B0C)
            9 -> Color(0xFF322316)
            10 -> Color(0xFF122036)
            else -> Color(0xFF1B181E)
        }
        scope.drawPath(pathMid, color = midColor)

        // Draw level-specific middle layer architectural or natural landmarks!
        val spacingMid = 400f
        val startMidX = -(offsetMid % spacingMid)
        var midX = startMidX
        while (midX < width + spacingMid) {
            val baseMidY = height - groundHeight

            when (level) {
                1 -> {
                    // Svaneti - Svan Towers silhouette on hills!
                    val towerHeight = 120f
                    val towerWidth = 35f
                    val towerBaseY = baseMidY - 30f

                    scope.drawRect(
                        color = SvanTowerGrey.copy(alpha = 0.5f),
                        topLeft = Offset(midX + 80f, towerBaseY - towerHeight),
                        size = Size(towerWidth, towerHeight)
                    )
                    val roofPath = Path().apply {
                        moveTo(midX + 76f, towerBaseY - towerHeight)
                        lineTo(midX + 80f + (towerWidth / 2f), towerBaseY - towerHeight - 15f)
                        lineTo(midX + 84f + towerWidth, towerBaseY - towerHeight)
                        close()
                    }
                    scope.drawPath(roofPath, color = GeorgianCrimson.copy(alpha = 0.6f))

                    // Window slits
                    scope.drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(midX + 85f, towerBaseY - towerHeight + 20f),
                        size = Size(6f, 10f)
                    )
                    scope.drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(midX + 100f, towerBaseY - towerHeight + 20f),
                        size = Size(6f, 10f)
                    )
                }
                2 -> {
                    // Kakheti - Grape Trellis Rows (wooden poles with vines climbing)
                    val frameH = 80f
                    scope.drawLine(
                        color = Color(0xFF4A3423).copy(alpha = 0.6f),
                        start = Offset(midX + 100f, baseMidY),
                        end = Offset(midX + 100f, baseMidY - frameH),
                        strokeWidth = 6f
                    )
                    scope.drawLine(
                        color = Color(0xFF4A3423).copy(alpha = 0.6f),
                        start = Offset(midX + 60f, baseMidY - frameH),
                        end = Offset(midX + 140f, baseMidY - frameH),
                        strokeWidth = 4f
                    )
                    // Hanging grape dots
                    scope.drawCircle(color = Color(0xFFA21CAF).copy(alpha = 0.7f), radius = 6f, center = Offset(midX + 75f, baseMidY - frameH + 12f))
                    scope.drawCircle(color = Color(0xFFA21CAF).copy(alpha = 0.7f), radius = 6f, center = Offset(midX + 125f, baseMidY - frameH + 12f))
                    scope.drawCircle(color = Color(0xFF047857).copy(alpha = 0.7f), radius = 8f, center = Offset(midX + 100f, baseMidY - frameH - 5f)) // leaf
                }
                3 -> {
                    // Tbilisi - Narikala brick domes and fort walls with sulfur bath steam animation
                    val wallW = 120f
                    val wallH = 60f
                    scope.drawRect(
                        color = Color(0xFFD4A373).copy(alpha = 0.45f),
                        topLeft = Offset(midX + 50f, baseMidY - wallH),
                        size = Size(wallW, wallH)
                    )
                    // Merlons (battlements code)
                    for (bx in 0..4) {
                        scope.drawRect(
                            color = Color(0xFFD4A373).copy(alpha = 0.45f),
                            topLeft = Offset(midX + 50f + bx * 24f, baseMidY - wallH - 12f),
                            size = Size(12f, 12f)
                        )
                    }
                    // Brick sulfur bath dome
                    val domeCenterX = midX + 140f
                    scope.drawArc(
                        color = Color(0xFFA78BFA).copy(alpha = 0.4f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(midX + 120f, baseMidY - 35f),
                        size = Size(40f, 40f)
                    )
                    // Puffy dynamic steam particles rising from the sulfur dome
                    val steamTick = (offsetNear * 0.4f + midX).toDouble()
                    for (sh in 0..2) {
                        val sy = baseMidY - 40f - ((steamTick + sh * 40) % 90).toFloat()
                        val sx = domeCenterX + sin(steamTick * 0.05 + sh).toFloat() * 15f
                        val sScale = 5f + ((baseMidY - 40f - sy) / 90f) * 15f
                        val sAlpha = 0.45f * (1f - (baseMidY - 40f - sy) / 90f)
                        scope.drawCircle(
                            color = Color.White.copy(alpha = maxOf(0f, sAlpha)),
                            radius = sScale,
                            center = Offset(sx, sy)
                        )
                    }
                }
                4 -> {
                    // Adjara Beach - Sailing boats and scenic clouds near the ocean
                    val boatX = midX + 150f
                    val boatY = baseMidY - 35f
                    val boatPath = Path().apply {
                        moveTo(boatX, boatY)
                        lineTo(boatX + 40f, boatY)
                        lineTo(boatX + 30f, boatY + 12f)
                        lineTo(boatX + 10f, boatY + 12f)
                        close()
                    }
                    scope.drawPath(boatPath, color = CaucasusSnow.copy(alpha = 0.5f))
                    // Sail
                    val sailPath = Path().apply {
                        moveTo(boatX + 20f, boatY)
                        lineTo(boatX + 20f, boatY - 25f)
                        lineTo(boatX + 35f, boatY - 8f)
                        close()
                    }
                    scope.drawPath(sailPath, color = Color(0xFFFFD700).copy(alpha = 0.4f))
                }
                5 -> {
                    // Kazbegi - Gergeti church silhouette on top of the mid hill!
                    val cx = midX + 100f
                    val cy = baseMidY - 110f
                    // Draw Cathedral rectangle
                    scope.drawRect(color = Color(0xFF475569).copy(alpha = 0.5f), topLeft = Offset(cx, cy - 40f), size = Size(50f, 40f))
                    // Dome cone roof
                    val domePath = Path().apply {
                        moveTo(cx + 10f, cy - 40f)
                        lineTo(cx + 25f, cy - 65f)
                        lineTo(cx + 40f, cy - 40f)
                        close()
                    }
                    scope.drawPath(domePath, color = SvanTowerGrey.copy(alpha = 0.6f))
                    // Cross
                    scope.drawLine(color = GeorgianGold.copy(alpha = 0.6f), start = Offset(cx + 25f, cy - 65f), end = Offset(cx + 25f, cy - 75f), strokeWidth = 2f)
                    scope.drawLine(color = GeorgianGold.copy(alpha = 0.6f), start = Offset(cx + 21f, cy - 71f), end = Offset(cx + 29f, cy - 71f), strokeWidth = 2f)
                }
                6 -> {
                    // Imereti Caves - Subterranean columns / Stalagmites growing from floor
                    val colX = midX + 120f
                    val colPath = Path().apply {
                        moveTo(colX, baseMidY)
                        lineTo(colX + 15f, baseMidY - 130f)
                        lineTo(colX + 25f, baseMidY - 130f)
                        lineTo(colX + 40f, baseMidY)
                        close()
                    }
                    scope.drawPath(colPath, color = Color(0xFFC084FC).copy(alpha = 0.4f))
                    // Crystal sparkles
                    scope.drawCircle(color = Color(0xFF22D3EE).copy(alpha = 0.7f), radius = 5f, center = Offset(colX + 20f, baseMidY - 90f))
                }
                7 -> {
                    // Samegrelo - Martvili waterfalls
                    val fallX = midX + 80f
                    scope.drawRect(color = Color(0xFF064E3B).copy(alpha = 0.5f), topLeft = Offset(fallX, baseMidY - 80f), size = Size(60f, 80f))
                    for (wi in 0..4) {
                        scope.drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                            start = Offset(fallX + 10f + wi * 10f, baseMidY - 50f),
                            end = Offset(fallX + 10f + wi * 10f, baseMidY),
                            strokeWidth = 3f
                        )
                    }
                }
                8 -> {
                    // Tusheti Keselo - rugged stone watchtowers nestled in cliffs with glowing fire beacons
                    val rx = midX + 60f
                    val fortPath = Path().apply {
                        moveTo(rx, baseMidY)
                        lineTo(rx + 15f, baseMidY - 80f)
                        lineTo(rx + 55f, baseMidY - 80f)
                        lineTo(rx + 70f, baseMidY)
                        close()
                    }
                    scope.drawPath(fortPath, color = Color(0xFF1E1E1E).copy(alpha = 0.6f))
                    scope.drawRect(color = Color(0xFF334155).copy(alpha = 0.5f), topLeft = Offset(rx + 25f, baseMidY - 110f), size = Size(20f, 30f))

                    // Flickering flame beacon on top of watchtower!
                    val fireX = rx + 35f
                    val fireBaseY = baseMidY - 110f
                    val flickerScale = 0.8f + sin(offsetNear * 0.15f).toFloat() * 0.25f
                    val firePath = Path().apply {
                        moveTo(fireX - 8f * flickerScale, fireBaseY)
                        quadraticTo(fireX - 10f * flickerScale, fireBaseY - 15f * flickerScale, fireX, fireBaseY - 25f * flickerScale)
                        quadraticTo(fireX + 10f * flickerScale, fireBaseY - 15f * flickerScale, fireX + 8f * flickerScale, fireBaseY)
                        close()
                    }
                    scope.drawPath(firePath, color = Color(0xFFF97316).copy(alpha = 0.85f))
                    scope.drawCircle(color = Color(0xFFEF4444).copy(alpha = 0.9f), radius = 4f * flickerScale, center = Offset(fireX, fireBaseY - 8f))
                }
                9 -> {
                    // Shida Kartli - Uplistsikhe carved hollow cave entrances!
                    val cx = midX + 120f
                    scope.drawArc(
                        color = Color(0xFF451A03).copy(alpha = 0.6f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(cx, baseMidY - 70f),
                        size = Size(80f, 80f)
                    )
                    scope.drawRect(color = Color(0xFF451A03).copy(alpha = 0.6f), topLeft = Offset(cx + 34f, baseMidY - 55f), size = Size(12f, 55f))
                }
                10 -> {
                    // Racha Shaori Lake log cabin
                    val rX = midX + 140f
                    scope.drawRect(color = Color(0xFF78350F).copy(alpha = 0.5f), topLeft = Offset(rX, baseMidY - 40f), size = Size(45f, 40f))
                    val cabinRoof = Path().apply {
                        moveTo(rX - 5f, baseMidY - 40f)
                        lineTo(rX + 22f, baseMidY - 60f)
                        lineTo(rX + 50f, baseMidY - 40f)
                        close()
                    }
                    scope.drawPath(cabinRoof, color = Color(0xFF991B1B).copy(alpha = 0.6f))
                }
            }

            midX += spacingMid
        }

        // 4. Near Ground Layer fast parallax (Foliage & vegetation native to each region)
        val pathNear = Path().apply {
            moveTo(0f, height - groundHeight)
            val spacing = 200f
            val startX = -(offsetNear % spacing)
            var currentX = startX
            lineTo(startX, height - groundHeight)
            while (currentX < width + spacing) {
                // Hill slopes
                lineTo(currentX + 100f, height - groundHeight - 20f)
                lineTo(currentX + 200f, height - groundHeight)
                currentX += spacing
            }
            lineTo(width, height - groundHeight)
            close()
        }
        val nearLayerCol = when (level) {
            4 -> Color(0xFFC4B592) // Sandy golden beach dune for Adjara!
            6 -> Color(0xFF2C253B) // Dark purple cave stone for Imereti!
            9 -> Color(0xFF86532B) // Golden dusty dirt for Uplistsikhe!
            else -> SvanCharcoal // Original nice dark coal
        }
        scope.drawPath(pathNear, color = nearLayerCol)

        // Draw fast-scrolling trees and region specific items near ground
        val treeSpacing = 280f
        var treeX = -(offsetNear % treeSpacing)
        while (treeX < width + treeSpacing) {
            val baseTy = height - groundHeight

            when (level) {
                4 -> {
                    // ADJARA: Palms with wind-swayed leaves!
                    val palmX = treeX + 60f
                    val palmH = 100f
                    // Curved Trunk
                    val trunkPath = Path().apply {
                        moveTo(palmX, baseTy)
                        quadraticTo(palmX - 15f, baseTy - palmH * 0.5f, palmX - 5f, baseTy - palmH)
                        lineTo(palmX + 3f, baseTy - palmH)
                        quadraticTo(palmX - 7f, baseTy - palmH * 0.5f, palmX + 8f, baseTy)
                        close()
                    }
                    scope.drawPath(trunkPath, color = Color(0xFF5C4033))
                    // Palm leafy foliage bowing dynamically in breeze
                    val windSway = sin(offsetNear * 0.08f + treeX).toFloat() * 12f
                    for (pd in listOf(-35f, -15f, 15f, 35f)) {
                        scope.drawLine(
                            color = Color(0xFF2E7D32),
                            start = Offset(palmX - 5f, baseTy - palmH),
                            end = Offset(palmX - 5f + pd + windSway, baseTy - palmH + 15f),
                            strokeWidth = 3f
                        )
                    }
                }
                2 -> {
                    // KAKHETI: Vine leafy bushes with purple hanging grapes!
                    val vineX = treeX + 50f
                    scope.drawCircle(color = Color(0xFF1B5E20), radius = 22f, center = Offset(vineX, baseTy - 18f))
                    scope.drawCircle(color = Color(0xFF2E7D32), radius = 15f, center = Offset(vineX - 15f, baseTy - 12f))
                    scope.drawCircle(color = Color(0xFF2E7D32), radius = 15f, center = Offset(vineX + 15f, baseTy - 12f))
                    // Purple grapes
                    scope.drawCircle(color = Color(0xFF4A148C), radius = 5f, center = Offset(vineX, baseTy - 12f))
                    scope.drawCircle(color = Color(0xFF4A148C), radius = 5f, center = Offset(vineX - 8f, baseTy - 8f))
                    scope.drawCircle(color = Color(0xFF4A148C), radius = 5f, center = Offset(vineX + 8f, baseTy - 8f))
                }
                6 -> {
                    // IMERETI: Glowing stalagmites & neon crystal shards!
                    val cryX = treeX + 40f
                    val cryPath = Path().apply {
                        moveTo(cryX, baseTy)
                        lineTo(cryX + 10f, baseTy - 50f)
                        lineTo(cryX + 15f, baseTy - 50f)
                        lineTo(cryX + 25f, baseTy)
                        close()
                    }
                    scope.drawPath(cryPath, color = Color(0xFF00E5FF))
                    // Shimmer crystal tip
                    val crystalShine = 0.5f + sin(offsetNear * 0.12f + cryX).toFloat() * 0.5f
                    scope.drawCircle(
                        color = Color(0xFF22D3EE).copy(alpha = crystalShine),
                        radius = 4f,
                        center = Offset(cryX + 12f, baseTy - 65f)
                    )
                }
                10 -> {
                    // RACHA: Autumn brilliant orange/amber maple trees!
                    val trX = treeX + 45f
                    val mapleH = 95f
                    scope.drawRect(color = Color(0xFF3E2723), topLeft = Offset(trX, baseTy - 25f), size = Size(6f, 25f))
                    scope.drawCircle(color = Color(0xFFE65100), radius = 25f, center = Offset(trX + 3f, baseTy - mapleH + 40f))
                    scope.drawCircle(color = Color(0xFFFACC15), radius = 18f, center = Offset(trX - 15f, baseTy - mapleH + 50f))
                    scope.drawCircle(color = Color(0xFFFF8F00), radius = 18f, center = Offset(trX + 18f, baseTy - mapleH + 50f))
                }
                7 -> {
                    // SAMEGRELO: Rich green ferns swaying
                    val fernX = treeX + 50f
                    val swayFern = sin(offsetNear * 0.05f + treeX).toFloat() * 8f
                    for (fd in listOf(-30f, -10f, 10f, 30f)) {
                        scope.drawArc(
                            color = Color(0xFF065F46),
                            startAngle = if (fd < 0) 180f else 270f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(fernX + fd - 15f + swayFern, baseTy - 30f),
                            size = Size(35f, 35f)
                        )
                    }
                }
                else -> {
                    // Svaneti (1), Tbilisi (3), Kazbegi (5), Tusheti (8), Shida Kartli (9) - Pine Trees
                    val treeH = 90f

                    // Trunk
                    scope.drawRect(
                        color = Color(0xFF422b12),
                        topLeft = Offset(treeX + 45f, baseTy - 20f),
                        size = Size(8f, 20f)
                    )
                    // Pine Foliage - Layer 1
                    val pathFoliage = Path().apply {
                        moveTo(treeX + 30f, baseTy - 20f)
                        lineTo(treeX + 49f, baseTy - treeH)
                        lineTo(treeX + 68f, baseTy - 20f)
                        close()
                    }
                    scope.drawPath(pathFoliage, color = VineGreen)

                    // Layer 2
                    val pathFoliage2 = Path().apply {
                        moveTo(treeX + 35f, baseTy - 50f)
                        lineTo(treeX + 49f, baseTy - treeH - 15f)
                        lineTo(treeX + 63f, baseTy - 50f)
                        close()
                    }
                    scope.drawPath(pathFoliage2, color = VineGreen.copy(red = 0.3f, green = 0.7f))
                }
            }

            treeX += treeSpacing
        }

        // 5. Solid ground base lines reflecting region aesthetic
        val groundBaseColor = when (level) {
            4 -> Color(0xFF1E3A5F) // Oceanic dark navy coast
            6 -> Color(0xFF130E20) // Deep underground cave floor
            7 -> Color(0xFF064E3B) // Dark moss forest
            9 -> Color(0xFF5C3A21) // Ancient sandstone rock floor
            else -> SvanObsidian
        }
        scope.drawRect(
            color = groundBaseColor,
            topLeft = Offset(0f, height - groundHeight),
            size = Size(width, groundHeight)
        )

        // Ground separation ribbon changing dynamically according to region energy
        val ribbonColor = when (level) {
            1 -> GeorgianGold
            2 -> Color(0xFFD946EF)
            3 -> GeorgianCrimson
            4 -> Color(0xFF00E5FF)
            5 -> Color(0xFF10B981)
            6 -> Color(0xFFC084FC)
            7 -> Color(0xFF00C853)
            8 -> Color(0xFFEF4444)
            9 -> Color(0xFFF59E0B)
            10 -> Color(0xFFFFD700)
            else -> GeorgianGold
        }
        scope.drawRect(
            color = ribbonColor,
            topLeft = Offset(0f, height - groundHeight),
            size = Size(width, 4f)
        )

        // 5.2 Foreground Organic Particle Field & Foliage Overlay (Placed over ground base for a cool animated depth effect)
        if (level == 2) {
            // Kakheti: Falling purple and royal wine grape leaves
            val leafRand = java.util.Random(3333L)
            for (lf in 0..14) {
                val seedX = leafRand.nextFloat() * width
                val seedY = leafRand.nextFloat() * (height - groundHeight - 100f)
                val leafX = (seedX - offsetNear * 0.8f) % (width + 60f)
                val leafY = (seedY + offsetNear * 0.4f + sin(offsetNear * 0.05f + lf).toFloat() * 15f) % (height - groundHeight)
                if (leafX > 0f) {
                    val leafCol = if (lf % 2 == 0) Color(0xFFD946EF) else Color(0xFF8B5CF6)
                    scope.drawCircle(
                        color = leafCol.copy(alpha = 0.45f + sin(offsetNear * 0.08f + lf).toFloat() * 0.2f),
                        radius = 4f + leafRand.nextFloat() * 5f,
                        center = Offset(leafX, leafY)
                    )
                }
            }
        } else if (level == 10) {
            // Racha: Swirling amber and scarlet maple leaves drifting in circles
            val leafRand = java.util.Random(9999L)
            for (lf in 0..18) {
                val seedX = leafRand.nextFloat() * width
                val seedY = leafRand.nextFloat() * (height - groundHeight - 60f)
                val leafX = (seedX - offsetNear * 1.5f) % (width + 60f)
                val leafY = (seedY + offsetNear * 0.5f + cos(offsetNear * 0.04f + lf * 0.5f).toFloat() * 25f) % (height - groundHeight - 30f)
                if (leafX > 0f) {
                    val leafCol = when (lf % 3) {
                        0 -> Color(0xFFF59E0B) // amber
                        1 -> Color(0xFFEF4444) // red
                        else -> Color(0xFFEA580C) // deep orange
                    }
                    scope.drawCircle(
                        color = leafCol.copy(alpha = 0.65f),
                        radius = 3f + leafRand.nextFloat() * 6f,
                        center = Offset(leafX, leafY)
                    )
                }
            }
        } else if (level == 7) {
            // Samegrelo: Beautiful glowing lime-green fireflies hovering and buzzing
            val fireflyRand = java.util.Random(777L)
            for (fi in 0..15) {
                val seedX = fireflyRand.nextFloat() * width
                val seedY = (height - groundHeight - 160f) + fireflyRand.nextFloat() * 150f
                val fx = (seedX - offsetNear * 0.4f + sin(offsetNear * 0.06f + fi).toFloat() * 35f) % width
                val fy = seedY + cos(offsetNear * 0.04f + fi).toFloat() * 25f
                val fAlpha = 0.4f + 0.6f * sin(offsetNear * 0.12f + fi).toFloat()
                scope.drawCircle(
                    color = Color(0xFF4ADE80).copy(alpha = maxOf(0f, fAlpha)),
                    radius = 3.5f + fireflyRand.nextFloat() * 3.5f,
                    center = Offset(fx, fy)
                )
            }
        } else if (level == 6) {
            // Imereti Caves: Pulsing magical cavern floating light orbs (cyan & purple)
            val orbRand = java.util.Random(666L)
            for (oi in 0..12) {
                val seedX = orbRand.nextFloat() * width
                val speedY = 0.8f + orbRand.nextFloat() * 1.5f
                val ox = (seedX - offsetNear * 0.2f) % width
                val baseOy = height - groundHeight - 20f
                val oy = (baseOy - (offsetNear * speedY + oi * 70f) % (height - groundHeight - 40f))
                val oAlpha = sin((oy / (height - groundHeight)) * 3.14159f).toFloat() * 0.4f
                if (oy > 60f) {
                    val orbCol = if (oi % 2 == 0) Color(0xFFA855F7) else Color(0xFF06B6D4)
                    scope.drawCircle(
                        color = orbCol.copy(alpha = maxOf(0f, oAlpha)),
                        radius = 5f + orbRand.nextFloat() * 10f,
                        center = Offset(ox, oy)
                    )
                }
            }
        } else if (level == 1) {
            // Svaneti: Gentle white falling alpine snowflakes
            val snowRand = java.util.Random(1111L)
            for (si in 0..12) {
                val seedX = snowRand.nextFloat() * width
                val seedY = snowRand.nextFloat() * (height - groundHeight)
                val sx = (seedX - offsetNear * 0.3f) % width
                val sy = (seedY + offsetNear * 0.5f) % (height - groundHeight)
                scope.drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 2.5f + snowRand.nextFloat() * 3f,
                    center = Offset(sx, sy)
                )
            }
        }
    }

    fun drawHero(
        scope: DrawScope,
        y: Float, // height off the ground
        isCrouching: Boolean,
        invincibleTicks: Boolean,
        ticks: Long,
        skinId: String = "default"
    ) {
        val groundHeight = 120f
        val width = scope.size.width
        val height = scope.size.height

        // Calculate absolute position on screen
        val heroX = 140f
        val heroBaseY = height - groundHeight - y

        // Handle invincibility flashing
        if (invincibleTicks && (ticks / 3) % 2 == 0L) {
            return
        }

        // 1. Draw Skin-specific special backgrounds/trails (Amirani Fire Trail or Golden Aura)
        if (skinId == "amirani_fire") {
            // Volcanic fire trial particles
            for (i in 0..4) {
                val trailX = heroX - 32f - i * 18f
                val trailY = heroBaseY - 20f - ((ticks * 2 + i * 5) % 30f)
                scope.drawCircle(
                    color = Color(0xFFFF4500).copy(alpha = maxOf(0f, 0.8f - i * 0.15f)),
                    radius = 20f - i * 3f,
                    center = Offset(trailX, trailY)
                )
                scope.drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = maxOf(0f, 0.6f - i * 0.12f)),
                    radius = 12f - i * 2f,
                    center = Offset(trailX - 5f, trailY)
                )
            }
        } else if (skinId == "golden") {
            // Golden celestial glow aura
            scope.drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.35f),
                radius = 65f,
                center = Offset(heroX, heroBaseY - 60f)
            )
            scope.drawCircle(
                color = Color(0xFFFFA500).copy(alpha = 0.15f),
                radius = 85f,
                center = Offset(heroX, heroBaseY - 60f)
            )
        } else if (skinId == "royal_tamar") {
            // Royal red/crimson flapping cape behind him
            val capePath = Path().apply {
                moveTo(heroX - 16f, heroBaseY - 75f)
                // Flapping offset
                val flapX = heroX - 42f - (ticks % 12)
                val flapY = heroBaseY - 45f + (ticks % 6)
                lineTo(flapX, flapY)
                lineTo(heroX - 12f, heroBaseY - 35f)
                close()
            }
            scope.drawPath(capePath, color = Color(0xFFC41E3A)) // Majestic Crimson
        }

        // Define adaptive visual skin tokens
        val chokhaColor = when (skinId) {
            "svan_scout" -> Color(0xFF2E6F40) // Rustic forest-green chokha
            "khevsur_warrior" -> Color(0xFF1565C0) // Deep Royal Blue chokha
            "golden" -> Color(0xFF9E7E1D) // Dark gold chokha
            "royal_tamar" -> Color(0xFF1E3F66) // Deep Royal Blue
            "amirani_fire" -> Color(0xFF262626) // Volcanic ash dark
            else -> Color.Black // Default Obsidian
        }

        val papakhaColor = when (skinId) {
            "svan_scout" -> Color(0xFF4A3C31) // Brown sheep wool
            "khevsur_warrior" -> Color(0xFF1C1D21) // Black sheep wool papakha
            "golden" -> Color(0xFFFFD700) // Golden warrior helm
            "royal_tamar" -> Color(0xFFE5E4E2) // Pure Platinum fluff
            "amirani_fire" -> Color(0xFFFF3D00) // Flaming Red hair/wool
            else -> CaucasusSnow // Default White fluffy papakha
        }

        val beltColor = when (skinId) {
            "svan_scout" -> Color(0xFF8D8D8D) // Leather silver belt
            "khevsur_warrior" -> Color(0xFFE6C229) // Golden cross belt
            "golden" -> Color(0xFFFFD700) // Gold belt
            "amirani_fire" -> Color(0xFFFF5722) // Lava orange belt
            else -> GeorgianGold // Default Yellow gold
        }

        val swordColor = when (skinId) {
            "svan_scout" -> Color(0xFFB0BEC5) // Scout simple steel
            "khevsur_warrior" -> Color(0xFFFFD54F) // Gilded gladius blade
            "golden" -> Color(0xFFFFD700) // Celestial Golden Blade
            "royal_tamar" -> Color(0xFF00E5FF) // Light Blue magical blade
            "amirani_fire" -> Color(0xFFFF1744) // Neon Lava red blade
            else -> SvanTowerGrey // Traditional steel grey
        }

        if (isCrouching) {
            // Draw crouching hero with bronze round shield
            // Head (crouched)
            scope.drawCircle(
                color = if (skinId == "amirani_fire") Color(0xFFE65100) else SvanTowerGrey, // Skin shadow / Magma skin
                radius = 16f,
                center = Offset(heroX, heroBaseY - 35f)
            )
            // Papakha wool hat
            scope.drawCircle(
                color = papakhaColor,
                radius = 14f,
                center = Offset(heroX, heroBaseY - 45f)
            )
            // Crouched Chokha body
            scope.drawRect(
                color = chokhaColor,
                topLeft = Offset(heroX - 25f, heroBaseY - 26f),
                size = Size(50f, 26f)
            )

            // Shield covering side (Protected from arrows!)
            scope.drawCircle(
                color = if (skinId == "royal_tamar") Color(0xFFD4AF37) else beltColor,
                radius = 28f,
                center = Offset(heroX, heroBaseY - 14f)
            )
            // Shield Sun ray engravings
            scope.drawCircle(
                color = if (skinId == "amirani_fire") Color(0xFF3E2723) else GeorgianCrimson,
                radius = 20f,
                center = Offset(heroX, heroBaseY - 14f)
            )
            scope.drawCircle(
                color = swordColor,
                radius = 8f,
                center = Offset(heroX, heroBaseY - 14f)
            )
            // Metal rivets or shield spikes
            for (i in 0..7) {
                val angle = i * Math.PI / 4.0
                val rx = heroX + cos(angle) * 16f
                val ry = heroBaseY - 14f + sin(angle) * 16f
                scope.drawCircle(
                    color = if (skinId == "amirani_fire") Color(0xFFFF5722) else CaucasusSnow,
                    radius = 3f,
                    center = Offset(rx.toFloat(), ry.toFloat())
                )
            }
        } else {
            // Stand/Run Warrior in classic Chokha with white Gazyrs
            val runningLegCycle = (ticks / 4) % 4

            // Head & Papakha top
            scope.drawCircle(
                color = if (skinId == "amirani_fire") Color(0xFFFFCC80) else Color(0xFFF1D4B3), // peach skin / heat skin
                radius = 18f,
                center = Offset(heroX, heroBaseY - 100f)
            )

            // Papakha (Traditional wool hat or Helm)
            if (skinId == "golden") {
                // Helmet peak
                val helmPath = Path().apply {
                    moveTo(heroX - 18f, heroBaseY - 98f)
                    lineTo(heroX, heroBaseY - 130f)
                    lineTo(heroX + 18f, heroBaseY - 98f)
                    close()
                }
                scope.drawPath(helmPath, color = papakhaColor)
                // Red feather plume
                scope.drawCircle(color = GeorgianCrimson, radius = 9f, center = Offset(heroX, heroBaseY - 132f))
            } else {
                scope.drawRect(
                    color = papakhaColor,
                    topLeft = Offset(heroX - 18f, heroBaseY - 128f),
                    size = Size(36f, 30f)
                )
                // Flaunt details of wool curls
                scope.drawCircle(color = papakhaColor, radius = 8f, center = Offset(heroX - 18f, heroBaseY - 113f))
                scope.drawCircle(color = papakhaColor, radius = 8f, center = Offset(heroX + 18f, heroBaseY - 113f))
                scope.drawCircle(color = papakhaColor, radius = 10f, center = Offset(heroX, heroBaseY - 128f))
            }

            // Chokha Coat (Majestic Obsidian/Black, Gold, or Blue coat)
            scope.drawRect(
                color = chokhaColor,
                topLeft = Offset(heroX - 20f, heroBaseY - 82f),
                size = Size(40f, 60f)
            )

            // Chokha Belt
            scope.drawRect(
                color = beltColor,
                topLeft = Offset(heroX - 21f, heroBaseY - 50f),
                size = Size(42f, 8f)
            )

            // Gazyrs (Wooden/platinum ammunition vials or Khevsur crosses on breast coat)
            if (skinId == "khevsur_warrior") {
                // Draw Khevsur sacred dynamic crosses on breast coat (Talavari style)
                // Left Cross
                scope.drawLine(color = GeorgianGold, start = Offset(heroX - 14f, heroBaseY - 74f), end = Offset(heroX - 4f, heroBaseY - 74f), strokeWidth = 3f)
                scope.drawLine(color = GeorgianGold, start = Offset(heroX - 9f, heroBaseY - 79f), end = Offset(heroX - 9f, heroBaseY - 69f), strokeWidth = 3f)
                // Right Cross
                scope.drawLine(color = GeorgianGold, start = Offset(heroX + 4f, heroBaseY - 74f), end = Offset(heroX + 14f, heroBaseY - 74f), strokeWidth = 3f)
                scope.drawLine(color = GeorgianGold, start = Offset(heroX + 9f, heroBaseY - 79f), end = Offset(heroX + 9f, heroBaseY - 69f), strokeWidth = 3f)
            } else {
                scope.drawRect(
                    color = if (skinId == "amirani_fire") Color(0xFFFF5722) else CaucasusSnow,
                    topLeft = Offset(heroX - 16f, heroBaseY - 76f),
                    size = Size(12f, 10f)
                )
                scope.drawRect(
                    color = if (skinId == "amirani_fire") Color(0xFFFF5722) else CaucasusSnow,
                    topLeft = Offset(heroX + 4f, heroBaseY - 76f),
                    size = Size(12f, 10f)
                )
                // Separation slot marks
                for (offset in listOf(-12f, -8f, -4f, 8f, 12f, 16f)) {
                    scope.drawLine(
                        color = Color.DarkGray,
                        start = Offset(heroX + offset, heroBaseY - 76f),
                        end = Offset(heroX + offset, heroBaseY - 66f),
                        strokeWidth = 1.5f
                    )
                }
            }

            // Traditional Dagger (Kindjal) on Belt
            val kindjalPath = Path().apply {
                moveTo(heroX - 5f, heroBaseY - 42f)
                lineTo(heroX + 10f, heroBaseY - 26f)
                lineTo(heroX + 15f, heroBaseY - 28f)
                lineTo(heroX - 2f, heroBaseY - 45f)
                close()
            }
            scope.drawPath(kindjalPath, color = if (skinId == "amirani_fire") Color(0xFFFF7043) else SvanTowerGrey)
            scope.drawCircle(color = beltColor, radius = 3.5f, center = Offset(heroX - 4f, heroBaseY - 44f)) // pommel

            // Hands holding majestic steel Broadsword while running
            scope.drawLine(
                color = if (skinId == "amirani_fire") Color(0xFFFFCC80) else Color(0xFFF1D4B3),
                start = Offset(heroX + 10f, heroBaseY - 55f),
                end = Offset(heroX + 32f, heroBaseY - 47f),
                strokeWidth = 8f
            )
            // Sword blade pointing forward
            scope.drawLine(
                color = swordColor,
                start = Offset(heroX + 30f, heroBaseY - 47f),
                end = Offset(heroX + 65f, heroBaseY - 42f),
                strokeWidth = 5f
            )
            // Guard
            scope.drawLine(
                color = beltColor,
                start = Offset(heroX + 29f, heroBaseY - 54f),
                end = Offset(heroX + 31f, heroBaseY - 40f),
                strokeWidth = 3f
            )

            // Running legs cycles
            val legColor = if (skinId == "amirani_fire") Color(0xFF3E2723) else Color.Black
            if (y > 0f) {
                // In Air JUMP legs: legs angled back and down
                scope.drawLine(legColor, Offset(heroX - 10f, heroBaseY - 22f), Offset(heroX - 25f, heroBaseY - 2f), strokeWidth = 8f)
                scope.drawLine(legColor, Offset(heroX + 10f, heroBaseY - 22f), Offset(heroX - 8f, heroBaseY + 5f), strokeWidth = 8f)
            } else {
                // Ground RUN leg cycles
                when (runningLegCycle) {
                    0L -> {
                        scope.drawLine(legColor, Offset(heroX - 10f, heroBaseY - 22f), Offset(heroX - 20f, heroBaseY), strokeWidth = 8f)
                        scope.drawLine(legColor, Offset(heroX + 10f, heroBaseY - 22f), Offset(heroX + 22f, heroBaseY), strokeWidth = 8f)
                    }
                    1L -> {
                        scope.drawLine(legColor, Offset(heroX - 10f, heroBaseY - 22f), Offset(heroX, heroBaseY), strokeWidth = 8f)
                        scope.drawLine(legColor, Offset(heroX + 10f, heroBaseY - 22f), Offset(heroX + 10f, heroBaseY - 5f), strokeWidth = 8f)
                    }
                    2L -> {
                        scope.drawLine(legColor, Offset(heroX - 10f, heroBaseY - 22f), Offset(heroX + 15f, heroBaseY), strokeWidth = 8f)
                        scope.drawLine(legColor, Offset(heroX + 10f, heroBaseY - 22f), Offset(heroX - 15f, heroBaseY), strokeWidth = 8f)
                    }
                    else -> {
                        scope.drawLine(legColor, Offset(heroX - 10f, heroBaseY - 22f), Offset(heroX - 10f, heroBaseY - 5f), strokeWidth = 8f)
                        scope.drawLine(legColor, Offset(heroX + 10f, heroBaseY - 22f), Offset(heroX, heroBaseY), strokeWidth = 8f)
                    }
                }
            }
        }
    }

    fun drawGameObject(scope: DrawScope, x: Float, y: Float, type: ItemType, rotation: Float) {
        val groundHeight = 120f
        val height = scope.size.height
        val absY = height - groundHeight - y

        // Draw dynamic, colorful, glowing pulse aura
        when (type) {
            ItemType.GRAPE -> {
                scope.drawCircle(
                    color = Color(0x338E24AA), // grape purple mist
                    radius = 36f,
                    center = Offset(x, absY)
                )
                scope.drawCircle(
                    color = Color(0x22E040FB),
                    radius = 24f,
                    center = Offset(x, absY)
                )
            }
            ItemType.COIN -> {
                scope.drawCircle(
                    color = Color(0x33FFB300), // ancient golden aura
                    radius = 32f,
                    center = Offset(x, absY)
                )
                scope.drawCircle(
                    color = Color(0x22FFD54F),
                    radius = 22f,
                    center = Offset(x, absY)
                )
            }
            ItemType.KVEVRI -> {
                scope.drawCircle(
                    color = Color(0x33FF5722), // terracotta clay warmth
                    radius = 45f,
                    center = Offset(x, absY)
                )
                scope.drawCircle(
                    color = Color(0x22FFAB91),
                    radius = 30f,
                    center = Offset(x, absY)
                )
            }
            ItemType.SCROLL -> {
                scope.drawCircle(
                    color = Color(0x3300B0FF), // cyan parchment light
                    radius = 40f,
                    center = Offset(x, absY)
                )
                scope.drawCircle(
                    color = Color(0x2280D8FF),
                    radius = 26f,
                    center = Offset(x, absY)
                )
            }
            else -> {}
        }

        scope.rotate(degrees = rotation, pivot = Offset(x, absY)) {
            when (type) {
                ItemType.GRAPE -> {
                    // Draw Georgia's grape cluster motif (Source of wine)
                    // Draw multiple purple grapes offsetted
                    scope.drawCircle(color = Color(0xFF5E35B1), radius = 8f, center = Offset(x - 8f, absY - 14f))
                    scope.drawCircle(color = Color(0xFF673AB7), radius = 8f, center = Offset(x + 8f, absY - 14f))
                    scope.drawCircle(color = Color(0xFF512DA8), radius = 8f, center = Offset(x, absY - 8f))
                    scope.drawCircle(color = Color(0xFF4527A0), radius = 8f, center = Offset(x - 6f, absY))
                    scope.drawCircle(color = Color(0xFF311B92), radius = 8f, center = Offset(x + 6f, absY))
                    scope.drawCircle(color = Color(0xFF5E35B1), radius = 8f, center = Offset(x, absY + 8f))

                    // Green Leaf
                    val leafPath = Path().apply {
                        moveTo(x, absY - 16f)
                        quadraticTo(x - 14f, absY - 26f, x - 18f, absY - 18f)
                        quadraticTo(x - 8f, absY - 12f, x, absY - 16f)
                    }
                    scope.drawPath(leafPath, color = VineGreen)
                    // Stem
                    scope.drawLine(
                        color = Color(0xFF5d4037),
                        start = Offset(x, absY - 14f),
                        end = Offset(x + 5f, absY - 24f),
                        strokeWidth = 3f
                    )
                }
                ItemType.KVEVRI -> {
                    // Draw a clay Kvevri vessel (ancient wine jug shape)
                    val scaleX = 1f
                    val scaleY = 1.3f
                    val rWidth = 46f
                    val rHeight = 52f

                    // Draw terracotta clay base jar
                    val kvevriPath = Path().apply {
                        // Oval top rim
                        moveTo(x - 15f, absY - 24f)
                        lineTo(x + 15f, absY - 24f)

                        // Outer curve to belly
                        quadraticTo(x + 23f, absY - 15f, x + 23f, absY)
                        // Down to pointed bottom
                        quadraticTo(x + 16f, absY + 18f, x, absY + 28f)
                        quadraticTo(x - 16f, absY + 18f, x - 23f, absY)
                        quadraticTo(x - 23f, absY - 15f, x - 15f, absY - 24f)
                        close()
                    }
                    scope.drawPath(kvevriPath, color = Color(0xFFC57A57)) // Terracotta red clay

                    // Shadow outline/depth
                    val kvevriInner = Path().apply {
                        moveTo(x - 12f, absY - 21f)
                        lineTo(x + 12f, absY - 21f)
                        quadraticTo(x + 18f, absY - 13f, x + 18f, absY)
                        quadraticTo(x + 12f, absY + 14f, x, absY + 23f)
                        quadraticTo(x - 12f, absY + 14f, x - 18f, absY)
                        quadraticTo(x - 18f, absY - 13f, x - 12f, absY - 21f)
                        close()
                    }
                    scope.drawPath(kvevriInner, color = Color(0xFFA25937)) // Darker shade

                    // Yellow wine decorations on belly or clay stripes
                    scope.drawLine(
                        color = GeorgianGold,
                        start = Offset(x - 18f, absY),
                        end = Offset(x + 18f, absY),
                        strokeWidth = 3f
                    )
                    // Kvevri cap (clay lid)
                    scope.drawRect(
                        color = Color(0xFF524A45),
                        topLeft = Offset(x - 10f, absY - 28f),
                        size = Size(20f, 5f)
                    )
                }
                ItemType.COIN -> {
                    // Golden ancient Colchian coin
                    scope.drawCircle(color = GeorgianGold, radius = 22f, center = Offset(x, absY))
                    // Rim
                    scope.drawCircle(color = Color(0xFFB18E1E), radius = 18f, center = Offset(x, absY))
                    // Star/Cross engravings center in Georgian style (The Borjgali)
                    scope.drawCircle(color = GeorgianGold, radius = 8f, center = Offset(x, absY))
                    // Borjgali sun waves
                    for (i in 0..6) {
                        val angle = (i * 2.0 * Math.PI) / 7.0
                        val sx = x + cos(angle) * 14f
                        val sy = absY + sin(angle) * 14f
                        scope.drawLine(
                            color = GeorgianGold,
                            start = Offset(x, absY),
                            end = Offset(sx.toFloat(), sy.toFloat()),
                            strokeWidth = 2.5f
                        )
                    }
                }
                ItemType.SCROLL -> {
                    // Royal parchment roll with crimson velvet tie
                    val sWidth = 44f
                    val sHeight = 22f
                    // Main parchment rolled body
                    scope.drawRoundRect(
                        color = Color(0xFFEDD49E), // warm parchment
                        topLeft = Offset(x - sWidth / 2f, absY - sHeight / 2f),
                        size = Size(sWidth, sHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    // Rolled handles on left and right
                    scope.drawCircle(color = GeorgianGold, radius = 8f, center = Offset(x - sWidth / 2f, absY))
                    scope.drawCircle(color = GeorgianGold, radius = 8f, center = Offset(x + sWidth / 2f, absY))
                    // Crimson red tie ribbon wrapped in middle
                    scope.drawRect(
                        color = GeorgianCrimson,
                        topLeft = Offset(x - 5f, absY - sHeight / 2f),
                        size = Size(10f, sHeight)
                    )
                }
                ItemType.STONE -> {
                    // Falling textured boulder (danger!)
                    scope.drawCircle(color = Color(0xFF5D5B61), radius = 26f, center = Offset(x, absY))
                    // Outline and crevices
                    scope.drawCircle(color = Color(0xFF3B393D), radius = 22f, center = Offset(x, absY))
                    // Cracks
                    scope.drawLine(Color.Black, Offset(x - 18f, absY - 10f), Offset(x + 5f, absY + 5f), strokeWidth = 2.5f)
                    scope.drawLine(Color.Black, Offset(x + 5f, absY + 5f), Offset(x + 15f, absY - 15f), strokeWidth = 2.5f)
                    scope.drawLine(Color.Black, Offset(x - 2f, absY + 2f), Offset(x - 5f, absY + 18f), strokeWidth = 2.5f)
                }
                ItemType.ARROW -> {
                    // Flying archer hazard arrows (flying high)
                    val aLen = 70f
                    // Wood shaft
                    scope.drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(x + aLen / 2f, absY),
                        end = Offset(x - aLen / 2f, absY),
                        strokeWidth = 3.5f
                    )
                    // Yellow Arrow tip (pointed steel)
                    val tipPath = Path().apply {
                        moveTo(x - aLen / 2f - 10f, absY)
                        lineTo(x - aLen / 2f, absY - 8f)
                        lineTo(x - aLen / 2f, absY + 8f)
                        close()
                    }
                    scope.drawPath(tipPath, color = GeorgianGold)
                    // Red feathers fletching at back
                    scope.drawLine(GeorgianCrimson, Offset(x + aLen / 2f, absY), Offset(x + aLen / 2f + 10f, absY - 10f), strokeWidth = 4f)
                    scope.drawLine(GeorgianCrimson, Offset(x + aLen / 2f, absY), Offset(x + aLen / 2f + 10f, absY + 10f), strokeWidth = 4f)
                }
            }
        }
    }
}
