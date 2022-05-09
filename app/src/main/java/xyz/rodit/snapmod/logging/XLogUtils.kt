package xyz.rodit.snapmod.logging

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import java.util.*

private val logMap = WeakHashMap<Int, XLog>()

val Any.log: XLog
    get() {
        return logMap.computeIfAbsent(this.hashCode()) { XLog(this.javaClass.simpleName) }!!
    }

fun XLog.dumpMap(map: Map<*, *>) {
    this.debug("${map.javaClass.simpleName}:\n" + map.entries.joinToString("\n") { "${it.key}: ${it.value}" })
}

fun XLog.dump(o: Any?) {
    if (o == null) {
        this.debug("Null object dumped.")
        return
    }

    this.debug("${o}\n" +
            o.javaClass.fields.map {
                try {
                    "${it.name}: ${it.get(o)}"
                } catch (e: Exception) {
                    "Error getting field value for ${it.name}: ${e.message}"
                }
            }.joinToString("\n")
    )
}

fun XLog.dumpStackTrace() {
    this.debug(Log.getStackTraceString(Exception()))
}

fun XLog.dumpMethodCall(param: XC_MethodHook.MethodHookParam) {
    log.debug(
        "Called ${param.method.declaringClass.name}#${param.method.name}\n" +
                "this: ${param.thisObject}\n" +
                "Arguments:\n" +
                param.args.mapIndexed { i, o -> "$i: $o" }.joinToString("\n")
    )
}