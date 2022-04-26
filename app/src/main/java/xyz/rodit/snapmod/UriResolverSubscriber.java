package xyz.rodit.snapmod;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.RxObserver;

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

    public interface ResolutionListener {

        void accept(Object result);
    }
}
