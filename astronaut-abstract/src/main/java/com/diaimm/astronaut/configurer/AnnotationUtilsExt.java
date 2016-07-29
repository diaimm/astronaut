package com.diaimm.astronaut.configurer;

import java.lang.annotation.Annotation;

public class AnnotationUtilsExt extends org.springframework.core.annotation.AnnotationUtils {
	private AnnotationUtilsExt() {
		throw new UnsupportedOperationException("instantiation is not allowed");
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> A find(Annotation[] annotations, Class<A> target) {
		for (Annotation annotation : annotations) {
			if (target.isAssignableFrom(annotation.annotationType())) {
				return (A) annotation;
			}
		}

		return null;
	}

	public static boolean contains(Annotation[] annotations, Class<? extends Annotation> annotationType) {
		for (Annotation annotation : annotations) {
			if (annotationType.isAssignableFrom(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}
}
