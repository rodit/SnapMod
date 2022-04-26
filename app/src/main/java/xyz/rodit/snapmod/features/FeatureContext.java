package xyz.rodit.snapmod.features;

import android.app.Activity;
import android.content.Context;

import xyz.rodit.snapmod.util.ConversationManager;
import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.client.FileClient;
import xyz.rodit.xposed.client.http.StreamServer;

public class FeatureContext {

    private static final String PINNED_CONVERSATIONS_FILE = "pinned.list";
    private static final String STEALTH_CONVERSATIONS_FILE = "stealth.list";

    public final Context appContext;
    public final ClassLoader classLoader;
    public final ConfigurationClient config;
    public final FileClient files;
    public final StreamServer server;

    public final ConversationManager pinned;
    public final ConversationManager stealth;

    public Activity activity;

    public FeatureContext(Context appContext, ClassLoader classLoader, ConfigurationClient config, FileClient files, StreamServer server) {
        this.appContext = appContext;
        this.classLoader = classLoader;
        this.config = config;
        this.files = files;
        this.server = server;

        this.pinned = new ConversationManager(appContext.getFilesDir(), PINNED_CONVERSATIONS_FILE);
        this.stealth = new ConversationManager(appContext.getFilesDir(), STEALTH_CONVERSATIONS_FILE);
    }
}
