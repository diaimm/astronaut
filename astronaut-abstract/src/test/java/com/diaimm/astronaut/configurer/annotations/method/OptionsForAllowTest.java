package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;
import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.DeleteRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.OptionsForAllowRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class OptionsForAllowTest {
	@Autowired
	private OptionsForAllowRepository optionsForAllowRepository;

	@SuppressWarnings("unchecked")
	@Test
	public void restTemplateInvokerTest() throws Exception {
		Method method = OptionsForAllowRepository.class.getDeclaredMethod("paramMapping", String.class, int.class);
		method.setAccessible(true);
		OptionsForAllow optionsForAllow = AnnotationUtilsExt.find(method.getAnnotations(), OptionsForAllow.class).get();

		OptionsForAllow.RestTemplateInvoker restTemplateInvoker = new OptionsForAllow.RestTemplateInvoker();

		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		APICallInfoCompactizer<OptionsForAllow> compactizer = Mockito.mock(APICallInfoCompactizer.class);
		restTemplateInvoker.doInvoke((TypeHandlingRestOperations) restTemplate, compactizer, Mockito.mock(Type.class), optionsForAllow);

		Mockito.verify(restTemplate).optionsForAllow(Mockito.anyString(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.reset(restTemplate, compactizer);

		TypeHandlingAsyncRestOperations restTemplate2 = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		Mockito.when(restTemplate2.optionsForAllow(Mockito.anyString(), (Object[]) Mockito.anyObject())).thenReturn(Mockito.mock(ListenableFuture.class));
		ListenableFuture<?> result = restTemplateInvoker.doInvoke((TypeHandlingAsyncRestOperations) restTemplate2, compactizer,
			Mockito.mock(Type.class), optionsForAllow);

		Assert.assertNotNull(result);
		Mockito.verify(restTemplate2).optionsForAllow(Mockito.anyString(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.reset(restTemplate, compactizer);
	}

	@Test
	public void paramMappingTest() {
		APIResponse<Set<HttpMethod>> response = optionsForAllowRepository.paramMapping("diaimm", 111);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/url/path?id={id}&age={age}", response.getApiUrl());
		Assert.assertEquals("diaimm", response.getArgs()[0]);
		Assert.assertEquals(111, response.getArgs()[1]);
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/url/path"));
	}

	@Test
	public void pathParamMappingTest() {
		APIResponse<Set<HttpMethod>> response = optionsForAllowRepository.pathParamMapping("diaimm", 111, "test");
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = optionsForAllowRepository.pathParamMapping(null, 111, "test");
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = optionsForAllowRepository.pathParamMapping(null, 111, null);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	@Test
	public void usingParamDTOTest() {
		APIResponse<Set<HttpMethod>> response = optionsForAllowRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = optionsForAllowRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = optionsForAllowRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}", response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}
}
