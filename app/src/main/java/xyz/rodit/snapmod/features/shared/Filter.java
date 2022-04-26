package xyz.rodit.snapmod.features.shared;

public interface Filter {

    boolean isEnabled();

    boolean shouldFilter(Object object);
}
