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
public @interface PathParam {
	String value() default "";

	@SuppressWarnings("rawtypes")
	Class<? extends APIArgumentNormalizer> normalizer() default APIArgumentNormalizer.class;

	public static class EnumNormalizer implements APIArgumentNormalizer<Enum<? extends Enum<?>>> {
		@Override
		public Object normalize(Enum<?> value) {
			return value.name();
		}
	}
}
