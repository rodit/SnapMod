package xyz.rodit.snapmod.logging

import java.util.*

private val logMap = WeakHashMap<Int, XLog>()

val Any.log: XLog
    get() {
        return logMap.computeIfAbsent(this.hashCode()) { XLog(this.javaClass.simpleName) }!!
    }