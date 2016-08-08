package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import org.springframework.http.HttpHeaders;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.google.common.base.Supplier;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = HeadForHeaders.RestTemplateInvoker.class)
public @interface HeadForHeaders {
	@RequestURI
	String url() default "";

	Class<? extends Supplier<?>> dummySupplier() default DummySupplierImpl.class;

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<HeadForHeaders> {
		@Override
		protected HttpHeaders doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<HeadForHeaders> compactizer,
			Type returnType, HeadForHeaders annotation) throws Exception {
			return restTemplate.headForHeaders(compactizer.getApiUrl(), compactizer.getArguments());
		}
	}

	static class DummySupplierImpl implements Supplier<HttpHeaders> {
		@Override
		public HttpHeaders get() {
			return new HttpHeaders();
		}
	}
}
