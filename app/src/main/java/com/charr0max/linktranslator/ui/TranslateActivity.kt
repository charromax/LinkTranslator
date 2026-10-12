package com.charr0max.linktranslator.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charr0max.linktranslator.Option
import com.charr0max.linktranslator.Outcome
import com.charr0max.linktranslator.openUrl
import com.charr0max.linktranslator.ui.theme.LinkTranslatorTheme

class TranslateActivity : ComponentActivity() {
    private val viewModel: TranslateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.start(
            if (intent.action == Intent.ACTION_SEND) intent.getStringExtra(Intent.EXTRA_TEXT) else intent.dataString
        )
        setContent {
            LinkTranslatorTheme {
                val state by viewModel.state.collectAsState()
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (val s = state) {
                        UiState.Loading -> CircularProgressIndicator()
                        UiState.NoLink -> {
                            Text("No music link found in the shared text.")
                            TextButton(onClick = ::finish) { Text("Close") }
                        }
                        is UiState.Error -> {
                            Text("Couldn't translate this link. Check your connection.")
                            Button(onClick = viewModel::retry) { Text("Retry") }
                            TextButton(onClick = { openUrl(s.source.url); finish() }) { Text("Open original link") }
                        }
                        is UiState.Picker -> Picker(s.outcome.options) { option, always ->
                            if (always) viewModel.remember(option.platform)
                            openUrl(option.url, option.platform)
                            finish()
                        }
                        is UiState.Launch -> LaunchedEffect(s) {
                            when (val o = s.outcome) {
                                is Outcome.Open -> openUrl(o.option.url, o.option.platform)
                                is Outcome.Browser -> openUrl(o.url)
                                is Outcome.Choose -> Unit
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Picker(options: List<Option>, onPick: (Option, Boolean) -> Unit) {
    var always by remember { mutableStateOf(false) }
    Text("Open in", style = MaterialTheme.typography.titleMedium)
    options.forEach { option ->
        Text(
            option.platform.displayName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().clickable { onPick(option, always) }.padding(16.dp),
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = always, onCheckedChange = { always = it })
        Text("Always use the app I pick")
    }
}
