package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;
import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PostForLocationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class PostForLocationTest {
	@Autowired
	private PostForLocationRepository postForLocationRepository;

	@SuppressWarnings("unchecked")
	@Test
	public void restTemplateInvokerTest() throws Exception {
		Method method = PostForLocationRepository.class.getDeclaredMethod("paramMapping", String.class, int.class);
		method.setAccessible(true);
		PostForLocation getForObject = AnnotationUtilsExt.find(method.getAnnotations(), PostForLocation.class).get();

		PostForLocation.RestTemplateInvoker restTemplateInvoker = new PostForLocation.RestTemplateInvoker();

		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		APICallInfoCompactizer<PostForLocation> compactizer = Mockito.mock(APICallInfoCompactizer.class);
		restTemplateInvoker.doInvoke((TypeHandlingRestOperations) restTemplate, compactizer, Mockito.mock(Type.class), getForObject);

		Mockito.verify(restTemplate).postForLocation(Mockito.anyString(), (Object) Mockito.anyObject(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.verify(compactizer).getPostBody();
		Mockito.reset(restTemplate, compactizer);

		TypeHandlingAsyncRestOperations restTemplate2 = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		Mockito.when(restTemplate2.postForLocation(Mockito.anyString(), (HttpEntity<?>) Mockito.anyObject(), (Object[]) Mockito.anyObject())).thenReturn(
			Mockito.mock(ListenableFuture.class));
		ListenableFuture<?> result = restTemplateInvoker.doInvoke((TypeHandlingAsyncRestOperations) restTemplate2, compactizer,
			Mockito.mock(Type.class), getForObject);

		Assert.assertNotNull(result);
		Mockito.verify(restTemplate2).postForLocation(Mockito.anyString(), (HttpEntity<?>) Mockito.anyObject(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.verify(compactizer).getPostBody();
		Mockito.reset(restTemplate, compactizer);
	}

	@Test
	public void paramMappingTest() {
		try {
			postForLocationRepository.paramMapping("diaimm", 111);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/url/path"));
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
