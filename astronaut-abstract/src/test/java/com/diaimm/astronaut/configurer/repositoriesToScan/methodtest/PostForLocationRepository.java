package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import java.net.URI;

import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.method.PostForLocation;

@RestAPIRepository("resourceName")
public interface PostForLocationRepository {
	@PostForLocation(url = "/sample/url/path")
	URI paramMapping(@Param("id") String id, @Param("age") int age);

	@PostForLocation(url = "/sample/{!path1}/{path2}/{path3}")
	URI pathParamMapping(@PathParam("path1") String path1, @PathParam("path2") int path2, @PathParam("path3") String path3);

	@PostForLocation(url = "/sample/{!path1}/{path2}/{path3}")
	URI usingParamDTO(@Form PathParamDTO paramDTO);
}
