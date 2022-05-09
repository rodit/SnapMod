package xyz.rodit.snapmod.features.messagemenu

import android.app.AlertDialog
import android.text.InputType
import android.view.View
import android.widget.EditText
import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.after

class AudioSpeedModifier(context: FeatureContext) : MenuPlugin(context) {

    private val messageToSpeed = mutableMapOf<String, Double>()
    private val messageToSession = mutableMapOf<String, AudioNotePlaySession>()

    override fun isEnabled(): Boolean {
        return true
    }

    override fun createOptions(model: ChatModelBase): List<ChatMenuItem> {
        return if (ChatModelAudioNote.isInstance(model.instance)) listOf(
            ChatMenuItem(
                null,
                CustomResources.string.chat_action_playback_speed,
                createOnClick(model.messageData.arroyoMessageId)
            )
        ) else emptyList()
    }

    private fun createOnClick(messageId: String): View.OnClickListener {
        return View.OnClickListener {
            val initSpeed = messageToSpeed.computeIfAbsent(messageId) { 1.0 }
            context.activity?.runOnUiThread {
                val speedInput = EditText(context.activity)
                speedInput.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                speedInput.setText(initSpeed.toString())
                AlertDialog.Builder(context.activity)
                    .setTitle("Set Playback Speed")
                    .setView(speedInput)
                    .setPositiveButton("OK") { _, _ ->
                        val speed = speedInput.text.toString().toDouble()
                        messageToSpeed[messageId] = speed
                        applyPlaybackSpeed(messageId)
                    }
                    .show()
            }
        }
    }

    private fun applyPlaybackSpeed(messageId: String) {
        messageToSession[messageId]?.let {
            it.playbackSpeed =
                messageToSpeed.getOrDefault(
                    messageId,
                    context.config.getDouble("audio_playback_speed", 1.0)
                )
        }
    }

    override fun performHooks() {
        AudioNoteViewBindingDelegate.bindSession.after {
            if (!ChatModelAudioNote.isInstance(it.args[0])) return@after

            val delegate = AudioNoteViewBindingDelegate.wrap(it.thisObject)
            val model = ChatModelBase.wrap(it.args[0])
            val messageId = model.messageData.arroyoMessageId
            messageToSession[messageId] = delegate.session
            applyPlaybackSpeed(messageId)
        }
    }
}