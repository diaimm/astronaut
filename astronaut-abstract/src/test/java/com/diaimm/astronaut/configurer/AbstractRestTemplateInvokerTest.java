package com.diaimm.astronaut.configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;

import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject;
import com.google.common.base.Supplier;

public class AbstractRestTemplateInvokerTest {
	@Test
	public void matchTest1() {
		String sample = "{param1}";
		Pattern pattern = Pattern.compile("\\{\\s*\\p{Graph}+\\s*\\}");
		Assert.assertTrue(pattern.matcher(sample).find());
	}

	@Test
	public void matchTest2() {
		String sample = "{ !param1}";
		Pattern pattern = Pattern.compile("\\{\\s*!param1\\s*\\}");
		Matcher matcher = pattern.matcher(sample);
		Assert.assertEquals("", matcher.replaceFirst(""));
	}

	@Test
	public void matchTest3() {
		String sample = "http://1234.co.kr/abc/def//hi.aa?";
		String sample2 = "https://1234.co.kr/abc/def//hi.aa?";

		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		Assert.assertEquals("http://1234.co.kr/abc/def/hi.aa?", target.makeUrlValid(sample));
		Assert.assertEquals("https://1234.co.kr/abc/def/hi.aa?", target.makeUrlValid(sample2));
	}

	@Test
	public void pathParameterPatternMatchingTest() {
		String sample = "/test/{param1}/ddd/{ param2}/ddd/{param3 }/{ param4 }";

		Pattern pattern = Pattern.compile("\\{\\s*([a-zA-Z_0-9^}]+)\\s*\\}");
		Matcher matcher = pattern.matcher(sample);
		while (matcher.find()) {
			System.out.println(matcher.group(1));
		}
	}

