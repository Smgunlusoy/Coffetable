package com.example.coffetable.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.coffetable.MainActivity // Import MainActivity for the speak function

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeTableScreen(mainActivity: MainActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coffee Table") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Coffee Table!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    mainActivity.speak("Opening the external camera")
                    mainActivity.openExternalCamera() // Open the external camera
                }) {
                    Text("Open External Camera")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    mainActivity.speak("Opening the internal camera")
                    mainActivity.openInternalCamera() // Open the internal camera
                }) {
                    Text("Open Internal Camera")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    mainActivity.speak("Getting your coffee")
                    // Logic to get the coffee goes here
                }) {
                    Text("Get Coffee")
                }
            }
        }
    )
}