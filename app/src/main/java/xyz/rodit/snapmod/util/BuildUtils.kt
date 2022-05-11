package xyz.rodit.snapmod.util

import android.content.Context
import android.os.Build

val Context.versionCode: Long
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            this.packageManager.getPackageInfo(this.packageName, 0).longVersionCode else
            this.packageManager.getPackageInfo(this.packageName, 0).versionCode.toLong()
    }

fun Long.to(max: Long): LongRange {
    return LongRange(this, max)
}

fun Int.to(max: Long): LongRange {
    return LongRange(this.toLong(), max)
}

fun Long.toMax(): LongRange {
    return LongRange(this, Long.MAX_VALUE)
}

fun Int.toMax(): LongRange {
    return LongRange(this.toLong(), Long.MAX_VALUE)
}