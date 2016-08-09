package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;

public class PathParamDTO {
	@PathParam("path1")
	private String path1;

	@PathParam("path2")
	private int path2;

	@PathParam("path3")
	private String path3;

	public static PathParamDTO create(String path1, int path2, String path3) {
		PathParamDTO pathParamDTO = new PathParamDTO();
		pathParamDTO.path1 = path1;
		pathParamDTO.path2 = path2;
		pathParamDTO.path3 = path3;
		return pathParamDTO;
	}
}
