package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import org.springframework.http.HttpHeaders;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.method.HeadForHeaders;

@RestAPIRepository("resourceName")
public interface HeadForHeadersRepository {
	@HeadForHeaders(url = "/sample/url/path")
	APIResponse<HttpHeaders> paramMapping(@Param("id") String id, @Param("age") int age);

	@HeadForHeaders(url = "/sample/{!path1}/{path2}/{path3}")
	APIResponse<HttpHeaders> pathParamMapping(@PathParam("path1") String path1, @PathParam("path2") int path2, @PathParam("path3") String path3);

	@HeadForHeaders(url = "/sample/{!path1}/{path2}/{path3}")
	APIResponse<HttpHeaders> usingParamDTO(@Form PathParamDTO paramDTO);
}
