package xyz.rodit.snapmod.features;

public abstract class Feature extends Contextual {

    public Feature(FeatureContext context) {
        super(context);
    }

    protected void init() {

    }

    protected void onConfigLoaded(boolean first) {

    }

    protected abstract void performHooks();
}
