package org.bytecodefx.fx.internal;

import org.bytecodefx.fx.ClassFXBuilder;
import org.bytecodefx.fx.ClassLoaderProvider;

/**
 *
 */
public class ClassFXBuilderImpl<T> implements ClassFXBuilder<T> {

    private final Class<T> theClassToEnhance;

    public ClassFXBuilderImpl(Class<T> theClassToEnhance) {
        this.theClassToEnhance = theClassToEnhance;
    }

    public Class<? extends T> createClass() {
        return null;
    }

    public ClassFXBuilder<T> withInterfaces(Class<?>... interfaces) {
        return this;
    }

    public ClassFXBuilder<T> withClassLoader(ClassLoaderProvider classLoaderProvider) {
        return this;
    }

    public T newInstance() {
        return null;
    }
}
