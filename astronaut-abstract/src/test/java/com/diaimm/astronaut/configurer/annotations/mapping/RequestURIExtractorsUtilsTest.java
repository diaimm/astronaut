package com.diaimm.astronaut.configurer.annotations.mapping;

import org.junit.Assert;
import org.junit.Test;

import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractorsUtils;
import com.google.common.base.Optional;

public class RequestURIExtractorsUtilsTest {
	@Test
	public void findRequestURIFromFieldsTest() {
		Optional<String> result = RequestURIExtractorsUtils.findRequestURIFromFields(null);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample0());
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample1());
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromFields(new RequestURISample2());
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals("sample", result.get());
	}

	@Test
	public void findRequestURIFromMethodTest() {
		Optional<String> result = RequestURIExtractorsUtils.findRequestURIFromMethod(null);
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample0());
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample1());
		Assert.assertFalse(result.isPresent());

		result = RequestURIExtractorsUtils.findRequestURIFromMethod(new RequestURISample2());
		Assert.assertTrue(result.isPresent());
		Assert.assertEquals("sample", result.get());
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
		public void method1() {
		}

		@RequestURI
		public String method2() {
			return null;
		}

		@RequestURI
		public String method3() {
			return "sample";
		}
	}
}
