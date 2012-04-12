package org.bytecodefxusage.enhancer;

import org.bytecodefx.ClassFX;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.bytecodefx.testutils.ClassMemberMatcher.arg;
import static org.bytecodefx.testutils.ClassMemberMatcher.declaredConstructor;
import static org.bytecodefx.testutils.ClassMemberMatcher.declaredMethod;
import static org.bytecodefx.testutils.BytecodeFXAssertions.assertThat;

public class CanCopyAnnotationAttributesOfConcreteTypesTest {
    @Test
    public void can_copy_runtime_annotation_attributes_of_concrete_class() throws Exception {
        Class<? extends Object> enhanced = ClassFX.of(Object.class).createClass();

        assertThat(enhanced)
                .isAnnotatedBy(RuntimeAnnotation.class)
                .has(declaredMethod("annotatedMethod").annotatedBy(RuntimeAnnotation.class).withArgs(arg(String.class).annotatedBy(RuntimeAnnotation.class)))
                .has(declaredConstructor().noArgs().annotatedBy(RuntimeAnnotation.class))
                ;

    }



    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE })
    @interface RuntimeAnnotation {
        String value();
    }

    @RuntimeAnnotation("on type")
    static class AnnotatedClass {
        @RuntimeAnnotation("on constructor")
        AnnotatedClass() {
        }

        @RuntimeAnnotation("on method")
        public void annotatedMethod(
                @RuntimeAnnotation("on param") String string
        ) { }
    }

}
