package com.diaimm.astronaut.configurer.factorybean;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.RestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.diaimm.astronaut.configurer.factorybean.RestTemplateRepositoryInvocationHandler.RestTemplateInvokerCache;
import com.google.common.base.Supplier;

public class RestTemplateRepositoryInvocationHandlerTest {
	@Test
	public void invokeTest() throws Exception {
		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		TypeHandlingAsyncRestOperations asyncRestTemplate = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		String apiURLPrefix = "http://this.is.url.prefix";
		URI uri = new URI("/and/this/is/the/path");
		RestTemplateRepositoryInvocationHandler target = new RestTemplateRepositoryInvocationHandler(restTemplate, asyncRestTemplate, uri,
			apiURLPrefix);

		Method someMethod = SomeRepository.class.getDeclaredMethod("someMethod2", String.class);
		Object invoked = target.invoke(new Object(), someMethod, new Object[] { "uri sample" });
		Assert.assertEquals("sampleReturn", invoked);
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
			Assert.assertEquals(3, invokerInstance.getAPIArgumentNomalizers().size());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
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
