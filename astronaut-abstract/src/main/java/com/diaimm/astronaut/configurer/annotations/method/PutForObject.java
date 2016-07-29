package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.mapping.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = PutForObject.RestTemplateInvoker.class)
public @interface PutForObject {
	@RequestURI
	String url() default "";

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<PutForObject> {
		@Override
		protected Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<PutForObject> compactizer, Type returnType,
			PutForObject annotation)
				throws Exception {
			String apiUrl = compactizer.getApiUrl();
			Object[] args = compactizer.getArguments();
			if (args == null || args.length == 0) {
				return restTemplate.putForObject(apiUrl, null, returnType);
			}
			if (args.length == 1) {
				return restTemplate.putForObject(apiUrl, args[0], returnType);
			}
			return restTemplate.putForObject(apiUrl, args[args.length - 1], returnType, Arrays.copyOfRange(args, 0, args.length - 1));
		}
	}
}
