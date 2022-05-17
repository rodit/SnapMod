package xyz.rodit.snapmod.util

import xyz.rodit.snapmod.logging.log
import java.math.BigInteger

private val BIGINT_2 = BigInteger.valueOf(2)
private const val TYPE_VAR_INT = 0
private const val TYPE_STRING = 2

internal inline infix fun Byte.and(other: Int): Int = toInt() and other

internal inline infix fun Byte.shl(other: Int): Int = toInt() shl other

class ProtoReader(private val data: ByteArray) {

    private var position = 0
    private var checkpoint = 0

    fun read(): List<ProtoPart> {
        val parts = mutableListOf<ProtoPart>()

        while (position < data.size) {
            checkpoint = position

            val varInt = internalReadVarint32()
            val type = varInt and 0b111
            val index = varInt shr 3

            var value = ByteArray(0)

            if (type == TYPE_VAR_INT) {
                value = internalReadVarint32().toString().toByteArray()
            } else if (type == TYPE_STRING) {
                val length = internalReadVarint32()
                value = ByteArray(length)
                data.copyInto(value, 0, position, position + length)
                position += length
            } else {
                log.error("Unknown protobuf type $type")
            }

            parts.add(ProtoPart(index, type, value))
        }

        return parts
    }

    private fun readByte(): Byte {
        return data[position++]
    }

    private fun internalReadVarint32(): Int {
        var tmp = readByte()
        if (tmp >= 0) {
            return tmp.toInt()
        }
        var result = tmp and 0x7f
        tmp = readByte()
        if (tmp >= 0) {
            result = result or (tmp shl 7)
        } else {
            result = result or (tmp and 0x7f shl 7)
            tmp = readByte()
            if (tmp >= 0) {
                result = result or (tmp shl 14)
            } else {
                result = result or (tmp and 0x7f shl 14)
                tmp = readByte()
                if (tmp >= 0) {
                    result = result or (tmp shl 21)
                } else {
                    result = result or (tmp and 0x7f shl 21)
                    tmp = readByte()
                    result = result or (tmp shl 28)
                    if (tmp < 0) {
                        for (i in 0..4) {
                            if (readByte() >= 0) {
                                return result
                            }
                        }
                    }
                }
            }
        }
        return result
    }
}

data class ProtoPart(val index: Int, val type: Int, val value: ByteArray)