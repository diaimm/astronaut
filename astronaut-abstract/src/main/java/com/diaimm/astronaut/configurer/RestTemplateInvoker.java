package com.diaimm.astronaut.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

public interface RestTemplateInvoker<T extends Annotation> {
	Object invoke(TypeHandlingRestTemplate restTemplate, String apiUrl, Method method, T annotation, Object[] args);

	String extractAPIUrl(T annotation, Method method, Object[] methodArguemtns);

	void addAPIArgumentNormalizer(Class<?> supportType, APIArgumentNormalizer<?> normalizer);
	
	Map<Class<?>, APIArgumentNormalizer<?>> getAPIArgumentNomalizers();
}
