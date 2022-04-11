package xyz.rodit.snapmod;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.MediaStreamProvider;
import xyz.rodit.snapmod.mappings.RxObserver;
import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.client.FileClient;
import xyz.rodit.xposed.client.http.StreamProvider;
import xyz.rodit.xposed.client.http.StreamServer;
import xyz.rodit.xposed.client.http.streams.CachedStreamProvider;
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider;

public class UriResolverSubscriber implements InvocationHandler {

    private static final String TAG = "UriResolverSubscriber";

    private final ResolutionListener listener;

    public UriResolverSubscriber(ResolutionListener listener) {
        this.listener = listener;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals(RxObserver.accept.getDexName())) {
            Log.d(TAG, "Resolved content uri - notifying listener.");
            if (listener != null) {
                listener.accept(args[0]);
            }
        } else if (method.getName().equals(RxObserver.error.getDexName())) {
            XposedBridge.log("Error while resolving uri.");
            XposedBridge.log((Throwable) args[0]);
        }

        return null;
    }

    public static class MediaUriDownloader extends UriResolverSubscriber {

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

    public interface ResolutionListener {

        void accept(Object result);
    }
}
