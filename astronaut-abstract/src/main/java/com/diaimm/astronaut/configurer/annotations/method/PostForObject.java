package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.mapping.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = PostForObject.RestTemplateInvoker.class)
public @interface PostForObject {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<PostForObject> {
		@Override
		protected Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<PostForObject> compactizer, Type returnType,
			PostForObject annotation)
			throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] arguments = compactizer.getArguments();
			Object[] sourceArguments = compactizer.getSourceArguments();
			if (sourceArguments == null || sourceArguments.length == 0) {
				return restTemplate.postForObject(apiUrl, null, returnType);
			}

			if (sourceArguments.length == 1) {
				return restTemplate.postForObject(apiUrl, sourceArguments[0], returnType);
			}

			return restTemplate.postForObject(apiUrl, sourceArguments[0], returnType, arguments);
		}
	}
}
