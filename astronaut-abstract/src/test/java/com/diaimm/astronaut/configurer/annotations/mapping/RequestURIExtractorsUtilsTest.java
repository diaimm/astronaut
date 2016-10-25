package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractorsUtils;
import com.google.common.base.Optional;

public class RequestURIExtractorsUtilsTest {
	@Test
	public void initTest() throws NoSuchMethodException {
		
		try{
			RequestURIExtractorsUtils.class.getConstructor();
			Assert.fail();
		} catch (Exception e){
			Assert.assertEquals(NoSuchMethodException.class, e.getClass());
		}
		
		Constructor<RequestURIExtractorsUtils> constructor = RequestURIExtractorsUtils.class.getDeclaredConstructor();
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
	public void findRequestURIFromFieldsTest() {
		Optional<String> result = RequestURIExtractorsUtils.findRequestURIFromFields(null, true);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample0(), true);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample1(), true);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample2(), true);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals("sample", result.get());
		
		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample2(), false);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void findRequestURIFromMethodTest() {
		Optional<String> result = RequestURIExtractorsUtils.findRequestURIFromMethod(null, true);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample0(), true);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample1(), true);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample2(), true);
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals("sample", result.get());
		
		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample2(), false);
		Assert.assertFalse(result.isPresent());
	}

	public static class RequestURISample0 {

	}

	public static class RequestURISample1 {
		private String field1;

		public String method1() {
			return "sample";
		}
	}

	public static class RequestURISample2 {
		private String field1;
		@RequestURI
		private Void field2;
		@RequestURI
		private String field3 = null;
		@RequestURI
		private String field4 = "sample";

		@RequestURI
		private void method1() {
		}

		@RequestURI
		private String method2() {
			return null;
		}

		@RequestURI
		private String method3() {
			return "sample";
		}
	}
}
