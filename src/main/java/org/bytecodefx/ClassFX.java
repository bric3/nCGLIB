package org.bytecodefx;

import org.bytecodefx.fx.ClassFXBuilder;
import org.bytecodefx.fx.internal.ClassFXBuilderImpl;

public abstract class ClassFX {
    public static <T> ClassFXBuilder<T> of(Class<T> classWithoutTheNewEffects) {
        return new ClassFXBuilderImpl(classWithoutTheNewEffects);
    }
}
