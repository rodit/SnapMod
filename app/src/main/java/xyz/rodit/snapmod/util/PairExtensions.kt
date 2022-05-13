package xyz.rodit.snapmod.util

import de.robv.android.xposed.XposedHelpers

val Any.pairFirst: Any?
    get() {
        return XposedHelpers.getObjectField(this, "a")
    }

val Any.pairSecond: Any?
    get() {
        return XposedHelpers.getObjectField(this, "b")
    }