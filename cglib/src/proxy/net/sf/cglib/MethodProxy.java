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

import java.lang.reflect.*;
import net.sf.cglib.util.*;
import net.sf.cglib.reflect.*;

/**
 * Classes generated by Enhancer pass this object to the
 * registered MethodInterceptors when an intercepted method is invoked. It can
 * be used to either invoke the original method, or call the same method on a different
 * object of the same type.
 * @see Enhancer
 * @see MethodInterceptor
 * @version $Id: MethodProxy.java,v 1.25 2003-09-10 17:49:10 herbyderby Exp $
 */
public class MethodProxy {
    private Invocable method;
    private Invocable superMethod;

    public static MethodProxy create(Method method, Method superMethod) {
        return new MethodProxy(method, superMethod);
    }

    protected MethodProxy(Method method, Method superMethod) {
        ClassLoader loader = superMethod.getDeclaringClass().getClassLoader();
        this.method = createMethod(method);
        this.superMethod = createMethod(superMethod);
    }

    private static Invocable createMethod(Method method) {
        if (Modifier.isProtected(method.getModifiers())) {
            final String sig = ReflectUtils.getMethodDescriptor(method);
            return new Invocable() {
                public Object invoke(Object obj, Object[] args) throws IllegalAccessException {
                    throw new IllegalAccessException("Protected method: " + sig);
                }
            };
        }
        return FastClass.create(method.getDeclaringClass()).getMethod(method);
    }

    /**
     * Invoke the original (super) method on the specified object.
     * @param obj the enhanced object, must be the object passed as the first
     * argument to the MethodInterceptor
     * @param args the arguments passed to the intercepted method; you may substitute a different
     * argument array as long as the types are compatible
     * @see MethodInterceptor#intercept
     */
    public Object invokeSuper(Object obj, Object[] args) throws Throwable {
        return superMethod.invoke(obj, args);
    }

    /**
     * Invoke the original method, on a different object of the same type.
     * @param obj the compatible object; recursion will result if you use the object passed as the first
     * argument to the MethodInterceptor (usually not what you want)
     * @param args the arguments passed to the intercepted method; you may substitute a different
     * argument array as long as the types are compatible
     * @see MethodInterceptor#intercept
     */
    public Object invoke(Object obj, Object[] args) throws Throwable {
        return method.invoke(obj, args);
    }
}
