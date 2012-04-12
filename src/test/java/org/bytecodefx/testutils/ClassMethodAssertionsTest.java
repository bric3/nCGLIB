package org.bytecodefx.testutils;

import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.bytecodefx.testutils.ClassMemberMatcher.arg;
import static org.bytecodefx.testutils.ClassMemberMatcher.declaredMethod;

@SuppressWarnings("unchecked") // FIXME
public class ClassMethodAssertionsTest {
    @Test
    public void assertion_pass_when_method_with_expected_name_is_found() throws Exception {
        new ClassAssert(List.class).has(declaredMethod("add"));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_method_with_expected_name_is_missing() throws Exception {
        new ClassAssert(List.class).has(declaredMethod("missing"));
    }

    @Test
    public void assertion_pass_when_method_with_expected_annotation_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("annotated_method").annotatedBy(SomeAnnotation.class));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_method_with_expected_annotation_is_missing() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("nonAnnotated_method").annotatedBy(SomeAnnotation.class));
    }

    @Test
    public void assertion_pass_when_method_with_expected_args_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("method_with_String_Short_arguments").withArgs(
                arg(String.class),
                arg(Short.class) // wrapper
        ));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_method_with_expected_args_is_missing() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("method_with_String_Short_arguments").withArgs(
                arg(String.class),
                arg(short.class) // not a wrapper
        ));
    }

    @Test
    public void assertion_pass_when_method_with_expected_annotated_args_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("method_with_first_argument_annotated").withArgs(
                arg(double.class).annotatedBy(SomeAnnotation.class),
                arg(short.class)
        ));
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_method_with_expected_annotated_args_is_missing() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("method_with_first_argument_annotated").withArgs(
                arg(String.class).annotatedBy(SomeAnnotation.class),
                arg(short.class).annotatedBy(SomeAnnotation.class)
        ));
    }

    @Test
    public void assertion_pass_when_method_without_args_is_found() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("no_arg_method").noArgs());
    }
    @Test(expected = AssertionError.class)
    public void assertion_fail_when_method_without_args_is_missing() throws Exception {
        new ClassAssert(SomeClass.class).has(declaredMethod("missing_no_arg_method").noArgs());
    }



    @Retention(RetentionPolicy.RUNTIME)
    @interface SomeAnnotation {
    }

    class SomeClass {
        @SomeAnnotation void annotated_method() {}
        void non_annotated_method() {}
        void no_arg_method() {}
        void missing_no_arg_method(String string) {}
        void method_with_String_Short_arguments(String aString, Short aShort) {}
        void method_with_first_argument_annotated(@SomeAnnotation double annotated_arg, short not_annotated_arg) {}
    }
}
