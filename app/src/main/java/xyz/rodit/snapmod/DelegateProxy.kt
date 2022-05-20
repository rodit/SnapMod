package xyz.rodit.snapmod

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

typealias DelegateFunction = (Any, Array<Any>) -> Any?

class DelegateProxy(private val delegate: DelegateFunction) : InvocationHandler {

    override fun invoke(target: Any, method: Method, args: Array<Any>?): Any? {
        return delegate(target, args ?: emptyArray())
    }
}

fun Class<*>.createDelegate(classLoader: ClassLoader, delegate: DelegateFunction): Any {
    return Proxy.newProxyInstance(
        classLoader,
        arrayOf(this),
        DelegateProxy(delegate)
    )
}