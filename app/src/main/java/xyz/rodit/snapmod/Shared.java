package xyz.rodit.snapmod;

import android.os.Environment;

public class Shared {

    public static final String SNAP_PACKAGE = "com.snapchat.android";
    public static final String SNAPMOD_ROOT = Environment.getExternalStorageDirectory().getPath() + "/SnapMod";
    public static final String SNAPMOD_CONFIG = SNAPMOD_ROOT + "/config.xml";
    public static final String SNAPMOD_MAPPINGS_DIR = SNAPMOD_ROOT + "/mappings";
    public static final String SNAPMOD_MEDIA_DIR = SNAPMOD_ROOT + "/media";

    public static final String SNAPMOD_MEDIA_PREFIX = "SnapMod_";

    public static final String[] MONTHS = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "Unknown"};
}
