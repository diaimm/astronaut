package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;
import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.RestTemplateAdapterTestConfiguration;
import com.diaimm.astronaut.configurer.TypeHandlingAsyncRestOperations;
import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyBooleanSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyCollectionSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyListSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyLongSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyMapSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyObjectSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummyPageResponseSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.DummySetSupplier;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject.PageResponse;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.ComplexParamDTO;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.GetForObjectRepository.SampleResponse;
import com.diaimm.astronaut.configurer.repositoriesToScan.methodtest.PathParamDTO;
import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class GetForObjectTest {
	@Autowired
	private GetForObjectRepository getForObjectRepository;
	
	@Test
	public void PageResponseTest(){
		List<String> orgContents = Lists.<String>newArrayList();
		
		PageResponse<String> target1 = new PageResponse<String>();
		Assert.assertNull(target1.getContent());
		
		PageResponse<String> target = new PageResponse<String>(orgContents, 1, 10, 100);
		Assert.assertEquals(10, target.getTotalPages());
		Assert.assertEquals(true, target.hasNextPage());
		Assert.assertEquals(1, target.getPage());
		
		target.setPage(2l);
		Assert.assertEquals(2, target.getPage());

		Assert.assertEquals(10L, target.getSize());
		
		target.setSize(11);
		Assert.assertEquals(11L, target.getSize());
		
		Assert.assertEquals(100L, target.getTotal());

		target.setTotal(110);
		Assert.assertEquals(110L, target.getTotal());
		
		Assert.assertEquals(orgContents, target.getContent());

		List<String> newContents = Lists.<String>newArrayList();
		target.setContent(newContents);
		Assert.assertEquals(newContents, target.getContent());
	}
	
	@Test
	public void dummySuppliersTest(){
		DummyBooleanSupplier booleanDummy = new DummyBooleanSupplier();
		Assert.assertEquals(false, booleanDummy.get());
		
		DummyLongSupplier longDummy = new DummyLongSupplier();
		Assert.assertEquals(0L, (long)longDummy.get());
		
		DummyObjectSupplier objectDummy = new DummyObjectSupplier();
		Assert.assertEquals(DummyObjectSupplier.DUMMY, objectDummy.get());

		DummyMapSupplier mapDummy = new DummyMapSupplier();
		Assert.assertEquals(MapUtils.EMPTY_MAP, mapDummy.get());
		
		DummySetSupplier setDummy = new DummySetSupplier();
		Assert.assertEquals(SetUtils.EMPTY_SET, setDummy.get());
		
		DummyListSupplier listDummy = new DummyListSupplier();
		Assert.assertEquals(ListUtils.EMPTY_LIST, listDummy.get());
		
		DummyCollectionSupplier collectionDummy = new DummyCollectionSupplier();
		Assert.assertEquals(CollectionUtils.EMPTY_COLLECTION, collectionDummy.get());
		
		DummyPageResponseSupplier pageDummy = new DummyPageResponseSupplier();
		Assert.assertEquals(DummyPageResponseSupplier.DUMMY, pageDummy.get());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void restTemplateInvokerTest() throws Exception {
		Method method = GetForObjectRepository.class.getDeclaredMethod("paramMapping", String.class, int.class);
		method.setAccessible(true);
		GetForObject getForObject = AnnotationUtilsExt.find(method.getAnnotations(), GetForObject.class).get();

		GetForObject.RestTemplateInvoker restTemplateInvoker = new GetForObject.RestTemplateInvoker();

		TypeHandlingRestOperations restTemplate = Mockito.mock(TypeHandlingRestOperations.class);
		APICallInfoCompactizer<GetForObject> compactizer = Mockito.mock(APICallInfoCompactizer.class);
		restTemplateInvoker.doInvoke((TypeHandlingRestOperations) restTemplate, compactizer, Mockito.mock(Type.class), getForObject);

		Mockito.verify(restTemplate).getForObject(Mockito.anyString(), (Type) Mockito.anyObject(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.reset(restTemplate, compactizer);

		TypeHandlingAsyncRestOperations restTemplate2 = Mockito.mock(TypeHandlingAsyncRestOperations.class);
		Mockito.when(restTemplate2.getForEntity(Mockito.anyString(), (Type) Mockito.anyObject(), (Object[]) Mockito.anyObject())).thenReturn(
			Mockito.mock(ListenableFuture.class));
		ListenableFuture<?> result = restTemplateInvoker.doInvoke((TypeHandlingAsyncRestOperations) restTemplate2, compactizer,
			Mockito.mock(Type.class), getForObject);

		Assert.assertNotNull(result);
		Mockito.verify(restTemplate2).getForEntity(Mockito.anyString(), (Type) Mockito.anyObject(), (Object[]) Mockito.anyObject());
		Mockito.verify(compactizer).getApiUrl();
		Mockito.verify(compactizer).getArguments();
		Mockito.reset(restTemplate, compactizer);
	}

	@Test
	public void paramMappingTest() {
		APIResponse<SampleResponse> response = getForObjectRepository.paramMapping("diaimm", 111);
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/url/path?id={id}&age={age}", response.getApiUrl());
		Assert.assertEquals("diaimm", response.getArgs()[0]);
		Assert.assertEquals(111, response.getArgs()[1]);
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/url/path"));
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
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}",
			response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/diaimm/111/test"));

		response = getForObjectRepository.usingParamDTO(createComplexParamDTO(null, 111, "test"));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}",
			response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/test"));

		response = getForObjectRepository.usingParamDTO(createComplexParamDTO(null, 111, null));
		Assert.assertFalse(response.isSuccess());
		Assert.assertEquals("http://api.url.property.sample/v1/sample/{!path1}/{path2}/{path3}?param1={param1}&param2={param2}&param3={param3}",
			response.getApiUrl());
		Assert.assertTrue(response.getMessage().contains("http://api.url.property.sample/v1/sample/111/"));
	}

	private ComplexParamDTO createComplexParamDTO(String path1, int path2, String path3) {
		ComplexParamDTO param2 = ComplexParamDTO.create(path1, path2, path3);
		param2.setParam1("param111");
		param2.setParam2("param112");
		param2.setParam3("param113");
		return param2;
	}
}
