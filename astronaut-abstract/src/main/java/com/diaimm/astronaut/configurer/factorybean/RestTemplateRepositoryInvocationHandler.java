package com.diaimm.astronaut.configurer.factorybean;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diaimm.astronaut.configurer.APIArgumentNormalizer;
import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.RestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@SuppressWarnings({ "rawtypes", "unchecked" })
class RestTemplateRepositoryInvocationHandler implements InvocationHandler {
	private static final Logger log = LoggerFactory.getLogger(RestTemplateRepositoryInvocationHandler.class);
	private TypeHandlingRestTemplate restTemplate;
	private String apiURLPrefix;

	RestTemplateRepositoryInvocationHandler(TypeHandlingRestTemplate restTemplate, URI apiURI, String pathPrefix) {
		this.apiURLPrefix = apiURI + pathPrefix;
		this.restTemplate = restTemplate;
	}

	@Override
	public Object invoke(Object instance, Method method, Object[] args) {
		Annotation[] annotations = method.getAnnotations();

		Optional<Annotation> apiMappingAnnotation = AnnotationUtilsExt.findAnyAnnotationAnnotatedWith(annotations, APIMapping.class);
		if (!apiMappingAnnotation.isPresent()) {
			return null;
		}

		Annotation annotation = apiMappingAnnotation.get();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		try {
			RestTemplateInvoker invokerInstance = RestTemplateInvokerCache.getInvokerInstance(annotationType,
				annotationType.getAnnotation(APIMapping.class));
			String apiURL = apiURLPrefix + invokerInstance.extractAPIUrl(annotation, method, args);
			return invokerInstance.invoke(restTemplate, apiURL, method, annotation, args);
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	static class RestTemplateInvokerCache {
		private static final Map<Class<? extends Annotation>, RestTemplateInvoker<?>> restTemplateInvokerCache = Maps.newConcurrentMap();
		private static final Object restTemplateInvokerCacheLock = new Object();
		private static final ObjectMapper mapper = new ObjectMapper();

		static {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		static RestTemplateInvoker getInvokerInstance(Class<? extends Annotation> annotationType, APIMapping apiMapping)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			synchronized (restTemplateInvokerCacheLock) {
				if (!restTemplateInvokerCache.containsKey(annotationType)) {
					Class<? extends RestTemplateInvoker> handler = apiMapping.handler();
					Constructor<? extends RestTemplateInvoker> declaredConstructor = handler.getDeclaredConstructor();
					declaredConstructor.setAccessible(true);

					restTemplateInvokerCache.put(annotationType, addArgumentNormalizers(declaredConstructor.newInstance()));
				}

				return restTemplateInvokerCache.get(annotationType);
			}
		}

		private static RestTemplateInvoker addArgumentNormalizers(RestTemplateInvoker newInstance) {
			newInstance.addAPIArgumentNormalizer(DateTime.class, new APIArgumentNormalizer<DateTime>() {
				@Override
				public Object normalize(DateTime value) {
					return value.getMillis();
				}
			});

			newInstance.addAPIArgumentNormalizer(Collection.class, new APIArgumentNormalizer<Collection>() {
				@Override
				public Object normalize(Collection value) {
					try {
						return mapper.writeValueAsString(value);
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
				}
			});

			newInstance.addAPIArgumentNormalizer(Map.class, new APIArgumentNormalizer<Map>() {
				@Override
				public Object normalize(Map value) {
					try {
						return mapper.writeValueAsString(value);
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
				}
			});

			return newInstance;
		}
	}

	String getApiURLPrefix() {
		return this.apiURLPrefix;
	}

	TypeHandlingRestTemplate getRestTemplate() {
		return this.restTemplate;
	}
}
