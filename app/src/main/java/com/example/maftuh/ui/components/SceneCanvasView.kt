package com.example.maftuh.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.maftuh.data.EvidenceItem
import com.example.maftuh.data.LocationId
import com.example.maftuh.data.Suspect

private const val SCENE_WIDTH = 1280f
private const val SCENE_HEIGHT = 800f

data class ObstacleRect(val x: Float, val y: Float, val w: Float, val h: Float)

@Composable
fun SceneCanvasView(
    location: LocationId,
    playerX: Float,
    playerY: Float,
    playerDir: String,
    flashlightOn: Boolean,
    evidenceList: List<EvidenceItem>,
    discoveredEvidenceIds: Set<String>,
    suspects: List<Suspect>,
    onMovePlayer: (x: Float, y: Float, dir: String) -> Unit,
    onTapEvidence: (EvidenceItem) -> Unit,
    onTapSuspect: (Suspect) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val obstacles = remember(location) {
        getObstaclesForLocation(location)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(location) {
                detectTapGestures { tapOffset ->
                    val scaleX = SCENE_WIDTH / size.width
                    val scaleY = SCENE_HEIGHT / size.height
                    val worldX = tapOffset.x * scaleX
                    val worldY = tapOffset.y * scaleY

                    // Check evidence clicks
                    val clickedEv = evidenceList.find { ev ->
                        Math.hypot((worldX - ev.posX).toDouble(), (worldY - ev.posY).toDouble()) < 50.0
                    }
                    if (clickedEv != null) {
                        onTapEvidence(clickedEv)
                        return@detectTapGestures
                    }

                    // Check suspect clicks
                    val clickedSus = suspects.find { sus ->
                        Math.hypot((worldX - sus.posX).toDouble(), (worldY - sus.posY).toDouble()) < 50.0
                    }
                    if (clickedSus != null) {
                        onTapSuspect(clickedSus)
                        return@detectTapGestures
                    }

                    // Walk towards tap position
                    val clamped = clampMovement(playerX, playerY, worldX, worldY, obstacles)
                    val dir = when {
                        Math.abs(clamped.first - playerX) > Math.abs(clamped.second - playerY) ->
                            if (clamped.first > playerX) "right" else "left"
                        else -> if (clamped.second > playerY) "down" else "up"
                    }
                    onMovePlayer(clamped.first, clamped.second, dir)
                }
            }
            .pointerInput(location, playerX, playerY) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val scaleX = SCENE_WIDTH / size.width
                    val scaleY = SCENE_HEIGHT / size.height
                    val targetX = playerX + dragAmount.x * scaleX * 1.5f
                    val targetY = playerY + dragAmount.y * scaleY * 1.5f

                    val clamped = clampMovement(playerX, playerY, targetX, targetY, obstacles)
                    val dir = when {
                        Math.abs(dragAmount.x) > Math.abs(dragAmount.y) ->
                            if (dragAmount.x > 0) "right" else "left"
                        else -> if (dragAmount.y > 0) "down" else "up"
                    }
                    onMovePlayer(clamped.first, clamped.second, dir)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / SCENE_WIDTH
            val scaleY = size.height / SCENE_HEIGHT

            // 1. Draw Illustrated Background Scene
            when (location) {
                LocationId.APARTMENT -> drawApartmentScene(scaleX, scaleY)
                LocationId.STREET -> drawStreetScene(scaleX, scaleY)
                LocationId.STATION -> drawStationScene(scaleX, scaleY)
            }

            // 2. Draw Interactive Clue Markers
            for (ev in evidenceList) {
                val isDiscovered = discoveredEvidenceIds.contains(ev.id)
                drawEvidenceMarker(
                    x = ev.posX * scaleX,
                    y = ev.posY * scaleY,
                    isDiscovered = isDiscovered,
                    pulse = pulseScale,
                    scale = scaleX
                )
            }

            // 3. Draw Suspects / Witnesses in Location
            for (sus in suspects) {
                drawSuspectActor(
                    x = sus.posX * scaleX,
                    y = sus.posY * scaleY,
                    name = sus.nameAr,
                    emoji = sus.avatarEmoji,
                    scale = scaleX
                )
            }

            // 4. Draw Detective Captain Youssef
            drawDetectiveActor(
                x = playerX * scaleX,
                y = playerY * scaleY,
                dir = playerDir,
                scale = scaleX
            )

            // 5. Flashlight Cone / Atmospheric Noir Vignette
            if (flashlightOn) {
                drawFlashlightCone(
                    playerX = playerX * scaleX,
                    playerY = playerY * scaleY,
                    dir = playerDir,
                    screenWidth = size.width,
                    screenHeight = size.height
                )
            } else {
                // Subtle noir cinematic vignette
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x66050810)),
                        center = Offset(playerX * scaleX, playerY * scaleY),
                        radius = size.width * 0.85f
                    ),
                    size = size
                )
            }
        }
    }
}

