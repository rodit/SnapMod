package xyz.rodit.snapmod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.MediaStreamProvider;
import xyz.rodit.snapmod.mappings.RxObserver;
import xyz.rodit.snapmod.utils.StreamUtils;

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

        public MediaUriDownloader(File dest) {
            super(new UriListener(dest));
        }

        private static class UriListener implements ResolutionListener {

            private final File dest;

            public UriListener(File dest) {
                this.dest = dest;
            }

            @Override
            public void accept(Object result) {
                MediaStreamProvider streamProvider = MediaStreamProvider.wrap(result);
                StreamUtils.copyTo(streamProvider.getMediaStream(), dest);
            }
        }
    }

    public interface ResolutionListener {

        void accept(Object result);
    }
}
