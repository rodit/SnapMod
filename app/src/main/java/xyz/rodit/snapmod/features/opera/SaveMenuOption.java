package xyz.rodit.snapmod.features.opera;

import java.util.Collection;
import java.util.Collections;

import xyz.rodit.snapmod.features.Contextual;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel;
import xyz.rodit.snapmod.mappings.OperaContextActions;

public class SaveMenuOption extends Contextual implements MenuPlugin {

    public SaveMenuOption(FeatureContext context) {
        super(context);
    }

    @Override
    public boolean isEnabled() {
        return context.config.getBoolean("allow_download_stories");
    }

    @Override
    public Collection<OperaActionMenuOptionViewModel> createActions() {
        return Collections.singleton(OperaContextActions.getSaveAction());
    }
}
