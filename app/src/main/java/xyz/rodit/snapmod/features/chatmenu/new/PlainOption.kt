package xyz.rodit.snapmod.features.chatmenu.new

import xyz.rodit.snapmod.createDelegate
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ActionPlain
import xyz.rodit.snapmod.mappings.Func0

typealias ClickHandler = (key: String) -> Unit

class PlainOption(
    private val context: FeatureContext,
    private val text: String,
    private val click: ClickHandler
) : MenuPlugin() {

    override fun shouldCreate() = true

    override fun createModel(key: String): Any = ActionPlain(
        text,
        Func0.wrap(Func0.getMappedClass().createDelegate(context.classLoader) { _, _ ->
            click(key)
        })
    ).instance
}