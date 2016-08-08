package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import java.util.Set;

import org.springframework.http.HttpMethod;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.method.OptionsForAllow;

@RestAPIRepository("resourceName")
public interface OptionsForAllowRepository {
	@OptionsForAllow(url = "/sample/url/path")
	APIResponse<Set<HttpMethod>> paramMapping(@Param("id") String id, @Param("age") int age);

	@OptionsForAllow(url = "/sample/{!path1}/{path2}/{path3}")
	APIResponse<Set<HttpMethod>> pathParamMapping(@PathParam("path1") String path1, @PathParam("path2") int path2, @PathParam("path3") String path3);

	@OptionsForAllow(url = "/sample/{!path1}/{path2}/{path3}")
	APIResponse<Set<HttpMethod>> usingParamDTO(@Form PathParamDTO paramDTO);
}
