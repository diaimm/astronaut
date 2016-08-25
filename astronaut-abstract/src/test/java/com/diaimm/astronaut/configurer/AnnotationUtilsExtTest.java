package com.diaimm.astronaut.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject;
import com.diaimm.astronaut.configurer.annotations.method.PostForObject;
import com.diaimm.astronaut.configurer.annotations.method.Put;
import com.google.common.base.Supplier;

public class AnnotationUtilsExtTest {
	@Test
	public void cannotMakeAnyInstance() {
		try {
			Constructor<?> constructor = AnnotationUtilsExt.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			constructor.newInstance();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
			Assert.assertEquals("instantiation is not allowed", e.getCause().getMessage());
		}
	}

	@Test
	public void findTest() throws NoSuchMethodException, SecurityException {
		Annotation[] annotations = SampleClass.class.getDeclaredMethod("annotatedMethod").getAnnotations();

		Assert.assertTrue(AnnotationUtilsExt.find(annotations, GetForObject.class).isPresent());
		Assert.assertFalse(AnnotationUtilsExt.find(annotations, Put.class).isPresent());
	}
	
	@Test
	public void containsTest() throws NoSuchMethodException, SecurityException{
		Annotation[] annotations = SampleClass.class.getDeclaredMethod("annotatedMethod").getAnnotations();

		Assert.assertTrue(AnnotationUtilsExt.contains(annotations, GetForObject.class));
		Assert.assertFalse(AnnotationUtilsExt.contains(annotations, Put.class));
	}
	
	@Test
	public void findAnyAnnotationAnnotatedWithTest() throws NoSuchMethodException, SecurityException{
		Annotation[] annotations = SampleClass.class.getDeclaredMethod("annotatedMethod").getAnnotations();
		
		Assert.assertTrue(AnnotationUtilsExt.findAnyAnnotationAnnotatedWith(annotations, APIMapping.class).isPresent());
		Assert.assertFalse(AnnotationUtilsExt.findAnyAnnotationAnnotatedWith(annotations, Transaction.class).isPresent());
	}

	private static interface SampleClass {
		@GetForObject(dummySupplier = DummySupplier.class)
		@PostForObject(dummySupplier = DummySupplier.class)
		void annotatedMethod();
	}

	private static class DummySupplier implements Supplier<String> {
		@Override
		public String get() {
			return null;
		}

	}
}
