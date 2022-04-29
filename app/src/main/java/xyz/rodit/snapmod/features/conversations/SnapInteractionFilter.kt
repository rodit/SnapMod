package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.SnapInteractionType
import xyz.rodit.snapmod.mappings.SnapManager
import xyz.rodit.snapmod.util.UUIDUtil

class SnapInteractionFilter(context: FeatureContext) : StealthFeature(context) {

    override fun init() {
        setClass(SnapManager.getMapping())

        putFilters(
            SnapManager.onSnapInteraction,
            { SnapInteractionType.wrap(it.args[0]) },
            { UUIDUtil.fromSnap(it.args[1]) },
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