package xyz.rodit.snapmod.util

import xyz.rodit.snapmod.mappings.UUID
import java.nio.ByteBuffer

fun Any.toUUIDString(): String {
    return UUID.wrap(this).toUUIDString()
}

fun UUID.toUUIDString(): String {
    return (this.id as ByteArray).toUUIDString()
}

fun ByteArray.toUUIDString(): String {
    val bb = ByteBuffer.wrap(this)
    val high = bb.long
    val low = bb.long
    return java.util.UUID(high, low).toString()
}

fun String.toSnapUUID(): UUID {
    return UUID(arrayOf(this.toUUIDBytes()))
}

fun String.toUUIDBytes(): ByteArray {
    val uuid = java.util.UUID.fromString(this)
    val bb = ByteBuffer.wrap(ByteArray(16))
    bb.putLong(uuid.mostSignificantBits)
    bb.putLong(uuid.leastSignificantBits)
    return bb.array()
}