package xyz.rodit.snapmod.features.opera;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import xyz.rodit.snapmod.features.Contextual;
import xyz.rodit.snapmod.features.FeatureContext;

public class MenuModifier extends Contextual implements OperaPlugin {

    private static final String KEY_NAME = "action_menu_options";

    private final List<MenuPlugin> plugins = new ArrayList<>();

    public MenuModifier(FeatureContext context) {
        super(context);

        plugins.add(new SaveMenuOption(context));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean shouldOverride(String key) {
        return KEY_NAME.equals(key);
    }

    @Override
    public Object override(String key, Object value) {
        List newList = new ArrayList();
        newList.addAll(((List) value));
        newList.addAll(plugins.stream().filter(MenuPlugin::isEnabled).flatMap(p -> p.createActions().stream().map(a -> a.instance)).collect(Collectors.toList()));
        return newList;
    }
}
