package xyz.rodit.snapmod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.MediaStreamProvider;
import xyz.rodit.snapmod.mappings.RxObserver;
import xyz.rodit.xposed.client.FileClient;
import xyz.rodit.xposed.client.http.StreamServer;
import xyz.rodit.xposed.client.http.streams.ProxyStreamProvider;

public class UriResolverSubscriber implements InvocationHandler {

    private final ResolutionListener listener;

    public UriResolverSubscriber(ResolutionListener listener) {
        this.listener = listener;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (method.getName().equals(RxObserver.accept.getDexName())) {
            if (listener != null) {
                listener.accept(args[0]);
            }
        } else if (method.getName().equals(RxObserver.error.getDexName())) {
            XposedBridge.log("Error while resolving uri.");
            XposedBridge.log((Throwable) args[0]);
        }

        return method.invoke(proxy, args);
    }

    public static class MediaUriDownloader extends UriResolverSubscriber {

        public MediaUriDownloader(FileClient files, StreamServer server, String dest) {
            super(new UriListener(files, server, dest));
        }

        private static class UriListener implements ResolutionListener {

            private final FileClient files;
            private final StreamServer server;
            private final String dest;

            public UriListener(FileClient files, StreamServer server, String dest) {
                this.files = files;
                this.server = server;
                this.dest = dest;
            }

            @Override
            public void accept(Object result) {
                MediaStreamProvider streamProvider = MediaStreamProvider.wrap(result);
                String uuid = UUID.randomUUID().toString();
                server.mapStream(uuid, new ProxyStreamProvider(streamProvider::getMediaStream));
                files.download(true, server.getRoot() + "/" + uuid, dest, "Audio Note", null);
            }
        }
    }

    public interface ResolutionListener {

        void accept(Object result);
    }
}
