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
		return setupWithPath(path1, path2, path3, new PathParamDTO());
	}

	protected static <T extends PathParamDTO> T setupWithPath(String path1, int path2, String path3, T result) {
		PathParamDTO pathParamDTO = (PathParamDTO) result;
		pathParamDTO.path1 = path1;
		pathParamDTO.path2 = path2;
		pathParamDTO.path3 = path3;

		return result;
	}
}
