package xyz.rodit.snapmod.features.notifications

import android.app.Notification
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.SnapNotificationBuilder
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.getList
import xyz.rodit.snapmod.util.toMax

class FilterTypes(context: FeatureContext) : Feature(context, 84608.toMax()) {

    private val hiddenTypes = hashSetOf<String>()

    override fun onConfigLoaded(first: Boolean) {
        hiddenTypes.clear()
        hiddenTypes.addAll(context.config.getList("filtered_notification_types"))
    }

    override fun performHooks() {
        SnapNotificationBuilder.build.after {
            if (hiddenTypes.isEmpty()) return@after

            val notification = it.result as Notification
            val snapBundle = notification.extras.getBundle("system_notification_extras") ?: return@after
            val type = snapBundle.getString("notification_type") ?: return@after
            if (hiddenTypes.contains(type)) {
                it.result = null
            }
        }
    }
}