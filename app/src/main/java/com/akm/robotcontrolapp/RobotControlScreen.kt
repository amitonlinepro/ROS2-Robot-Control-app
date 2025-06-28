package com.akm.robotcontrolapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
    onViewMaps: () -> Unit,
    onMapTap: (Double, Double) -> Unit
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.vacuum_cleaner),
                            contentDescription = "Robot Icon",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Robot Controller")
                    }
                },
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
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isConnected.value) "🟢 Server Connected" else "🔴 Server Disconnected",
                        fontSize = 14.sp,
                    )
                    Text(
                        text = if (map != null)
                            "🧹 Robot Active"
                        else
                            "\uD83D\uDD52 Waiting for Robot",
                        fontSize = 14.sp
                    )
                }

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
                            DrawMap(map = map, pose = pose, onMapTap = onMapTap)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_map),
                        contentDescription = "Map Placeholder",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
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
