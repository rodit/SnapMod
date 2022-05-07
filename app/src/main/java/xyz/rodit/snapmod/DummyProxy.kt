package xyz.rodit.snapmod

import xyz.rodit.dexsearch.client.xposed.MappedObject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

const val DummyProxyString = "DummyProxy"

class DummyProxy : InvocationHandler {

    override fun invoke(target: Any?, method: Method, args: Array<out Any>?): Any? {
        if (method.name == "toString") return DummyProxyString
        return null
    }
}

fun Class<*>.createDummyProxy(classLoader: ClassLoader): Any {
    return Proxy.newProxyInstance(
        classLoader,
        arrayOf(this),
        DummyProxy()
    )
}

val Any.isDummyProxy: Boolean
    get() {
        return if (this is MappedObject) this.instance.isDummyProxy else this.toString() == DummyProxyString
    }