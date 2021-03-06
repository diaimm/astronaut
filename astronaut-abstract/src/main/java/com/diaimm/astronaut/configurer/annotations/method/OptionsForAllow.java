package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = OptionsForAllow.RestTemplateInvoker.class)
public @interface OptionsForAllow {
	@RequestURI
	String url() default "";

	Class<? extends Supplier<?>>dummySupplier() default DummySupplierImpl.class;

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<OptionsForAllow> {
		@Override
		protected Set<HttpMethod> doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<OptionsForAllow> compactizer,
			Type returnType, OptionsForAllow annotation) throws Exception {
			return restTemplate.optionsForAllow(compactizer.getApiUrl(), compactizer.getArguments());
		}

		@Override
		protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
			com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<OptionsForAllow> compactizer, Type returnType,
			OptionsForAllow annotation) throws Exception {
			return restTemplate.optionsForAllow(compactizer.getApiUrl(), compactizer.getArguments());
		}
	}

	static class DummySupplierImpl implements Supplier<Set<HttpMethod>> {
		@Override
		public Set<HttpMethod> get() {
			return Sets.newHashSet();
		}
	}
}
