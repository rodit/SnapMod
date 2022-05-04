package xyz.rodit.snapmod

import android.app.Activity
import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.FeatureManager
import xyz.rodit.snapmod.features.InstanceManager
import xyz.rodit.snapmod.mappings.MainActivity
import xyz.rodit.xposed.HooksBase
import xyz.rodit.xposed.mappings.LoadScheme
import java.util.*

class SnapHooks : HooksBase(
    listOf(Shared.SNAPCHAT_PACKAGE),
    EnumSet.of(LoadScheme.CACHED_ON_CONTEXT, LoadScheme.SERVICE),
    Shared.SNAPMOD_PACKAGE_NAME,
    Shared.SNAPMOD_CONFIG_ACTION,
    Shared.CONTEXT_HOOK_CLASS,
    Shared.CONTEXT_HOOK_METHOD
) {

    private var mainActivity: Activity? = null
    private var featureContext: FeatureContext? = null
    private var features: FeatureManager? = null
    private var queueFeatureConfig = false

    override fun onPackageLoad() {
        XposedBridge.hookAllMethods(
            DevicePolicyManager::class.java,
            "getCameraDisabled",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (config == null || !config.isLoaded || config.getBoolean("disable_camera")) {
                        param.result = true
                    }
                }
            })

        CustomResources.init()
    }

    override fun onContextHook(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            XposedBridge.log("Uncaught exception on thread $thread.")
            XposedBridge.log(throwable)
        }
    }

    override fun onConfigLoaded(first: Boolean) {
        mainActivity?.let {
            Handler(it.mainLooper).postDelayed({
                val intent = Intent().apply {
                    setClassName(
                        Shared.SNAPMOD_PACKAGE_NAME,
                        Shared.SNAPMOD_FORCE_RESUME_ACTIVITY
                    )
                }
                it.startActivity(intent)
            }, 500)
        }

        if (features != null) {
            features!!.onConfigLoaded(first)
        } else {
            queueFeatureConfig = true
        }
    }

    override fun performHooks() {
        requireFileService(Shared.SNAPMOD_FILES_ACTION)
        requireStreamServer(0)

        MainActivity.attachBaseContext.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                featureContext!!.activity = param.thisObject as Activity
                mainActivity = featureContext!!.activity
            }
        })

        featureContext =
            FeatureContext(
                appContext,
                lpparam.classLoader,
                config,
                files,
                server,
                InstanceManager()
            )

        (appContext as Application).registerActivityLifecycleCallbacks(
            FeatureContextUpdater(featureContext!!)
        )

        features = FeatureManager(featureContext!!).apply {
            load()
            init()
            performHooks()

            if (queueFeatureConfig) onConfigLoaded(true)
        }
    }
}