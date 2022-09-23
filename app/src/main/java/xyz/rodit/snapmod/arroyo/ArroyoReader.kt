package xyz.rodit.snapmod.arroyo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import xyz.rodit.snapmod.logging.log
import xyz.rodit.snapmod.util.ProtoReader
import java.io.File

class ArroyoReader(private val context: Context) {

    fun getMessageContent(conversationId: String, messageId: String): String? {
        val blob = getMessageBlob(conversationId, messageId) ?: return null
        return readChatMessageContent(blob)
    }

    fun getAllMessages(conversationId: String, after: Long = 0): Pair<List<ArroyoMessage>, Set<String>> {
        val messages = mutableListOf<ArroyoMessage>()
        val senderIds = hashSetOf<String>()
        SQLiteDatabase.openDatabase(
            File(context.filesDir, "../databases/arroyo.db").path,
            null,
            0
        ).use {
            it.rawQuery(
                "SELECT message_content,creation_timestamp,sender_id FROM conversation_message WHERE client_conversation_id='$conversationId' AND creation_timestamp>$after AND content_type=1 ORDER BY creation_timestamp ASC",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val content = cursor.getBlob(0)
                    val timestamp = cursor.getLong(1)
                    val senderId = cursor.getString(2)
                    val contentString = readChatMessageContent(content) ?: continue
                    messages.add(ArroyoMessage(contentString, timestamp, senderId))
                    senderIds.add(senderId)
                }
            }
        }

        return messages to senderIds
    }

    fun getKeyAndIv(conversationId: String, messageId: String): Pair<ByteArray, ByteArray>? {
        val blob = getMessageBlob(conversationId, messageId) ?: return null
        val key = followProtoString(blob, 4, 4, 3, 3, 5, 1, 1, 4, 1) ?: return null
        val iv = followProtoString(blob, 4, 4, 3, 3, 5, 1, 1, 4, 2) ?: return null
        return Base64.decode(key, Base64.DEFAULT) to Base64.decode(iv, Base64.DEFAULT)
    }

    fun getSnapData(conversationId: String, messageId: String): Triple<ByteArray, ByteArray, String>? {
        val blob = getMessageBlob(conversationId, messageId) ?: return null
        val key = followProto(blob, 4, 4, 11, 5, 1, 1, 19, 1) ?: return null
        val iv = followProto(blob, 4, 4, 11, 5, 1, 1, 19, 2) ?: return null
        val urlKey = followProtoString(blob, 4, 5, 1, 3, 2, 2) ?: return null
        return Triple(key, iv, urlKey)
    }

    private fun readChatMessageContent(blob: ByteArray): String? {
        return followProtoString(blob, 4, 4, 2, 1)
    }

    private fun getMessageBlob(conversationId: String, messageId: String, retries: Int = 10): ByteArray? {
        SQLiteDatabase.openDatabase(
            File(context.filesDir, "../databases/arroyo.db").path,
            null,
            0
        ).use {
            fun getBlob(): ByteArray? {
                it.rawQuery(
                    "SELECT message_content FROM conversation_message WHERE client_conversation_id='$conversationId' AND server_message_id=$messageId",
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) return cursor.getBlob(0)
                }
                return null
            }
            for (i in 1..retries) {
                val blob = getBlob()
                if (blob != null) {
                    return blob
                }
                if (retries > 0) {
                    Thread.sleep(500)
                }
            }
            log.debug("No message found in db after $retries retries, conversationId: $conversationId, messageId: $messageId")
        }

        return null
    }
}

fun followProtoString(data: ByteArray, vararg indices: Int): String? {
    val proto = followProto(data, *indices)
    return if (proto != null) String(proto) else null
}

fun followProto(data: ByteArray, vararg indices: Int): ByteArray? {
    var current = data
    indices.forEach { i ->
        val parts = ProtoReader(current).read()
        current = parts.firstOrNull { it.index == i }?.value ?: return null
    }

    return current
}