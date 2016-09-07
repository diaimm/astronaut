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
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PutRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class PutTest {
	@Autowired
	private PutRepository putRepository;

	@SuppressWarnings("unchecked")
	@Test
	public void restTemplateInvokerTest() throws Exception {
		Method method = PutRepository.class.getDeclaredMethod("paramMapping", String.class, int.class);
		method.setAccessible(true);
		Put getForObject = AnnotationUtilsExt.find(method.getAnnotations(), Put.class).get();

		Put.RestTemplateInvoker restTemplateInvoker = new Put.RestTemplateInvoker();

		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		APICallInfoCompactizer<Put> compactizer = Mockito.mock(APICallInfoCompactizer.class);
		restTemplateInvoker.doInvoke((TypeHandlingRestOperations) restTemplate, compactizer, Mockito.mock(Type.class), getForObject);

		Mockito.verify(restTemplate).put(Mockito.anyString(), (Object) Mockito.anyObject(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.verify(compactizer).getPostBody();
		Mockito.reset(restTemplate, compactizer);

		TypeHandlingAsyncRestOperations restTemplate2 = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		Mockito.when(restTemplate2.put(Mockito.anyString(), (HttpEntity<?>) Mockito.anyObject(), (Object[]) Mockito.anyObject())).thenReturn(
			Mockito.mock(ListenableFuture.class));
		ListenableFuture<?> result = restTemplateInvoker.doInvoke((TypeHandlingAsyncRestOperations) restTemplate2, compactizer,
			Mockito.mock(Type.class), getForObject);

		Assert.assertNotNull(result);
		Mockito.verify(restTemplate2).put(Mockito.anyString(), (HttpEntity<?>) Mockito.anyObject(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.verify(compactizer).getPostBody();
		Mockito.reset(restTemplate, compactizer);
	}

	@Test
	public void doInvoke2Test() throws Exception{
		Method method = PutRepository.class.getDeclaredMethod("paramMapping", String.class, int.class);
		method.setAccessible(true);
		Put put = AnnotationUtilsExt.find(method.getAnnotations(), Put.class).get();

		Put.RestTemplateInvoker restTemplateInvoker = new Put.RestTemplateInvoker();

		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		restTemplateInvoker.doInvoke((TypeHandlingRestOperations) restTemplate, "", put);
		
		Mockito.verify(restTemplate).put(Mockito.anyString(), (Object) Mockito.anyObject(), (Object[]) Mockito.anyObject());
	}
	@Test
	public void paramMappingTest() {
		try {
			putRepository.paramMapping("diaimm", 111);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/url/path"));
		}
	}

	@Test
	public void pathParamMappingTest() {
		try {
			putRepository.pathParamMapping("diaimm", 111, "test");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));
		}
		try {
			putRepository.pathParamMapping(null, 111, "test");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));
		}
		try {
			putRepository.pathParamMapping(null, 111, null);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
		}
	}

	@Test
	public void usingParamDTOTest() {
		try {
			putRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));
		}
		try {
			putRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));
		}
		try {
			putRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
		}
	}
}
