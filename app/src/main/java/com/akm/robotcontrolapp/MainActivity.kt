package com.akm.robotcontrolapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsDataStore = SettingsDataStore(applicationContext)

        var currentScreen by mutableStateOf("loading")
        var savedIp by mutableStateOf("192.168.1.5")
        val isConnected = mutableStateOf(false)
        val mapData = mutableStateOf<MapData?>(null)
        val robotPose = mutableStateOf<Pose2D?>(null)
        val isRobotOnline = mutableStateOf(false)


        lifecycleScope.launch {
            savedIp = settingsDataStore.getServerIp()
            currentScreen = "control"
        }

        setContent {
            when (currentScreen) {
                "control" -> {
                    LaunchedEffect(savedIp) {
                        webSocketManager = WebSocketManager("ws://$savedIp:8000/ws") { connected ->
                            isConnected.value = connected
                        }
                        webSocketManager.onMapUpdate = {
                            mapData.value = it
                            isRobotOnline.value = true
                        }
                        webSocketManager.onPoseUpdate = {
                            robotPose.value = it
                        }

                        webSocketManager.connect()
                    }

                    RobotControlScreen(
                        isConnected = isConnected,
                        map = mapData.value,
                        pose = robotPose.value,
                        onCommand = { direction -> webSocketManager.sendCommand(direction) },
                        onSaveMap = { name, onResult -> saveMapFromApp(name, savedIp, onResult) },
                        onSettings = { currentScreen = "settings" },
                        onViewMaps = { currentScreen = "maps" },
                        onMapTap = { x, y -> webSocketManager.sendNavigationGoal(x, y) }

                    )
                }

                "maps" -> {
                    BackHandler { currentScreen = "control" }
                    MapListScreen(
                        serverIp = savedIp,
                        onBack = { currentScreen = "control" }
                    )
                }


                "settings" -> {
                    BackHandler { currentScreen = "control" }

                    SettingsScreen(
                        initialIp = savedIp,
                        onIpSave = { ip ->
                            lifecycleScope.launch {
                                settingsDataStore.saveServerIp(ip)
                                savedIp = ip
                                currentScreen = "control"
                            }
                        },
                        onBack = { currentScreen = "control" }
                    )
                }
            }
        }
    }

    fun saveMapFromApp(filename: String, ip: String, onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val json = """{"filename": "$filename"}"""
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:8000/save_map")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapSave", "Failed: ${e.message}")
                onResult("❌ Failed to save map")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onResult("✅ Map '$filename' saved successfully!")
                } else {
                    onResult("❌ Error saving map (${response.code})")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webSocketManager.isInitialized) webSocketManager.close()
    }
}