private fun clampMovement(
    currX: Float,
    currY: Float,
    targetX: Float,
    targetY: Float,
    obstacles: List<ObstacleRect>
): Pair<Float, Float> {
    val pr = 16f
    var nextX = targetX.coerceIn(50f, SCENE_WIDTH - 50f)
    var nextY = targetY.coerceIn(50f, SCENE_HEIGHT - 50f)

    for (obs in obstacles) {
        if (nextX + pr > obs.x && nextX - pr < obs.x + obs.w &&
            currY + pr > obs.y && currY - pr < obs.y + obs.h
        ) {
            nextX = currX
        }
        if (currX + pr > obs.x && currX - pr < obs.x + obs.w &&
            nextY + pr > obs.y && nextY - pr < obs.y + obs.h
        ) {
            nextY = currY
        }
    }

    return Pair(nextX, nextY)
}

private fun getObstaclesForLocation(loc: LocationId): List<ObstacleRect> = when (loc) {
    LocationId.APARTMENT -> listOf(
        ObstacleRect(160f, 20f, 1040f, 25f),
        ObstacleRect(160f, 720f, 1040f, 25f),
        ObstacleRect(160f, 20f, 25f, 700f),
        ObstacleRect(1180f, 20f, 25f, 700f),
        ObstacleRect(360f, 260f, 20f, 460f),
        ObstacleRect(160f, 250f, 210f, 20f),
        ObstacleRect(670f, 20f, 20f, 360f),
        ObstacleRect(430f, 370f, 250f, 20f),
        ObstacleRect(750f, 380f, 20f, 340f),
        ObstacleRect(680f, 400f, 500f, 20f),
        ObstacleRect(755f, 100f, 200f, 185f),
        ObstacleRect(445f, 30f, 215f, 75f),
        ObstacleRect(495f, 250f, 115f, 78f),
        ObstacleRect(410f, 475f, 70f, 120f),
        ObstacleRect(510f, 515f, 140f, 90f),
        ObstacleRect(835f, 500f, 220f, 125f)
    )
    LocationId.STATION -> listOf(
        ObstacleRect(40f, 40f, 1200f, 25f),
        ObstacleRect(40f, 740f, 1200f, 25f),
        ObstacleRect(40f, 40f, 25f, 720f),
        ObstacleRect(1220f, 40f, 25f, 720f),
        ObstacleRect(195f, 170f, 160f, 92f),
        ObstacleRect(655f, 465f, 170f, 190f)
    )
    LocationId.STREET -> listOf(
        ObstacleRect(0f, 0f, 1280f, 280f),
        ObstacleRect(0f, 760f, 1280f, 40f),
        ObstacleRect(500f, 400f, 150f, 125f),
        ObstacleRect(760f, 670f, 220f, 70f)
    )
}

// -------------------------------------------------------------
// Canvas Scene Drawing
// -------------------------------------------------------------

