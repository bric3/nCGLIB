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
package net.sf.cglib;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 *
 * this code returns Enhanced Vector to intercept  all methods for tracing
 *   <pre>
 *         java.util.Vector vector = (java.util.Vector)Enhancer.enhance(
 *        java.util.Vector.<b>class</b>,
 *        new Class[]{java.util.List.<b>class</b>},
 *
 *        new BeforeAfterAdapter(){
 *        <b>public</b> Object <b>afterReturn</b>(  Object obj,     java.lang.reflect.Method method,
 *        Object args[],
 *        boolean invokedSuper, Object retValFromSuper,
 *        java.lang.Throwable e )throws java.lang.Throwable{
 *            System.out.println(method);
 *            return retValFromSuper;//return the same as supper
 *        }
 *
 *    });
 * </pre>
 *@author     Juozas Baliuka <a href="mailto:baliuka@mwm.lt">
 *      baliuka@mwm.lt</a>
 *@version    $Id: Enhancer.java,v 1.5 2002-12-03 06:49:01 herbyderby Exp $
 */
public class Enhancer {
    private static final String CLASS_PREFIX = "net.sf.cglib";
    private static final String CLASS_SUFFIX = "$$EnhancedByCGLIB$$";
    private static int index = 0;
    private static final Map factories = new HashMap();
    private static final Map cache =  new WeakHashMap();
    private static final EnhancerKey keyFactory =
      (EnhancerKey)KeyFactory.makeFactory(EnhancerKey.class, null);
    private static final ClassLoader defaultLoader = Enhancer.class.getClassLoader();
    private static final String methodInterceptorName = MethodInterceptor.class.getName();

    /* package */ interface EnhancerKey {
        public Object newInstance(Class cls, Class[] interfaces, Method wreplace,
                                  Class interceptor, boolean delegating);
    }
    
    private Enhancer() {}

    public static MethodInterceptor getMethodInterceptor(Object enhanced){
      
            return ((Factory)enhanced).getInterceptor();
        
    }
    
    /**
     *  implements decorator  for the first parameter,
     *  returned instance extends obj.getClass() and implements Factory interface,
     *  MethodProxy delegates calls to obj methods
     *  @param obj object to decorate
     *  @param interceptor interceptor used to handle implemented methods
     *  @return decorated instanse of obj.getClass()  class
     */
    
    
     public static Factory decorate(Object obj, MethodInterceptor interceptor ){
     
         return (Factory)enhanceHelper(true, obj, obj.getClass(), null , interceptor,
                               obj.getClass().getClassLoader(), null );
     }
    
    
    /**
     *  overrides Class methods and implements all abstract methods.  
     *  returned instance extends clazz and implements Factory interface,
     *  MethodProxy delegates calls to supper Class (clazz) methods, if not abstract.
     *  @param clazz Class to override
     *  @param interceptor interceptor used to handle implemented methods
     *  @return instanse of clazz class, new Class is defined in the same class loader
     */
    
     
      public static Factory override(Class clazz, MethodInterceptor interceptor ){
     
         return (Factory)enhanceHelper(false, null , clazz, null , interceptor,
                               clazz.getClassLoader(), null );
     }
    
     
     
    /**
     *  implemented as
     * return enhance(cls,interfaces,ih, null,null,false);
     */
    public static Object enhance(
    Class cls,
    Class interfaces[],
    MethodInterceptor ih) {
        
        return enhance(
        cls,
        interfaces,
        ih,
        null,
        null);
    }
     public static Object enhance(
    Class cls,
    Class interfaces[],
    MethodInterceptor ih,
    ClassLoader loader ) {
        return enhance(
        cls,
        interfaces,
        ih,
        loader,
        null);
   
     } 
    /** enhances public not final class,
     * source class must have public or protected no args constructor.
     * Code is generated for protected and public not final methods,
     * package scope methods supported from source class package.
     * Defines new class in  source class package, if it not java*.
     * @param cls class to extend, uses Object.class if null
     * @param interfaces interfaces to implement, can be null
     * @param ih valid interceptor implementation
     * @param loader classloater for enhanced class, uses "current" if null
     * @param wreplace  static method to implement writeReplace, must have
     * single Object type parameter(to replace) and return object, 
     * default implementation from InternalReplace is used if
     * parameter is null : static public Object InternalReplace.writeReplace( 
     *                                                       Object enhanced )
     *                 throws ObjectStreamException;
     * @throws Throwable on error
     * @return instanse of enhanced  class
     */
    public static Object enhance(Class cls,
                                 Class[] interfaces,
                                 MethodInterceptor ih,
                                 ClassLoader loader,
                                 Method wreplace) {
        return enhanceHelper(false, null, cls, interfaces, ih, loader, wreplace);
    }

