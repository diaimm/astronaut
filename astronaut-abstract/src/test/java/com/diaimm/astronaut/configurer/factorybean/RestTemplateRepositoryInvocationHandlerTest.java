package com.diaimm.astronaut.configurer.factorybean;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.APIArgumentNormalizer;
import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.RestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.diaimm.astronaut.configurer.factorybean.RestTemplateRepositoryInvocationHandler.RestTemplateInvokerCache;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RestTemplateRepositoryInvocationHandlerTest {
	@Test
	public void fieldsTest() throws URISyntaxException {
		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		RestTemplateRepositoryInvocationHandler target = initTarget(restTemplate);
		Assert.assertEquals("http://this.is.url.prefix/and/this/is/the/path", target.getApiURLPrefix());
		Assert.assertEquals(restTemplate, target.getRestTemplate());
	}

	@Test
	public void invokeTest() throws Exception {
		RestTemplateRepositoryInvocationHandler target = initTarget(null);

		Method someMethod = SomeRepository.class.getDeclaredMethod("someMethod2", String.class);
		Object invoked = target.invoke(new Object(), someMethod, new Object[] { "uri sample" });
		Assert.assertEquals("sampleReturn", invoked);
	}

	private RestTemplateRepositoryInvocationHandler initTarget(TypeHandlingRestOperations restTemplate) throws URISyntaxException {
		if (restTemplate == null) {
			restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		}
		TypeHandlingAsyncRestOperations asyncRestTemplate = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		String apiURLPrefix = "http://this.is.url.prefix";
		URI uri = new URI("/and/this/is/the/path");
		RestTemplateRepositoryInvocationHandler target = new RestTemplateRepositoryInvocationHandler(restTemplate, asyncRestTemplate, uri,
			apiURLPrefix);
		return target;
	}

	@Test
	public void RestTemplateInvokerCacheTest() {
		try {
			Method someMethod = SomeRepository.class.getDeclaredMethod("someMethod");
			Class<? extends Annotation> annotationType = APICallerSample.class;
			RestTemplateInvoker<?> invokerInstance = RestTemplateInvokerCache.getInvokerInstance(annotationType,
				someMethod.getAnnotation(APIMapping.class));

			RestTemplateInvoker<?> invokerInstance2 = RestTemplateInvokerCache.getInvokerInstance(annotationType,
				someMethod.getAnnotation(APIMapping.class));

			Assert.assertEquals(invokerInstance, invokerInstance2);

			Map<Class<?>, APIArgumentNormalizer<?>> apiArgumentNomalizers = invokerInstance2.getAPIArgumentNomalizers();
			Assert.assertEquals(3, apiArgumentNomalizers.size());
			dateNormalizerTest(apiArgumentNomalizers);
			listNormalizerTest(apiArgumentNomalizers);
			mapNormalizerTest(apiArgumentNomalizers);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void listNormalizerTest(Map<Class<?>, APIArgumentNormalizer<?>> apiArgumentNomalizers) {
		Assert.assertEquals("[{\"field2\":null}]",
			((APIArgumentNormalizer<Collection>) apiArgumentNomalizers.get(Collection.class)).normalize(
				(Collection) Lists.newArrayList(new SampleToStringify())));
		try {

			Assert.assertEquals("[{\"field2\":null}]",
				((APIArgumentNormalizer<Collection>) apiArgumentNomalizers.get(Collection.class)).normalize(
					(Collection) Lists.newArrayList(new SampleToStringify() {
						public String getField2() {
							throw new UnsupportedOperationException("test");
						}
					})));
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(IllegalArgumentException.class, e.getClass());
			Assert.assertEquals("test", e.getCause().getCause().getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void mapNormalizerTest(Map<Class<?>, APIArgumentNormalizer<?>> apiArgumentNomalizers) {
		Map<String, String> sample1 = Maps.newHashMap();
		sample1.put("test", "sample");
		Assert.assertEquals("{\"test\":\"sample\"}",
			((APIArgumentNormalizer<Map>) apiArgumentNomalizers.get(Map.class)).normalize((Map) sample1));
		try {
			Map<String, SampleToStringify> sample2 = Maps.newHashMap();
			sample2.put("test", new SampleToStringify() {
				public String getField2() {
					throw new UnsupportedOperationException("test");
				}
			});

			Assert.assertEquals("{\"test\":{\"field2\":null}}",
				((APIArgumentNormalizer<Map>) apiArgumentNomalizers.get(Map.class)).normalize((Map) sample2));
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(IllegalArgumentException.class, e.getClass());
			Assert.assertEquals("test", e.getCause().getCause().getMessage());
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void dateNormalizerTest(Map<Class<?>, APIArgumentNormalizer<?>> apiArgumentNomalizers) {
		DateTime now = DateTime.now();
		Assert.assertEquals(now.getMillis(), ((APIArgumentNormalizer<DateTime>) apiArgumentNomalizers.get(DateTime.class)).normalize(now));
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@APIMapping(handler = RestTemplateInvokerSample.class)
	static @interface APICallerSample {
	}

	private static interface SomeRepository {
		@APIMapping(handler = RestTemplateInvokerSample.class)
		void someMethod();

		@APICallerSample
		String someMethod2(@RequestURI String requestURI);
	}

	public static class SampleToStringify {
		public String getField2() {
			return null;
		}
	}

	public static class DummySupplierSample implements Supplier<String> {
		@Override
		public String get() {
			return "test";
		}
	}

	private static class RestTemplateInvokerSample extends AbstractRestTemplateInvoker<APICallerSample> {
		@Override
		protected Object doInvoke(TypeHandlingRestOperations restTemplate,
			com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<APICallerSample> compactizer, Type returnType,
			APICallerSample annotation) throws Exception {
			return "sampleReturn";
		}

		@Override
		protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
			com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<APICallerSample> compactizer, Type returnType,
			APICallerSample annotation) throws Exception {
			return new AsyncResult<String>("sampleReturn");
		}
	}
}
