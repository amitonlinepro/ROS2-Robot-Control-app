package com.akm.robotcontrolapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ControlButtons(onMove: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onMove("forward") }) {
            Text("↑ Forward")
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Button(onClick = { onMove("left") }) {
                Text("← Left")
            }
            Button(onClick = { onMove("stop") }) {
                Text("■ Stop")
            }
            Button(onClick = { onMove("right") }) {
                Text("→ Right")
            }
        }

        Button(onClick = { onMove("backward") }) {
            Text("↓ Backward")
        }
    }
}
