package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.diaimm.astronaut.configurer.APIArgumentNormalizer;

@Target(value = { ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {
	String value() default "";

	@SuppressWarnings("rawtypes")
	Class<? extends APIArgumentNormalizer> normalizer() default APIArgumentNormalizer.class;
}
