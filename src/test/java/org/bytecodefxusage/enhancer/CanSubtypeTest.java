package org.bytecodefxusage.enhancer;

import org.bytecodefx.ClassFX;
import org.junit.Test;

import java.io.Serializable;
import java.util.Observer;

import static org.bytecodefx.testutils.BytecodeFXAssertions.assertThat;


public class CanSubtypeTest {

    @Test
    public void can_subtype_Object() throws Exception {
        Class<? extends Object> subTypeOfObserver = ClassFX.of(Object.class).createClass();

        assertThat(subTypeOfObserver).isStrictSubtypeOf(Object.class);
    }

    @Test
    public void can_subtype_a_non_final_class() throws Exception {
        Class<? extends Observer> subTypeOfObserver = ClassFX.of(Observer.class).createClass();

        assertThat(subTypeOfObserver).isStrictSubtypeOf(Observer.class);
    }

    @Test
    public void can_create_interface_implementing_class() throws Exception {
        Class<? extends Observer> subTypeOfObserver = ClassFX.of(Observer.class)
                                                             .withInterfaces(Appendable.class, Serializable.class)
                                                             .createClass();

        assertThat(subTypeOfObserver)
                .isStrictSubtypeOf(Appendable.class)
                .isStrictSubtypeOf(Serializable.class);
    }
}
