package xyz.rodit.snapmod.features.saving;

import android.content.Context;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.util.PathManager;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ContextActionMenuModel;
import xyz.rodit.snapmod.mappings.ContextClickHandler;
import xyz.rodit.snapmod.mappings.EncryptionAlgorithm;
import xyz.rodit.snapmod.mappings.OperaContextAction;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.client.FileClient;
import xyz.rodit.xposed.client.http.StreamProvider;
import xyz.rodit.xposed.client.http.StreamServer;
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider;

public class StoriesSaving extends Feature {

    public StoriesSaving(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Override save story click.
        ContextActionMenuModel.constructors.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                ContextActionMenuModel model = ContextActionMenuModel.wrap(param.thisObject);
                if (context.config.getBoolean("allow_download_stories")
                        && model.getAction().instance == OperaContextAction.SAVE().instance) {
                    Object clickProxy = Proxy.newProxyInstance(context.classLoader, new Class[]{ContextClickHandler.getMappedClass()}, new StoryDownloadProxy(context.appContext, context.config, context.server, context.files));
                    model.setOnClick(ContextClickHandler.wrap(clickProxy));
                }
            }
        });
    }

    public static class StoryDownloadProxy implements InvocationHandler {

        private final Context context;
        private final ConfigurationClient config;
        private final StreamServer server;
        private final FileClient files;

        public StoryDownloadProxy(Context context, ConfigurationClient config, StreamServer server, FileClient files) {
            this.context = context;
            this.config = config;
            this.server = server;
            this.files = files;
        }

        @Override
        public Object invoke(Object thisObject, Method method, Object[] args) {
            if (method.getName().equals(ContextClickHandler.invoke.getDexName())) {
                ParamsMap map = ParamsMap.wrap(args[0]);
                StoryHelper.StoryMedia media = StoryHelper.getMediaInfo(map);
                if (media != null && media.info.isNotNull()) {
                    StreamProvider provider = new FileProxyStreamProvider(context, () -> {
                        try {
                            InputStream stream = new URL(media.info.getUri()).openStream();
                            EncryptionAlgorithm enc = media.info.getEncryption();
                            if (enc.isNotNull()) {
                                stream = enc.decryptStream(stream);
                                XposedBridge.log("Stream was encrypted.");
                            }

                            XposedBridge.log("Media stream opened.");
                            return stream;
                        } catch (Exception e) {
                            XposedBridge.log("Error opening stream.");
                            XposedBridge.log(e);
                        }

                        return null;
                    });

                    String uuid = UUID.randomUUID().toString();
                    server.mapStream(uuid, provider);

                    String dest = PathManager.getUri(config, PathManager.DOWNLOAD_STORY, PathManager.createParams("u", media.username), media.extension);
                    files.download(config.getBoolean("use_android_download_manager", true), server.getRoot() + "/" + uuid, dest, media.username + "'s Story", null);
                } else {
                    XposedBridge.log("Null media info for story download.");
                }
            }

            return null;
        }
    }

}
