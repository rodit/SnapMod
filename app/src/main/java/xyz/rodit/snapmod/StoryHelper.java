package xyz.rodit.snapmod;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.OperaMediaInfo;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.snapmod.mappings.StoryMetadata;

public class StoryHelper {

    public static final String REPORT_EVENT_NAME = "IN_APP_REPORT";
    public static final String SAVE_EVENT_NAME = "SAVE";

    public static OperaMediaInfo getMediaInfo(ParamsMap metadata) {
        Map<?, ?> map = metadata.getMap();
        Object imageMedia = map.get(StoryMetadata.getImageMediaInfo());
        if (imageMedia != null) {
            return OperaMediaInfo.wrap(imageMedia);
        }

        Object videoMedia = map.get(StoryMetadata.getVideoMediaInfo());
        if (videoMedia != null) {
            return OperaMediaInfo.wrap(videoMedia);
        }

        Object imageOverlayMedia = map.get(StoryMetadata.getOverlayImageMediaInfo());
        if (imageOverlayMedia != null) {
            return OperaMediaInfo.wrap(imageOverlayMedia);
        }

        XposedBridge.log("Error getting media info for " + metadata + ".");
        return null;
    }
}
