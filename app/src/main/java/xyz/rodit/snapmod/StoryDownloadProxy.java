package xyz.rodit.snapmod;

import android.content.Context;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.ContextClickHandler;
import xyz.rodit.snapmod.mappings.EncryptionAlgorithm;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.client.FileClient;
import xyz.rodit.xposed.client.http.StreamProvider;
import xyz.rodit.xposed.client.http.StreamServer;
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider;

public class StoryDownloadProxy implements InvocationHandler {

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

                String username = media.menuProperty.isNull() ? "unknown" : media.menuProperty.getFriendUsername();
                String dest = PathManager.getUri(config, PathManager.DOWNLOAD_STORY, PathManager.createParams("u", username), media.extension);
                files.download(config.getBoolean("use_android_download_manager", true), server.getRoot() + "/" + uuid, dest, username + "'s Story", null);
            } else {
                XposedBridge.log("Null media info for story download.");
            }
        }

        return null;
    }
}
