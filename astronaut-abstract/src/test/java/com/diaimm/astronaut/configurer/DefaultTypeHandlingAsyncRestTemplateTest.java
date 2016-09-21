package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.util.UriTemplateHandler;

import com.diaimm.astronaut.configurer.DefaultTypeHandlingAsyncRestTemplate.AsyncClientHttpRequestInvoker;
import com.diaimm.astronaut.configurer.DefaultTypeHandlingAsyncRestTemplate.AsyncRequestCallbackAdapter;

public class DefaultTypeHandlingAsyncRestTemplateTest {
	@Test
	public void getForEntityTest() throws URISyntaxException {
		DefaultTypeHandlingRestTemplate implicit = Mockito.mock(DefaultTypeHandlingRestTemplate.class);
		ClientHttpRequestFactory requestFactory = Mockito.mock(ClientHttpRequestFactory.class);
		Mockito.when(implicit.getRequestFactory()).thenReturn(requestFactory);

		UriTemplateHandler uriTemplateHandler = Mockito.mock(UriTemplateHandler.class);
		Mockito.when(uriTemplateHandler.expand("url", new Object[0])).thenReturn(new URI("http://some.url.com"));

		Mockito.when(implicit.getUriTemplateHandler()).thenReturn(uriTemplateHandler);

		DefaultTypeHandlingAsyncRestTemplate target = new DefaultTypeHandlingAsyncRestTemplate(implicit);
		ListenableFuture<ResponseEntity<Object>> forEntity = target.getForEntity("url", (Type) Mockito.anyObject(),
			new Object[0]);
		Assert.assertNotNull(forEntity);

		ListenableFuture<ResponseEntity<Object>> forEntity2 = target.postForEntity("url", (HttpEntity<?>) Mockito.anyObject(),
			(Type) Mockito.anyObject(),
			new Object[0]);
		Assert.assertNotNull(forEntity2);
	}

	@Test
	public void AsyncRequestCallbackAdapter_doWithRequestTest() {
		AsyncRequestCallbackAdapter target = new AsyncRequestCallbackAdapter(null);
		try {
			target.doWithRequest(null);
		} catch (IOException e) {
			Assert.fail();
		}

		RequestCallback requestCallback = Mockito.mock(RequestCallback.class);
		target = new AsyncRequestCallbackAdapter(requestCallback);
		try {
			AsyncClientHttpRequest asyncClientHttpRequest = Mockito.mock(AsyncClientHttpRequest.class);
			target.doWithRequest(asyncClientHttpRequest);

			Mockito.verify(requestCallback).doWithRequest((ClientHttpRequest) Mockito.anyObject());
		} catch (IOException e) {
			Assert.fail();
		}
	}

	@Test
	public void AsyncClientHttpRequestInvokerTest() throws IOException {
		AsyncClientHttpRequest asyncClientHttpRequest = Mockito.mock(AsyncClientHttpRequest.class);
		AsyncClientHttpRequestInvoker target = new AsyncClientHttpRequestInvoker(asyncClientHttpRequest);
		try {
			target.execute();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(UnsupportedOperationException.class, e.getClass());
		}
		target.getBody();
		Mockito.verify(asyncClientHttpRequest).getBody();
		
		target.getMethod();
		Mockito.verify(asyncClientHttpRequest).getMethod();
		
		target.getURI();
		Mockito.verify(asyncClientHttpRequest).getURI();
		
		target.getHeaders();
		Mockito.verify(asyncClientHttpRequest).getHeaders();
	}
}