    public static Object enhance(Object obj,
                                 Class cls,
                                 Class[] interfaces,
                                 MethodInterceptor ih,
                                 ClassLoader loader,
                                 Method wreplace) {
        return enhanceHelper(true, obj, cls, interfaces, ih, loader, wreplace);
    }

    
    private synchronized static Object enhanceHelper(boolean delegating,
                                        Object obj,
                                        Class cls,
                                        Class[] interfaces,
                                        MethodInterceptor ih,
                                        ClassLoader loader,
                                        Method wreplace) {
        if (ih == null) {
            throw new IllegalArgumentException("MethodInterceptor is null");
        }

        if (cls != null && obj != null && !cls.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("Class must be same class or superclass of delegate");
        }

        if (cls == null) {
            if (obj == null) {
                cls = Constants.TYPE_OBJECT;
            } else {
                cls = obj.getClass();
            }
        }

        if (loader == null) {
            loader = defaultLoader;
        }

        Map map = (Map)cache.get(loader);
        if (map == null) {
            map = new Hashtable();
            cache.put(loader, map);
        }

      
      Object key = keyFactory.newInstance(cls, interfaces, wreplace, ih.getClass(), delegating);
      Class result = (Class) map.get(key);

          
        if ( result == null ) {
            String class_name = cls.getName() + CLASS_SUFFIX;
            if (class_name.startsWith("java")) {
                class_name = CLASS_PREFIX + class_name;
            }
            class_name += index++;
            result = new EnhancerGenerator(class_name, cls, interfaces, ih, loader, wreplace, delegating).define();
            map.put(key, result);
        }
      

      
        Factory factory = (Factory)factories.get(result);
        if (factory == null) {
            try {
                Class mi = Class.forName(methodInterceptorName, true, loader);
                if (delegating) {
                    factory = (Factory)result.getConstructor(new Class[]{ mi, Constants.TYPE_OBJECT })
                        .newInstance(new Object[] { null, null });
                } else {
                    factory = (Factory)result.getConstructor(new Class[]{ mi })
                        .newInstance(new Object[] { null });
                }
                factories.put(result,factory);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new CodeGenerationException(e);
            }
        }
        if (delegating) {
            return factory.newInstance(ih, obj);
        } else {
            return factory.newInstance(ih);
        }
    }

    public static class InternalReplace implements Serializable {
        private String parentClassName;
        private String [] interfaceNames;
        private MethodInterceptor mi;
        
        public InternalReplace() {
        }
        
        private InternalReplace(String parentClassName, String[] interfaces,
                                MethodInterceptor mi) {
            this.parentClassName = parentClassName;
            this.interfaceNames   = interfaceNames;
            this.mi = mi;
        }
        
        public static Object writeReplace(Object enhanced) throws ObjectStreamException {
            MethodInterceptor mi = Enhancer.getMethodInterceptor(enhanced);
            String parentClassName = enhanced.getClass().getSuperclass().getName();
            Class interfaces[] = enhanced.getClass().getInterfaces();
            String [] interfaceNames = new String[interfaces.length];
            
            for (int i = 0; i < interfaces.length; i++) {
                interfaceNames[i] = interfaces[i].getName();
            }
            
            return new InternalReplace(parentClassName, interfaceNames, mi);
        }
        
        
        private Object readResolve() throws ObjectStreamException {
            try {
                ClassLoader loader = getClass().getClassLoader();
                Class parent = loader.loadClass(parentClassName);
                Class interfaces[] = null;
                
                if (interfaceNames != null) {
                    interfaces = new Class[interfaceNames.length];
                    for (int i = 0; i< interfaceNames.length; i++) {
                        interfaces[i] = loader.loadClass(interfaceNames[i]);
                    }
                }
                return Enhancer.enhance(parent, interfaces, mi, loader);
            } catch (ClassNotFoundException e) {
                throw new ReadResolveException(e);
            } catch (CodeGenerationException e) {
                throw new ReadResolveException(e.getCause());
            }
        }
    }

    public static class ReadResolveException extends ObjectStreamException {
        private Throwable cause;

        public ReadResolveException(Throwable cause) {
            super(cause.getMessage());
            this.cause = cause;
        }

        public Throwable getCause() {
            return cause;
        }
    }
}
