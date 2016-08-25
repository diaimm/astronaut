package com.diaimm.astronaut.configurer;

import java.lang.annotation.Annotation;

import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.google.common.base.Optional;

public class AnnotationUtilsExt extends org.springframework.core.annotation.AnnotationUtils {
	private AnnotationUtilsExt() {
		throw new UnsupportedOperationException("instantiation is not allowed");
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> Optional<A> find(Annotation[] annotations, Class<A> target) {
		for (Annotation annotation : annotations) {
			if (target.isAssignableFrom(annotation.annotationType())) {
				return Optional.of((A) annotation);
			}
		}

		return Optional.absent();
	}

	public static boolean contains(Annotation[] annotations, Class<? extends Annotation> annotationType) {
		for (Annotation annotation : annotations) {
			if (annotationType.isAssignableFrom(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	public static Optional<Annotation> findAnyAnnotationAnnotatedWith(Annotation[] annotations, Class<? extends Annotation> annotatedWith) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().isAnnotationPresent(annotatedWith)) {
				return Optional.of(annotation);
			}
		}
		
		return Optional.absent();
	}
}
