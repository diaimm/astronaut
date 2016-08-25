package com.diaimm.astronaut.configurer;

import org.junit.Test;
import org.mockito.Mockito;

import com.diaimm.astronaut.configurer.APIResponse.RestAPIException;
import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;

import junit.framework.Assert;

public class APIResponseTest {
	@Test
	public void testAll() {
		APIResponse<String> success = APIResponse.getInstance("url", new Object[0], "Sample", Mockito.mock(APICallInfoCompactizer.class));
		Assert.assertNotNull(success);
		Assert.assertTrue(success.isSuccess());
		Assert.assertEquals("Sample", success.getContents());
		Assert.assertEquals("url", success.getApiUrl());
		Assert.assertEquals(200, success.getCode());
		Assert.assertEquals("Sample", success.or("onFailResult"));
		Assert.assertEquals("Sample", success.orThrow());

		APIResponse<String> failWithRestAPIException = APIResponse.getInstance("url", new Object[0], new RestAPIException(500, "Test111"),
			Mockito.mock(APICallInfoCompactizer.class));
		Assert.assertNotNull(failWithRestAPIException);
		Assert.assertFalse(failWithRestAPIException.isSuccess());
		Assert.assertEquals("Test111", failWithRestAPIException.getMessage());
		Assert.assertEquals(500, failWithRestAPIException.getRestApiClientException().getStatus());
		Assert.assertEquals(500, failWithRestAPIException.getCode());
		Assert.assertEquals("onFailResult", failWithRestAPIException.or("onFailResult"));
		try {
			failWithRestAPIException.orThrow();
			Assert.fail();
		} catch (RestAPIException e) {
			Assert.assertEquals("Test111", e.getMessage());
		}

		APIResponse<String> failWithOtherException = APIResponse.getInstance("url", new Object[0], new NullPointerException("Test"),
			Mockito.mock(APICallInfoCompactizer.class));
		Assert.assertNotNull(failWithOtherException);
		Assert.assertFalse(failWithOtherException.isSuccess());
		Assert.assertEquals("Test", failWithOtherException.getMessage());
		Assert.assertEquals(503, failWithOtherException.getRestApiClientException().getStatus());
		Assert.assertEquals(503, failWithOtherException.getCode());
		Assert.assertEquals("onFailResult", failWithOtherException.or("onFailResult"));
		try {
			failWithOtherException.orThrow();
			Assert.fail();
		} catch (RestAPIException e) {
			Assert.assertEquals("Test", e.getMessage());
		}

	}
}
