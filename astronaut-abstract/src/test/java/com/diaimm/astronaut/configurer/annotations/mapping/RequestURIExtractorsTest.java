package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors;
import com.google.common.base.Optional;

public class RequestURIExtractorsTest {
	@Test
	public void fromAnnotationTest() throws Exception {
		Method sampleMethod1 = SampleClass.class.getDeclaredMethod("sampleMethod1");
		Optional<SampleAnnotation1> find = AnnotationUtilsExt.find(sampleMethod1.getAnnotations(), SampleAnnotation1.class);
		Optional<String> apply = RequestURIExtractors.fromAnnotation.apply(find.get());
		Assert.assertFalse(apply.isPresent());
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	private static @interface SampleAnnotation1 {
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	private static @interface SampleAnnotation2 {
	}

	private static interface SampleClass {
		@SampleAnnotation1
		void sampleMethod1();
	}
}
