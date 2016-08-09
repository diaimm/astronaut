package com.diaimm.astronaut.configurer.annotations.method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.ComplexParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository.SampleResponse;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class GetForObjectTest {
	@Autowired
	private GetForObjectRepository getForObjectRepository;

	@Test
	public void paramMappingTest() {
		APIResponse<SampleResponse> response = getForObjectRepository.paramMapping("diaimm", 111);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/url/path?id={id}&age={age}", response.getApiUrl());
		Assert.assertEquals("diaimm", response.getArgs()[0]);
		Assert.assertEquals(111, response.getArgs()[1]);
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/url/path?id=diaimm&age=111"));
	}

	@Test
	public void pathParamMappingTest() {
		APIResponse<SampleResponse> response = getForObjectRepository.pathParamMapping("diaimm", 111, "test");
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = getForObjectRepository.pathParamMapping(null, 111, "test");
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = getForObjectRepository.pathParamMapping(null, 111, null);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	@Test
	public void usingParamDTOTest() {
		APIResponse<SampleResponse> response = getForObjectRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = getForObjectRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = getForObjectRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	@Test
	public void usingParamDTO2Test() {
		APIResponse<SampleResponse> response = getForObjectRepository.usingParamDTO(createComplexParamDTO("diaimm", 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test?param1=param111&param2=param112&param3=param113"));

		response = getForObjectRepository.usingParamDTO(createComplexParamDTO(null, 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test?param1=param111&param2=param112&param3=param113"));

		response = getForObjectRepository.usingParamDTO(createComplexParamDTO(null, 111, null));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/?param1=param111&param2=param112&param3=param113"));
	}

	private ComplexParamDTO createComplexParamDTO(String path1, int path2, String path3) {
		ComplexParamDTO param2 = ComplexParamDTO.create(path1, path2, path3);
		param2.setParam1("param111");
		param2.setParam2("param112");
		param2.setParam3("param113");
		return param2;
	}
}
