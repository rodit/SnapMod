package xyz.rodit.snapmod.features.conversations;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.GallerySnapMedia;
import xyz.rodit.snapmod.mappings.LiveSnapMedia;
import xyz.rodit.snapmod.mappings.MediaBaseBase;
import xyz.rodit.snapmod.mappings.MediaContainer;
import xyz.rodit.snapmod.mappings.MediaType;
import xyz.rodit.snapmod.mappings.MessageSenderCrossroad;
import xyz.rodit.snapmod.mappings.ParameterPackage;
import xyz.rodit.snapmod.mappings.SerializableContent;

public class SnapOverrides extends Feature {

    public SnapOverrides(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Hook message sending to convert gallery to live snap.
        MessageSenderCrossroad.apply.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("override_snap")) {
                    MessageSenderCrossroad $this = MessageSenderCrossroad.wrap(param.thisObject);
                    MediaContainer container = $this.getPayload().getMedia();
                    if (SerializableContent.isInstance(container.instance)) {
                        SerializableContent content = SerializableContent.wrap(container.instance);
                        MediaBaseBase message = content.getMessage();
                        if (GallerySnapMedia.isInstance(message.instance)) {
                            String id = GallerySnapMedia.wrap(message.instance).getMedia().getId();
                            LiveSnapMedia snap = new LiveSnapMedia();
                            double timer = Double.parseDouble(context.config.getString("override_snap_timer", "0"));
                            snap.setMediaId(id);

                            if (context.config.getBoolean("enable_snap_type_override")) {
                                String overrideType = context.config.getString("snap_type_override", "IMAGE");
                                snap.setMediaType(MediaType.valueOf(overrideType));
                            } else {
                                snap.setMediaType(MediaType.IMAGE());
                            }

                            ParameterPackage paramPackage = new ParameterPackage(timer == 0d, timer, null, null, null, null, null, null, null, null, null, null, false);
                            snap.setParameterPackage(paramPackage);
                            content.setMessage(MediaBaseBase.wrap(snap.instance));
                        }
                    }
                }
            }
        });
    }
}
