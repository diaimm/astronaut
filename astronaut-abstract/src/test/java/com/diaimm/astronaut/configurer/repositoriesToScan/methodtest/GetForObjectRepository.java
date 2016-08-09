package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject;
import com.google.common.base.Supplier;

@RestAPIRepository("resourceName")
public interface GetForObjectRepository {
	@GetForObject(url = "/sample/url/path", dummySupplier = DummySupplierImpl.class)
	APIResponse<SampleResponse> paramMapping(@Param("id") String id, @Param("age") int age);

	@GetForObject(url = "/sample/{!path1}/{path2}/{path3}", dummySupplier = DummySupplierImpl.class)
	APIResponse<SampleResponse> pathParamMapping(@PathParam("path1") String path1, @PathParam("path2") int path2, @PathParam("path3") String path3);

	@GetForObject(url = "/sample/{!path1}/{path2}/{path3}", dummySupplier = DummySupplierImpl.class)
	APIResponse<SampleResponse> usingParamDTO(@Form PathParamDTO paramDTO);

	@GetForObject(url = "/sample/{!path1}/{path2}/{path3}", dummySupplier = DummySupplierImpl.class)
	APIResponse<SampleResponse> usingParamDTO(@Form ComplexParamDTO paramDTO);

	static class DummySupplierImpl implements Supplier<SampleResponse> {
		@Override
		public SampleResponse get() {
			return SampleResponse.dummy;
		}
	}

	static class SampleResponse {
		private static final SampleResponse dummy = new SampleResponse();
	}
}
