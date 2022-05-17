package xyz.rodit.snapmod.features.notifications

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.NotificationData
import xyz.rodit.snapmod.mappings.NotificationHandler
import xyz.rodit.snapmod.util.before
import xyz.rodit.snapmod.util.getList
import xyz.rodit.snapmod.util.toMax

class FilterTypes(context: FeatureContext) : Feature(context, 84608.toMax()) {

    private val hiddenTypes = hashSetOf<String>()

    override fun onConfigLoaded(first: Boolean) {
        hiddenTypes.clear()
        hiddenTypes.addAll(context.config.getList("filtered_notification_types"))
    }

    override fun performHooks() {
        NotificationHandler.handle.before {
            if (hiddenTypes.isEmpty()) return@before

            val handler = NotificationHandler.wrap(it.thisObject)
            val data = NotificationData.wrap(handler.data)
            val bundle = data.bundle

            val type =
                bundle.getString("type") ?: bundle.getString("n_key")?.split('~')?.get(0) ?: ""
            if (hiddenTypes.contains(type.lowercase())) {
                it.result = null
            }
        }
    }
}