package com.diaimm.astronaut.configurer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.diaimm.astronaut.configurer.repositoriesToScan.samples1.SampleRestAPIRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.samples1.SampleRestAPIRepository2;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class RestTemplateAdapterLoaderTest {
	@Autowired
	private RestTemplate sampleRestTemplate;
	@Autowired
	private SampleRestAPIRepository repositoryForCurrentResource;
	@Autowired(required = false)
	private SampleRestAPIRepository2 repositoryNotForCurrentResource;

	@Test
	public void beanLoadingTest() {
		Assert.assertNotNull(sampleRestTemplate);
		Assert.assertNotNull(repositoryForCurrentResource);
		Assert.assertNull(repositoryNotForCurrentResource);
	}
}
