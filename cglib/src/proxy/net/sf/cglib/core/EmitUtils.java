/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sf.cglib.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class EmitUtils {
    private static final Signature CSTRUCT_NULL =
      TypeUtils.parseConstructor("");
    private static final Signature CSTRUCT_THROWABLE =
      TypeUtils.parseConstructor("Throwable");

    private static final Signature GET_NAME =
      TypeUtils.parseSignature("String getName()");
    private static final Signature HASH_CODE =
      TypeUtils.parseSignature("int hashCode()");
    private static final Signature EQUALS =
      TypeUtils.parseSignature("boolean equals(Object)");
    private static final Signature STRING_LENGTH =
      TypeUtils.parseSignature("int length()");
    private static final Signature STRING_CHAR_AT =
      TypeUtils.parseSignature("char charAt(int)");
    private static final Signature FOR_NAME =
      TypeUtils.parseSignature("Class forName(String)");
    private static final Signature DOUBLE_TO_LONG_BITS =
      TypeUtils.parseSignature("long doubleToLongBits(double)");
    private static final Signature FLOAT_TO_INT_BITS =
      TypeUtils.parseSignature("int floatToIntBits(float)");
    private static final Signature TO_STRING =
      TypeUtils.parseSignature("String toString()");
    private static final Signature APPEND_STRING =
      TypeUtils.parseSignature("StringBuffer append(String)");
    private static final Signature APPEND_INT =
      TypeUtils.parseSignature("StringBuffer append(int)");
    private static final Signature APPEND_DOUBLE =
      TypeUtils.parseSignature("StringBuffer append(double)");
    private static final Signature APPEND_FLOAT =
      TypeUtils.parseSignature("StringBuffer append(float)");
    private static final Signature APPEND_CHAR =
      TypeUtils.parseSignature("StringBuffer append(char)");
    private static final Signature APPEND_LONG =
      TypeUtils.parseSignature("StringBuffer append(long)");
    private static final Signature APPEND_BOOLEAN =
      TypeUtils.parseSignature("StringBuffer append(boolean)");
    private static final Signature LENGTH =
      TypeUtils.parseSignature("int length()");
    private static final Signature SET_LENGTH =
      TypeUtils.parseSignature("void setLength(int)");
    private static final Signature GET_DECLARED_METHOD =
      TypeUtils.parseSignature("java.lang.reflect.Method getDeclaredMethod(String, Class[])");

    public static final ArrayDelimiters DEFAULT_DELIMITERS = new ArrayDelimiters("{", ", ", "}");

    private EmitUtils() {
    }

    public static void factory_method(ClassEmitter ce, Signature sig) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, sig, null, null);
        e.new_instance_this();
        e.dup();
        e.load_args();
        e.invoke_constructor_this(TypeUtils.parseConstructor(sig.getArgumentTypes()));
        e.return_value();
        e.end_method();
    }

    public static void null_constructor(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, CSTRUCT_NULL, null, null);
        e.load_this();
        e.super_invoke_constructor();
        e.return_value();
        e.end_method();
    }
    
    /**
     * Process an array on the stack. Assumes the top item on the stack
     * is an array of the specified type. For each element in the array,
     * puts the element on the stack and triggers the callback.
     * @param type the type of the array (type.isArray() must be true)
     * @param callback the callback triggered for each element
     */
    public static void process_array(CodeEmitter e, Type type, ProcessArrayCallback callback) {
        Type componentType = TypeUtils.getComponentType(type);
        Local array = e.make_local();
        Local loopvar = e.make_local(Type.INT_TYPE);
        Label loopbody = e.make_label();
        Label checkloop = e.make_label();
        e.store_local(array);
        e.push(0);
        e.store_local(loopvar);
        e.goTo(checkloop);
        
        e.mark(loopbody);
        e.load_local(array);
        e.load_local(loopvar);
        e.array_load(componentType);
        callback.processElement(componentType);
        e.iinc(loopvar, 1);
        
        e.mark(checkloop);
        e.load_local(loopvar);
        e.load_local(array);
        e.arraylength();
        e.if_icmp(e.LT, loopbody);
    }
    
    /**
     * Process two arrays on the stack in parallel. Assumes the top two items on the stack
     * are arrays of the specified class. The arrays must be the same length. For each pair
     * of elements in the arrays, puts the pair on the stack and triggers the callback.
     * @param type the type of the arrays (type.isArray() must be true)
     * @param callback the callback triggered for each pair of elements
     */
    public static void process_arrays(CodeEmitter e, Type type, ProcessArrayCallback callback) {
        Type componentType = TypeUtils.getComponentType(type);
        Local array1 = e.make_local();
        Local array2 = e.make_local();
        Local loopvar = e.make_local(Type.INT_TYPE);
        Label loopbody = e.make_label();
        Label checkloop = e.make_label();
        e.store_local(array1);
        e.store_local(array2);
        e.push(0);
        e.store_local(loopvar);
        e.goTo(checkloop);
        
        e.mark(loopbody);
        e.load_local(array1);
        e.load_local(loopvar);
        e.array_load(componentType);
        e.load_local(array2);
        e.load_local(loopvar);
        e.array_load(componentType);
        callback.processElement(componentType);
        e.iinc(loopvar, 1);
        
        e.mark(checkloop);
        e.load_local(loopvar);
        e.load_local(array1);
        e.arraylength();
        e.if_icmp(e.LT, loopbody);
    }
    
    public static void string_switch(CodeEmitter e, String[] strings, int switchStyle, ObjectSwitchCallback callback) {
        try {
            switch (switchStyle) {
            case Constants.SWITCH_STYLE_TRIE:
                string_switch_trie(e, strings, callback);
                break;
            case Constants.SWITCH_STYLE_HASH:
                string_switch_hash(e, strings, callback, false);
                break;
            case Constants.SWITCH_STYLE_HASHONLY:
                string_switch_hash(e, strings, callback, true);
                break;
            default:
                throw new IllegalArgumentException("unknown switch style " + switchStyle);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Error ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private static void string_switch_trie(final CodeEmitter e,
                                           String[] strings,
                                           final ObjectSwitchCallback callback) throws Exception {
        final Label def = e.make_label();
        final Label end = e.make_label();
        final Map buckets = CollectionUtils.bucket(Arrays.asList(strings), new Transformer() {
            public Object transform(Object value) {
                return new Integer(((String)value).length());
            }
        });
        e.dup();
        e.invoke_virtual(Constants.TYPE_STRING, STRING_LENGTH);
        e.process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    stringSwitchHelper(e, bucket, callback, def, end, 0);
                }
                public void processDefault() {
                    e.goTo(def);
                }
            });
        e.mark(def);
        e.pop();
        callback.processDefault();
        e.mark(end);
    }

    private static void stringSwitchHelper(final CodeEmitter e,
                                           List strings,
                                           final ObjectSwitchCallback callback,
                                           final Label def,
                                           final Label end,
                                           final int index) throws Exception {
        final int len = ((String)strings.get(0)).length();
        final Map buckets = CollectionUtils.bucket(strings, new Transformer() {
            public Object transform(Object value) {
                return new Integer(((String)value).charAt(index));
            }
        });
        e.dup();
        e.push(index);
        e.invoke_virtual(Constants.TYPE_STRING, STRING_CHAR_AT);
        e.process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
                public void processCase(int key, Label ignore_end) throws Exception {
                    List bucket = (List)buckets.get(new Integer(key));
                    if (index + 1 == len) {
                        e.pop();
                        callback.processCase(bucket.get(0), end);
                    } else {
                        stringSwitchHelper(e, bucket, callback, def, end, index + 1);
                    }
                }
                public void processDefault() {
                    e.goTo(def);
                }
            });
    }        

    static int[] getSwitchKeys(Map buckets) {
        int[] keys = new int[buckets.size()];
        int index = 0;
        for (Iterator it = buckets.keySet().iterator(); it.hasNext();) {
            keys[index++] = ((Integer)it.next()).intValue();
        }
        Arrays.sort(keys);
        return keys;
    }

    private static void string_switch_hash(final CodeEmitter e,
                                           final String[] strings,
                                           final ObjectSwitchCallback callback,
                                           final boolean skipEquals) throws Exception {
        final Map buckets = CollectionUtils.bucket(Arrays.asList(strings), new Transformer() {
            public Object transform(Object value) {
                return new Integer(value.hashCode());
            }
        });
        final Label def = e.make_label();
        final Label end = e.make_label();
        e.dup();
        e.invoke_virtual(Constants.TYPE_OBJECT, HASH_CODE);
        e.process_switch(getSwitchKeys(buckets), new ProcessSwitchCallback() {
            public void processCase(int key, Label ignore_end) throws Exception {
                List bucket = (List)buckets.get(new Integer(key));
                Label next = null;
                if (skipEquals && bucket.size() == 1) {
                    callback.processCase((String)bucket.get(0), end);
                } else {
                    for (Iterator it = bucket.iterator(); it.hasNext();) {
                        String string = (String)it.next();
                        if (next != null) {
                            e.mark(next);
                        }
                        if (it.hasNext()) {
                            e.dup();
                        }
                        e.push(string);
                        e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                        if (it.hasNext()) {
                            e.if_jump(e.EQ, next = e.make_label());
                            e.pop();
                        } else {
                            e.if_jump(e.EQ, def);
                        }
                        callback.processCase(string, end);
                    }
                }
            }
            public void processDefault() {
                e.pop();
            }
        });
        e.mark(def);
        callback.processDefault();
        e.mark(end);
    }

    public static void load_class_this(CodeEmitter e) {
        load_class_helper(e, e.getClassEmitter().getClassType());
    }
    
    public static void load_class(CodeEmitter e, Type type) {
        if (TypeUtils.isPrimitive(type)) {
            if (type == Type.VOID_TYPE) {
                throw new IllegalArgumentException("cannot load void type");
            }
            e.getstatic(TypeUtils.getBoxedType(type), "TYPE", Constants.TYPE_CLASS);
        } else {
            load_class_helper(e, type);
        }
    }

    private static void load_class_helper(CodeEmitter e, final Type type) {
        if (e.isStaticHook()) {
            // have to fall back on non-optimized load
            e.push(TypeUtils.emulateClassGetName(type));
            e.invoke_static(Constants.TYPE_CLASS, FOR_NAME);
        } else {
            ClassEmitter ce = e.getClassEmitter();
            String fieldName = "CGLIB$load_class$" + type.getClassName().replace('.', '_');
            if (!ce.isFieldDeclared(fieldName)) {
                ce.declare_field(Constants.PRIVATE_FINAL_STATIC, fieldName, Constants.TYPE_CLASS, null, null);
                CodeEmitter hook = ce.getStaticHook();
                hook.push(TypeUtils.emulateClassGetName(type));
                hook.invoke_static(Constants.TYPE_CLASS, FOR_NAME);
                hook.putstatic(ce.getClassType(), fieldName, Constants.TYPE_CLASS);
            }
            e.getfield(fieldName);
        }
    }

    public static void push_array(CodeEmitter e, Object[] array) {
        e.push(array.length);
        e.newarray(Type.getType(array.getClass().getComponentType()));
        for (int i = 0; i < array.length; i++) {
            e.dup();
            e.push(i);
            push_object(e, array[i]);
            e.aastore();
        }
    }
    
    public static void push_object(CodeEmitter e, Object obj) {
        if (obj == null) {
            e.aconst_null();
        } else {
            Class type = obj.getClass();
            if (type.isArray()) {
                push_array(e, (Object[])obj);
            } else if (obj instanceof String) {
                e.push((String)obj);
            } else if (obj instanceof Class) {
                load_class(e, Type.getType((Class)obj));
            } else if (obj instanceof BigInteger) {
                e.new_instance(Constants.TYPE_BIG_INTEGER);
                e.dup();
                e.push(obj.toString());
                e.invoke_constructor(Constants.TYPE_BIG_INTEGER);
            } else if (obj instanceof BigDecimal) {
                e.new_instance(Constants.TYPE_BIG_DECIMAL);
                e.dup();
                e.push(obj.toString());
                e.invoke_constructor(Constants.TYPE_BIG_DECIMAL);
            } else {
                throw new IllegalArgumentException("unknown type: " + obj.getClass());
            }
        }
    }

    public static void hash_code(CodeEmitter e, Type type, int multiplier, Customizer customizer) {
        if (TypeUtils.isArray(type)) {
            hash_array(e, type, multiplier, customizer);
        } else {
            e.swap(Type.INT_TYPE, type);
            e.push(multiplier);
            e.math(e.MUL, Type.INT_TYPE);
            e.swap(type, Type.INT_TYPE);
            if (TypeUtils.isPrimitive(type)) {
                hash_primitive(e, type);
            } else {
                hash_object(e, type, customizer);
            }
            e.math(e.ADD, Type.INT_TYPE);
        }
    }

    private static void hash_array(final CodeEmitter e, Type type, final int multiplier, final Customizer customizer) {
        Label skip = e.make_label();
        Label end = e.make_label();
        e.dup();
        e.ifnull(skip);
        EmitUtils.process_array(e, type, new ProcessArrayCallback() {
            public void processElement(Type type) {
                hash_code(e, type, multiplier, customizer);
            }
        });
        e.goTo(end);
        e.mark(skip);
        e.pop();
        e.mark(end);
    }

    private static void hash_object(CodeEmitter e, Type type, Customizer customizer) {
        // (f == null) ? 0 : f.hashCode();
        Label skip = e.make_label();
        Label end = e.make_label();
        e.dup();
        e.ifnull(skip);
        if (customizer != null) {
            customizer.customize(e, type);
        }
        e.invoke_virtual(Constants.TYPE_OBJECT, HASH_CODE);
        e.goTo(end);
        e.mark(skip);
        e.pop();
        e.push(0);
        e.mark(end);
    }

    private static void hash_primitive(CodeEmitter e, Type type) {
        switch (type.getSort()) {
        case Type.BOOLEAN:
            // f ? 0 : 1
            e.push(1);
            e.math(e.XOR, Type.INT_TYPE);
            break;
        case Type.FLOAT:
            // Float.floatToIntBits(f)
            e.invoke_static(Constants.TYPE_FLOAT, FLOAT_TO_INT_BITS);
            break;
        case Type.DOUBLE:
            // Double.doubleToLongBits(f), hash_code(Long.TYPE)
            e.invoke_static(Constants.TYPE_DOUBLE, DOUBLE_TO_LONG_BITS);
            // fall through
        case Type.LONG:
            hash_long(e);
        }
    }

    private static void hash_long(CodeEmitter e) {
        // (int)(f ^ (f >>> 32))
        e.dup2();
        e.push(32);
        e.math(e.USHR, Type.LONG_TYPE);
        e.math(e.XOR, Type.LONG_TYPE);
        e.cast_numeric(Type.LONG_TYPE, Type.INT_TYPE);
    }

