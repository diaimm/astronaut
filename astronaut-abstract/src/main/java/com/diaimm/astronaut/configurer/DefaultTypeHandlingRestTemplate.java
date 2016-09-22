package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class DefaultTypeHandlingRestTemplate extends RestTemplate implements TypeHandlingRestOperations {
	private final List<Header> defaultHeaders;
	private final int connectTimeout;
	private final int readTimeout;
	private final int maxConnTotal;
	private final int maxConnPerRoute;

	public DefaultTypeHandlingRestTemplate(int connectTimeout, int readTimeout, int maxConnTotal, int maxConnPerRoute,
		List<Header> defaultHeaders) {
		super();
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.maxConnTotal = maxConnTotal;
		this.maxConnPerRoute = maxConnPerRoute;
		this.defaultHeaders = defaultHeaders;
		this.setRequestFactory(httpRequestFactory());
	}

	protected final HttpComponentsClientHttpRequestFactory httpRequestFactory() {
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeout).setConnectTimeout(connectTimeout).build();
		HttpClient httpClient = HttpClientBuilder.create().useSystemProperties().setDefaultHeaders(defaultHeaders).setMaxConnTotal(
			maxConnTotal).setMaxConnPerRoute(maxConnPerRoute).setDefaultRequestConfig(requestConfig).build();
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	@Override
	public <T> T postForObject(String url, Object request, Type responseType, Object... uriVariables) {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> T getForObject(String url, Type responseType, Object... urlVariables) {
		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		HttpMessageConverterExtractor<T> responseExtractor = new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, urlVariables);
	}

	public RequestCallback acceptHeaderRequestCallback(Type responseType) {
		return new AcceptHeaderRequestCallback(responseType);
	}

	public RequestCallback httpEntityCallback(HttpEntity<?> requestBody, Type responseType) {
		return new HttpEntityRequestCallback(requestBody, responseType);
	}

	/**
	 * Request callback implementation that prepares the request's accept
	 * headers.
	 */
	private class AcceptHeaderRequestCallback implements RequestCallback {
		private final Type responseType;

		private AcceptHeaderRequestCallback(Type responseType) {
			this.responseType = responseType;
		}

		public void doWithRequest(ClientHttpRequest request) throws IOException {
			if (responseType == null) {
				return;
			}

			Class<?> responseClass = getResponseClass();
			List<MediaType> allSupportedMediaTypes = getMessageConverters().stream().filter(converter -> {
				if (responseClass != null && converter.canRead(responseClass, null)) {
					return true;
				}

				return converter instanceof GenericHttpMessageConverter;
			}).flatMap(converter -> getSupportedMediaTypes(converter).stream()).collect(Collectors.toList());

			if (allSupportedMediaTypes.isEmpty()) {
				return;
			}

			MediaType.sortBySpecificity(allSupportedMediaTypes);
			if (logger.isDebugEnabled()) {
				logger.debug("Setting request Accept header to " + allSupportedMediaTypes);
			}
			request.getHeaders().setAccept(allSupportedMediaTypes);
		}

		private Class<?> getResponseClass() {
			if (responseType instanceof Class) {
				return (Class<?>) responseType;
			}

			return null;
		}

		private List<MediaType> getSupportedMediaTypes(HttpMessageConverter<?> messageConverter) {
			return messageConverter.getSupportedMediaTypes().stream().map(supportedMediaType -> {
				if (supportedMediaType.getCharset() != null) {
					return new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
				}

				return supportedMediaType;
			}).collect(Collectors.toList());
		}
	}

	/**
	 * Request callback implementation that writes the given object to the
	 * request stream.
	 */
	private class HttpEntityRequestCallback extends AcceptHeaderRequestCallback {
		private final HttpEntity<?> requestEntity;

		private HttpEntityRequestCallback(Object requestBody) {
			this(requestBody, null);
		}

		private HttpEntityRequestCallback(Object requestBody, Type responseType) {
			super(responseType);
			if (requestBody instanceof HttpEntity) {
				this.requestEntity = (HttpEntity<?>) requestBody;
				return;
			}

			if (requestBody != null) {
				this.requestEntity = new HttpEntity<Object>(requestBody);
				return;
			}

			this.requestEntity = HttpEntity.EMPTY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
			super.doWithRequest(httpRequest);

			if (!requestEntity.hasBody()) {
				HttpHeaders httpHeaders = httpRequest.getHeaders();
				HttpHeaders requestHeaders = requestEntity.getHeaders();
				if (!requestHeaders.isEmpty()) {
					httpHeaders.putAll(requestHeaders);
				}

				if (httpHeaders.getContentLength() == -1) {
					httpHeaders.setContentLength(0L);
				}

				return;
			}

			Object requestBody = requestEntity.getBody();
			Class<?> requestType = requestBody.getClass();
			HttpHeaders requestHeaders = requestEntity.getHeaders();
			MediaType requestContentType = requestHeaders.getContentType();
			HttpMessageConverter<?> messageConverter = findMessageConverterForRequestBody(requestType, requestContentType);

			if (!requestHeaders.isEmpty()) {
				httpRequest.getHeaders().putAll(requestHeaders);
			}

			if (logger.isDebugEnabled()) {
				if (requestContentType != null) {
					logger.debug("Writing [" + requestBody + "] as \"" + requestContentType + "\" using [" + messageConverter + "]");
				} else {
					logger.debug("Writing [" + requestBody + "] using [" + messageConverter + "]");
				}

			}

			((HttpMessageConverter<Object>) messageConverter).write(requestBody, requestContentType, httpRequest);
		}

		private HttpMessageConverter<?> findMessageConverterForRequestBody(Class<?> requestType, MediaType requestContentType)
			throws RestClientException {
			Optional<HttpMessageConverter<?>> found = getMessageConverters().stream().filter(messageConverter -> {
				return messageConverter.canWrite(requestType, requestContentType);
			}).findFirst();

			HttpMessageConverter<?> messageConverter = found.orElseThrow(() -> {
				String message = "Could not write request: no suitable HttpMessageConverter found for request type [" + requestType.getName() + "]";
				if (requestContentType != null) {
					message += " and content type [" + requestContentType + "]";
				}
				throw new RestClientException(message);
			});
			return messageConverter;
		}
	}
}
