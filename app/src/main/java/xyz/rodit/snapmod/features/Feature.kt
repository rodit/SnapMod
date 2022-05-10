package xyz.rodit.snapmod.features

abstract class Feature(
    context: FeatureContext, val support: LongRange = LongRange(0, Long.MAX_VALUE)
) : Contextual(context) {

    open fun init() {}
    open fun onConfigLoaded(first: Boolean) {}

    abstract fun performHooks()
}