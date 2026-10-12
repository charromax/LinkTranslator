package com.charr0max.linktranslator

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charr0max.linktranslator.ui.theme.LinkTranslatorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinkTranslatorTheme {
                val scope = rememberCoroutineScope()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(innerPadding, scope)
                }
            }
        }
    }

    @Composable
    private fun HomeScreen(
        innerPadding: PaddingValues,
        scope: CoroutineScope
    ) {
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Link Translator", style = MaterialTheme.typography.headlineMedium)
            Text("Share a Spotify link to this app from the share sheet to open the same song in the music app you use.")
            Text("To translate links when you tap them, go to Settings > Apps > Default apps > Opening links > Link Translator, then add open.spotify.com. You may need to turn off link handling in the Spotify app.")
            Button(onClick = {
                startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
            }) { Text("Open Apps settings") }
            OutlinedButton(onClick = { scope.launch { setPreferredPlatform(null) } }) {
                Text("Forget preferred music app")
            }
        }
    }
}
