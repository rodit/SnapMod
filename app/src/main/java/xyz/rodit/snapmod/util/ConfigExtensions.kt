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