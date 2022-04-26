package xyz.rodit.snapmod.util;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class DebugUtils {

    public static void dumpMap(Map map) {
        for (Object key : map.keySet()) {
            XposedBridge.log(key + ": " + map.get(key));
        }
    }

    public static void dumpObject(Object o) {
        if (o == null) {
            XposedBridge.log("null object dumped");
            return;
        }

        XposedBridge.log(o.toString());
        for (Field f : o.getClass().getFields()) {
            try {
                XposedBridge.log(f.getName() + ": " + f.get(o));
            } catch (Exception e) {
                XposedBridge.log("Error getting field value for " + f.getName() + ".");
            }
        }
    }

    public static void dumpStackTrace() {
        XposedBridge.log(Log.getStackTraceString(new Exception()));
    }

    public static void dumpMethodCall(XC_MethodHook.MethodHookParam param) {
        XposedBridge.log("Called " + param.method.getDeclaringClass().getName() + "#" + param.method.getName());
        XposedBridge.log("this: " + param.thisObject);
        XposedBridge.log("Arguments:");
        for (int i = 0; i < param.args.length; i++) {
            XposedBridge.log(i + ": " + param.args[i]);
        }
    }
}
