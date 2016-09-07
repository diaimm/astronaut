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

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = Put.RestTemplateInvoker.class)
public @interface Put {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<Put> {
		@Override
		protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Put> compactizer, Type returnType, Put annotation)
			throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object postBody = compactizer.getPostBody();

			restTemplate.put(apiUrl, postBody, arguments);
			return true;
		}

		public Object doInvoke(TypeHandlingRestOperations restTemplate, String apiUrl, Object args) {
			restTemplate.put(apiUrl, null, args);
			return null;
		}

		@Override
		protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
			com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Put> compactizer, Type returnType, Put annotation)
				throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object postBody = compactizer.getPostBody();
			return restTemplate.put(apiUrl, new HttpEntity<>(postBody), arguments);
		}
	}
}
