package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

import com.diaimm.astronaut.configurer.DefaultTypeHandlingRestTemplate.HttpAccessorLogger;
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

		Mockito.reset(headers);
		restTemplate.setMessageConverters(Lists.newArrayList(Mockito.mock(HttpMessageConverter.class)));
		Mockito.verify(headers, Mockito.never()).setAccept((List<MediaType>) Mockito.anyList());
	}

	@Test
	public void HttpAccessorLogger_Test() {
		Log logger = Mockito.mock(Log.class);
		HttpAccessorLogger accessorLogger = new HttpAccessorLogger(logger);

		Mockito.when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		List<MediaType> mediaTypes = Lists.newArrayList();
		MediaType mediaType = Mockito.mock(MediaType.class);
		String body = "test";
		HttpMessageConverter httpMessageConverter = Mockito.mock(HttpMessageConverter.class);
		accessorLogger.logIfNeed(mediaTypes);
		accessorLogger.logIfNeed(body, mediaType, httpMessageConverter);

		Mockito.verify(logger, Mockito.times(2)).debug(Mockito.anyString());

		Mockito.reset(logger);
		Mockito.when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		accessorLogger.logIfNeed(mediaTypes);
		accessorLogger.logIfNeed(body, null, httpMessageConverter);
		Mockito.verify(logger, Mockito.times(2)).debug(Mockito.anyString());
		
		Mockito.reset(logger);
		Mockito.when(logger.isDebugEnabled()).thenReturn(Boolean.FALSE);
		accessorLogger.logIfNeed(mediaTypes);
		accessorLogger.logIfNeed(body, null, httpMessageConverter);
		Mockito.verify(logger, Mockito.never()).debug(Mockito.anyString());
	}

	private DefaultTypeHandlingRestTemplate createTarget() {
		return new DefaultTypeHandlingRestTemplate(300, 100, 100, 10, Lists.<Header> newArrayList());
	}
}