private fun DrawScope.drawApartmentScene(sx: Float, sy: Float) {
    // Background Dark Base
    drawRect(Color(0xFF090E16))

    // Exterior Cairo city night edges
    drawRect(Color(0xFF0A101B), Offset(0f, 0f), Size(150f * sx, 800f * sy))
    drawRect(Color(0xFF0A101B), Offset(1110f * sx, 0f), Size(170f * sx, 800f * sy))

    // Parquet Wood floor
    drawRect(Color(0xFF49301F), Offset(160f * sx, 20f * sy), Size(1040f * sx, 700f * sy))

    // Bathroom Tiled Floor
    drawRect(Color(0xFF29434A), Offset(160f * sx, 20f * sy), Size(270f * sx, 240f * sy))

    // Kitchen Tiled Floor
    drawRect(Color(0xFF303B4A), Offset(435f * sx, 20f * sy), Size(240f * sx, 350f * sy))

    // Master Bedroom Floor
    drawRect(Color(0xFF422818), Offset(680f * sx, 20f * sy), Size(520f * sx, 360f * sy))

    // Balcony
    drawRect(Color(0xFF4A3328), Offset(380f * sx, 720f * sy), Size(480f * sx, 70f * sy))

    // Persian Rug in Living Room
    drawRoundRect(
        color = Color(0xFF54252A),
        topLeft = Offset(405f * sx, 445f * sy),
        size = Size(310f * sx, 245f * sy),
        cornerRadius = CornerRadius(6f * sx, 6f * sy)
    )
    drawRoundRect(
        color = Color(0xFFD49B49),
        topLeft = Offset(415f * sx, 455f * sy),
        size = Size(290f * sx, 225f * sy),
        style = Stroke(width = 3f * sx),
        cornerRadius = CornerRadius(4f * sx, 4f * sy)
    )

    // Sofas in Living Room
    drawRoundRect(Color(0xFF806D58), Offset(410f * sx, 475f * sy), Size(70f * sx, 120f * sy), CornerRadius(8f * sx, 8f * sy))
    drawRoundRect(Color(0xFF806D58), Offset(520f * sx, 650f * sy), Size(130f * sx, 55f * sy), CornerRadius(8f * sx, 8f * sy))
    drawRoundRect(Color(0xFF806D58), Offset(680f * sx, 480f * sy), Size(70f * sx, 120f * sy), CornerRadius(8f * sx, 8f * sy))

    // Center Coffee Table (Where clues sit)
    drawRoundRect(Color(0xFF2B190F), Offset(515f * sx, 520f * sy), Size(130f * sx, 78f * sy), CornerRadius(4f * sx, 4f * sy))
    drawRoundRect(Color(0xFF8A552E), Offset(520f * sx, 525f * sy), Size(120f * sx, 68f * sy), CornerRadius(3f * sx, 3f * sy))

    // Kitchen Dining Table
    drawRoundRect(Color(0xFF6C4428), Offset(495f * sx, 250f * sy), Size(115f * sx, 78f * sy), CornerRadius(4f * sx, 4f * sy))
    // Kitchen Refrigerator and Counters
    drawRect(Color(0xFFB8C2C6), Offset(448f * sx, 118f * sy), Size(56f * sx, 90f * sy))
    drawRect(Color(0xFF6C4428), Offset(445f * sx, 32f * sy), Size(215f * sx, 65f * sy))

    // Bedroom Bed with Plaid Blanket
    drawRoundRect(Color(0xFF3A2418), Offset(760f * sx, 105f * sy), Size(190f * sx, 180f * sy), CornerRadius(8f * sx, 8f * sy))
    drawRoundRect(Color(0xFF253D60), Offset(770f * sx, 150f * sy), Size(170f * sx, 125f * sy), CornerRadius(6f * sx, 6f * sy))
    drawRoundRect(Color(0xFFEEE4D2), Offset(775f * sx, 115f * sy), Size(70f * sx, 30f * sy), CornerRadius(4f * sx, 4f * sy))
    drawRoundRect(Color(0xFFEEE4D2), Offset(860f * sx, 115f * sy), Size(70f * sx, 30f * sy), CornerRadius(4f * sx, 4f * sy))

    // Study Executive Desk
    drawRoundRect(Color(0xFF25170F), Offset(840f * sx, 505f * sy), Size(210f * sx, 115f * sy), CornerRadius(4f * sx, 4f * sy))
    drawRoundRect(Color(0xFF704326), Offset(848f * sx, 513f * sy), Size(194f * sx, 99f * sy), CornerRadius(3f * sx, 3f * sy))
    // Laptop on Desk
    drawRoundRect(Color(0xFF111820), Offset(880f * sx, 530f * sy), Size(75f * sx, 45f * sy), CornerRadius(3f * sx, 3f * sy))
    drawRect(Color(0xFF2C7892), Offset(886f * sx, 536f * sy), Size(63f * sx, 31f * sy))

    // Walls & Partitions (Strong Noir Outline)
    val wallColor = Color(0xFF1A222E)
    val wallBorder = Color(0xFF0F151F)
    val wallThickness = 12f * sx

    // Exterior Box
    drawRect(wallBorder, Offset(155f * sx, 15f * sy), Size(1050f * sx, 715f * sy), style = Stroke(wallThickness))
    drawRect(wallColor, Offset(155f * sx, 15f * sy), Size(1050f * sx, 715f * sy), style = Stroke(wallThickness * 0.7f))

    // Internal Dividers
    drawLine(wallColor, Offset(370f * sx, 260f * sy), Offset(370f * sx, 720f * sy), strokeWidth = wallThickness)
    drawLine(wallColor, Offset(160f * sx, 250f * sy), Offset(370f * sx, 250f * sy), strokeWidth = wallThickness)
    drawLine(wallColor, Offset(680f * sx, 20f * sy), Offset(680f * sx, 380f * sy), strokeWidth = wallThickness)
    drawLine(wallColor, Offset(430f * sx, 370f * sy), Offset(680f * sx, 370f * sy), strokeWidth = wallThickness)
    drawLine(wallColor, Offset(680f * sx, 400f * sy), Offset(1200f * sx, 400f * sy), strokeWidth = wallThickness)
    drawLine(wallColor, Offset(760f * sx, 400f * sy), Offset(760f * sx, 720f * sy), strokeWidth = wallThickness)
}

