package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;
import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.method.Delete.RestTemplateInvoker;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.DeleteRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class DeleteTest {
	@Autowired
	private DeleteRepository deleteRepository;

	@SuppressWarnings("unchecked")
	@Test
	public void restTemplateInvokerTest() throws Exception {
		Method method = DeleteRepository.class.getDeclaredMethod("paramMapping", String.class, int.class);
		method.setAccessible(true);
		Delete delete = AnnotationUtilsExt.find(method.getAnnotations(), Delete.class).get();

		RestTemplateInvoker restTemplateInvoker = new RestTemplateInvoker();

		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		APICallInfoCompactizer<Delete> compactizer = Mockito.mock(APICallInfoCompactizer.class);
		restTemplateInvoker.doInvoke((TypeHandlingRestOperations) restTemplate, compactizer, Mockito.mock(Type.class), delete);

		Mockito.verify(restTemplate).delete(Mockito.anyString(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.reset(restTemplate, compactizer);

		TypeHandlingAsyncRestOperations restTemplate2 = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		Mockito.when(restTemplate2.delete(Mockito.anyString(), (Object[]) Mockito.anyObject())).thenReturn(Mockito.mock(ListenableFuture.class));
		ListenableFuture<?> result = restTemplateInvoker.doInvoke((TypeHandlingAsyncRestOperations) restTemplate2, compactizer,
			Mockito.mock(Type.class), delete);

		Assert.assertNotNull(result);
		Mockito.verify(restTemplate2).delete(Mockito.anyString(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.reset(restTemplate, compactizer);
	}

	@Test
	public void paramMappingTest() {
		try {
			deleteRepository.paramMapping("diaimm", 111);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/url/path"));
		}
	}

	@Test
	public void pathParamMappingTest() {
		try {
			deleteRepository.pathParamMapping("diaimm", 111, "test");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));
		}
		try {
			deleteRepository.pathParamMapping(null, 111, "test");
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));
		}
		try {
			deleteRepository.pathParamMapping(null, 111, null);
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
		}
	}

	@Test
	public void usingParamDTOTest() {
		try {
			deleteRepository.usingParamDTO(PathParamDTO.create("diaimm", 111, "test"));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));
		}
		try {
			deleteRepository.usingParamDTO(PathParamDTO.create(null, 111, "test"));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));
		}
		try {
			deleteRepository.usingParamDTO(PathParamDTO.create(null, 111, null));
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
		}
	}
}
