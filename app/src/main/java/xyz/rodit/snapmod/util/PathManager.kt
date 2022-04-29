package xyz.rodit.snapmod.util

import android.net.Uri
import android.os.Build
import android.os.Environment
import xyz.rodit.xposed.client.ConfigurationClient
import java.io.File
import java.util.regex.Pattern

object PathManager {
    const val DOWNLOAD_STORY = "story"
    const val DOWNLOAD_AUDIO_NOTE = "audio_note"
    const val DOWNLOAD_PROFILE = "profile"

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
        DOWNLOAD_STORY to "\$Movies/SnapMod/%u_story_%t",
        DOWNLOAD_AUDIO_NOTE to "\$Movies/SnapMod/%id_audio_%t",
        DOWNLOAD_PROFILE to "\$Movies/SnapMod/%u_profile_%t"
    )

    private fun appendDefaultParamsMap(paramsMap: Map<String, String>): Map<String, String> {
        val appended: MutableMap<String, String> = HashMap(paramsMap)
        appended["t"] = System.currentTimeMillis().toString()
        return appended
    }

    fun getPath(
        config: ConfigurationClient,
        downloadType: String,
        paramsMap: Map<String, String>,
        extension: String
    ): File {
        val params = appendDefaultParamsMap(paramsMap)
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

        defaultPaths[DOWNLOAD_STORY] = "\$Movies/SnapMod/%u_story_%t"
        defaultPaths[DOWNLOAD_AUDIO_NOTE] = "\$Movies/SnapMod/%id_audio_%t"
        defaultPaths[DOWNLOAD_PROFILE] = "\$Movies/SnapMod/%u_profile_%t"
    }
}