private fun DrawScope.drawStreetScene(sx: Float, sy: Float) {
    // Night sky & Cairo buildings
    drawRect(Color(0xFF080E18))
    drawRect(Color(0xFF1A2431), Offset(0f, 0f), Size(1280f * sx, 280f * sy))

    // Building windows with warm lights
    for (i in 0..7) {
        val wx = (30f + i * 160f) * sx
        drawRect(Color(0xFFDCA64F), Offset(wx, 40f * sy), Size(90f * sx, 45f * sy))
        drawRect(Color(0xFFDCA64F), Offset(wx, 120f * sy), Size(90f * sx, 45f * sy))
    }

    // Street Sidewalk
    drawRect(Color(0xFF62594D), Offset(0f, 280f * sy), Size(1280f * sx, 200f * sy))
    // Shop awnings
    drawRect(Color(0xFFB53D35), Offset(40f * sx, 282f * sy), Size(330f * sx, 30f * sy))
    drawRect(Color(0xFFE3A640), Offset(440f * sx, 280f * sy), Size(340f * sx, 30f * sy))
    drawRect(Color(0xFF287E75), Offset(855f * sx, 282f * sy), Size(380f * sx, 30f * sy))

    // Supermarket Kiosk (بقالة البركة)
    drawRoundRect(Color(0xFF3A2417), Offset(500f * sx, 380f * sy), Size(150f * sx, 125f * sy), CornerRadius(6f * sx, 6f * sy))
    drawRoundRect(Color(0xFF7A4B2B), Offset(510f * sx, 390f * sy), Size(130f * sx, 105f * sy), CornerRadius(4f * sx, 4f * sy))
    drawRect(Color(0xFFD2A14B), Offset(510f * sx, 390f * sy), Size(130f * sx, 25f * sy))
    // CCTV Camera mounted on kiosk
    drawRect(Color(0xFF202A35), Offset(640f * sx, 390f * sy), Size(24f * sx, 16f * sy))
    drawCircle(Color(0xFF5AB8D4), 4f * sx, Offset(646f * sx, 398f * sy))

    // Ornamental Planter Tree at building entrance
    drawCircle(Color(0xFF68482E), 12f * sx, Offset(420f * sx, 530f * sy))
    drawCircle(Color(0xFF184C37), 35f * sx, Offset(420f * sx, 510f * sy))

    // Asphalt Road with Lane Lines
    drawRect(Color(0xFF141C27), Offset(0f, 480f * sy), Size(1280f * sx, 320f * sy))
    drawLine(
        color = Color(0xFFD4C7A3),
        start = Offset(0f, 640f * sy),
        end = Offset(1280f * sx, 640f * sy),
        strokeWidth = 4f * sx
    )

    // Sherif's Parked Mitsubishi Lancer Sedan
    drawRoundRect(Color(0xFF121B2A), Offset(760f * sx, 670f * sy), Size(220f * sx, 70f * sy), CornerRadius(16f * sx, 16f * sy))
    drawRoundRect(Color(0xFF273E55), Offset(800f * sx, 655f * sy), Size(130f * sx, 45f * sy), CornerRadius(12f * sx, 12f * sy))
    drawCircle(Color(0xFF090D12), 16f * sx, Offset(800f * sx, 735f * sy))
    drawCircle(Color(0xFF090D12), 16f * sx, Offset(940f * sx, 735f * sy))
}

