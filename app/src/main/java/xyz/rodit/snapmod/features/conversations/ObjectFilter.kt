package xyz.rodit.snapmod.features.conversations

import xyz.rodit.dexsearch.client.xposed.MappedObject
import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.shared.Filter

open class ObjectFilter<T>(context: FeatureContext, private val configKey: String, vararg filtered: T) : Contextual(context), Filter {

    private val filtered: MutableSet<Any> = HashSet()

    override val isEnabled: Boolean
        get() = context.config.getBoolean(configKey)

    override fun shouldFilter(item: Any?): Boolean {
        val compare = if (item is MappedObject) item.instance else item
        return filtered.contains(compare)
    }

    init {
        this.filtered.addAll(filtered.mapNotNull { if (it is MappedObject) it.instance else it })
    }
}
