package xyz.rodit.snapmod;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.Collections;
import java.util.EnumSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.features.FeatureManager;
import xyz.rodit.snapmod.mappings.MainActivity;
import xyz.rodit.xposed.HooksBase;
import xyz.rodit.xposed.mappings.LoadScheme;

public class SnapHooks extends HooksBase {

    private Activity mainActivity;
    private FeatureContext featureContext;
    private FeatureManager features;

    public SnapHooks() {
        super(Collections.singletonList(Shared.SNAPCHAT_PACKAGE),
                EnumSet.of(LoadScheme.CACHED_ON_CONTEXT, LoadScheme.SERVICE),
                Shared.SNAPMOD_PACKAGE_NAME,
                Shared.SNAPMOD_CONFIG_ACTION,
                Shared.CONTEXT_HOOK_CLASS,
                Shared.CONTEXT_HOOK_METHOD);
    }

    @Override
    protected void onPackageLoad() {
        XposedBridge.hookAllMethods(DevicePolicyManager.class, "getCameraDisabled", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config == null || !config.isLoaded() || config.getBoolean("disable_camera")) {
                    param.setResult(true);
                }
            }
        });

        CustomResources.init();
    }

    @Override
    protected void onContextHook(Context context) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            XposedBridge.log("Uncaught exception on thread " + thread + ".");
            XposedBridge.log(throwable);
        });
    }

    @Override
    protected void onConfigLoaded(boolean first) {
        if (mainActivity != null) {
            new Handler(mainActivity.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent();
                intent.setClassName(Shared.SNAPMOD_PACKAGE_NAME, Shared.SNAPMOD_FORCE_RESUME_ACTIVITY);
                mainActivity.startActivity(intent);
            }, 500);
        }

        features.onConfigLoaded(first);
    }

    @Override
    protected void performHooks() {
        requireFileService(Shared.SNAPMOD_FILES_ACTION);
        requireStreamServer(0);

        MainActivity.attachBaseContext.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                mainActivity = featureContext.activity = (Activity) param.thisObject;
            }
        });

        featureContext = new FeatureContext(appContext, lpparam.classLoader, config, files, server);
        features = new FeatureManager(featureContext);
        features.load();
        features.init();
        features.performHooks();
    }
}