private fun DrawScope.drawStationScene(sx: Float, sy: Float) {
    // Police station tiled floor
    drawRect(Color(0xFF273342))
    drawRect(Color(0xFF2C3745), Offset(45f * sx, 45f * sy), Size(500f * sx, 310f * sy))
    drawRect(Color(0xFF242D39), Offset(570f * sx, 45f * sy), Size(650f * sx, 310f * sy))
    drawRect(Color(0xFF202C3B), Offset(45f * sx, 390f * sy), Size(525f * sx, 350f * sy))
    drawRect(Color(0xFF2A3038), Offset(590f * sx, 390f * sy), Size(630f * sx, 350f * sy))

    // Police Walls
    val wallColor = Color(0xFF566273)
    val wt = 12f * sx
    drawLine(wallColor, Offset(30f * sx, 28f * sy), Offset(1250f * sx, 28f * sy), wt)
    drawLine(wallColor, Offset(555f * sx, 28f * sy), Offset(555f * sx, 360f * sy), wt)
    drawLine(wallColor, Offset(30f * sx, 375f * sy), Offset(1250f * sx, 375f * sy), wt)
    drawLine(wallColor, Offset(850f * sx, 375f * sy), Offset(850f * sx, 772f * sy), wt)

    // Commander Desk & Rug
    drawRoundRect(Color(0xFF48252B), Offset(100f * sx, 95f * sy), Size(355f * sx, 220f * sy), CornerRadius(6f * sx, 6f * sy))
    drawRoundRect(Color(0xFF81542F), Offset(195f * sx, 170f * sy), Size(160f * sx, 92f * sy), CornerRadius(4f * sx, 4f * sy))

    // Egyptian Police Emblem
    drawRect(Color(0xFFC3A36C), Offset(75f * sx, 55f * sy), Size(10f * sx, 115f * sy))
    drawRect(Color(0xFFD53A3A), Offset(85f * sx, 58f * sy), Size(85f * sx, 24f * sy))
    drawRect(Color(0xFFF1E7CF), Offset(85f * sx, 82f * sy), Size(85f * sx, 24f * sy))
    drawRect(Color(0xFF1C704D), Offset(85f * sx, 106f * sy), Size(85f * sx, 24f * sy))

    // Case Board on Station Wall
    drawRoundRect(Color(0xFF633D25), Offset(650f * sx, 170f * sy), Size(510f * sx, 160f * sy), CornerRadius(6f * sx, 6f * sy))
    drawRoundRect(Color(0xFF875B3C), Offset(662f * sx, 182f * sy), Size(486f * sx, 136f * sy), CornerRadius(4f * sx, 4f * sy))

    // Forensic Autopsy Lab Desk (Dr. Salma's station)
    drawRoundRect(Color(0xFF1B110B), Offset(220f * sx, 190f * sy), Size(150f * sx, 85f * sy), CornerRadius(4f * sx, 4f * sy))
    drawRoundRect(Color(0xFF76502E), Offset(225f * sx, 195f * sy), Size(140f * sx, 75f * sy), CornerRadius(3f * sx, 3f * sy))

    // Interrogation Table
    drawRoundRect(Color(0xFF4C292B), Offset(655f * sx, 465f * sy), Size(170f * sx, 190f * sy), CornerRadius(4f * sx, 4f * sy))
}

// -------------------------------------------------------------
// Interactive Actors & Clues
// -------------------------------------------------------------

