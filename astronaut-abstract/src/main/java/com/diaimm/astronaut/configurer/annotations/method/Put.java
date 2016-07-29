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
@APIMapping(handler = Put.RestTemplateInvoker.class)
public @interface Put {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<Put> {
		@Override
		protected Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<Put> compactizer, Type returnType, Put annotation)
			throws Exception {
			restTemplate.put(compactizer.getApiUrl(), null, compactizer.getArguments());
			return true;
		}

		public Object doInvoke(TypeHandlingRestTemplate restTemplate, String apiUrl, Object args) {
			restTemplate.put(apiUrl, null, args);
			return null;
		}
	}
}
