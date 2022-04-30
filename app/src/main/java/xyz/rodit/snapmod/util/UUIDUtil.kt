package xyz.rodit.snapmod.util

import xyz.rodit.snapmod.mappings.UUID
import java.nio.ByteBuffer

object UUIDUtil {

    fun fromSnap(snapUUID: Any?): String {
        return fromByteArray(UUID.wrap(snapUUID).id as ByteArray)
    }

    fun fromByteArray(bytes: ByteArray): String {
        val bb = ByteBuffer.wrap(bytes)
        val high = bb.long
        val low = bb.long
        return java.util.UUID(high, low).toString()
    }
}