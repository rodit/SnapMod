package xyz.rodit.snapmod.features

abstract class Feature(context: FeatureContext) : Contextual(context) {

    open fun init() {}
    open fun onConfigLoaded(first: Boolean) {}

    abstract fun performHooks()
}