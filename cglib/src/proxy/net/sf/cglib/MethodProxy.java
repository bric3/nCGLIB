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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @version $Id: MethodProxy.java,v 1.16 2003-02-02 15:02:57 baliuka Exp $
 */
abstract public class MethodProxy {
    
    private static final Method INVOKE_SUPER =
      ReflectUtils.findMethod("MethodProxy.invokeSuper(Object, Object[])");
        private static final Method INVOKE =
      ReflectUtils.findMethod("MethodProxy.invoke(Object, Object[])");


    private static final ClassNameFactory NAME_FACTORY =
      new ClassNameFactory("ProxiedByCGLIB");

    private static final ClassLoader DEFAULT_LOADER =
      MethodProxy.class.getClassLoader();

    abstract public Object invokeSuper(Object obj, Object[] args) throws Throwable;
    
    abstract public Object invoke(Object obj, Object[] args) throws Throwable;

    protected MethodProxy() { }

    public static MethodProxy create(Method method, Method superMethod) {
        return create(method, superMethod, null);
    }

    public static MethodProxy create(Method method, Method superMethod, ClassLoader loader) {
        try {
            Class declaring = superMethod.getDeclaringClass();
            String className = NAME_FACTORY.getNextName(declaring);
            if (loader == null) {
                loader = declaring.getClassLoader();
                if (loader == null) {
                    loader = DEFAULT_LOADER;
                }
            }
            Class gen = new Generator(className, superMethod, method , loader).define();
            return (MethodProxy)gen.getConstructor(Constants.TYPES_EMPTY).newInstance(null);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

   static class Generator extends CodeGenerator {
        private Method method;
        private Method superMethod;
        
        public Generator(String className, Method superMethod, Method method, ClassLoader loader) {
            super(className, MethodProxy.class, loader);
            this.method = method;
            this.superMethod = superMethod;
        }

     public void generate() {
         generateNullConstructor();  
         generate(MethodProxy.INVOKE,method);
         generate(MethodProxy.INVOKE_SUPER,superMethod);
       }
       private void generate(Method proxyMethod, Method method) {
            
            begin_method(proxyMethod);
            if( Modifier.isProtected( method.getModifiers() ) ){
              throw_exception(IllegalAccessException.class, "not public method: " + method );
            }else{
            load_arg(0);
            checkcast(method.getDeclaringClass());
            Class[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                load_arg(1);
                push(i);
                aaload();
                unbox(types[i]);
            }
            this.invoke(method);
            box(method.getReturnType());
            }
            return_value();
            end_method();
        }
    }
}
