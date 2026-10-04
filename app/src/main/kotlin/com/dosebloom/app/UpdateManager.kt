package com.dosebloom.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateCheckResult {
    data class UpdateAvailable(val version: String, val notes: String, val downloadUrl: String) : UpdateCheckResult()
    data object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val version: String) : DownloadState()
    data class ReadyToInstall(val version: String, val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object UpdateManager {
    private const val GITHUB_API_URL = "https://api.github.com/repos/poorangryman/dosebloom-public/releases/latest"

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "DoseBloom-App")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (connection.responseCode != 200) {
                return@withContext UpdateCheckResult.Error("HTTP ${connection.responseCode}")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val tagName = json.optString("tag_name", "").removePrefix("v").trim()
            val body = json.optString("body", "").trim()

            var downloadUrl = ""
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (tagName.isNotEmpty() && isNewerVersion(tagName, currentVersion)) {
                if (downloadUrl.isEmpty()) {
                    downloadUrl = "https://github.com/poorangryman/dosebloom-public/releases/tag/v$tagName"
                }
                UpdateCheckResult.UpdateAvailable(tagName, body, downloadUrl)
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    internal fun isNewerVersion(remote: String, current: String): Boolean {
        try {
            val rParts = remote.split(".").mapNotNull { it.toIntOrNull() }
            val cParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(rParts.size, cParts.size)
            for (i in 0 until maxLen) {
                val r = if (i < rParts.size) rParts[i] else 0
                val c = if (i < cParts.size) cParts[i] else 0
                if (r > c) return true
                if (r < c) return false
            }
        } catch (_: Exception) {}
        return false
    }

    fun getDownloadedFile(context: Context, version: String): File {
        val fileName = "DoseBloom-v$version.apk"
        return File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    }

    fun startDownloadAndInstall(context: Context, downloadUrl: String, version: String) {
        val appContext = context.applicationContext
        val destination = getDownloadedFile(appContext, version)

        if (destination.exists() && destination.length() > 500_000) {
            _downloadState.value = DownloadState.ReadyToInstall(version, destination)
            installApk(context, destination)
            return
        }

        if (destination.exists()) {
            destination.delete()
        }

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("DoseBloom v$version")
            setDescription("Downloading update...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(destination))
            setMimeType("application/vnd.android.package-archive")
        }

        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        _downloadState.value = DownloadState.Downloading(version)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(recvContext: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                if (id == downloadId) {
                    try {
                        appContext.unregisterReceiver(this)
                    } catch (_: Exception) {}

                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor? = downloadManager.query(query)
                    var success = false
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (statusIndex != -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            success = true
                        }
                        cursor.close()
                    }

                    if (success && destination.exists()) {
                        _downloadState.value = DownloadState.ReadyToInstall(version, destination)
                        installApk(context, destination)
                    } else {
                        _downloadState.value = DownloadState.Error("Download failed")
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            appContext.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
