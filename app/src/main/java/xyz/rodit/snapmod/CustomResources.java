package xyz.rodit.snapmod;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class CustomResources {

    private static final Map<Integer, String> CUSTOM_STRINGS = new HashMap<>();

    static {
        putString(string.menu_option_stealth_mode, "Stealth Mode");
    }

    public static void putString(int key, String value) {
        CUSTOM_STRINGS.put(key, value);
    }

    public static void init() {
        XposedBridge.hookAllMethods(Context.class, "getString", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Integer id = (Integer) param.args[0];
                if (CUSTOM_STRINGS.containsKey(id)) {
                    param.setResult(CUSTOM_STRINGS.get(id));
                }
            }
        });
    }

    public static class string {

        public static final int menu_option_stealth_mode = -100000;
    }
}
