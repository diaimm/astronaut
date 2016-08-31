package com.diaimm.astronaut.configurer;

import java.lang.reflect.Type;

import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestOperations;

public interface TypeHandlingRestOperations extends RestOperations {
	<T> T getForObject(String url, Type responseType, Object... urlVariables);

	<T> T postForObject(String url, Object request, Type responseType, Object... uriVariables);

	ClientHttpRequestFactory getRequestFactory();

	RequestCallback acceptHeaderRequestCallback(Type responseType);

	RequestCallback httpEntityCallback(HttpEntity<?> request, Type responseType);
}