	@Test
	public void invokeWithParamsTest() throws Exception {
		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				Object[] args = compactizer.getArguments();
				Assert.assertEquals(7, args.length);
				Assert.assertEquals("1", args[0]);
				Assert.assertEquals("7", args[6]);
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		try {
			Object[] args = new Object[] { new SampleParam(new StringBuilder("/test/{path1}/ddd/{ !path2}/ddd/{!path3 }/{ path4 }")) };
			Method method = SampleClass.class.getDeclaredMethod("sampleMethod", SampleParam.class);
			GetForObject annotation = method.getAnnotation(GetForObject.class);
			String apiUrl = target.extractAPIUrl(annotation, method, args);
			Object[] normalizeArguments = target.normalizeArguments(apiUrl, method, args);
			System.out.println(ArrayUtils.toString(normalizeArguments, ""));

			target.invoke(null, null, apiUrl, method, null, args);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void invokeWithParamsWithNullValueTest() throws Exception {
		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				Object[] args = compactizer.getArguments();
				String apiUrl = compactizer.getApiUrl();
				Assert.assertEquals(5, args.length);
				Assert.assertEquals(null, args[0]);
				Assert.assertEquals("7", args[4]);
				Assert.assertEquals("/test/{path1}/ddd/ddd/{ path4 }?value5={value5}&value6={value6}&value7={value7}", apiUrl);
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		try {
			SampleParam sampleParam = new SampleParam(new StringBuilder("/test/{path1}/ddd/ddd/{ path4 }"));
			sampleParam.path1 = null;
			sampleParam.path2 = null;
			sampleParam.path3 = " ";

			Object[] args = new Object[] { sampleParam };
			Method method = SampleClass.class.getDeclaredMethod("sampleMethod", SampleParam.class);
			GetForObject annotation = method.getAnnotation(GetForObject.class);
			String apiUrl = target.extractAPIUrl(annotation, method, args);
			Object[] normalizeArguments = target.normalizeArguments(apiUrl, method, args);
			System.out.println(ArrayUtils.toString(normalizeArguments, ""));

			target.invoke(null, null, apiUrl, method, null, args);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void invokeWithFormWithNullsTest() throws Exception {
		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				Object[] args = compactizer.getArguments();
				String apiUrl = compactizer.getApiUrl();
				Assert.assertEquals(5, args.length);
				Assert.assertEquals(null, args[0]);
				Assert.assertEquals("7", args[4]);
				Assert.assertEquals("/test/{p1}/ddd/ddd/{ p4 }?param1={param1}&param2={param2}&param3={param3}", apiUrl);
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		try {
			SampleParam sampleParam = new SampleParam(new StringBuilder("/test/{p1}/ddd/ddd/{ p4 } }"));
			sampleParam.path1 = null;
			sampleParam.path2 = null;

			Object[] args = new Object[] { null, null, " ", "4", "5", "6", "7" };
			Method method = SampleClass.class.getDeclaredMethod("sampleMethod2", String.class, String.class, String.class, String.class,
				String.class,
				String.class, String.class);
			GetForObject annotation = method.getAnnotation(GetForObject.class);
			String apiUrl = target.extractAPIUrl(annotation, method, args);
			Object[] normalizeArguments = target.normalizeArguments(apiUrl, method, args);
			System.out.println(ArrayUtils.toString(normalizeArguments, ""));

			target.invoke(null, null, apiUrl, method, null, args);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void invokeWithFormTest() throws Exception {
		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				Object[] args = compactizer.getArguments();
				String apiUrl = compactizer.getApiUrl();
				Assert.assertEquals(7, args.length);
				Assert.assertEquals("1", args[0]);
				Assert.assertEquals("7", args[6]);
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		try {
			Object[] args = new Object[] { "1", "2", "3", "4", "5", "6", "7" };
			Method method = SampleClass.class.getDeclaredMethod("sampleMethod2", String.class, String.class, String.class, String.class,
				String.class,
				String.class, String.class);
			GetForObject annotation = method.getAnnotation(GetForObject.class);
			String apiUrl = target.extractAPIUrl(annotation, method, args);
			Object[] normalizeArguments = target.normalizeArguments(apiUrl, method, args);
			System.out.println(ArrayUtils.toString(normalizeArguments, ""));

			target.invoke(null, null, apiUrl, method, null, args);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void getRequestURIFromParamTest() throws Exception {
		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				Object[] args = compactizer.getArguments();
				String apiUrl = compactizer.getApiUrl();
				Assert.assertEquals(7, args.length);
				Assert.assertEquals("1", args[0]);
				Assert.assertEquals("7", args[6]);
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		try {
			Object[] args = new Object[] { new StringBuilder("/test/{p1}/ddd/{!p2}/ddd/{!p3}/{ p4 }"), "1", "2", "3", "4", "5", "6", "7" };
			Method method = SampleClass.class.getDeclaredMethod("sampleMethod4", StringBuilder.class, String.class, String.class, String.class,
				String.class,
				String.class,
				String.class, String.class);
			GetForObject annotation = method.getAnnotation(GetForObject.class);
			String apiUrl = target.extractAPIUrl(annotation, method, args);
			Object[] normalizeArguments = target.normalizeArguments(apiUrl, method, args);
			System.out.println(ArrayUtils.toString(normalizeArguments, ""));

			target.invoke(null, null, apiUrl, method, null, args);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void getRequestURIFromFormTest() throws Exception {
		AbstractRestTemplateInvoker<Annotation> target = new AbstractRestTemplateInvoker<Annotation>() {
			@Override
			protected Object doInvoke(TypeHandlingRestOperations restTemplate, APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation)
					throws Exception {
				Object[] args = compactizer.getArguments();
				String apiUrl = compactizer.getApiUrl();
				Assert.assertEquals(5, args.length);
				Assert.assertEquals(null, args[0]);
				Assert.assertEquals("7", args[4]);
				Assert.assertEquals("/test/{path1}/ddd/ddd/{ path4 }?value5={value5}&value6={value6}&value7={value7}", apiUrl);
				return null;
			}

			@Override
			protected ListenableFuture<?> doInvoke(TypeHandlingAsyncRestOperations restTemplate,
				com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer<Annotation> compactizer, Type returnType,
				Annotation annotation) throws Exception {
				return null;
			}
		};

		try {
			SampleParam sampleParam = new SampleParam(new StringBuilder("/test/{path1}/ddd/ddd/{ path4 }"));
			sampleParam.path1 = null;
			sampleParam.path2 = null;
			sampleParam.path3 = " ";

			Object[] args = new Object[] { sampleParam };
			Method method = SampleClass.class.getDeclaredMethod("sampleMethod3", SampleParam.class);
			GetForObject annotation = method.getAnnotation(GetForObject.class);
			String apiUrl = target.extractAPIUrl(annotation, method, args);
			Object[] normalizeArguments = target.normalizeArguments(apiUrl, method, args);
			System.out.println(ArrayUtils.toString(normalizeArguments, ""));

			target.invoke(null, null, apiUrl, method, null, args);
		} catch (Exception e) {
			throw e;
		}
	}

	public static class SampleClass {
		@GetForObject(url = "/test/{path1}/ddd/{ !path2}/ddd/{!path3 }/{ path4 }", dummySupplier = SampleParamSupplier.class)
		public void sampleMethod(@Form SampleParam param) {
		}

		@GetForObject(url = "/test/{p1}/ddd/{!p2}/ddd/{!p3}/{ p4 }", dummySupplier = SampleParamSupplier.class)
		public void sampleMethod2(@PathParam("p1") String path1, @PathParam("p2") String path2, @PathParam("p3") String path3,
			@PathParam("p4") String path4, @Param("param1") String param1, @Param("param2") String param2, @Param("param3") String param3) {
		}

		@GetForObject(dummySupplier = SampleParamSupplier.class)
		public void sampleMethod3(@Form SampleParam param) {
		}

		@GetForObject(dummySupplier = SampleParamSupplier.class)
		public void sampleMethod4(@RequestURI StringBuilder requestURI, @PathParam("p1") String path1, @PathParam("p2") String path2,
			@PathParam("p3") String path3,
			@PathParam("p4") String path4, @Param("param1") String param1, @Param("param2") String param2, @Param("param3") String param3) {
		}
	}

	public static class SampleParamSupplier implements Supplier<SampleParam> {
		@Override
		public SampleParam get() {
			return new SampleParam(new StringBuilder("/test/{p1}/ddd/ddd/{ p4 } }"));
		}
	}

	public static class SampleParam {
		@RequestURI
		private final StringBuilder requestUri;
		@PathParam
		private String path1 = "1";
		@PathParam
		private String path2 = "2";
		@PathParam
		private String path3 = "3";
		@PathParam
		private String path4 = "4";
		@Param
		private String value5 = "5";
		@Param
		private String value6 = "6";
		@Param
		private String value7 = "7";

		SampleParam(StringBuilder builder) {
			this.requestUri = builder;
		}
	}
}
