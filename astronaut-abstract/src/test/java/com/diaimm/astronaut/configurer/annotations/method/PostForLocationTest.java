package com.diaimm.astronaut.configurer.annotations.method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PostForLocationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class PostForLocationTest {
	@Autowired
	private PostForLocationRepository postForLocationRepository;

	@Test
	public void paramMappingTest() {
		try {
			postForLocationRepository.paramMapping("diaimm", 111);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/url/path?id=diaimm&age=111"));
		}
	}

	@Test
	public void pathParamMappingTest() {
		try {
			postForLocationRepository.pathParamMapping("diaimm", 111, "test");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));
		}
		try {
			postForLocationRepository.pathParamMapping(null, 111, "test");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));
		}
		try {
			postForLocationRepository.pathParamMapping(null, 111, null);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
		}
	}

	@Test
	public void usingParamDTOTest() {
		try {
			postForLocationRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));
		}
		try {
			postForLocationRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));
		}
		try {
			postForLocationRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
		}
	}
}
