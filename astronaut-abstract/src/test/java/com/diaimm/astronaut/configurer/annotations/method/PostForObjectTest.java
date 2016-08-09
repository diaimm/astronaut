package com.diaimm.astronaut.configurer.annotations.method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.ComplexParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PostForObjectRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PostForObjectRepository.SampleResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class PostForObjectTest {
	@Autowired
	private PostForObjectRepository postForObjectRepository;

	@Test
	public void paramMappingTest() {
		APIResponse<SampleResponse> response = postForObjectRepository.paramMapping("diaimm", 111);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/url/path?id={id}&age={age}", response.getApiUrl());
		Assert.assertEquals("diaimm", response.getArgs()[0]);
		Assert.assertEquals(111, response.getArgs()[1]);
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/url/path?id=diaimm&age=111"));
	}

	@Test
	public void pathParamMappingTest() {
		APIResponse<SampleResponse> response = postForObjectRepository.pathParamMapping("diaimm", 111, "test");
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = postForObjectRepository.pathParamMapping(null, 111, "test");
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = postForObjectRepository.pathParamMapping(null, 111, null);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	@Test
	public void usingParamDTOTest() {
		APIResponse<SampleResponse> response = postForObjectRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = postForObjectRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = postForObjectRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	@Test
	public void usingParamDTO2Test() {
		APIResponse<SampleResponse> response = postForObjectRepository.usingParamDTO(ComplexParamDTO.create("diaimm", 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test?param1=param111&param2=param112&param3=param113"));

		response = postForObjectRepository.usingParamDTO(ComplexParamDTO.create(null, 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test?param1=param111&param2=param112&param3=param113"));

		response = postForObjectRepository.usingParamDTO(ComplexParamDTO.create(null, 111, null));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/?param1=param111&param2=param112&param3=param113"));
	}
}
