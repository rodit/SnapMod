package xyz.rodit.snapmod.features.conversations

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import xyz.rodit.dexsearch.client.ClassMapping
import xyz.rodit.dexsearch.client.xposed.MappedObject
import xyz.rodit.dexsearch.client.xposed.MethodRef
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.shared.Filter

typealias FilterObjectSupplier = (MethodHookParam) -> Any?
typealias ConversationIdSupplier = (MethodHookParam) -> String?

abstract class StealthFeature(context: FeatureContext) : Feature(context) {

    private val filters: MutableMap<String, MutableList<Filter>> = HashMap()
    private val suppliers: MutableMap<String, FilterObjectSupplier> = HashMap()
    private val conversationIdSuppliers: MutableMap<String, ConversationIdSupplier> = HashMap()

    private var className: String? = null

    fun setClass(mapping: ClassMapping) {
        className = mapping.niceClassName
    }

    fun putFilters(
        method: MethodRef,
        supplier: FilterObjectSupplier,
        conversationIdSupplier: ConversationIdSupplier,
        vararg filters: Filter
    ) {
        putFilters(method.name, supplier, conversationIdSupplier, *filters)
    }

    fun putFilters(
        methodName: String,
        supplier: FilterObjectSupplier,
        conversationIdSupplier: ConversationIdSupplier,
        vararg filters: Filter
    ) {
        this.filters.computeIfAbsent(methodName) { ArrayList() }.addAll(filters)
        suppliers[methodName] = supplier
        conversationIdSuppliers[methodName] = conversationIdSupplier
    }

    protected open fun onPostHook(param: MethodHookParam) {}

    override fun performHooks() {
        filters.forEach {
            val methodName = it.key
            val supplier: FilterObjectSupplier? = suppliers[methodName]
            val conversationIdSupplier = conversationIdSuppliers[methodName]

            MappedObject.hook(className, methodName, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val obj = supplier?.invoke(param)
                    val id = conversationIdSupplier?.invoke(param) ?: return
                    val stealth = context.stealth.isEnabled(id)

                    if (filters[methodName]!!.any { f ->
                            (f.isEnabled || stealth) && f.shouldFilter(obj)
                        }) {
                        param.result = null
                        onPostHook(param)
                    }
                }
            })
        }
    }
}