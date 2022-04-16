package xyz.rodit.snapmod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ObjectProxy implements InvocationHandler {

    private final Object object;

    public ObjectProxy(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        return method.invoke(object, args);
    }
}
