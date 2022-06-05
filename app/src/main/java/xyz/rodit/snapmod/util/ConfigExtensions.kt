package xyz.rodit.snapmod.util

import xyz.rodit.xposed.client.ConfigurationClient

fun ConfigurationClient.getList(key: String): List<String> {
    return this.getString(key, "[]")
        .drop(1)
        .dropLast(1)
        .split(',')
        .filter(String::isNotBlank)
        .map(String::trim)
}

fun ConfigurationClient.getNonDefault(key: String, default: Int = 0): Int? {
    val value = this.getInt(key, default)
    return if (value == default) null else value
}

fun ConfigurationClient.getResolution(key: String): Resolution? {
    val parts = this.getString(key, "0").split('x')
    if (parts.size != 2) return null
    return try {
        val dimens = parts.map { it.toInt() }
        Resolution(dimens[0], dimens[1])
    } catch (ex: Exception) {
        null
    }
}

data class Resolution(val width: Int, val height: Int)