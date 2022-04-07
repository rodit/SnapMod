package xyz.rodit.snapmod;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.OperaContextMenuProperty;
import xyz.rodit.snapmod.mappings.OperaMediaInfo;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.snapmod.mappings.StoryMetadata;

public class StoryHelper {

    public static StoryMedia getMediaInfo(ParamsMap metadata) {
        Map<?, ?> map = metadata.getMap();
        OperaContextMenuProperty menuProperty = OperaContextMenuProperty.wrap(map.get(StoryMetadata.getMenuProperty().instance));
        Object imageMedia = map.get(StoryMetadata.getImageMediaInfo().instance);
        if (imageMedia != null) {
            return new StoryMedia(OperaMediaInfo.wrap(imageMedia), menuProperty, ".jpg");
        }

        Object videoMedia = map.get(StoryMetadata.getVideoMediaInfo().instance);
        if (videoMedia != null) {
            return new StoryMedia(OperaMediaInfo.wrap(videoMedia), menuProperty, ".mp4");
        }

        Object imageOverlayMedia = map.get(StoryMetadata.getOverlayImageMediaInfo().instance);
        if (imageOverlayMedia != null) {
            return new StoryMedia(OperaMediaInfo.wrap(imageOverlayMedia), menuProperty, ".jpg");
        }

        XposedBridge.log("Error getting media info for " + metadata + ".");
        return null;
    }

    public static class StoryMedia {

        public final OperaMediaInfo info;
        public final OperaContextMenuProperty menuProperty;
        public final String extension;

        public StoryMedia(OperaMediaInfo info, OperaContextMenuProperty menuProperty, String extension) {
            this.info = info;
            this.menuProperty = menuProperty;
            this.extension = extension;
        }
    }
}
