package xyz.rodit.snapmod;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import xyz.rodit.dexsearch.client.xposed.MappedObject;

public class Config {

    private final File configFile = new File(Shared.SNAPMOD_CONFIG);

    private XSharedPreferences prefs;
    private long lastLoadTime = 0L;

    public XSharedPreferences getPreferences() {
        if (configFile.lastModified() > lastLoadTime || prefs == null) {
            XposedBridge.log("Loading preferences from " + configFile + " (exists=" + configFile.exists() + ").");
            prefs = new XSharedPreferences(configFile);
            lastLoadTime = System.currentTimeMillis();
            XposedBridge.log("Loaded preferences.");
        }

        return prefs;
    }

    public boolean get(String pref) {
        return getPreferences().getBoolean(pref, false);
    }

    public void prevent(XC_MethodHook.MethodHookParam param, String pref, Object enumObj, Object... enumVals) {
        if (get(pref)) {
            for (Object value : enumVals) {
                if (value instanceof MappedObject) {
                    value = ((MappedObject) value).instance;
                }

                if (enumObj.equals(value)) {
                    param.setResult(null);
                    break;
                }
            }
        }
    }
}
