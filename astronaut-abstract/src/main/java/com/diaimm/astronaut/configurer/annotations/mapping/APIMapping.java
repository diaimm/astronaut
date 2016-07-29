package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.Mapping;

import com.diaimm.astronaut.configurer.RestTemplateInvoker;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Mapping
public @interface APIMapping {
	Class<? extends RestTemplateInvoker<?>> handler();
}
