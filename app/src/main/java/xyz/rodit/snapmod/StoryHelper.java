package xyz.rodit.snapmod;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel;
import xyz.rodit.snapmod.mappings.OperaContextActions;
import xyz.rodit.snapmod.mappings.OperaMediaInfo;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.snapmod.mappings.StoryMetadata;

public class StoryHelper {

    private static boolean swappedStoryActions;

    public static void swapReportAndSave() {
        if (!swappedStoryActions) {
            OperaActionMenuOptionViewModel reportAction = OperaContextActions.getReportAction();
            OperaActionMenuOptionViewModel saveAction = OperaContextActions.getSaveAction();
            reportAction.setIconResource(saveAction.getIconResource());
            reportAction.setTextResource(saveAction.getTextResource());
            reportAction.setTextColorResource(saveAction.getTextColorResource());
            reportAction.setIsLoading(false);
            swappedStoryActions = true;
            XposedBridge.log("Replaced report with save button.");
        }
    }

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
