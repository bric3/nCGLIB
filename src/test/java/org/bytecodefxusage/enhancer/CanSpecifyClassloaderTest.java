package org.bytecodefxusage.enhancer;

import org.bytecodefx.ClassFX;
import org.bytecodefx.fx.ClassLoaderProvider;
import org.junit.Test;

import java.util.List;

import static org.bytecodefx.testutils.BytecodeFXAssertions.assertThat;


public class CanSpecifyClassloaderTest {

    @Test
    public void can_load_new_class_with_provided_class_loader() throws Exception {
        Class<? extends List> theClass = ClassFX.of(List.class).withClassLoader(classLoader("A")).createClass();

        assertThat(theClass).hasClassLoaderOfType(SpecificClassLoader.class);
    }

    @Test
    public void can_create_same_byte_code_with_different_classloaders() throws Exception {
        Class<? extends List> class_loaded_in_ClassLoader_A = ClassFX.of(List.class).withClassLoader(classLoader("A")).createClass();
        Class<? extends List> class_loaded_in_ClassLoader_B = ClassFX.of(List.class).withClassLoader(classLoader("B")).createClass();

        assertThat(class_loaded_in_ClassLoader_A).hasDifferentClassLoader(class_loaded_in_ClassLoader_B);
    }

    private ClassLoaderProvider classLoader(final String id) {
        return new ClassLoaderProvider() {
            public ClassLoader getClassLoader() {
                return new SpecificClassLoader(id);
            }
        };
    }

    ////// TODO make a classloader that don't defer class loading to the parent
    private static class SpecificClassLoader extends ClassLoader {
        private final String id;
        public SpecificClassLoader(String id) { this.id = id; }
        @Override public String toString() { return "ClassLoader 'id'"; }
    }
}
