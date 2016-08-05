package com.diaimm.astronaut.configurer.annotations.method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository.PathParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository.SampleResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class GetForObjectTest {
	@Autowired
	private GetForObjectRepository getForObjectRepository;

	@Test
	public void paramMappingTest() {
		APIResponse<SampleResponse> someMethod = getForObjectRepository.paramMapping("diaimm", 111);
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/url/path?id={id}&age={age}", someMethod.getApiUrl());
		Assert.assertEquals("diaimm", someMethod.getArgs()[0]);
		Assert.assertEquals(111, someMethod.getArgs()[1]);
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/url/path?id=diaimm&age=111"));
	}

	@Test
	public void pathParamMappingTest() {
		APIResponse<SampleResponse> someMethod = getForObjectRepository.pathParamMapping("diaimm", 111, "test");
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", someMethod.getApiUrl());
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		someMethod = getForObjectRepository.pathParamMapping(null, 111, "test");
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", someMethod.getApiUrl());
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		someMethod = getForObjectRepository.pathParamMapping(null, 111, null);
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", someMethod.getApiUrl());
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	@Test
	public void usingParamDTOTest() {
		APIResponse<SampleResponse> someMethod = getForObjectRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", someMethod.getApiUrl());
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		someMethod = getForObjectRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", someMethod.getApiUrl());
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		someMethod = getForObjectRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		Assert.assertFalse(someMethod.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", someMethod.getApiUrl());
		Assert.assertTrue(someMethod.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}
}
