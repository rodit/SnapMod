package xyz.rodit.snapmod.features.friendsfeed;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.ObjectProxy;
import xyz.rodit.snapmod.Shared;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.FriendsFeedRecordHolder;
import xyz.rodit.snapmod.mappings.FriendsFeedView;
import xyz.rodit.snapmod.mappings.SnapIterable;

public class FeedModifier extends Feature {

    private final List<FeedSorter> sorters = new ArrayList<>();

    public FeedModifier(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        sorters.add(new PinSorter(context));
    }

    @Override
    protected void performHooks() {
        // Hook feed list and apply re-ordering.
        FriendsFeedRecordHolder.constructors.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                FriendsFeedRecordHolder $this = FriendsFeedRecordHolder.wrap(param.thisObject);
                $this.getEmojis().getMap().put(Shared.PINNED_FRIENDMOJI_NAME, Shared.PINNED_FRIENDMOJI_EMOJI);

                List<FriendsFeedView> views = new ArrayList<>();
                for (Object record : (Iterable) $this.getRecords().instance) {
                    views.add(FriendsFeedView.wrap(record));
                }

                for (FeedSorter sorter : sorters) {
                    if (sorter.shouldApply()) {
                        views = sorter.sort(views);
                    }
                }

                List<Object> sorted = views.stream().map(v -> v.instance).collect(Collectors.toList());

                Object iterableProxy = Proxy.newProxyInstance(context.classLoader, new Class[]{SnapIterable.getMappedClass()}, new ObjectProxy(sorted));
                $this.setRecords(SnapIterable.wrap(iterableProxy));
            }
        });
    }
}
