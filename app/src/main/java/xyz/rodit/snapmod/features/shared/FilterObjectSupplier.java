package xyz.rodit.snapmod.features.shared;

import de.robv.android.xposed.XC_MethodHook;

@FunctionalInterface
public interface FilterObjectSupplier {

    Object get(XC_MethodHook.MethodHookParam param);
}
