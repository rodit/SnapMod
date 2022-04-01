package xyz.rodit.snapmod;

import android.os.Environment;

public class Shared {

    public static final String SNAPCHAT_PACKAGE = "com.snapchat.android";

    public static final String SNAPMOD_PACKAGE_NAME = "xyz.rodit.snapmod";
    public static final String SNAPMOD_CONFIG_ACTION = SNAPMOD_PACKAGE_NAME + ".ACTION_CONFIG";
    public static final String SNAPMOD_FILES_ACTION = SNAPMOD_PACKAGE_NAME + ".ACTION_FILES";

    public static final String CONTEXT_HOOK_CLASS = "com.snap.mushroom.MainActivity";
    public static final String CONTEXT_HOOK_METHOD = "attachBaseContext";

    public static final String SNAPMOD_MEDIA_PREFIX = "SnapMod_";

    public static final String[] MONTHS = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "Unknown"};
}
