package xyz.rodit.snapmod.logging

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.*

private val logMap = WeakHashMap<Any, XLog>()

val Any.log: XLog
    get() {
        return logMap.computeIfAbsent(this) { XLog(this.javaClass.simpleName) }!!
    }

fun XLog.dumpMap(map: Map<*, *>) {
    this.debug("${map.javaClass.simpleName}:\n" + map.entries.joinToString("\n") { "${it.key}: ${it.value}" })
}

fun XLog.dump(o: Any?) {
    if (o == null) {
        this.debug("Null object dumped.")
        return
    }

    this.debug("Object dump type=${o.javaClass}\n" +
            "${o}\n" +
            o.javaClass.fields.joinToString("\n") {
                try {
                    "${it.name}: ${it.get(o)}"
                } catch (e: Exception) {
                    "Error getting field value for ${it.name}: ${e.message}"
                }
            }
    )
}

fun XLog.dumpStackTrace() {
    this.debug(Log.getStackTraceString(Exception()))
}

fun XLog.dumpMethodCall(param: XC_MethodHook.MethodHookParam) {
    this.debug(
        "Called ${param.method.declaringClass.name}#${param.method.name}\n" +
                "this: ${param.thisObject}\n" +
                "Arguments:\n" +
                param.args.mapIndexed { i, o -> "$i: $o" }.joinToString("\n")
    )
}

fun XLog.dumpConstruction(className: String, classLoader: ClassLoader) {
    val cls = XposedHelpers.findClass(className, classLoader)
    XposedBridge.hookAllConstructors(cls, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            debug("${param.thisObject}")
        }
    })
}