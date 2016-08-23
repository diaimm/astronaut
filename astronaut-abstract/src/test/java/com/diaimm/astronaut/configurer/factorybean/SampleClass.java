package com.diaimm.astronaut.configurer.factorybean;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.annotations.method.PostForObject;
import com.google.common.base.Supplier;

interface SampleClass {
	@PostForObject(dummySupplier = SomeSmapleResponseDummySupplier.class)
	@Transaction(commit = "/this/is/to/commit", rollback = "/and/for/rollback")
	public APIResponse<SomeSampleResponse> testMethod();

	static class SomeSampleResponse {

	}

	public static class SomeSmapleResponseDummySupplier implements Supplier<SomeSampleResponse> {
		@Override
		public SomeSampleResponse get() {
			return new SomeSampleResponse();
		}
	}
}
