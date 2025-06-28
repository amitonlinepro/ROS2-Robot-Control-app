package com.akm.robotcontrolapp

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class WebSocketManager(
    private val url: String,
    private val onStatusChange: (Boolean) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    var onMapUpdate: ((MapData) -> Unit)? = null
    var onPoseUpdate: ((Pose2D) -> Unit)? = null



    fun connect() {
        val client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i("WebSocket", "Connected to $url")
                onStatusChange(true)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocket", "Message: $text")

                try {
                    val json = JSONObject(text)
                    when (json.getString("type")) {
                        "map" -> {
                            val poseJson = json.optJSONObject("pose")
                            val pose = if (poseJson != null) {
                                Pose2D(
                                    x = poseJson.optDouble("x", 0.0).toFloat(),
                                    y = poseJson.optDouble("y", 0.0).toFloat(),
                                    theta = poseJson.optDouble("theta", 0.0).toFloat()
                                )
                            } else null
                            Log.d("WebSocket", "Pose: $pose")
                            val map = MapData(
                                width = json.getInt("width"),
                                height = json.getInt("height"),
                                resolution = json.getDouble("resolution").toFloat(),
                                originX = json.getJSONObject("origin").getDouble("x").toFloat(),
                                originY = json.getJSONObject("origin").getDouble("y").toFloat(),
                                data = json.getJSONArray("data").let { array ->
                                    List(array.length()) { i -> array.getInt(i) }
                                },
                                pose = null
                            )
                            Log.d("WebSocket", "Map data: ${map.width}x${map.height}, Pose: ${map.pose}")
                            onMapUpdate?.invoke(map)
                        }
                        "odom" -> {
                            val x = json.getDouble("x").toFloat()
                            val y = json.getDouble("y").toFloat()
                            val theta = json.optDouble("theta", 0.0).toFloat()

                            Log.d("WebSocket", "Received odom pose: x=$x, y=$y, theta=$theta")
                            onPoseUpdate?.invoke(Pose2D(x, y, theta))

                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("WebSocket", "Closed: $reason")
                onStatusChange(false)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Failed: ${t.localizedMessage}", t)
                onStatusChange(false)

                // Retry after 5 seconds using coroutine
                scope.launch {
                    delay(5000)
                    connect()
                }
            }

        })
    }


    fun sendCommand(direction: String) {
        val json = when (direction) {
            "forward" -> """{"type":"cmd_vel","linear_x":0.2,"angular_z":0.0}"""
            "backward" -> """{"type":"cmd_vel","linear_x":-0.2,"angular_z":0.0}"""
            "left" -> """{"type":"cmd_vel","linear_x":0.0,"angular_z":0.5}"""
            "right" -> """{"type":"cmd_vel","linear_x":0.0,"angular_z":-0.5}"""
            else -> """{"type":"cmd_vel","linear_x":0.0,"angular_z":0.0}"""
        }
        webSocket?.send(json)
    }

    fun close() {
        scope.cancel()  // Cancel any ongoing coroutines (like reconnect)

        webSocket?.close(1000, "Closed by app")
    }

    fun sendNavigationGoal(x: Double, y: Double, theta: Double = 0.0) {
        val message = JSONObject().apply {
            put("type", "navigate_to_pose")
            put("x", x)
            put("y", y)
            put("theta", theta)
        }
        webSocket?.send(message.toString())
        Log.d("WebSocket", "Sent navigation goal: $message")
    }

}
