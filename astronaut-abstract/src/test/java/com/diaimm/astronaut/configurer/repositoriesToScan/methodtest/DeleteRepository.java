package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.method.Delete;
import com.google.common.base.Supplier;

@RestAPIRepository("resourceName")
public interface DeleteRepository {
	@Delete(url = "/sample/url/path")
	void paramMapping(@Param("id") String id, @Param("age") int age);

	@Delete(url = "/sample/{!path1}/{path2}/{path3}")
	void pathParamMapping(@PathParam("path1") String path1, @PathParam("path2") int path2, @PathParam("path3") String path3);

	@Delete(url = "/sample/{!path1}/{path2}/{path3}")
	void usingParamDTO(@Form PathParamDTO paramDTO);

	static class PathParamDTO {
		@PathParam("path1")
		private String path1;

		@PathParam("path2")
		private int path2;

		@PathParam("path3")
		private String path3;

		public static PathParamDTO create(String path1, int path2, String path3) {
			PathParamDTO result = new PathParamDTO();
			result.path1 = path1;
			result.path2 = path2;
			result.path3 = path3;
			
			return result;
		}
	}
}
