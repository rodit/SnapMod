package xyz.rodit.snapmod.util

import de.robv.android.xposed.XposedHelpers

val Any.pairFirst: Any?
    get() {
        return try {
            XposedHelpers.getObjectField(this, "a")
        } catch (ex: Throwable) {
            null
        }
    }

val Any.pairSecond: Any?
    get() {
        return try {
            XposedHelpers.getObjectField(this, "b")
        } catch (ex: Throwable) {
            null
        }
    }