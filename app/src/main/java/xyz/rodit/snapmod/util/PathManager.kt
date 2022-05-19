package xyz.rodit.snapmod.util

import android.net.Uri
import android.os.Build
import android.os.Environment
import xyz.rodit.xposed.client.ConfigurationClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object PathManager {
    const val DOWNLOAD_STORY = "story"
    const val DOWNLOAD_AUDIO_NOTE = "audio_note"
    const val DOWNLOAD_PROFILE = "profile"
    const val DOWNLOAD_SNAP = "snap"

    private const val DEFAULT_DATE_FORMAT = "dd-MM-yyyy_HH-mm-ss"

    private val PATTERN_PUBLIC_DIR = Pattern.compile("""\$(\w+)""")
    private val PATTERN_PARAMETER = Pattern.compile("%([A-Za-z]+)")
    private val publicDirs: MutableMap<String, String> = mutableMapOf(
        "Movies" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path,
        "Pictures" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
        "Alarms" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).path,
        "DCIM" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path,
        "Music" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path,
        "Ringtones" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).path
    )
    private val defaultPaths: MutableMap<String, String> = mutableMapOf(
        DOWNLOAD_STORY to "\$Movies/SnapMod/%u_story_%d",
        DOWNLOAD_AUDIO_NOTE to "\$Movies/SnapMod/%u_audio_%d",
        DOWNLOAD_PROFILE to "\$Movies/SnapMod/%u_profile_%d",
        DOWNLOAD_SNAP to "\$Movies/SnapMod/%u_snap_%d"
    )

    private fun appendDefaultParamsMap(config: ConfigurationClient, paramsMap: Map<String, String>): Map<String, String> {
        val appended: MutableMap<String, String> = HashMap(paramsMap)
        appended["t"] = System.currentTimeMillis().toString()
        val dateFormat =
            SimpleDateFormat(
                config.getString("download_date_format", DEFAULT_DATE_FORMAT),
                Locale.getDefault()
            )
        appended["d"] = dateFormat.format(Date())
        return appended
    }

    fun getPath(
        config: ConfigurationClient,
        downloadType: String,
        paramsMap: Map<String, String>,
        extension: String
    ): File {
        val params = appendDefaultParamsMap(config, paramsMap)
        var path = config.getString("download_path_$downloadType", defaultPaths[downloadType])

        var matcher = PATTERN_PUBLIC_DIR.matcher(path)
        if (matcher.find()) {
            publicDirs[matcher.group(1).orEmpty()]?.let {
                path = matcher.replaceFirst(it)
            }
        }

        matcher = PATTERN_PARAMETER.matcher(path)
        val buffer = StringBuffer()
        while (matcher.find()) {
            params[matcher.group(1).orEmpty()]?.let {
                matcher.appendReplacement(buffer, it)
            }
        }
        matcher.appendTail(buffer)

        return File(buffer.toString() + extension)
    }

    fun getUri(
        config: ConfigurationClient,
        downloadType: String,
        paramsMap: Map<String, String>,
        extension: String
    ): String {
        return Uri.fromFile(getPath(config, downloadType, paramsMap, extension)).toString()
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            publicDirs["Recordings"] =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS).path
            publicDirs["Screenshots"] =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).path
        }
    }
}