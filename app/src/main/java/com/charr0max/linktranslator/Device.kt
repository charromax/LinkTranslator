package com.charr0max.linktranslator

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")
private val preferredKey = stringPreferencesKey("preferred_platform")

suspend fun Context.preferredPlatform(): Platform? =
    dataStore.data.map { it[preferredKey] }.first()?.let(Platform::fromKey)

suspend fun Context.setPreferredPlatform(platform: Platform?) {
    dataStore.edit { prefs ->
        if (platform == null) prefs.remove(preferredKey) else prefs[preferredKey] = platform.odesliKey
    }
}

fun Context.installedPlatforms(): Set<Platform> =
    Platform.entries.filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }.toSet()

fun Context.openUrl(url: String, platform: Platform? = null) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(platform?.let { Intent(intent).setPackage(it.packageName) } ?: intent)
    } catch (_: ActivityNotFoundException) {
        if (platform != null) openUrl(url)
    }
}
