package xyz.rodit.snapmod;

import java.lang.reflect.Field;
import java.util.Map;

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
}
