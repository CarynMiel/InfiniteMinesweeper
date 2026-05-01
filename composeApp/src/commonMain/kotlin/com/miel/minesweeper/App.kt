package com.miel.minesweeper

import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.miel.minesweeper.core.Tile.Companion.key
import com.miel.minesweeper.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import kotlin.math.*
import androidx.compose.material3.Icon

import androidx.compose.foundation.text.input.*
import kotlin.math.pow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment

import minesweeper_v6.composeapp.generated.resources.Res
import minesweeper_v6.composeapp.generated.resources.back_icon
import minesweeper_v6.composeapp.generated.resources.randomize_icon
import minesweeper_v6.composeapp.generated.resources.settings_icon



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        // game variables
        var game = remember { Game(123456, 0.15) }
        var frame by remember {mutableStateOf(0f)}
        var games by remember {mutableStateOf(0f)}

        // visual variables
        val textMeasurer = rememberTextMeasurer()
        var tileSize by remember {mutableStateOf(45f)}
        var border by remember {mutableStateOf(5f)}
        var topLeft by remember {mutableStateOf(Offset(0f, 0f))}

        // bottom app bar variables
        var selectionMode by remember {mutableStateOf(0)}

        // New game vars
        val modes = mapOf("baby" to 0.1, "easy" to 0.15, "medium" to 0.2, "hard" to 0.25, "impossible" to 0.3)
        var selectedDifficulty by remember{ mutableStateOf(2) }
        var newDensity: Double by remember { mutableStateOf(modes[ modes.keys.toList()[selectedDifficulty] ]!!) }

        var newSeed by remember { mutableStateOf(Random.nextLong()) }

        // side menu variables
        val sideMenuState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val sidePadding = 16.dp
        val spacer = 24.dp

        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet (
                    drawerContentColor = AppConstants.barContent,
                    drawerContainerColor = AppConstants.barColor,
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Column(
                        modifier = Modifier
                            .padding(horizontal = sidePadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (sideMenuState.isOpen) {
                                        sideMenuState.close()
                                    } else {
                                        sideMenuState.open()
                                    }
                                } // scope.launch
                            }, // onClick
                            modifier = Modifier.padding(top = sidePadding),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.back_icon),
                                contentDescription = "Back Icon",
                                // tint = AppTheme.barContent
                            ) // Icon
                        } // IconButton
                        Spacer(Modifier.height(spacer))
                        Text(
                            text = "Minesweeper Game Options",
                            modifier = Modifier.padding(sidePadding),
                            style = MaterialTheme.typography.titleLarge,
                            // color = AppTheme.barContent,
                        ) // Text
                        HorizontalDivider()
                        key (games) {
                            Text(
                                text = "Current Game Setup",
                                modifier = Modifier.padding(sidePadding),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Seed: ${game.hidden.seed}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = sidePadding),
                            )
                        }

                        val difficulty = if(game.hidden.density <= 0.10) { "baby" }
                        else if(game.hidden.density <= 0.15) { "easy" }
                        else if(game.hidden.density <= 0.20) { "medium" }
                        else if(game.hidden.density <= 0.25) { "hard" }
                        else { "impossible" }

                        Text(
                            text = "Difficulty: $difficulty",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = sidePadding),
                        )


                        Spacer(Modifier.height(spacer))
                        HorizontalDivider()
                        Text(
                            text = "New Game Options",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(sidePadding),
                        )

                        Text(
                            text = "Seed Options",
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                            modifier = Modifier.padding(horizontal = sidePadding),
                        )

                        var customSeed by remember{ mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = customSeed,
                                onCheckedChange = { customSeed = it },
                                modifier = Modifier.padding(horizontal = sidePadding),
                            )
                            if (customSeed) {
                                Text(
                                    text = "Randomized Seed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = sidePadding),
                                )
                            } else {
                                Text(
                                    text = "Custom Seed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = sidePadding),
                                )
                            }
                        }

                        if(customSeed) {
                            val textSeed = rememberTextFieldState("seed")
                            TextField(
                                state = textSeed,
                                placeholder = { Text("Seed") },
                                lineLimits = TextFieldLineLimits.SingleLine,
                                inputTransformation = InputTransformation.maxLength(20),
                                modifier = Modifier.padding(horizontal = sidePadding),
                            )
                            fun textToSeed(text: String): Long {
                                var total: Long = 0

                                if(text.toIntOrNull() != null) {return text.toLong()}

                                for(index in 0..<text.length) {
                                    val multiplier = 128.toDouble().pow(text.length - 1 - index)
                                    total += text.toCharArray()[index].code * multiplier.toLong()
                                }
                                return total
                            }
                            newSeed = textToSeed(textSeed.text.toString())
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if(!customSeed) {
                                IconButton(
                                    onClick = { newSeed = Random.nextLong() },
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.randomize_icon),
                                        contentDescription = "Randomize Icon",
                                    ) // Icon
                                } // IconButton
                            }
                            Text(
                                text = "Seed: $newSeed",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = sidePadding),
                            )
                        } // Row


                        Spacer(Modifier.height(spacer))

                        Text(
                            text = "Density Options",
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                            modifier = Modifier.padding(horizontal = sidePadding),
                        )

                        var customDensity by remember{ mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Switch(
                                checked = customDensity,
                                onCheckedChange = { customDensity = it },
                                modifier = Modifier.padding(horizontal = sidePadding),
                            )
                            if (!customDensity) {
                                Text(
                                    text = "Difficulty/Density Modes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = sidePadding),
                                )
                            } else {
                                Text(
                                    text = "Custom Density/Density (Not recommended)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = sidePadding),
                                )
                            }
                        }
                        if(customDensity){
                            var sliderDensity by remember{ mutableStateOf(0f) }
                            Slider(
                                value = sliderDensity,
                                onValueChange = { sliderDensity = it },
                                valueRange = 0f..100f,
                                modifier = Modifier
                                    .fillMaxWidth(0.66f)
                                    .padding(horizontal = sidePadding),
                            )
                            newDensity = sliderDensity/100.toDouble()
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                Text(
                                    text = "Modes: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(sidePadding),
                                )


                                SingleChoiceSegmentedButtonRow {
                                    modes.keys.forEachIndexed { index, key ->
                                        val textColor = if (selectedDifficulty == index) {
                                            AppConstants.barColor
                                        } else {
                                            AppConstants.barContent
                                        }
                                        SegmentedButton(
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = modes.size,
                                            ),
                                            onClick = { selectedDifficulty = index; newDensity = modes[key]!! },
                                            selected = selectedDifficulty == index,
                                            label = { Text(
                                                text = key,
                                                color = textColor
                                            ) }
                                        ) // SegmentedButton
                                    } // options
                                } // SingleChoiceSegmentedButtonRow
                            } // Row
                        }


                        Text(
                            text = "Density: $newDensity",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = sidePadding),
                        )

                        Spacer(Modifier.height(spacer))

                        Row(
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(
                                onClick = {
                                    game = Game(newSeed, newDensity)
                                    newSeed = Random.nextLong()
                                    frame++
                                    games++
                                }, // onClick
                                content = { Text("New Game") },
                                modifier = Modifier.padding(sidePadding),
                            ) // Button
                        }// Row


                    } // Column
                } // ModalDrawerSheet
            }, // drawerContent
            drawerState = sideMenuState,
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = topAppBarColors(
                            containerColor = AppConstants.barColor,
                            titleContentColor = AppConstants.barContent,
                        ), // colors
                        title = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                key(frame) {
                                    Text("    Flags: ${game.flags}    ")
                                    Text("    Score: ${game.score}    ")
                                }
                            } // Row
                        }, // title
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if(sideMenuState.isOpen) {
                                        sideMenuState.close()
                                    } else {
                                        sideMenuState.open()
                                    }
                                } // scope.launch
                            }) {
                                // Icon
                                Icon(
                                    painter = painterResource(Res.drawable.settings_icon),
                                    contentDescription = "Settings Icon",
                                    tint  = AppConstants.barContent,
                                ) // Icon
                            } // IconButton
                        } // navigationIcon
                    ) // TopAppBar
                }, // topBar
                bottomBar = {
                    BottomAppBar(
                        containerColor = AppConstants.barColor,
                        contentColor = AppConstants.barContent,
                    ) {
                        val options = listOf("Digging", "Flagging")

                        SingleChoiceSegmentedButtonRow {
                            options.forEachIndexed { index, label ->
                                val textColor = if (selectionMode == index) {
                                    AppConstants.barColor
                                } else {
                                    AppConstants.barContent
                                }
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = options.size,
                                    ),
                                    onClick = { selectionMode = index },
                                    selected = selectionMode == index,
                                    label = { Text(
                                        text = label,
                                        color = textColor
                                    ) }
                                ) // SegmentedButton
                            } // options
                        } // SingleChoiceSegmentedButtonRow
                    }// BottomAppBar
                } // bottomBar
            ) { innerPadding ->
                // the actual game board
                key(frame) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(AppConstants.background)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    topLeft += pan
                                    tileSize *= zoom
                                    border *= zoom
                                }
                            }
                            .pointerInput(Unit) {
                                fun flag(tapSpot: Offset) {
                                    val xTap = ((tapSpot.x - topLeft.x) / (tileSize + border)).toInt()
                                    val yTap = ((tapSpot.y - topLeft.y) / (tileSize + border)).toInt()
                                    game.flag(xTap, yTap)
                                    frame++
                                }

                                fun open(tapSpot: Offset) {
                                    val xTap = floor((tapSpot.x - topLeft.x) / (tileSize + border)).toInt()
                                    val yTap = floor((tapSpot.y - topLeft.y) / (tileSize + border)).toInt()
                                    game.open(xTap, yTap)
                                    frame++
                                }
                                detectTapGestures(
                                    onTap = { tapSpot ->
                                        if (selectionMode == 0) {
                                            open(tapSpot)
                                        } else {
                                            flag(tapSpot)
                                        }
                                    },
                                    onLongPress = { tapSpot ->
                                        if (selectionMode == 0) {
                                            flag(tapSpot)
                                        } else {
                                            open(tapSpot)
                                        }
                                    }
                                )
                            },
                    ) {
                        val bufferTiles = 3

                        // screen constraints
                        val width = size.width
                        val height = size.height

                        // number of tiles that fit on the screen
                        val tileWidth = (width / (tileSize + border)).toInt()
                        val tileHeight = (height / (tileSize + border)).toInt()


                        for (x in 0 - bufferTiles..tileWidth + bufferTiles) {
                            for (y in 0 - bufferTiles..tileHeight + bufferTiles) {
                                // screen location
                                val location = Offset(
                                    x.toFloat() * (tileSize + border) + (topLeft.x) % (tileSize + border),
                                    y.toFloat() * (tileSize + border) + (topLeft.y) % (tileSize + border)
                                )

                                // board location
                                val tileKey = key(
                                    x - (topLeft.x / (tileSize + border)).toInt(),
                                    y - (topLeft.y / (tileSize + border)).toInt()
                                )
                                val value = (game.shown[tileKey] ?: Tile.UNKNOWN)
                                var tileColor = AppConstants.unknownTile
                                var textStyle = TextStyle(
                                    color = tileColor
                                )

                                fun makeTile(tileColor: Color, textStyle: TextStyle) {
                                    drawRoundRect(
                                        size = Size(tileSize, tileSize),
                                        color = tileColor,
                                        topLeft = location,
                                    )// drawRect
                                    drawText(
                                        text = value.toString(),
                                        textMeasurer = textMeasurer,
                                        size = Size(width = tileSize, height = tileSize),
                                        topLeft = location,
                                        style = textStyle,
                                    ) // drawText
                                }


                                when (value) {
                                    Tile.UNKNOWN -> {
                                        tileColor = AppConstants.unknownTile
                                        textStyle = TextStyle(
                                            color = tileColor
                                        )
                                        makeTile(tileColor, textStyle)
                                    } // tile value is unknown

                                    Tile.FLAG -> {
                                        tileColor = AppConstants.tileColor
                                        textStyle = TextStyle(
                                            color = tileColor
                                        )
                                        makeTile(tileColor, textStyle)

                                        val drawFlag = Path()
                                        drawFlag.moveTo(location.x + (tileSize / 6), location.y + (tileSize * 5 / 6))
                                        drawFlag.lineTo(location.x + (tileSize / 6), location.y + (tileSize / 6))
                                        drawFlag.lineTo(
                                            location.x + (tileSize * 5 / 6),
                                            location.y + (tileSize * 4 / 9)
                                        )
                                        drawFlag.lineTo(location.x + (tileSize / 6), location.y + (tileSize * 6 / 9))
                                        drawFlag.close()

                                        drawPath(
                                            color = AppConstants.textFlag,
                                            path = drawFlag,
                                            style = Stroke(width = 10f)
                                        )
                                    } // tile value is flag

                                    Tile.BOMB -> {
                                        tileColor = AppConstants.bomb
                                        textStyle = TextStyle(
                                            color = tileColor
                                        )
                                        makeTile(tileColor, textStyle)

                                        val drawBomb = Path()
                                        drawBomb.moveTo(location.x + (tileSize / 6), location.y + (tileSize / 6))
                                        drawBomb.lineTo(
                                            location.x + (tileSize * 5 / 6),
                                            location.y + (tileSize * 5 / 6)
                                        )
                                        drawBomb.moveTo(location.x + (tileSize / 6), location.y + (tileSize * 5 / 6))
                                        drawBomb.lineTo(location.x + (tileSize * 5 / 6), location.y + (tileSize / 6))
                                        drawBomb.close()

                                        drawPath(
                                            path = drawBomb,
                                            color = AppConstants.textBomb,
                                            style = Stroke(width = 10f)
                                        )
                                    } // tile value is bomb

                                    else -> {
                                        tileColor = AppConstants.tileColor
                                        val textColor = when (value) {
                                            Tile.BLANK -> tileColor
                                            '1' -> AppConstants.textOne
                                            '2' -> AppConstants.textTwo
                                            '3' -> AppConstants.textThree
                                            '4' -> AppConstants.textFour
                                            '5' -> AppConstants.textFive
                                            '6' -> AppConstants.textSix
                                            '7' -> AppConstants.textSeven
                                            else -> AppConstants.textEight
                                        } // when value is a number

                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontSize = 22.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                        makeTile(tileColor, textStyle)
                                    } // tile value is number
                                } // when tile value
                            } // for y
                        } // for x
                    } // Canvas
                } // Key
            } // Scaffold
        } // ModalNavigationDrawer
    } // MaterialTheme
} // App