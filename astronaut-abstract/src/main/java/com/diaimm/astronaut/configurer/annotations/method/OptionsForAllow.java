package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Set;

import org.springframework.http.HttpMethod;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.mapping.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = OptionsForAllow.RestTemplateInvoker.class)
public @interface OptionsForAllow {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<OptionsForAllow> {
		@Override
		protected Set<HttpMethod> doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<OptionsForAllow> compactizer,
			Type returnType, OptionsForAllow annotation) throws Exception {
			return restTemplate.optionsForAllow(compactizer.getApiUrl(), compactizer.getArguments());
		}
	}
}
