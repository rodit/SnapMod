package xyz.rodit.snapmod.features.chatmenu;

import static xyz.rodit.snapmod.features.chatmenu.ChatMenuModifier.EVENT_DELIMITER;
import static xyz.rodit.snapmod.features.chatmenu.ChatMenuModifier.EVENT_PREFIX;

import xyz.rodit.snapmod.features.Contextual;
import xyz.rodit.snapmod.features.FeatureContext;

public abstract class MenuPlugin extends Contextual {

    protected final String name;

    public MenuPlugin(FeatureContext context, String name) {
        super(context);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected abstract  boolean shouldCreate();

    protected abstract Object createModel(String key);

    protected abstract void handleEvent(String data);

    protected static String createEventData(String pluginName, String key) {
        return EVENT_PREFIX + EVENT_DELIMITER + pluginName + EVENT_DELIMITER + key;
    }
}