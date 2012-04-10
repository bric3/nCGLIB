package org.bytecodefxusage.enhancer.misuse;

import org.bytecodefx.ClassFX;
import org.bytecodefx.reporting.ClassFXMisuse;
import org.junit.Test;

import java.util.Observer;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ReportMisusesTest {

    @Test
    public void report_misuse_when_class_is_final() throws Exception {
        try {
            ClassFX.of(Integer.class);
            fail();
        } catch (ClassFXMisuse e) {
            assertThat(e.getMessage())
                    .containsIgnoringCase("cannot create subclass of")
                    .contains("Integer")
                    .contains("final");
        }
    }

    @Test
    public void report_misuse_if_with_Interfaces_is_used_with_concrete_type() throws Exception {
        try {
            ClassFX.of(Observer.class).withInterfaces(Object.class);
        } catch (ClassFXMisuse e) {
            assertThat(e.getMessage()).contains("Object").contains("not an interface");
        }
    }
}
