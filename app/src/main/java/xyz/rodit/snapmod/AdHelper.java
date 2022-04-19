package xyz.rodit.snapmod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdHelper {

    private static final Set<String> AD_TWEAK_NAMES = new HashSet<>(Arrays.asList("ENABLE_APP_INSTALL_AD_TYPE",
            "ENABLE_REMOTE_WEBPAGE_AD_TYPE",
            "ENABLE_LONG_FORM_VIDEO_AD_TYPE",
            "ENABLE_THREE_V_AD_TYPE",
            "ENABLE_DEEP_LINK_AD_TYPE",
            "ENABLE_STORY_AD_TYPE",
            "ENABLE_COLLECTION_AD_TYPE",
            "ENABLE_AD_TO_LENS_AD_TYPE",
            "ENABLE_AD_TO_CALL_AD_TYPE",
            "ENABLE_AD_TO_MESSAGE_AD_TYPE",
            "ENABLE_AD_TO_PLACE_AD_TYPE",
            "ENABLE_LEAD_GENERATION_AD_TYPE",
            "ENABLE_SHOWCASE_AD_TYPE",
            "ENABLE_COLLECTION_SHOWCASE_AD",
            "ENABLE_DISCOVER_AD",
            "ENABLE_USER_STORY_AD",
            "ENABLE_ADS_IN_SHOWS",
            "ENABLE_CONTENT_INTERSTITIAL_ADS",
            "ENABLE_COGNAC_AD"));

    public static boolean isAddTweak(String name) {
        return AD_TWEAK_NAMES.contains(name);
    }
}
