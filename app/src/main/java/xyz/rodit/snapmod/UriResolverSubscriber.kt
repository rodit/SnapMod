package xyz.rodit.snapmod

import android.util.Log
import xyz.rodit.snapmod.logging.log
import xyz.rodit.snapmod.mappings.RxObserver
import xyz.rodit.snapmod.util.TAG
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

typealias ResolutionListener = (result: Any?) -> Unit

open class UriResolverSubscriber(private val listener: ResolutionListener) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
        if (method.name == RxObserver.accept.dexName) {
            Log.d(TAG, "Resolved content uri - notifying listener.")
            listener.invoke(args[0])
        } else if (method.name == RxObserver.error.dexName) {
            log.error("Error resolving media uri.", args[0] as Throwable)
        }

        return null
    }
}