package xyz.rodit.snapmod.features.saving

import android.app.AlertDialog
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.PublicProfileTile
import xyz.rodit.snapmod.mappings.PublicProfileTileTransformer
import xyz.rodit.snapmod.util.PathManager

private const val PROFILE_PICTURE_RESOLUTION_PATTERN = "0,\\d+_"

class PublicProfileSaving(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Hook friend public profile tile to allow download profile picture.
        PublicProfileTileTransformer.transform.hook(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val view =
                    PublicProfileTileTransformer.wrap(param.thisObject).profileImageView as View
                val tile = PublicProfileTile.wrap(param.args[0])
                view.setOnLongClickListener {
                    if (!context.config.getBoolean("allow_download_public_dp")) return@setOnLongClickListener false

                    val url = tile.profilePictureUrl
                    context.activity!!.runOnUiThread {
                        AlertDialog.Builder(context.activity)
                            .setTitle("Download Profile Picture?")
                            .setPositiveButton("Yes") { _, _ ->
                                var resolution = context.config.getString("public_dp_resolution", "500")

                                var resDouble = resolution.toDouble()
                                if (resDouble < 1 || resDouble > 5000) resDouble = 500.0

                                resolution = resDouble.toInt().toString()
                                val resizedUrl = url.replace(
                                    PROFILE_PICTURE_RESOLUTION_PATTERN.toRegex(),
                                    "0," + resolution + "_"
                                )
                                val username = tile.info.metadata.username
                                val dest = PathManager.getUri(
                                    context.config,
                                    PathManager.DOWNLOAD_PROFILE,
                                    mapOf("u" to username),
                                    ".jpg"
                                )
                                context.files.download(
                                    context.config.getBoolean("use_android_download_manager"),
                                    resizedUrl,
                                    dest,
                                    "$username's profile picture",
                                    null
                                )
                            }
                            .setNegativeButton("No") { _, _ -> }
                            .show()
                    }

                    return@setOnLongClickListener true
                }
            }
        })
    }
}