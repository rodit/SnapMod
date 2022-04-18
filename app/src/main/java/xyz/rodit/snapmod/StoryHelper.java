package xyz.rodit.snapmod;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.mappings.ContextSession;
import xyz.rodit.snapmod.mappings.ContextStoryKeys;
import xyz.rodit.snapmod.mappings.FriendStoryKeys;
import xyz.rodit.snapmod.mappings.OperaMediaInfo;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.snapmod.mappings.PlayableStorySnap;
import xyz.rodit.snapmod.mappings.SnapUsername;
import xyz.rodit.snapmod.mappings.StoryMetadata;

public class StoryHelper {

    public static StoryMedia getMediaInfo(ParamsMap metadata) {
        Map<?, ?> map = metadata.getMap();

        String username = "unknown";
        ContextSession session = ContextSession.wrap(map.get(ContextStoryKeys.getContextSession().instance));
        SnapUsername snapUsername = session.getInfo().getUsername();
        if (snapUsername.isNotNull()) {
            username = snapUsername.getDisplayString();
        } else {
            PlayableStorySnap storySnap = PlayableStorySnap.wrap(map.get(FriendStoryKeys.getPlayableSnapStoryRecord().instance));
            if (storySnap.isNotNull()) {
                username = storySnap.getDisplayName();
            }
        }

        Object imageMedia = map.get(StoryMetadata.getImageMediaInfo().instance);
        if (imageMedia != null) {
            return new StoryMedia(OperaMediaInfo.wrap(imageMedia), username, ".jpg");
        }

        Object videoMedia = map.get(StoryMetadata.getVideoMediaInfo().instance);
        if (videoMedia != null) {
            return new StoryMedia(OperaMediaInfo.wrap(videoMedia), username, ".mp4");
        }

        Object imageOverlayMedia = map.get(StoryMetadata.getOverlayImageMediaInfo().instance);
        if (imageOverlayMedia != null) {
            return new StoryMedia(OperaMediaInfo.wrap(imageOverlayMedia), username, ".jpg");
        }

        XposedBridge.log("Error getting media info for " + metadata + ".");
        return null;
    }

    public static class StoryMedia {

        public final OperaMediaInfo info;
        public final String username;
        public final String extension;

        public StoryMedia(OperaMediaInfo info, String username, String extension) {
            this.info = info;
            this.username = username;
            this.extension = extension;
        }
    }
}
