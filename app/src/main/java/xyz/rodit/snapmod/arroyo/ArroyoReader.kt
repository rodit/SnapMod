package xyz.rodit.snapmod.arroyo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import xyz.rodit.snapmod.logging.log
import xyz.rodit.snapmod.util.ProtoReader
import java.io.File

class ArroyoReader(private val context: Context) {

    fun getMessageContent(conversationId: String, messageId: String): String? {
        try {
            val db: SQLiteDatabase =
                SQLiteDatabase.openDatabase(
                    File(context.filesDir, "../databases/arroyo.db").path,
                    null,
                    0
                )
            val cursor =
                db.rawQuery(
                    "SELECT message_content FROM conversation_message WHERE client_conversation_id='$conversationId' AND server_message_id=$messageId",
                    null
                )
            if (cursor.moveToFirst()) {
                var parts = ProtoReader(cursor.getBlob(0)).read()
                var container = parts.firstOrNull { it.index == 4 }?.value ?: return null
                parts = ProtoReader(container).read()
                container = parts.firstOrNull { it.index == 4 }?.value ?: return null
                parts = ProtoReader(container).read()
                container = parts.firstOrNull { it.index == 2 }?.value ?: return null
                parts = ProtoReader(container).read()
                container = parts.firstOrNull { it.index == 1 }?.value ?: return null
                return String(container)
            } else {
                log.debug("No result in db.")
            }
        } catch (e: Exception) {
            log.error("Error reading arroyo db", e)
        }

        return null
    }
}