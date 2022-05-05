package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.SnapInteractionType
import xyz.rodit.snapmod.mappings.SnapManager
import xyz.rodit.snapmod.util.toUUIDString

class SnapInteractionFilter(context: FeatureContext) : StealthFeature(context) {

    override fun init() {
        setClass(SnapManager.getMapping())

        putFilters(
            SnapManager.onSnapInteraction,
            { SnapInteractionType.wrap(it.args[0]) },
            { it.args[1].toUUIDString() },
            ObjectFilter(
                context,
                "hide_snap_views",
                SnapInteractionType.VIEWING_INITIATED(),
                SnapInteractionType.VIEWING_FINISHED(),
                SnapInteractionType.MARK_AS_INVALID()
            )
        )
    }
}