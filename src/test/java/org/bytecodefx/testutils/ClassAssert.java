package org.bytecodefx.testutils;

import org.fest.assertions.GenericAssert;

import java.lang.annotation.Annotation;

import static org.fest.assertions.Formatting.format;


public class ClassAssert<T extends Class<T>> extends GenericAssert<ClassAssert, Class<T>> {

    //@SuppressWarnings("unchecked")
    public ClassAssert(Class<T> actualClass) {
        super(ClassAssert.class, actualClass);
    }

    public ClassAssert<T> isStrictSubtypeOf(Class<?> expectedSuperType) {
        if (actual != expectedSuperType && expectedSuperType.isAssignableFrom(actual)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("<%s> is not a subtype of : <%s>", actual.getName(), expectedSuperType.getName()));
    }

    public ClassAssert<T> hasSameClassLoader(Class<?> other) {
        if (actual.getClassLoader() == other.getClassLoader()) return this;
        failIfCustomMessageIsSet();
        throw failure(format("<%s loaded by '%s'> should have the same classloader as : <%s loaded by '%s'>",
                             actual.getName(),
                             actual.getClassLoader(),
                             other.getName(),
                             other.getClassLoader()));
    }

    public ClassAssert<T> hasClassLoaderOfType(Class<? extends ClassLoader> classLoaderClass) {
        if (classLoaderClass.isAssignableFrom(actual.getClassLoader().getClass())) return this;
        failIfCustomMessageIsSet();
        throw failure(format("<%s> should have a classloader of type : <%s>",
                             actual.getName(),
                             classLoaderClass.getClass().getName()));
    }

    public ClassAssert<T> hasDifferentClassLoader(Class<?> other) {
        if (actual.getClassLoader() != other.getClassLoader()) return this;
        failIfCustomMessageIsSet();
        throw failure(format("<%s loaded by '%s'> should have a different classloader than : <%s loaded by '%s'>",
                             actual.getName(),
                             actual.getClassLoader(),
                             other.getName(),
                             other.getClassLoader()));
    }

    public ClassAssert<T> isAnnotatedBy(Class<? extends Annotation> annotation) {
        if (actual.isAnnotationPresent(annotation)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("<%s> should be annotated by : <%s>",
                             actual.getName(),
                             annotation.getName()));
    }

    public ClassAssert<T> has(ClassMemberMatcher classMember) {
        if(classMember.isDeclaredIn(actual)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("<%s> should declare a %s",
                             actual.getName(),
                             classMember));
    }
}