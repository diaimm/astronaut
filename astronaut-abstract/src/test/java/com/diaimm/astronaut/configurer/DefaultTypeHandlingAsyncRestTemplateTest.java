package com.diaimm.astronaut.configurer;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.util.UriTemplateHandler;

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
	}
}
