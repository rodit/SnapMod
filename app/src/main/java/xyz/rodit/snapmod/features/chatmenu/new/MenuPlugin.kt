package xyz.rodit.snapmod.features.chatmenu.new

abstract class MenuPlugin {

    abstract fun shouldCreate(): Boolean

    abstract fun createModel(key: String): Any

    open fun performHooks() {

    }
}