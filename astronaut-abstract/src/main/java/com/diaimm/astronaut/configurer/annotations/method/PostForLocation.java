package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = PostForLocation.RestTemplateInvoker.class)
public @interface PostForLocation {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<PostForLocation> {
		@Override
		protected URI doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<PostForLocation> compactizer, Type returnType,
			PostForLocation annotation)
				throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object postBody = compactizer.getPostBody();

			return restTemplate.postForLocation((String) apiUrl, (Object) postBody, (Object[]) arguments);
		}

		@Override
		protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
			com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<PostForLocation> compactizer, Type returnType,
			PostForLocation annotation) throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object postBody = compactizer.getPostBody();
			return restTemplate.postForLocation((String) apiUrl, new HttpEntity<>(postBody), (Object[]) arguments);
		}
	}
}
