package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.mapping.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = Delete.RestTemplateInvoker.class)
public @interface Delete {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<Delete> {
		@Override
		protected Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<Delete> compactizer, Type returnType, Delete annotation)
			throws Exception {
			restTemplate.delete(compactizer.getApiUrl(), compactizer.getArguments());
			return true;
		}
	}
}
