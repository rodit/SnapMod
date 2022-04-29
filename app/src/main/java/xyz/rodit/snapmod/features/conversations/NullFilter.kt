package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.FeatureContext

class NullFilter(context: FeatureContext, configKey: String) : ObjectFilter<Any?>(context, configKey, null) {

    override fun shouldFilter(item: Any?): Boolean {
        return true
    }
}