package xyz.rodit.snapmod.features.opera

interface OperaPlugin {

    val isEnabled: Boolean
    fun shouldOverride(key: String): Boolean
    fun override(key: String, value: Any): Any
}