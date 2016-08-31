package com.diaimm.astronaut.configurer;

import java.lang.reflect.Type;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.RestClientException;

public interface TypeHandlingAsyncRestOperations extends AsyncRestOperations {
	<T> ListenableFuture<ResponseEntity<T>> getForEntity(String url, Type responseType,
		Object... uriVariables) throws RestClientException;

	<T> ListenableFuture<ResponseEntity<T>> postForEntity(String url, HttpEntity<?> request,
		Type responseType, Object... uriVariables) throws RestClientException;
}
