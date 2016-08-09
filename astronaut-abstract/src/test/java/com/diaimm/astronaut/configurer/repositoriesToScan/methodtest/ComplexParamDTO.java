package com.diaimm.astronaut.configurer.repositoriesToScan.methodtest;

import com.diaimm.astronaut.configurer.annotations.mapping.Param;

public class ComplexParamDTO extends PathParamDTO {
	@Param("param1")
	private String param1;
	@Param("param2")
	private String param2;
	@Param("param3")
	private String param3;

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
		return setupWithPath(path1, path2, path3, new ComplexParamDTO());
	}
}
