package org.bytecodefx.testutils;

import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unchecked") // FIXME
public class ClassAssertionsTest {

    @Test
    public void assertion_pass_when_strict_subtype_is_correct() throws Exception {
        new ClassAssert(String.class).isStrictSubtypeOf(CharSequence.class);
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_subtype_is_same_as_asserted_type() throws Exception {
        new ClassAssert(String.class).isStrictSubtypeOf(String.class);
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_not_a_subtype_of_asserted_type() throws Exception {
        new ClassAssert(String.class).isStrictSubtypeOf(Appendable.class);
    }

    @Test
    public void assertion_pass_when_annotation_present() throws Exception {
        new ClassAssert(SomeAnnotatedClass.class).isAnnotatedBy(SomeAnnotation.class);
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_annotation_is_not_present() throws Exception {
        new ClassAssert(String.class).isAnnotatedBy(SomeAnnotation.class);
    }

    @Test(expected = AssertionError.class)
    public void assertion_fail_when_different_classloader() throws Exception {
        new ClassAssert(Short.class).hasSameClassLoader(this.getClass());
    }
    @Test
    public void assertion_pass_when_same_classloader() throws Exception {
        new ClassAssert(Short.class).hasSameClassLoader(Integer.class);
    }
    @Test
    public void assertion_pass_when_different_classloader() throws Exception {
        new ClassAssert(Double.class).hasDifferentClassLoader(this.getClass());
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_same_classloader() throws Exception {
        new ClassAssert(Double.class).hasDifferentClassLoader(Integer.class);
    }


    @Retention(RetentionPolicy.RUNTIME)
    @interface SomeAnnotation{}

    @SomeAnnotation
    class SomeAnnotatedClass {}

}
