package xyz.rodit.snapmod.util

import xyz.rodit.snapmod.logging.log
import java.io.File
import java.io.IOException

class ConversationManager(rootDir: File, fileName: String) {

    private val conversations: MutableSet<String> = HashSet()
    private val file: File = File(rootDir, fileName)
    private var lastLoaded: Long = 0

    val enabled: Set<String>
        get() {
            if (file.lastModified() > lastLoaded) {
                load()
            }

            return conversations
        }

    fun enable(key: String?) {
        key?.let {
            if (!conversations.contains(it)) {
                conversations.add(it)
                save()
            }
        }
    }

    fun disable(key: String?) {
        if (conversations.remove(key)) {
            save()
        }
    }

    fun toggle(key: String?) {
        if (isEnabled(key)) {
            disable(key)
        } else {
            enable(key)
        }
    }

    fun isEnabled(key: String?): Boolean {
        return enabled.contains(key)
    }

    private fun load() {
        try {
            conversations.clear()
            if (file.exists()) {
                file.readText().split("\n").filter { it.isNotEmpty() }.forEach(conversations::add)
            }

            lastLoaded = System.currentTimeMillis()
        } catch (e: IOException) {
            log.error("Error loading conversation data.", e)
        }
    }

    private fun save() {
        try {
            file.writeText(conversations.joinToString("\n"))
            lastLoaded = System.currentTimeMillis()
        } catch (e: IOException) {
            log.error("Error saving conversation data.", e)
        }
    }
}