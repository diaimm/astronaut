package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Header;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.Lists;

public class DefaultTypeHandlingRestTemplateTest {
	@Test
	public void getForObjectTest() throws URISyntaxException {
		DefaultTypeHandlingRestTemplate target = createTarget();
		try {
			target.getForObject("url", (Type) String.class, new Object[0]);
		} catch (Exception e) {
			Assert.assertEquals(ResourceAccessException.class, e.getClass());
		}

		try {
			target.postForObject("url", "", (Type) String.class, new Object[0]);
		} catch (Exception e) {
			Assert.assertEquals(ResourceAccessException.class, e.getClass());
		}

		DefaultTypeHandlingRestTemplate target2 = new DefaultTypeHandlingRestTemplate(300, 100, 100, 10, Lists.<Header> newArrayList()) {
			@Override
			public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
				ResponseExtractor<T> responseExtractor, Object... urlVariables) throws RestClientException {
				return null;
			}
		};
		Assert.assertNull(target2.getForObject("url", (Type) String.class, new Object[0]));
		Assert.assertNull(target2.postForObject("url", "", (Type) String.class, new Object[0]));
	}

	@Test
	public void acceptHeaderRequestCallbackTest() {
		DefaultTypeHandlingRestTemplate target = createTarget();
		RequestCallback callback = target.acceptHeaderRequestCallback((Type) String.class);
		Assert.assertNotNull(callback);
	}

	@Test
	public void httpEntityCallbackTest() {
		DefaultTypeHandlingRestTemplate target = createTarget();
		RequestCallback callback = target.httpEntityCallback(HttpEntity.EMPTY, (Type) String.class);
		Assert.assertNotNull(callback);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void AcceptHeaderRequestCallback_doWithRequestTest() throws IOException {
		DefaultTypeHandlingRestTemplate restTemplate = createTarget();
		RequestCallback target = restTemplate.acceptHeaderRequestCallback(null);
		target.doWithRequest(null);

		ClientHttpRequest request = Mockito.mock(ClientHttpRequest.class);
		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		Mockito.when(request.getHeaders()).thenReturn(headers);
		target = restTemplate.acceptHeaderRequestCallback(String.class);
		target.doWithRequest(request);

		Mockito.verify(headers).setAccept((List<MediaType>) Mockito.anyList());
	}

	private DefaultTypeHandlingRestTemplate createTarget() {
		return new DefaultTypeHandlingRestTemplate(300, 100, 100, 10, Lists.<Header> newArrayList());
	}
}
