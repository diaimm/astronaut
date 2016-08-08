package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.method.Delete;

@RestAPIRepository("resourceName")
public interface DeleteRepository {
	@Delete(url = "/sample/url/path")
	void paramMapping(@Param("id") String id, @Param("age") int age);

	@Delete(url = "/sample/{!path1}/{path2}/{path3}")
	void pathParamMapping(@PathParam("path1") String path1, @PathParam("path2") int path2, @PathParam("path3") String path3);

	@Delete(url = "/sample/{!path1}/{path2}/{path3}")
	void usingParamDTO(@Form PathParamDTO paramDTO);
}
