package xyz.rodit.snapmod.features.chatmenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.FriendChatActionHandler;
import xyz.rodit.snapmod.mappings.FriendChatActionMenuBuilder;
import xyz.rodit.snapmod.mappings.RxSingleton;
import xyz.rodit.snapmod.mappings.SendChatAction;

public class ChatMenuModifier extends Feature {

    protected static final String EVENT_PREFIX = "CUSTOM_ACTION";
    protected static final String EVENT_DELIMITER = "\0:\0";

    private final Map<String, MenuPlugin> plugins = new HashMap<>();

    public ChatMenuModifier(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        registerPlugin(new PinOption(context));
        registerPlugin(new StealthOption(context));
    }

    private void registerPlugin(MenuPlugin plugin) {
        plugins.put(plugin.getName(), plugin);
    }

    @Override
    protected void performHooks() {
        // Add plugin actions to chat action menu.
        FriendChatActionMenuBuilder.build.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                FriendChatActionMenuBuilder $this = FriendChatActionMenuBuilder.wrap(param.thisObject);
                String key = $this.getFeedInfoHolder().getInfo().getKey();
                List options = (List) RxSingleton.wrap(param.getResult()).getValue();

                for (MenuPlugin plugin : plugins.values()) {
                    if (plugin.shouldCreate()) {
                        options.add(plugin.createModel(key));
                    }
                }
            }
        });

        // Override plugin action events.
        FriendChatActionHandler.handle.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!SendChatAction.isInstance(param.args[0])) {
                    return;
                }

                SendChatAction action = SendChatAction.wrap(param.args[0]);
                String data = action.getDataModel().getKey();
                if (data.startsWith(EVENT_PREFIX)) {
                    String[] parts = data.split(EVENT_DELIMITER);
                    if (parts[0].equals(EVENT_PREFIX) && parts.length == 3) {
                        String pluginName = parts[1];
                        if (!plugins.containsKey(pluginName)) {
                            return;
                        }

                        MenuPlugin plugin = plugins.get(pluginName);
                        plugin.handleEvent(parts[2]);
                        param.setResult(null);
                    }
                }
            }
        });
    }
}
