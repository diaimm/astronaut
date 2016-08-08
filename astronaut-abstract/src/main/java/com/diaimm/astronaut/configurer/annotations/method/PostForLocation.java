package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.net.URI;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
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
		protected URI doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<PostForLocation> compactizer, Type returnType, PostForLocation annotation)
			throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] args = compactizer.getArguments();
			if (args != null && args.length > 0) {
				return restTemplate.postForLocation(apiUrl, args[0], returnType);
			}
			return restTemplate.postForLocation(apiUrl, null, returnType);
		}
	}
}
