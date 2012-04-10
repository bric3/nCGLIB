package org.bytecodefx.fx;

public interface ClassFXBuilder<T> {


    Class<? extends T> createClass();

    ClassFXBuilder<T> withInterfaces(Class<?>... interfaces);

    ClassFXBuilder<T> withClassLoader(ClassLoaderProvider classLoaderProvider);
}
