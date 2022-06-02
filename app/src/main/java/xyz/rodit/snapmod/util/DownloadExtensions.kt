package xyz.rodit.snapmod.util

import android.widget.Toast
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.xposed.client.http.StreamProvider
import java.util.*

fun FeatureContext.download(
    type: String,
    pathParams: Map<String, String>,
    extension: String,
    provider: StreamProvider,
    title: String? = null,
    description: String? = null
) {
    val dest = PathManager.getUri(config, type, pathParams, extension)
    download(dest, provider, title, description)
}

fun FeatureContext.download(
    dest: String, provider: StreamProvider,
    title: String? = null,
    description: String? = null
) {
    if (config.getBoolean("show_toast_on_download")) {
        activity?.runOnUiThread {
            Toast.makeText(appContext, "Download started.", Toast.LENGTH_SHORT).show()
        }
    }

    val uuid = UUID.randomUUID().toString()
    server.mapStream(uuid, provider)
    files.download(
        config.getBoolean("use_android_download_manager", true),
        "${server.root}/$uuid",
        dest,
        title,
        description
    )
}
