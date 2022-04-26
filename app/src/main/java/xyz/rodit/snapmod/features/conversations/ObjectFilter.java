package xyz.rodit.snapmod.features.conversations;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import xyz.rodit.dexsearch.client.xposed.MappedObject;
import xyz.rodit.snapmod.features.Contextual;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.features.shared.Filter;

public class ObjectFilter<T> extends Contextual implements Filter {

    private final String configKey;
    private final Set<Object> filtered = new HashSet<>();

    public ObjectFilter(FeatureContext context, String configKey, T... filtered) {
        super(context);
        this.configKey = configKey;
        this.filtered.addAll(Stream.of(filtered).map(f -> f instanceof MappedObject ? ((MappedObject) f).instance : f).collect(Collectors.toList()));
    }

    @Override
    public boolean isEnabled() {
        return context.config.getBoolean(configKey);
    }

    @Override
    public boolean shouldFilter(Object object) {
        Object compare = object instanceof MappedObject ? ((MappedObject) object).instance : object;
        return filtered.contains(compare);
    }
}
