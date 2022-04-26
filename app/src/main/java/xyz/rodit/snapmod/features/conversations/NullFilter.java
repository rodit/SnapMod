package xyz.rodit.snapmod.features.conversations;

import xyz.rodit.snapmod.features.FeatureContext;

public class NullFilter extends ObjectFilter<Object> {

    public NullFilter(FeatureContext context, String configKey) {
        super(context, configKey, (Object) null);
    }

    @Override
    public boolean shouldFilter(Object object) {
        return true;
    }
}
