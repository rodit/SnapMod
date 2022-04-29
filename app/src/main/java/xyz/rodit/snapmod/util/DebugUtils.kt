package xyz.rodit.snapmod.util

import android.util.Log
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge

object DebugUtils {

    fun dumpMap(map: Map<*, *>) {
        map.entries.forEach {
            XposedBridge.log("${it.key}: ${it.value}")
        }
    }

    fun dumpObject(o: Any?) {
        if (o == null) {
            XposedBridge.log("null object dumped")
            return
        }

        XposedBridge.log(o.toString())
        for (f in o.javaClass.fields) {
            try {
                XposedBridge.log(f.name + ": " + f.get(o))
            } catch (e: Exception) {
                XposedBridge.log("Error getting field value for " + f.name + ".")
            }
        }
    }

    fun dumpStackTrace() {
        XposedBridge.log(Log.getStackTraceString(Exception()))
    }

    fun dumpMethodCall(param: MethodHookParam) {
        XposedBridge.log("Called " + param.method.declaringClass.name + "#" + param.method.name)
        XposedBridge.log("this: " + param.thisObject)
        XposedBridge.log("Arguments:")
        param.args.forEachIndexed { i, o -> XposedBridge.log("$i: $o") }
    }
}