//     public static void not_equals(CodeEmitter e, Type type, Label notEquals) {
//         not_equals(e, type, notEquals, null);
//     }
    
    /**
     * Branches to the specified label if the top two items on the stack
     * are not equal. The items must both be of the specified
     * class. Equality is determined by comparing primitive values
     * directly and by invoking the <code>equals</code> method for
     * Objects. Arrays are recursively processed in the same manner.
     */
    public static void not_equals(final CodeEmitter e, Type type, final Label notEquals, final Customizer customizer) {
        (new ProcessArrayCallback() {
            public void processElement(Type type) {
                not_equals_helper(e, type, notEquals, customizer, this);
            }
        }).processElement(type);
    }
    
    private static void not_equals_helper(CodeEmitter e,
                                          Type type,
                                          Label notEquals,
                                          Customizer customizer,
                                          ProcessArrayCallback callback) {
        if (TypeUtils.isPrimitive(type)) {
            e.if_cmp(type, e.NE, notEquals);
        } else {
            Label end = e.make_label();
            nullcmp(e, notEquals, end);
            if (TypeUtils.isArray(type)) {
                Label checkContents = e.make_label();
                e.dup2();
                e.arraylength();
                e.swap();
                e.arraylength();
                e.if_icmp(e.EQ, checkContents);
                e.pop2();
                e.goTo(notEquals);
                e.mark(checkContents);
                EmitUtils.process_arrays(e, type, callback);
            } else {
                if (customizer != null) {
                    customizer.customize(e, type);
                    e.swap();
                    customizer.customize(e, type);
                }
                e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                e.if_jump(e.EQ, notEquals);
            }
            e.mark(end);
        }
    }

    /**
     * If both objects on the top of the stack are non-null, does nothing.
     * If one is null, or both are null, both are popped off and execution
     * branches to the respective label.
     * @param oneNull label to branch to if only one of the objects is null
     * @param bothNull label to branch to if both of the objects are null
     */
    private static void nullcmp(CodeEmitter e, Label oneNull, Label bothNull) {
        e.dup2();
        Label nonNull = e.make_label();
        Label oneNullHelper = e.make_label();
        Label end = e.make_label();
        e.ifnonnull(nonNull);
        e.ifnonnull(oneNullHelper);
        e.pop2();
        e.goTo(bothNull);
        
        e.mark(nonNull);
        e.ifnull(oneNullHelper);
        e.goTo(end);
        
        e.mark(oneNullHelper);
        e.pop2();
        e.goTo(oneNull);
        
        e.mark(end);
    }

    /*
    public static void to_string(CodeEmitter e,
                                 Type type,
                                 ArrayDelimiters delims,
                                 Customizer customizer) {
        e.new_instance(Constants.TYPE_STRING_BUFFER);
        e.dup();
        e.invoke_constructor(Constants.TYPE_STRING_BUFFER);
        e.swap();
        append_string(e, type, delims, customizer);
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, TO_STRING);
    }
    */

    public static void append_string(final CodeEmitter e,
                                     Type type,
                                     final ArrayDelimiters delims,
                                     final Customizer customizer) {
        final ArrayDelimiters d = (delims != null) ? delims : DEFAULT_DELIMITERS;
        ProcessArrayCallback callback =new ProcessArrayCallback() {
            public void processElement(Type type) {
                append_string_helper(e, type, d, customizer, this);
                e.push(d.inside);
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
            }
        };
        append_string_helper(e, type, d, customizer, callback);
    }

    private static void append_string_helper(CodeEmitter e,
                                             Type type,
                                             ArrayDelimiters delims,
                                             Customizer customizer,
                                             ProcessArrayCallback callback) {
        Label skip = e.make_label();
        Label end = e.make_label();
        if (TypeUtils.isPrimitive(type)) {
            switch (type.getSort()) {
            case Type.INT:
            case Type.SHORT:
            case Type.BYTE:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_INT);
                break;
            case Type.DOUBLE:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_DOUBLE);
                break;
            case Type.FLOAT:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_FLOAT);
                break;
            case Type.LONG:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_LONG);
                break;
            case Type.BOOLEAN:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_BOOLEAN);
                break;
            case Type.CHAR:
                e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_CHAR);
                break;
            }
        } else if (TypeUtils.isArray(type)) {
            e.dup();
            e.ifnull(skip);
            e.swap();
            e.push(delims.before);
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
            e.swap();
            EmitUtils.process_array(e, type, callback);
            shrinkStringBuffer(e, 2);
            e.push(delims.after);
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        } else {
            e.dup();
            e.ifnull(skip);
            if (customizer != null) {
                customizer.customize(e, type);
            }
            e.invoke_virtual(Constants.TYPE_OBJECT, TO_STRING);
            e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        }
        e.goTo(end);
        e.mark(skip);
        e.pop();
        e.push("null");
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, APPEND_STRING);
        e.mark(end);
    }

    private static void shrinkStringBuffer(CodeEmitter e, int amt) {
        e.dup();
        e.dup();
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, LENGTH);
        e.push(amt);
        e.math(e.SUB, Type.INT_TYPE);
        e.invoke_virtual(Constants.TYPE_STRING_BUFFER, SET_LENGTH);
    }

    public static class ArrayDelimiters {
        private String before;
        private String inside;
        private String after;
            
        public ArrayDelimiters(String before, String inside, String after) {
            this.before = before;
            this.inside = inside;
            this.after = after;
        }
    }

    public static void load_method(CodeEmitter e, Method method) {
        load_class(e, Type.getType(method.getDeclaringClass()));
        e.push(method.getName());
        push_object(e, method.getParameterTypes());
        e.invoke_virtual(Constants.TYPE_CLASS, GET_DECLARED_METHOD);
    }

    private interface ParameterTyper {
        Class[] getParameterTypes(Object member);
    }

    public static void method_switch(CodeEmitter e,
                                     Method[] methods,
                                     ObjectSwitchCallback callback) {
        member_switch_helper(e, Arrays.asList(methods), callback, true, new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                return ((Method)member).getParameterTypes();
            }
        });
    }

    public static void constructor_switch(CodeEmitter e,
                                          Constructor[] cstructs,
                                          ObjectSwitchCallback callback) {
        member_switch_helper(e, Arrays.asList(cstructs), callback, false, new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                return ((Constructor)member).getParameterTypes();
            }
        });
    }

    private static void member_switch_helper(final CodeEmitter e,
                                             List members,
                                             final ObjectSwitchCallback callback,
                                             boolean useName,
                                             final ParameterTyper typer) {
        try {
            final Map cache = new HashMap();
            final ParameterTyper cached = new ParameterTyper() {
                    public Class[] getParameterTypes(Object member) {
                        Class[] types = (Class[])cache.get(member);
                        if (types == null) {
                            cache.put(member, types = typer.getParameterTypes(member));
                        }
                        return types;
                    }
                };
            final Label def = e.make_label();
            final Label end = e.make_label();
            if (useName) {
                e.swap();
                final Map buckets = CollectionUtils.bucket(members, new Transformer() {
                        public Object transform(Object value) {
                            return ((Member)value).getName();
                        }
                    });
                String[] names = (String[])buckets.keySet().toArray(new String[buckets.size()]);
                EmitUtils.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                        public void processCase(Object key, Label dontUseEnd) throws Exception {
                            member_helper_size(e, (List)buckets.get(key), callback, cached, def, end);
                        }
                        public void processDefault() throws Exception {
                            e.goTo(def);
                        }
                    });
            } else {
                member_helper_size(e, members, callback, cached, def, end);
            }
            e.mark(def);
            e.pop();
            callback.processDefault();
            e.mark(end);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Error ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private static void member_helper_size(final CodeEmitter e,
                                           List members,
                                           final ObjectSwitchCallback callback,
                                           final ParameterTyper typer,
                                           final Label def,
                                           final Label end) throws Exception {
        final Map buckets = CollectionUtils.bucket(members, new Transformer() {
            public Object transform(Object value) {
                return new Integer(typer.getParameterTypes(value).length);
            }
        });
        e.dup();
        e.arraylength();
        e.process_switch(EmitUtils.getSwitchKeys(buckets), new ProcessSwitchCallback() {
            public void processCase(int key, Label dontUseEnd) throws Exception {
                List bucket = (List)buckets.get(new Integer(key));
                member_helper_type(e, bucket, callback, typer, def, end, new TinyBitSet());
            }
            public void processDefault() throws Exception {
                e.goTo(def);
            }
        });
    }

    private static void member_helper_type(final CodeEmitter e,
                                           List members,
                                           final ObjectSwitchCallback callback,
                                           final ParameterTyper typer,
                                           final Label def,
                                           final Label end,
                                           final TinyBitSet checked) throws Exception {
        if (members.size() == 1) {
            // need to check classes that have not already been checked via switches
            Member member = (Member)members.get(0);
            Class[] types = typer.getParameterTypes(member);
            for (int i = 0; i < types.length; i++) {
                if (checked == null || !checked.get(i)) {
                    e.dup();
                    e.aaload(i);
                    e.invoke_virtual(Constants.TYPE_CLASS, GET_NAME);
                    e.push(types[i].getName());
                    e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                    e.if_jump(e.EQ, def);
                }
            }
            e.pop();
            callback.processCase(member, end);
        } else {
            // choose the index that has the best chance of uniquely identifying member
            Class[] example = typer.getParameterTypes(members.get(0));
            Map buckets = null;
            int index = -1;
            for (int i = 0; i < example.length; i++) {
                final int j = i;
                Map test = CollectionUtils.bucket(members, new Transformer() {
                    public Object transform(Object value) {
                        return typer.getParameterTypes(value)[j].getName();
                    }
                });
                if (buckets == null || test.size() > buckets.size()) {
                    buckets = test;
                    index = i;
                }
            }
            if (buckets == null) {
                // TODO: switch by returnType
                // must have two methods with same name, types, and different return types
                e.goTo(def);
            } else {
                checked.set(index);

                e.dup();
                e.aaload(index);
                e.invoke_virtual(Constants.TYPE_CLASS, GET_NAME);

                final Map fbuckets = buckets;
                String[] names = (String[])buckets.keySet().toArray(new String[buckets.size()]);
                EmitUtils.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                    public void processCase(Object key, Label dontUseEnd) throws Exception {
                        member_helper_type(e, (List)fbuckets.get(key), callback, typer, def, end, checked);
                    }
                    public void processDefault() throws Exception {
                        e.goTo(def);
                    }
                });
            }
        }
    }

    public static void wrap_throwable(Block block, Type wrapper) {
        CodeEmitter e = block.getCodeEmitter();
        e.catch_exception(block, Constants.TYPE_THROWABLE);
        e.new_instance(wrapper);
        e.dup_x1();
        e.swap();
        e.invoke_constructor(wrapper, CSTRUCT_THROWABLE);
        e.athrow();
    }

    public static void add_properties(ClassEmitter ce, String[] names, Type[] types) {
        for (int i = 0; i < names.length; i++) {
            String fieldName = "$cglib_prop_" + names[i];
            ce.declare_field(Constants.ACC_PRIVATE, fieldName, types[i], null, null);
            EmitUtils.add_property(ce, names[i], types[i], fieldName);
        }
    }

    public static void add_property(ClassEmitter ce, String name, Type type, String fieldName) {
        String property = TypeUtils.upperFirst(name);
        CodeEmitter e;
        e = ce.begin_method(Constants.ACC_PUBLIC,
                         new Signature("get" + property,
                                       type,
                                       Constants.TYPES_EMPTY),
                         null,
                         null);
        e.load_this();
        e.getfield(fieldName);
        e.return_value();
        e.end_method();

        e = ce.begin_method(Constants.ACC_PUBLIC,
                         new Signature("set" + property,
                                       Type.VOID_TYPE,
                                       new Type[]{ type }),
                         null,
                         null);
        e.load_this();
        e.load_arg(0);
        e.putfield(fieldName);
        e.return_value();
        e.end_method();
    }
}
