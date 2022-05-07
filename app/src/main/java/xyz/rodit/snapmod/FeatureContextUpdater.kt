package xyz.rodit.snapmod

import android.app.Activity
import android.app.Application
import android.os.Bundle
import xyz.rodit.snapmod.features.FeatureContext

class FeatureContextUpdater(val context: FeatureContext) : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        context.activity = activity
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        if (context.activity == activity) context.activity = null
    }
}
