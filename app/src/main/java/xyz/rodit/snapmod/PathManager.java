package xyz.rodit.snapmod;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.rodit.xposed.client.ConfigurationClient;

public class PathManager {

    public static final String DOWNLOAD_STORY = "story";
    public static final String DOWNLOAD_AUDIO_NOTE = "audio_note";
    public static final String DOWNLOAD_PROFILE = "profile";

    private static final Pattern PATTERN_PUBLIC_DIR = Pattern.compile("\\$(\\w*)");
    private static final Pattern PATTERN_PARAMETER = Pattern.compile("%(\\w)");

    private static final Map<String, String> publicDirs = new HashMap<>();
    private static final Map<String, String> defaultPaths = new HashMap<>();

    static {
        publicDirs.put("Movies", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath());
        publicDirs.put("Pictures", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
        publicDirs.put("Alarms", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).getPath());
        publicDirs.put("DCIM", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath());
        publicDirs.put("Music", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
        publicDirs.put("Ringtones", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getPath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            publicDirs.put("Recordings", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS).getPath());
            publicDirs.put("Screenshots", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).getPath());
        }

        defaultPaths.put(DOWNLOAD_STORY, "$Movies/SnapMod/%u_story_%t");
        defaultPaths.put(DOWNLOAD_AUDIO_NOTE, "$Movies/SnapMod/%id_audio_%t");
        defaultPaths.put(DOWNLOAD_PROFILE, "$Movies/SnapMod/%u_profile_%t");
    }

    private static Map<String, String> appendDefaultParamsMap(Map<String, String> paramsMap) {
        Map<String, String> appended = new HashMap<>(paramsMap);
        appended.put("t", String.valueOf(System.currentTimeMillis()));
        return appended;
    }

    public static Map<String, String> createParams(String... params) {
        Map<String, String> paramsMap = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            paramsMap.put(params[i], params[i + 1]);
        }

        return paramsMap;
    }

    public static File getPath(ConfigurationClient config, String downloadType, Map<String, String> paramsMap, String extension) {
        Map<String, String> params = appendDefaultParamsMap(paramsMap);
        String path = config.getString("download_path_" + downloadType, defaultPaths.get(downloadType));
        Matcher matcher = PATTERN_PUBLIC_DIR.matcher(path);
        if (matcher.find()) {
            String replacement = publicDirs.get(matcher.group(1));
            if (replacement != null) {
                path = matcher.replaceFirst(replacement);
            }
        }

        matcher = PATTERN_PARAMETER.matcher(path);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = params.get(matcher.group(1));
            if (replacement != null) {
                matcher.appendReplacement(buffer, replacement);
            }
        }

        matcher.appendTail(buffer);
        return new File(buffer + extension);
    }

    public static String getUri(ConfigurationClient config, String downloadType, Map<String, String> paramsMap, String extension) {
        return Uri.fromFile(getPath(config, downloadType, paramsMap, extension)).toString();
    }
}
