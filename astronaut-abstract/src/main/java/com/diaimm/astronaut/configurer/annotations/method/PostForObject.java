package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import org.springframework.http.HttpEntity;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.google.common.base.Supplier;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = PostForObject.RestTemplateInvoker.class)
public @interface PostForObject {
	@RequestURI
	String url() default "";

	Class<? extends Supplier<?>>dummySupplier();

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<PostForObject> {
		@Override
		protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<PostForObject> compactizer, Type returnType,
			PostForObject annotation)
				throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object postBody = compactizer.getPostBody();

			return restTemplate.postForObject(apiUrl, postBody, returnType, arguments);
		}

		@Override
		protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
			com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<PostForObject> compactizer, Type returnType,
			PostForObject annotation) throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object postBody = compactizer.getPostBody();

			return restTemplate.postForEntity(apiUrl, new HttpEntity<>(postBody), returnType, arguments);
		}
	}
}
