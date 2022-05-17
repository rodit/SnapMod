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
        return followProtoString(blob, 4, 4, 2, 1)
    }

    fun getKeyAndIv(conversationId: String, messageId: String): Pair<ByteArray, ByteArray>? {
        val blob = getMessageBlob(conversationId, messageId) ?: return null
        val key = followProtoString(blob, 4, 4, 3, 3, 5, 1, 1, 4, 1) ?: return null
        val iv = followProtoString(blob, 4, 4, 3, 3, 5, 1, 1, 4, 2) ?: return null
        return Base64.decode(key, Base64.DEFAULT) to Base64.decode(iv, Base64.DEFAULT)
    }

    fun getSnapKeyAndIv(conversationId: String, messageId: String): Pair<ByteArray, ByteArray>? {
        val blob = getMessageBlob(conversationId, messageId) ?: return null
        val key = followProto(blob, 4, 4, 11, 5, 1, 1, 19, 1) ?: return null
        val iv = followProto(blob, 4, 4, 11, 5, 1, 1, 19, 2) ?: return null
        return key to iv
    }

    private fun followProtoString(data: ByteArray, vararg indices: Int): String? {
        val proto = followProto(data, *indices)
        return if (proto != null) String(proto) else null
    }

    private fun followProto(data: ByteArray, vararg indices: Int): ByteArray? {
        var current = data
        indices.forEach { i ->
            val parts = ProtoReader(current).read()
            current = parts.firstOrNull { it.index == i }?.value ?: return null
        }

        return current
    }

    private fun getMessageBlob(conversationId: String, messageId: String): ByteArray? {
        SQLiteDatabase.openDatabase(
            File(context.filesDir, "../databases/arroyo.db").path,
            null,
            0
        ).use {
            it.rawQuery(
                "SELECT message_content FROM conversation_message WHERE client_conversation_id='$conversationId' AND server_message_id=$messageId",
                null
            ).use { cursor ->
                if (cursor.moveToFirst()) return cursor.getBlob(0)
                else log.debug("No result in db.")
            }
        }

        return null
    }
}