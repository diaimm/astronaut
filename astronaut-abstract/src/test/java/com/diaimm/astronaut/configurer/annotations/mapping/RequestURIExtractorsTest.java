package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors.APIMethodInvocation;
import com.google.common.base.Optional;

public class RequestURIExtractorsTest {
	@Test 
	public void initRequestURIExtractorsTest(){
		APIMethodInvocation target = new APIMethodInvocation(null, null);
		Assert.assertNotNull(target);
	}
	
	@Test
	public void initTest() throws NoSuchMethodException {
		try {
			RequestURIExtractors.class.getConstructor();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(NoSuchMethodException.class, e.getClass());
		}

		Constructor<RequestURIExtractors> constructor = RequestURIExtractors.class.getDeclaredConstructor();
		try {
			constructor.newInstance();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(IllegalAccessException.class, e.getClass());
		}

		try {
			constructor.setAccessible(true);
			constructor.newInstance();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals("initiation is not allowed", e.getCause().getMessage());
			Assert.assertEquals(InvocationTargetException.class, e.getClass());
		}
	}

	@Test
	public void fromAnnotationTest() throws Exception {
		Method sampleMethod1 = SampleClass.class.getDeclaredMethod("sampleMethod1");
		Optional<SampleAnnotation1> found = AnnotationUtilsExt.find(sampleMethod1.getAnnotations(), SampleAnnotation1.class);
		Optional<String> apply = RequestURIExtractors.fromAnnotation.apply(found.get());
		Assert.assertFalse(apply.isPresent());
	}

	@Test
	public void fromArguementsTest() throws NoSuchMethodException, SecurityException {
		APIMethodInvocation paramMock = Mockito.mock(APIMethodInvocation.class);
		Mockito.when(paramMock.getApiMethod()).thenReturn(SampleClass.class.getMethod("sampleMethod2", String.class));
		Mockito.when(paramMock.getArgs()).thenReturn(new Object[] { null });
		Optional<String> applied = RequestURIExtractors.fromArguements.apply(paramMock);
		Assert.assertFalse(applied.isPresent());
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

		@SampleAnnotation1
		void sampleMethod2(String param1);
	}
}
