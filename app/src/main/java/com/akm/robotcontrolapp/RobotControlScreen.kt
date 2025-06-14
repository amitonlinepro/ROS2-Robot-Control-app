package com.akm.robotcontrolapp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RobotControlScreen(
    isConnected: State<Boolean>,
    map: MapData?,
    pose: Pose2D?,
    onCommand: (String) -> Unit,
    onSaveMap: (String, (String) -> Unit) -> Unit,
    onSettings: () -> Unit,
    onViewMaps: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mapName by remember { mutableStateOf("my_map") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("🤖 Robot Controller") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onViewMaps) {
                        Icon(Icons.Default.Map, contentDescription = "View Saved Maps")
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isConnected.value) "🟢 Connected" else "🔴 Disconnected",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                if (map != null && pose != null) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp)
                    ) {
                        val aspectRatio = map.width.toFloat() / map.height.toFloat()
                        val canvasWidth = maxWidth
                        val canvasHeight = canvasWidth / aspectRatio

                        Box(
                            modifier = Modifier
                                .width(canvasWidth)
                                .height(canvasHeight)
                        ) {
                            DrawMap(map = map, pose = pose)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }


                ControlButtons(onMove = onCommand)

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = mapName,
                    onValueChange = { mapName = it },
                    label = { Text("Map Name") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onSaveMap(mapName) { message ->
                            snackbarMessage = message
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("💾 Save Map")
                }
            }
        }
    }
}
