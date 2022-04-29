package xyz.rodit.snapmod.features.shared

interface Filter {

    val isEnabled: Boolean
    fun shouldFilter(item: Any?): Boolean
}