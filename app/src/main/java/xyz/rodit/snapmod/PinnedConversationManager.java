package xyz.rodit.snapmod;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.xposed.utils.StreamUtils;

public class PinnedConversationManager {

    private final Set<String> pinned = new HashSet<>();
    private final File file;

    private long lastLoaded;

    public PinnedConversationManager(File rootDir) {
        file = new File(rootDir, Shared.PINNED_FILE_NAME);
    }

    public Set<String> getPinned() {
        if (file.lastModified() > lastLoaded) {
            load();
        }

        return pinned;
    }

    public void pin(String key) {
        if (!pinned.contains(key)) {
            pinned.add(key);
            save();
        }
    }

    public void unpin(String key) {
        if (pinned.remove(key)) {
            save();
        }
    }

    public boolean isPinned(String key) {
        return getPinned().contains(key);
    }

    private void load() {
        try {
            pinned.clear();
            if (file.exists()) {
                String[] keys = StreamUtils.readFile(file).split("\n");
                for (String key : keys) {
                    if (!TextUtils.isEmpty(key)) {
                        pinned.add(key);
                    }
                }
            }

            lastLoaded = System.currentTimeMillis();
        } catch (IOException e) {
            XposedBridge.log("Error loading pinned conversations.");
            XposedBridge.log(e);
        }
    }

    private void save() {
        try {
            StringBuilder keys = new StringBuilder();
            for (String key : pinned) {
                keys.append(key)
                        .append('\n');
            }
            StreamUtils.writeFile(file, keys.toString());
            lastLoaded = System.currentTimeMillis();
        } catch (IOException e) {
            XposedBridge.log("Error saving pinned conversations.");
            XposedBridge.log(e);
        }
    }
}
