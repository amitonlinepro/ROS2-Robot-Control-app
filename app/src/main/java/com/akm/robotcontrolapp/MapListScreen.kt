package com.akm.robotcontrolapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapListScreen(
    serverIp: String,
    onBack: () -> Unit
) {
    var mapList by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        try {
            val maps: List<String> = withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("http://$serverIp:8000/saved_maps")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code ${response.code}")

                val body = response.body?.string()
                val json = JSONObject(body ?: "{}")
                val array = json.optJSONArray("maps")

                (0 until array.length()).map { i -> array.getString(i) }
            }

            mapList = maps

        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Maps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                errorMessage != null -> {
                    Text("❌ $errorMessage")
                }

                mapList.isEmpty() -> {
                    Text("No saved maps found.")
                }

                else -> {
                    LazyColumn {
                        items(mapList) { map ->
                            Text("📍 $map", modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }

        }
    }
}
