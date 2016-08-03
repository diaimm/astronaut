package com.diaimm.astronaut.configurer.repositoriesToScan.samples1;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject;
import com.google.common.base.Supplier;

@RestAPIRepository("resourceName")
public interface SampleRestAPIRepository {
	@GetForObject(url = "/sample/url/path", dummySupplier = DummySupplierImpl.class)
	APIResponse<SampleResponse> someMethod(@Param("id") String id, @Param("age") int age);

	public static class DummySupplierImpl implements Supplier<SampleResponse> {
		@Override
		public SampleResponse get() {
			return SampleResponse.dummy;
		}
	}

	public static class SampleResponse {
		private static final SampleResponse dummy = new SampleResponse();
	}
}
