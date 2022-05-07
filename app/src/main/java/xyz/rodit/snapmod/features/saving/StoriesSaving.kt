package xyz.rodit.snapmod.features.saving

import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.download
import xyz.rodit.xposed.client.http.StreamProvider
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL

class StoriesSaving(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override save story click.
        ContextActionMenuModel.constructors.after(context, "allow_download_stories") {
            val model = ContextActionMenuModel.wrap(it.thisObject)
            if (model.action.instance !== OperaContextAction.SAVE().instance) return@after

            val clickProxy = Proxy.newProxyInstance(
                context.classLoader,
                arrayOf(ContextClickHandler.getMappedClass()),
                StoryDownloadProxy(context)
            )
            model.onClick = ContextClickHandler.wrap(clickProxy)
        }

        context.callbacks.hook(
            ConversationManager::class,
            DefaultFetchMessageCallback.onFetchMessageComplete
        ) { DefaultFetchMessageCallback.wrap(it).dummy }
    }

    private class StoryDownloadProxy(private val context: FeatureContext) : InvocationHandler {

        override fun invoke(thisObject: Any, method: Method, args: Array<Any>?): Any? {
            if (method.name != ContextClickHandler.invoke.dexName) return null

            val map = ParamsMap.wrap(args!![0])
            StoryHelper.getMediaInfo(context, map, this::downloadStoryMedia)

            return null
        }

        private fun downloadStoryMedia(media: StoryHelper.StoryMedia?) {
            if (media == null || media.info.isNull) {
                XposedBridge.log("Null media info for story download.")
                return
            }

            val provider: StreamProvider = FileProxyStreamProvider(context.appContext) {
                try {
                    var stream = URL(media.info.uri).openStream()
                    val enc = media.info.encryption
                    if (enc.isNotNull) {
                        stream = enc.decryptStream(stream)
                    }

                    return@FileProxyStreamProvider stream
                } catch (e: Exception) {
                    XposedBridge.log("Error opening stream.")
                    XposedBridge.log(e)
                }
                return@FileProxyStreamProvider null
            }

            context.download(
                PathManager.DOWNLOAD_STORY,
                mapOf("u" to media.username),
                media.extension,
                provider,
                media.username + "'s Story"
            )
        }
    }
}