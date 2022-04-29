package xyz.rodit.snapmod

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ObjectProxy(private val target: Any) : InvocationHandler {

    @Throws(Throwable::class)
    override fun invoke(o: Any, method: Method, args: Array<Any>?): Any? {
        if (args == null) return method.invoke(target)
        return method.invoke(target, *args)
    }
}