package xyz.rodit.snapmod.logging

import de.robv.android.xposed.XposedBridge

const val LOG_NONE = 0
const val LOG_ERROR = 1
const val LOG_WARN = 2
const val LOG_DEBUG = 4

private val levelNames = mapOf(
    LOG_ERROR to "error",
    LOG_WARN to "warn",
    LOG_DEBUG to "debug"
)

class XLog(private val tag: String) {

    var level: Int = LOG_ERROR or LOG_WARN or LOG_DEBUG

    fun put(level: Int, message: String) {
        if ((globalLevel or this.level) and level == 0 || !levelNames.containsKey(level)) return

        XposedBridge.log("$tag/${levelNames[level]}: $message")
    }

    fun error(message: String, error: Throwable? = null) {
        put(LOG_ERROR, message)
        error?.let { put(LOG_ERROR, error.toString()) }
    }

    fun warn(message: String) {
        put(LOG_WARN, message)
    }

    fun debug(message: String) {
        put(LOG_DEBUG, message)
    }

    companion object {
        var globalLevel = LOG_NONE
    }
}