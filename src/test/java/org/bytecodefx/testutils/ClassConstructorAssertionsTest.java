package org.bytecodefx.testutils;

import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.bytecodefx.testutils.ClassMemberMatcher.arg;
import static org.bytecodefx.testutils.ClassMemberMatcher.declaredConstructor;

@SuppressWarnings("unchecked") // FIXME
public class ClassConstructorAssertionsTest {
    @Test
    public void assertion_pass_when_constructor_with_expected_name_is_found() throws Exception {
        new ClassAssert(Double.class).has(declaredConstructor());
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_constructor_cannot_be_found_is_missing() throws Exception {
        new ClassAssert(List.class).has(declaredConstructor());
    }

    @Test
    public void assertion_pass_when_constructor_with_expected_annotation_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredConstructor().annotatedBy(SomeAnnotation.class));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_constructor_with_expected_annotation_is_missing() throws Exception {
        new ClassAssert(Double.class).has(declaredConstructor().annotatedBy(SomeAnnotation.class));
    }

    @Test
    public void assertion_pass_when_constructor_with_expected_args_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredConstructor().withArgs(
                arg(String.class),
                arg(Short.class) // wrapper
        ));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_constructor_with_expected_args_is_missing() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredConstructor().withArgs(
                arg(String.class),
                arg(short.class) // not a wrapper
        ));
    }

    @Test
    public void assertion_pass_when_constructor_with_expected_annotated_args_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredConstructor().withArgs(
                arg(double.class).annotatedBy(SomeAnnotation.class),
                arg(short.class)
        ));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_constructor_with_expected_annotated_args_is_missing() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredConstructor().withArgs(
                arg(String.class).annotatedBy(SomeAnnotation.class),
                arg(short.class).annotatedBy(SomeAnnotation.class)
        ));
    }

    @Test
    public void assertion_pass_when_constructor_without_args_is_found() throws Exception {
        new ClassAssert(String.class).has(declaredConstructor().noArgs());
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_constructor_without_args_is_missing() throws Exception {
        new ClassAssert(Integer.class).has(declaredConstructor().noArgs());
    }



    @Retention(RetentionPolicy.RUNTIME)
    @interface SomeAnnotation {
    }

    @SuppressWarnings("unused")
    static class SomeClass {
        @SomeAnnotation SomeClass() {}
        SomeClass(String string) {}
        SomeClass(String aString, Short aShort) {}
        SomeClass(@SomeAnnotation double annotated_arg, short not_annotated_arg) {}
    }
}
