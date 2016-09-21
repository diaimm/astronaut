package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRequestCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

public class DefaultTypeHandlingAsyncRestTemplate extends AsyncRestTemplate implements TypeHandlingAsyncRestOperations {
	public DefaultTypeHandlingAsyncRestTemplate(DefaultTypeHandlingRestTemplate restTemplate) {
		super(new SimpleClientHttpRequestFactoryWithSimpleAsyncTaskExecutor(), restTemplate);
	}

	@Override
	public <T> ListenableFuture<ResponseEntity<T>> getForEntity(String url, Type responseType, Object... uriVariables) throws RestClientException {
		TypeHandlingRestOperations restTemplate = (TypeHandlingRestOperations) this.getRestOperations();
		AsyncRequestCallback requestCallback = new AsyncRequestCallbackAdapter(restTemplate.acceptHeaderRequestCallback(responseType));
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ListenableFuture<ResponseEntity<T>> postForEntity(String url, HttpEntity<?> request, Type responseType, Object... uriVariables)
		throws RestClientException {
		TypeHandlingRestOperations restTemplate = (TypeHandlingRestOperations) this.getRestOperations();
		AsyncRequestCallback requestCallback = new AsyncRequestCallbackAdapter(restTemplate.httpEntityCallback(request, responseType));
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	private static class SimpleClientHttpRequestFactoryWithSimpleAsyncTaskExecutor extends SimpleClientHttpRequestFactory {
		private SimpleClientHttpRequestFactoryWithSimpleAsyncTaskExecutor() {
			super();
			this.setTaskExecutor(new SimpleAsyncTaskExecutor());
		}
	}
	
	static class AsyncClientHttpRequestInvoker implements ClientHttpRequest {
		private final AsyncClientHttpRequest request;

		public AsyncClientHttpRequestInvoker(AsyncClientHttpRequest request) {
			this.request = request;
		}

		@Override
		public ClientHttpResponse execute() throws IOException {
			throw new UnsupportedOperationException("execute not supported");
		}

		@Override
		public OutputStream getBody() throws IOException {
			return request.getBody();
		}

		@Override
		public HttpMethod getMethod() {
			return request.getMethod();
		}

		@Override
		public URI getURI() {
			return request.getURI();
		}

		@Override
		public HttpHeaders getHeaders() {
			return request.getHeaders();
		}
	}
	
	/**
	 * Adapts a {@link RequestCallback} to the {@link AsyncRequestCallback} interface.
	 */
	static class AsyncRequestCallbackAdapter implements AsyncRequestCallback {
		private final RequestCallback adaptee;

		/**
		 * Create a new {@code AsyncRequestCallbackAdapter} from the given
		 * {@link RequestCallback}.
		 * @param requestCallback the callback to base this adapter on
		 */
		public AsyncRequestCallbackAdapter(RequestCallback requestCallback) {
			this.adaptee = requestCallback;
		}

		@Override
		public void doWithRequest(AsyncClientHttpRequest request) throws IOException {
			if (this.adaptee != null) {
				this.adaptee.doWithRequest(new AsyncClientHttpRequestInvoker(request));
			}
		}
	}
}
