package xyz.rodit.snapmod.features.opera;

import java.util.Collection;

import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel;

public interface MenuPlugin {

    boolean isEnabled();

    Collection<OperaActionMenuOptionViewModel> createActions();
}
