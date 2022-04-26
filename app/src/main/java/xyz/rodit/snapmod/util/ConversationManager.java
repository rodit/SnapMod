package xyz.rodit.snapmod.util;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.xposed.utils.StreamUtils;

public class ConversationManager {

    private final Set<String> conversations = new HashSet<>();
    private final File file;

    private long lastLoaded;

    public ConversationManager(File rootDir, String fileName) {
        file = new File(rootDir, fileName);
    }

    public Set<String> getEnabled() {
        if (file.lastModified() > lastLoaded) {
            load();
        }

        return conversations;
    }

    public void enable(String key) {
        if (!conversations.contains(key)) {
            conversations.add(key);
            save();
        }
    }

    public void disable(String key) {
        if (conversations.remove(key)) {
            save();
        }
    }

    public void toggle(String key) {
        if (isEnabled(key)) {
            disable(key);
        } else {
            enable(key);
        }
    }

    public boolean isEnabled(String key) {
        return getEnabled().contains(key);
    }

    private void load() {
        try {
            conversations.clear();
            if (file.exists()) {
                String[] keys = StreamUtils.readFile(file).split("\n");
                for (String key : keys) {
                    if (!TextUtils.isEmpty(key)) {
                        conversations.add(key);
                    }
                }
            }

            lastLoaded = System.currentTimeMillis();
        } catch (IOException e) {
            XposedBridge.log("Error loading conversation data.");
            XposedBridge.log(e);
        }
    }

    private void save() {
        try {
            StringBuilder keys = new StringBuilder();
            for (String key : conversations) {
                keys.append(key)
                        .append('\n');
            }
            StreamUtils.writeFile(file, keys.toString());
            lastLoaded = System.currentTimeMillis();
        } catch (IOException e) {
            XposedBridge.log("Error saving conversation data.");
            XposedBridge.log(e);
        }
    }
}
