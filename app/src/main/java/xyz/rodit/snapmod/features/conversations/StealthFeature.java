package xyz.rodit.snapmod.features.conversations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.dexsearch.client.ClassMapping;
import xyz.rodit.dexsearch.client.xposed.MappedObject;
import xyz.rodit.dexsearch.client.xposed.MethodRef;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.features.shared.Filter;
import xyz.rodit.snapmod.features.shared.FilterObjectSupplier;

public abstract class StealthFeature extends Feature {

    private final Map<String, List<Filter>> filters = new HashMap<>();
    private final Map<String, FilterObjectSupplier> suppliers = new HashMap<>();
    private final Map<String, ConversationIdSupplier> conversationIdSuppliers = new HashMap<>();

    private String className;

    public StealthFeature(FeatureContext context) {
        super(context);
    }

    public void setClass(ClassMapping mapping) {
        this.className = mapping.getNiceClassName();
    }

    public void putFilters(MethodRef method, FilterObjectSupplier supplier, ConversationIdSupplier conversationIdSupplier, Filter... filters) {
        putFilters(method.getName(), supplier, conversationIdSupplier, filters);
    }

    public void putFilters(String methodName, FilterObjectSupplier supplier, ConversationIdSupplier conversationIdSupplier, Filter... filters) {
        this.filters.computeIfAbsent(methodName, t -> new ArrayList<>()).addAll(Arrays.asList(filters));
        this.suppliers.put(methodName, supplier);
        this.conversationIdSuppliers.put(methodName, conversationIdSupplier);
    }

    protected void onPostHook(XC_MethodHook.MethodHookParam param) {

    }

    @Override
    protected void performHooks() {
        for (String methodName : filters.keySet()) {
            MappedObject.hook(className, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    FilterObjectSupplier supplier = suppliers.get(methodName);
                    ConversationIdSupplier conversationIdSupplier = conversationIdSuppliers.get(methodName);
                    Object obj = supplier.get(param);
                    String id = conversationIdSupplier.getConversationId(param);
                    boolean stealth = context.stealth.isEnabled(id);
                    for (Filter filter : filters.get(methodName)) {
                        if ((filter.isEnabled() || stealth) && filter.shouldFilter(obj)) {
                            param.setResult(null);
                            onPostHook(param);
                            return;
                        }
                    }
                }
            });
        }
    }

    @FunctionalInterface
    public interface ConversationIdSupplier {

        String getConversationId(XC_MethodHook.MethodHookParam param);
    }
}
