package xyz.rodit.snapmod.features.info

import xyz.rodit.snapmod.Shared
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.CalendarDate
import xyz.rodit.snapmod.mappings.FooterInfoItem
import xyz.rodit.snapmod.mappings.FriendProfilePageData
import xyz.rodit.snapmod.mappings.FriendProfileTransformer
import xyz.rodit.snapmod.util.after
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class AdditionalFriendInfo(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Show more info in friend profile footer.
        FriendProfileTransformer.apply.after(context, "more_profile_info") {
            val transformer = FriendProfileTransformer.wrap(it.thisObject)
            if (!FriendProfilePageData.isInstance(transformer.data) || it.result !is List<*>) return@after

            val viewModelList = it.result as List<*>
            if (viewModelList.isEmpty()) return@after

            val viewModel = viewModelList[0]!!
            if (!FooterInfoItem.isInstance(viewModel)) return@after

            val data = FriendProfilePageData.wrap(transformer.data)
            val friendDate = Date(max(data.addedTimestamp, data.reverseAddedTimestamp))
            val birthday = if (data.birthday.isNull) CalendarDate(13, -1) else data.birthday

            val info = """Friends with ${data.displayName} since ${
                SimpleDateFormat.getDateInstance().format(friendDate)
            }.
${data.displayName}'s birthday is ${birthday.day} ${Shared.MONTHS[birthday.month - 1]}
Friendship: ${data.friendLinkType.instance}
Added by: ${data.addSourceTypeForNonFriend.instance}"""
            FooterInfoItem.wrap(viewModel).text = info
        }
    }
}