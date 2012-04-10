package org.bytecodefx.testutils;

@SuppressWarnings("unchecked")
public abstract class BytecodeFXAssertions {

    public static <T> ClassAssert assertThat(Class<T> subTypeOfObserver) {
        return new ClassAssert(subTypeOfObserver);
    }

}
