package xyz.rodit.snapmod.features.callbacks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.dexsearch.client.xposed.MethodRef
import xyz.rodit.snapmod.isDummyProxy
import xyz.rodit.snapmod.mappings.DefaultFetchConversationCallback
import xyz.rodit.snapmod.mappings.DefaultFetchMessageCallback
import xyz.rodit.snapmod.util.before
import kotlin.reflect.KClass

typealias HookedCallback = (XC_MethodHook.MethodHookParam) -> Boolean

class CallbackManager {

    private val callbacks = mutableMapOf<String, MutableList<HookedCallback>>()

    fun hook(type: KClass<*>, method: MethodRef, obtainInterface: (Any) -> Any) {
        method.before {
            if (!obtainInterface(it.thisObject).isDummyProxy) return@before

            callbacks["${type.simpleName}:${method.name}"]?.let { list ->
                val remove = list.filter { c -> c(it) }
                list.removeAll(remove)
            }

            it.result = null
        }
    }

    fun on(type: KClass<*>, method: MethodRef, callback: HookedCallback) {
        callbacks.computeIfAbsent("${type.simpleName}:${method.name}") { mutableListOf() }.add(callback)
    }

    init {
        hook(
            DefaultFetchConversationCallback::class,
            DefaultFetchConversationCallback.onFetchConversationWithMessagesComplete
        ) { DefaultFetchConversationCallback.wrap(it).dummy }

        hook(
            DefaultFetchMessageCallback::class,
            DefaultFetchMessageCallback.onFetchMessageComplete
        ) { DefaultFetchMessageCallback.wrap(it).dummy }
    }
}
