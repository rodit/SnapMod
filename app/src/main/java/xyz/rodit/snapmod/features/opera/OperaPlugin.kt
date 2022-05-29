package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.mappings.ParamsMap

interface OperaPlugin {

    val isEnabled: Boolean
    fun shouldOverride(params: ParamsMap, key: String): Boolean
    fun override(params: ParamsMap, key: String, value: Any): Any
}