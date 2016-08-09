package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.mapping.PostBody;

public class ComplexParamDTO {
	@PathParam("path1")
	private String path1;
	@PathParam("path2")
	private int path2;
	@PathParam("path3")
	private String path3;
	@Param("param1")
	private String param1 = "param111";
	@Param("param2")
	private String param2 = "param112";
	@Param("param3")
	private String param3 = "param113";
	@PostBody
	private PostBodySample postBodySample = new PostBodySample();

	public static class PostBodySample {

	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public static ComplexParamDTO create(String path1, int path2, String path3) {
		ComplexParamDTO pathParamDTO = new ComplexParamDTO();
		pathParamDTO.path1 = path1;
		pathParamDTO.path2 = path2;
		pathParamDTO.path3 = path3;

		return pathParamDTO;
	}
}
