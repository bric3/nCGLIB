package org.bytecodefx.testutils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.deepToString;
import static java.util.Collections.emptyList;

public abstract class ClassMemberMatcher {

    public static DeclaredMethodMemberMatcher declaredMethod(String name) {
        return new DeclaredMethodMemberMatcher(name);
    }

    public abstract <T> boolean isDeclaredIn(Class<T> actual);


    public static class DeclaredMethodMemberMatcher extends ClassMemberMatcher {
        private final String methodName;
        private ParameterMatcher[] parameterMatchers = new ParameterMatcher[0];
        private List<Class<? extends Annotation>> annotations = emptyList();
        private boolean assertNoArgs = false;

        public DeclaredMethodMemberMatcher(String methodName) {
            this.methodName = methodName;
        }

        public DeclaredMethodMemberMatcher withArgs(ParameterMatcher... parameterMatchers) {
            this.parameterMatchers = parameterMatchers;
            this.assertNoArgs = false;
            return this;
        }

        public DeclaredMethodMemberMatcher noArgs() {
            this.parameterMatchers = new ParameterMatcher[0];
            this.assertNoArgs = true;
            return this;
        }

        public DeclaredMethodMemberMatcher annotatedBy(Class<? extends Annotation>... annotations) {
            this.annotations = asList(annotations);
            return this;
        }

        @Override public <T> boolean isDeclaredIn(Class<T> actual) {
            for (Method method : actual.getDeclaredMethods()) {
                if (!(methodName != null && methodName.equals(method.getName()))) {
                    continue;
                }
                if (assertNoArgs && method.getParameterTypes().length > 0) {
                    continue;
                }
                if (!parametersMatch(method)) {
                    continue;
                }
                if (!annotationsContainedIn(annotationTypesOf(method.getDeclaredAnnotations()), annotations)) {
                    continue;
                }
                return true;
            }
            return false;
        }

        private boolean parametersMatch(Method method) {
            if (parameterMatchers.length == 0) {
                return true;
            }
            if (parameterMatchers.length != method.getParameterTypes().length) {
                return false;
            }
            for (int i = 0, parameterMatchersLength = parameterMatchers.length; i < parameterMatchersLength; i++) {
                ParameterMatcher parameterMatcher = parameterMatchers[i];
                if (!parameterMatcher.matches(method.getParameterTypes()[i], method.getParameterAnnotations()[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("method <");
            if (methodName != null) {
                sb.append('\'').append(methodName).append('\'');
            }
            if (assertNoArgs) {
                sb.append(" without arguments ");
            }
            if (parameterMatchers.length > 0) {
                sb.append(" with ").append(deepToString(parameterMatchers));
            }
            if (!annotations.isEmpty()) {
                sb.append(" annotated by ").append(annotations);
            }
            sb.append('>');
            return sb.toString();
        }

    }

    public static DeclaredConstructorMemberMatcher declaredConstructor() {
        return new DeclaredConstructorMemberMatcher();
    }

    public static class DeclaredConstructorMemberMatcher extends ClassMemberMatcher {
        private ParameterMatcher[] parameterMatchers = new ParameterMatcher[0];
        private List<Class<? extends Annotation>> annotations = emptyList();
        private boolean assertNoArgs = false;


        public DeclaredConstructorMemberMatcher withArgs(ParameterMatcher... parameterMatchers) {
            this.parameterMatchers = parameterMatchers;
            this.assertNoArgs = false;
            return this;
        }

        public DeclaredConstructorMemberMatcher noArgs() {
            this.parameterMatchers = new ParameterMatcher[0];
            this.assertNoArgs = true;
            return this;
        }

        public DeclaredConstructorMemberMatcher annotatedBy(Class<? extends Annotation>... annotations) {
            this.annotations = asList(annotations);
            return this;
        }

        @Override public <T> boolean isDeclaredIn(Class<T> actual) {
            for (Constructor<?> constructor : actual.getDeclaredConstructors()) {
                if (assertNoArgs && constructor.getParameterTypes().length > 0) {
                    continue;
                }
                if (!parametersMatch(constructor)) {
                    continue;
                }
                if (!annotationsContainedIn(annotationTypesOf(constructor.getDeclaredAnnotations()), annotations)) {
                    continue;
                }
                return true;
            }
            return false;
        }

        private boolean parametersMatch(Constructor<?> constructor) {
            if (parameterMatchers.length == 0) {
                return true;
            }
            if (parameterMatchers.length != constructor.getParameterTypes().length) {
                return false;
            }
            for (int i = 0, parameterMatchersLength = parameterMatchers.length; i < parameterMatchersLength ; i++) {
                ParameterMatcher parameterMatcher = parameterMatchers[i];
                if (!parameterMatcher.matches(constructor.getParameterTypes()[i], constructor.getParameterAnnotations()[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            if (assertNoArgs) {
                sb.append("<default constructor");
            } else {
                sb.append("<constructor");
            }
            if (parameterMatchers.length > 0) {
                sb.append(" with ").append(deepToString(parameterMatchers));
            }
            if (!annotations.isEmpty()) {
                sb.append(" annotated by ").append(annotations);
            }
            sb.append('>');
            return sb.toString();
        }
    }

    public static ParameterMatcher arg(Class<?> argType) {
        return new ParameterMatcher(argType);
    }

    public static class ParameterMatcher {
        private final Class<?> argType;
        private List<Class<? extends Annotation>> annotations = emptyList();

        public ParameterMatcher(Class<?> argType) {
            this.argType = argType;
        }

        public ParameterMatcher annotatedBy(Class<? extends Annotation>... annotations) {
            this.annotations = Arrays.asList(annotations);
            return this;
        }

        public boolean matches(Class<?> paramType, Annotation[] paramAnnotations) {
            if (argType != paramType) {
                return false;
            }
            if (!annotationsContainedIn(annotationTypesOf(paramAnnotations), annotations)) {
                return false;
            }
            return true;
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("argument of type '").append(argType.getCanonicalName()).append('\'');
            if (!annotations.isEmpty()) {
                sb.append(" annotated by ").append(annotations);
            }
            return sb.toString();
        }
    }


    static boolean annotationsContainedIn(List<Class<? extends Annotation>> actualAnnotations, List<Class<? extends Annotation>> expectedAnnotations) {
        for (Class<? extends Annotation> annotation : expectedAnnotations) {
            if (!actualAnnotations.contains(annotation)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    static List<Class<? extends Annotation>> annotationTypesOf(Annotation... annotations) {
        List annotationTypes = new ArrayList(annotations.length);
        for (Annotation annotation : annotations) {
            annotationTypes.add(annotation.annotationType());
        }
        return annotationTypes;
    }

}
