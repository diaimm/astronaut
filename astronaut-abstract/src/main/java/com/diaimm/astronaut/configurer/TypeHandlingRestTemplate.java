package com.diaimm.astronaut.configurer;

import java.lang.reflect.Type;

import org.springframework.web.client.RestOperations;

public interface TypeHandlingRestTemplate extends RestOperations {
	<T> T getForObject(String url, Type responseType, Object... urlVariables);

	<T> T postForObject(String url, Object request, Type responseType, Object... uriVariables);

	<T> T putForObject(String url, Object request, Type responseType, Object... uriVariables);

	<T> T deleteForObject(String url, Object request, Type responseType, Object... uriVariables);

	<T> T put(String url, Object request, Type responseType, Object... uriVariables);
}
