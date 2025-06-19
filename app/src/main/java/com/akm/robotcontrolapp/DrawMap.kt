package com.akm.robotcontrolapp

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor

//TODO : Needs update to manage large maps
@Composable
fun DrawMap(map: MapData?, pose: Pose2D?) {
    Log.d("DrawMap", "DrawMap called with map: ${map?.width}x${map?.height}")
    if (map == null) {
        Log.e("DrawMap", "Map is null, skipping draw.")
        return
    }

    val width = map.width
    val height = map.height

    if (width <= 0 || height <= 0 || map.data.size != width * height) {
        Log.e("DrawMap", "Invalid map: width=$width height=$height size=${map.data.size}")
        return
    }
    Log.d("DrawMap", "Rendering map...")


    val pixels = IntArray(width * height) { i ->
        when (map.data[i]) {
            0 -> AndroidColor.WHITE
            100 -> AndroidColor.BLACK
            else -> AndroidColor.GRAY
        }
    }

    val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

    // Flip vertically
    val flippedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(flippedBitmap)
    val matrix = android.graphics.Matrix().apply {
        preScale(1f, -1f)
        postTranslate(0f, height.toFloat())
    }
    canvas.drawBitmap(bitmap, matrix, null)

    val imageBitmap = flippedBitmap.asImageBitmap()

    Log.d("DrawMap", "Bitmap: ${width}x$height  resolution=${map.resolution}")

    Box(
        modifier = Modifier
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )

            pose?.let { pose ->
                try {
                    val px = ((pose.x - map.originX) / map.resolution) * size.width / width
                    val py =
                        size.height - ((pose.y - map.originY) / map.resolution) * size.height / height

                    drawCircle(
                        color = Color.Red,
                        radius = 4.dp.toPx(),
                        center = Offset(px.toFloat(), py.toFloat())
                    )

                    // Orientation arrow
                    val angle = pose.theta  // angle in radians
                    val arrowLength = 20f  // pixels; adjust as needed

                    // Compute dx, dy for the heading line
                    val dx = arrowLength * kotlin.math.cos(angle)
                    val dy = arrowLength * kotlin.math.sin(angle)

                    // Flip dy because canvas Y-axis is downward
                    drawLine(
                        color = Color.Red,
                        start = Offset(px, py),
                        end = Offset(px + dx, py - dy),
                        strokeWidth = 3.dp.toPx()
                    )

                } catch (e: Exception) {
                    Log.e("DrawMap", "Pose drawing error: ${e.message}")
                }
            }
        }
    }
}