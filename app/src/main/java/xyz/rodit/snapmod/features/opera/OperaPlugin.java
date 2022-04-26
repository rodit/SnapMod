package xyz.rodit.snapmod.features.opera;

public interface OperaPlugin {

    boolean isEnabled();

    boolean shouldOverride(String key);

    Object override(String key, Object value);
}