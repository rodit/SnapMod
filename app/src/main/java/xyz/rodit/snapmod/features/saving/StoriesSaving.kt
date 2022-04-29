package xyz.rodit.snapmod.features.saving

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ContextActionMenuModel
import xyz.rodit.snapmod.mappings.ContextClickHandler
import xyz.rodit.snapmod.mappings.OperaContextAction
import xyz.rodit.snapmod.mappings.ParamsMap
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.xposed.client.http.StreamProvider
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL
import java.util.*

class StoriesSaving(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override save story click.
        ContextActionMenuModel.constructors.hook(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val model = ContextActionMenuModel.wrap(param.thisObject)
                if (!context.config.getBoolean("allow_download_stories")
                    || model.action.instance !== OperaContextAction.SAVE().instance
                ) return

                val clickProxy = Proxy.newProxyInstance(
                    context.classLoader,
                    arrayOf(ContextClickHandler.getMappedClass()),
                    StoryDownloadProxy(context)
                )
                model.onClick = ContextClickHandler.wrap(clickProxy)

            }
        })
    }

    private class StoryDownloadProxy(private val context: FeatureContext) : InvocationHandler {

        override fun invoke(thisObject: Any, method: Method, args: Array<Any>?): Any? {
            if (method.name != ContextClickHandler.invoke.dexName) return null

            val map = ParamsMap.wrap(args!![0])
            val media = StoryHelper.getMediaInfo(map)
            if (media == null || media.info.isNull) {
                XposedBridge.log("Null media info for story download.")
                return null
            }

            val provider: StreamProvider = FileProxyStreamProvider(context.appContext) {
                try {
                    var stream = URL(media.info.uri).openStream()
                    val enc = media.info.encryption
                    if (enc.isNotNull) {
                        stream = enc.decryptStream(stream)
                        XposedBridge.log("Stream was encrypted.")
                    }

                    XposedBridge.log("Media stream opened.")
                    return@FileProxyStreamProvider stream
                } catch (e: Exception) {
                    XposedBridge.log("Error opening stream.")
                    XposedBridge.log(e)
                }
                return@FileProxyStreamProvider null
            }
            val uuid = UUID.randomUUID().toString()
            context.server.mapStream(uuid, provider)
            val dest = PathManager.getUri(
                context.config,
                PathManager.DOWNLOAD_STORY,
                mapOf("u" to media.username),
                media.extension
            )
            context.files.download(
                context.config.getBoolean("use_android_download_manager", true),
                context.server.root + "/" + uuid,
                dest,
                media.username + "'s Story",
                null
            )

            return null
        }
    }
}