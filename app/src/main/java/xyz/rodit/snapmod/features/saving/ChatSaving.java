package xyz.rodit.snapmod.features.saving;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.UriResolverSubscriber;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ChatActionHelper;
import xyz.rodit.snapmod.mappings.ChatMediaHandler;
import xyz.rodit.snapmod.mappings.ChatModelAudioNote;
import xyz.rodit.snapmod.mappings.ChatModelBase;
import xyz.rodit.snapmod.mappings.ChatModelLiveSnap;
import xyz.rodit.snapmod.mappings.ChatModelSavedSnap;
import xyz.rodit.snapmod.mappings.LiveSnapMedia;
import xyz.rodit.snapmod.mappings.MediaStreamProvider;
import xyz.rodit.snapmod.mappings.RxObserver;
import xyz.rodit.snapmod.mappings.SaveToCameraRollActionHandler;
import xyz.rodit.snapmod.mappings.SaveType;
import xyz.rodit.snapmod.util.PathManager;
import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.client.FileClient;
import xyz.rodit.xposed.client.http.StreamProvider;
import xyz.rodit.xposed.client.http.StreamServer;
import xyz.rodit.xposed.client.http.streams.CachedStreamProvider;
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider;

public class ChatSaving extends Feature {

    private final Map<Integer, Object> chatMediaMap = new HashMap<>();

    private ChatMediaHandler chatMediaHandler;

    public ChatSaving(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Obtain chat media handler instance
        ChatMediaHandler.constructors.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                chatMediaHandler = ChatMediaHandler.wrap(param.thisObject);
            }
        });

        // Allow save action in chat long-press menu.
        ChatActionHelper.canSave.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    param.setResult(true);
                }
            }
        });

        // Allow save action in chat model.
        ChatModelBase.canSave.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    param.setResult(true);
                }
            }
        });

        // Override save type to gallery to allow saving any snaps.
        ChatModelBase.getSaveType.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("allow_save_snaps")
                        && (ChatModelLiveSnap.isInstance(param.thisObject)
                        || ChatModelAudioNote.isInstance(param.thisObject))) {
                    param.setResult(SaveType.SNAPCHAT_ALBUM().instance);
                }
            }
        });

        // Map live snap model hashCode to media object for download later.
        ChatModelLiveSnap.constructors.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    int hashCode = param.thisObject.hashCode();
                    chatMediaMap.put(hashCode, param.args[6]);
                }
            }
        });

        // Export non-savable media (live snaps and voice notes).
        SaveToCameraRollActionHandler.exportMedia.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    if (ChatModelLiveSnap.isInstance(param.args[1])) {
                        // Convert live snap to saved snap.
                        int hashCode = param.args[1].hashCode();
                        LiveSnapMedia media = LiveSnapMedia.wrap(chatMediaMap.get(hashCode));
                        ChatModelBase base = ChatModelBase.wrap(param.args[1]);
                        param.args[1] = new ChatModelSavedSnap(base.getContext(), base.getMessageData(), base.getSenderId(), Collections.emptyMap(), true, base.getReactionsViewModel(), true, 0, 0, media, null, base.getStatus(), true, true).instance;
                    } else if (ChatModelAudioNote.isInstance(param.args[1])) {
                        // Resolve audio uri and resolve through proxy of RxObserver.
                        // Note: the content resolver provided by appContext cannot open a stream from the uri.
                        ChatModelBase base = ChatModelBase.wrap(param.args[1]);
                        ChatModelAudioNote audio = ChatModelAudioNote.wrap(param.args[1]);
                        String dest = PathManager.getUri(context.config, PathManager.DOWNLOAD_AUDIO_NOTE, PathManager.createParams("id", base.getSenderId()), ".aac");
                        XposedBridge.log("Downloading audio note from " + audio.getUri() + " to " + dest + ".");
                        Object observerProxy = Proxy.newProxyInstance(context.classLoader, new Class[]{RxObserver.getMappedClass()}, new MediaUriDownloader(context.appContext, context.config, context.files, context.server, dest));
                        chatMediaHandler.resolve(audio.getUri(), Collections.emptySet(), true, Collections.emptySet()).subscribe(RxObserver.wrap(observerProxy));
                        param.setResult(null);
                    }
                }
            }
        });
    }

    private static class MediaUriDownloader extends UriResolverSubscriber {

        private static final String TAG = "MediaUriDownloader";

        public MediaUriDownloader(Context context, ConfigurationClient config, FileClient files, StreamServer server, String dest) {
            super(new UriListener(context, config, files, server, dest));
        }

        private static class UriListener implements ResolutionListener {

            private final Context context;
            private final ConfigurationClient config;
            private final FileClient files;
            private final StreamServer server;
            private final String dest;

            public UriListener(Context context, ConfigurationClient config, FileClient files, StreamServer server, String dest) {
                this.context = context;
                this.config = config;
                this.files = files;
                this.server = server;
                this.dest = dest;
            }

            @Override
            public void accept(Object result) {
                Log.d(TAG, "Accepted media stream provider: " + result);
                MediaStreamProvider streamProvider = MediaStreamProvider.wrap(result);
                String uuid = UUID.randomUUID().toString();
                StreamProvider provider = new CachedStreamProvider(new FileProxyStreamProvider(context, streamProvider::getMediaStream));
                try {
                    provider.provide();
                } catch (IOException e) {
                    Log.e(TAG, "Error pre-providing cached stream.", e);
                }

                server.mapStream(uuid, provider);
                files.download(config.getBoolean("use_android_download_manager", true), server.getRoot() + "/" + uuid, dest, "Audio Note", null);
            }
        }
    }
}
