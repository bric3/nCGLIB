package org.bytecodefx.testutils;

import org.fest.assertions.GenericAssert;

import static org.fest.assertions.Formatting.format;


public class ClassAssert<T> extends GenericAssert<ClassAssert<T>, Class<T>> {

    @SuppressWarnings("unchecked")
    public ClassAssert(Class<T> actualClass) {
        super((Class<ClassAssert<T>>) ClassAssert.class, actualClass);
    }

    public ClassAssert<T> isStrictSubtypeOf(Class<?> expectedSuperType) {
        if (actual != expectedSuperType && expectedSuperType.isAssignableFrom(actual)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("'<%s>' should be a subtype of : '<%s>'", actual.getName(), expectedSuperType.getName()));
    }

    public ClassAssert<T> hasSameClassLoader(Class<?> other) {
        if (actual.getClassLoader() == other.getClassLoader()) return this;
        failIfCustomMessageIsSet();
        throw failure(format("'<%s>' should have the same classloader as : '<%s>'", actual.getName(), other.getName()));
    }

    public ClassAssert<T> hasClassLoaderOfType(Class<? extends ClassLoader> classLoaderClass) {
        if (classLoaderClass.isAssignableFrom(actual.getClassLoader().getClass())) return this;
        failIfCustomMessageIsSet();
        throw failure(format("'<%s>' should have a classloader of type : '<%s>'",
                             actual.getName(),
                             classLoaderClass.getClass().getName()));
    }

    public ClassAssert<T> hasDifferentClassLoader(Class<?> other) {
        if (actual.getClassLoader() == other.getClassLoader()) return this;
        failIfCustomMessageIsSet();
        throw failure(format("'<%s>' should have a different classloader than : '<%s>'",
                             actual.getName(),
                             other.getName()));
    }
}