private fun DrawScope.drawEvidenceMarker(
    x: Float,
    y: Float,
    isDiscovered: Boolean,
    pulse: Float,
    scale: Float
) {
    val radius = 18f * scale

    // Pulsing outer glow
    drawCircle(
        color = if (isDiscovered) Color(0x3310B981) else Color(0x44F59E0B),
        radius = radius * pulse * 1.35f,
        center = Offset(x, y)
    )

    // Inner gold disk
    drawCircle(
        color = if (isDiscovered) Color(0xFF10B981) else Color(0xFFF59E0B),
        radius = radius,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0xFF121826),
        radius = radius * 0.75f,
        center = Offset(x, y)
    )

    // Center icon dot or checkmark
    drawCircle(
        color = if (isDiscovered) Color(0xFF34D399) else Color(0xFFFDE68A),
        radius = radius * 0.35f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawDetectiveActor(
    x: Float,
    y: Float,
    dir: String,
    scale: Float
) {
    val r = 18f * scale

    // Actor Drop Shadow
    drawOval(
        color = Color(0x77050810),
        topLeft = Offset(x - r * 1.1f, y + r * 0.6f),
        size = Size(r * 2.2f, r * 0.8f)
    )

    // Detective Trenchcoat Body
    drawCircle(
        color = Color(0xFF2A364F), // Dark navy detective trenchcoat
        radius = r,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0xFF1E2738),
        radius = r * 0.85f,
        center = Offset(x, y)
    )

    // Detective Fedor / Hat
    drawOval(
        color = Color(0xFF8B6B46), // Classic detective tan hat
        topLeft = Offset(x - r * 0.85f, y - r * 0.5f),
        size = Size(r * 1.7f, r * 1.1f)
    )
    drawOval(
        color = Color(0xFF5D4327),
        topLeft = Offset(x - r * 0.55f, y - r * 0.4f),
        size = Size(r * 1.1f, r * 0.8f)
    )

    // Detective Badge Gold Accent
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 3.5f * scale,
        center = Offset(x + r * 0.35f, y + r * 0.2f)
    )

    // Direction indicator
    val dirOffset = when (dir) {
        "up" -> Offset(x, y - r * 1.1f)
        "down" -> Offset(x, y + r * 1.1f)
        "left" -> Offset(x - r * 1.1f, y)
        else -> Offset(x + r * 1.1f, y)
    }
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = 3f * scale,
        center = dirOffset
    )
}

private fun DrawScope.drawSuspectActor(
    x: Float,
    y: Float,
    name: String,
    emoji: String,
    scale: Float
) {
    val r = 16f * scale

    // Drop Shadow
    drawOval(
        color = Color(0x66050810),
        topLeft = Offset(x - r, y + r * 0.6f),
        size = Size(r * 2f, r * 0.7f)
    )

    // Actor Avatar Circle
    drawCircle(
        color = Color(0xFFB45309),
        radius = r,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = r * 0.75f,
        center = Offset(x, y)
    )

    // Speech Bubble Icon Above
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(x - 16f * scale, y - 42f * scale),
        size = Size(32f * scale, 20f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 2.5f * scale,
        center = Offset(x - 6f * scale, y - 32f * scale)
    )
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 2.5f * scale,
        center = Offset(x, y - 32f * scale)
    )
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 2.5f * scale,
        center = Offset(x + 6f * scale, y - 32f * scale)
    )
}

private fun DrawScope.drawFlashlightCone(
    playerX: Float,
    playerY: Float,
    dir: String,
    screenWidth: Float,
    screenHeight: Float
) {
    // 1. Dark noir darkness overlay
    drawRect(Color(0xBB04070D))

    // 2. Spotlight beam cone extending in player's direction
    val coneLength = 320f
    val coneSpread = 160f

    val tip = Offset(playerX, playerY)
    val (baseCenter, p1, p2) = when (dir) {
        "up" -> {
            val bc = Offset(playerX, playerY - coneLength)
            Triple(bc, Offset(bc.x - coneSpread, bc.y), Offset(bc.x + coneSpread, bc.y))
        }
        "down" -> {
            val bc = Offset(playerX, playerY + coneLength)
            Triple(bc, Offset(bc.x - coneSpread, bc.y), Offset(bc.x + coneSpread, bc.y))
        }
        "left" -> {
            val bc = Offset(playerX - coneLength, playerY)
            Triple(bc, Offset(bc.x, bc.y - coneSpread), Offset(bc.x, bc.y + coneSpread))
        }
        else -> {
            val bc = Offset(playerX + coneLength, playerY)
            Triple(bc, Offset(bc.x, bc.y - coneSpread), Offset(bc.x, bc.y + coneSpread))
        }
    }

    val conePath = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }

    drawPath(
        path = conePath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0x99FFF3C4), Color(0x44F59E0B), Color.Transparent),
            center = tip,
            radius = coneLength
        ),
        style = Fill
    )

    // Spotlight around detective feet
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x88FFFBEB), Color.Transparent),
            center = tip,
            radius = 80f
        ),
        radius = 80f,
        center = tip
    )
}
