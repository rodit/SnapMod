package xyz.rodit.snapmod.features

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

typealias InflateListener = (view: View) -> Unit
typealias AddListener = (parent: ViewGroup, view: View) -> Unit

class ViewInterceptor(context: FeatureContext) : Contextual(context) {

    private val inflateListeners = mutableMapOf<Int, MutableList<InflateListener>>()
    private val addListeners = mutableMapOf<Int, MutableList<AddListener>>()

    private val tagWithLayout = mutableSetOf<Int>()

    fun onInflate(resource: String, listener: InflateListener) {
        inflateListeners.computeIfAbsent(getResourceId(resource)) { mutableListOf() }.add(listener)
    }

    fun onAdd(resource: String, listener: AddListener) {
        val resourceId = getResourceId(resource)
        addListeners.computeIfAbsent(resourceId) { mutableListOf() }.add(listener)
        tagWithLayout.add(resourceId)
    }

    private fun getResourceId(resource: String): Int {
        val resourceId = context.appContext.resources.getIdentifier(
            resource,
            "layout",
            context.appContext.packageName
        )
        return if (resourceId == 0) throw RuntimeException("Resource for interception listener not found $resource.") else resourceId
    }

    init {
        XposedHelpers.findAndHookMethod(
            LayoutInflater::class.java,
            "inflate",
            Int::class.java,
            ViewGroup::class.java,
            Boolean::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val layoutId = param.args[0]
                    if (layoutId !is Int) return

                    val view = param.result
                    if (view !is View) return

                    if (tagWithLayout.contains(layoutId)) view.tag = layoutId

                    inflateListeners[layoutId]?.let {
                        it.forEach { listener -> listener(view) }
                    }
                }
            })

        XposedHelpers.findAndHookMethod(
            ViewGroup::class.java,
            "addView",
            View::class.java,
            Int::class.java,
            ViewGroup.LayoutParams::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val parent = param.thisObject as ViewGroup
                    val view = param.args[0]
                    if (view !is View) return

                    val layoutId = view.tag
                    if (layoutId !is Int) return

                    addListeners[layoutId]?.let {
                        it.forEach { listener -> listener(parent, view) }
                    }
                }
            }
        )
    }
}