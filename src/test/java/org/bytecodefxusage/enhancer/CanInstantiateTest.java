package org.bytecodefxusage.enhancer;


import org.bytecodefx.ClassFX;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.Observer;

import static org.fest.assertions.Assertions.assertThat;

public class CanInstantiateTest {

    @Test
    public void can_instantiate_new_type_from_interface() throws Exception {
        Observer observerFX = ClassFX.of(Observer.class).newInstance();

        assertThat(observerFX).isNotNull();
    }

    @Test
    public void can_instantiate_new_type_from_type_without_no_arg_constructor() throws Exception {
        PrintWriter printWriter = ClassFX.of(PrintWriter.class).newInstance();

        assertThat(printWriter).isNotNull();
    }
}
