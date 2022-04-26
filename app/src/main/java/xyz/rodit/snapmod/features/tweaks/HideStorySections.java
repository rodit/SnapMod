package xyz.rodit.snapmod.features.tweaks;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.DiscoverFeedObservableSection;
import xyz.rodit.snapmod.mappings.DiscoverViewBinder;

public class HideStorySections extends Feature {

    private final Set<String> hiddenStorySections = new HashSet<>();

    public HideStorySections(FeatureContext context) {
        super(context);
    }

    @Override
    protected void onConfigLoaded(boolean first) {
        hiddenStorySections.clear();
        String sectionList = context.config.getString("disable_story_sections", "[]");
        sectionList = sectionList.substring(1, sectionList.length() - 1);
        for (String section : sectionList.split(",")) {
            if (!TextUtils.isEmpty(section)) {
                hiddenStorySections.add(section.trim());
            }
        }
    }

    @Override
    protected void performHooks() {
        // Hide story sections.
        DiscoverViewBinder.setSections.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (hiddenStorySections.size() > 0) {
                    List sections = (List) param.args[0];
                    List filtered = new ArrayList();
                    for (Object section : sections) {
                        String name = DiscoverFeedObservableSection.wrap(section).getModel().getName();
                        if (name == null || !hiddenStorySections.contains(name)) {
                            filtered.add(section);
                        }
                    }

                    param.args[0] = filtered;
                }
            }
        });
    }
}
