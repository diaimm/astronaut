package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors.APIMethodInvocation;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractRestTemplateInvoker<T extends Annotation> implements RestTemplateInvoker<T> {
	private static Logger log = LoggerFactory.getLogger(AbstractRestTemplateInvoker.class);
	private static final String COMPACTIZE_TARGET_PREFIX = "!";
	private final Pattern BINDING_PATTERN = Pattern.compile("\\{\\s*([!]{0,1}[a-zA-Z_0-9^}]+)\\s*\\}");
	private final Object cacheLock = new Object();
	private Map<Class<?>, APIArgumentNormalizer<?>> argumentNormalizers = Maps.newConcurrentMap();

	/**
	 * LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new
	 * LocalVariableTableParameterNameDiscoverer();
	 */
	@Override
	public String extractAPIUrl(T annotation, Method method, Object[] methodArguemtns) {
		StringBuffer baseUrl = new StringBuffer(this.getBaseUrl(annotation, method, methodArguemtns));

		List<String> parameterNames = getParamAnnotatedParameters(method);
		for (String paramterName : parameterNames) {
			if (baseUrl.indexOf("?") == -1) {
				baseUrl.append("?");
			} else {
				baseUrl.append("&");
			}

			baseUrl.append(paramterName).append("={").append(paramterName).append("}");
		}
		return baseUrl.toString();
	}

	private List<String> getParamAnnotatedParameters(Method methodIn) {
		Class<?>[] parameterTypes = methodIn.getParameterTypes();
		Annotation[][] parameterAnnotations = methodIn.getParameterAnnotations();

		List<String> result = Lists.newArrayList();
		for (int index = 0; index < parameterTypes.length; index++) {
			Class<?> parameterType = parameterTypes[index];
			Annotation[] parameterAnnotation = parameterAnnotations[index];
			if (AnnotationUtilsExt.contains(parameterAnnotation, Form.class)) {
				if (parameterTypes.length > 1) {
					raiseFormArgumentValidationFailException();
				}
				return processParamAnnotatedArgumentFromForm(parameterType, parameterAnnotation);
			}

			if (AnnotationUtilsExt.contains(parameterAnnotation, Param.class)) {
				processParamAnnotatedArgument(result, parameterAnnotation);
			}
		}
		return result;
	}

	private List<String> processParamAnnotatedArgumentFromForm(Class<?> parameterType, Annotation[] annotations) {
		List<String> result = Lists.newArrayList();

		for (Field field : parameterType.getDeclaredFields()) {
			if (field.isAnnotationPresent(Param.class)) {
				result.add(field.getName());
			}
		}

		return result;
	}

	private void processParamAnnotatedArgument(List<String> result, Annotation[] annotations) {
		Param param = AnnotationUtilsExt.find(annotations, Param.class);
		if (param == null) {
			return;
		}
		result.add(param.value());
	}

	@Override
	public Object invoke(TypeHandlingRestTemplate restTemplate, String apiUrl, Method method, T annotation, Object[] args) {
		try {
			Class<?> returnType = method.getReturnType();
			if (APIResponse.class.isAssignableFrom(returnType)) {
				Type genericReturnType = method.getGenericReturnType();
				if (ParameterizedType.class.isAssignableFrom(genericReturnType.getClass())) {
					ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
					Object forEntity = processDoInvoke(restTemplate, apiUrl, ((ParameterizedType) parameterizedType).getActualTypeArguments()[0],
						annotation, normalizeArguments(apiUrl, method, args));
					return APIResponse.getInstance(apiUrl, args, forEntity);
				}
			}

			return processDoInvoke(restTemplate, apiUrl, method.getGenericReturnType(), annotation, normalizeArguments(apiUrl, method, args));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (APIResponse.class.isAssignableFrom(method.getReturnType())) {
				return APIResponse.getInstance(apiUrl, args, e);
			}

			throw new IllegalStateException(e);
		}
	}

	private Object processDoInvoke(TypeHandlingRestTemplate restTemplate, String apiUrl, Type returnType, T annotation,
		Object[] normalizedArguments) throws Exception {

		APICallInfoCompactizer<T> compactizer = new APICallInfoCompactizer<T>(this, apiUrl, normalizedArguments);
		Object result = doInvoke(restTemplate, compactizer, returnType, annotation);
		if (result instanceof ResponseEntity) {
			return ((ResponseEntity<?>) result).getBody();
		}
		return result;
	}

	public static class APICallInfoCompactizer<T extends Annotation> {
		private final AbstractRestTemplateInvoker<T> invoker;
		private final String sourceApiUrl;
		private final Object[] sourceArguments;
		private String apiUrl;
		private Object[] arguments;

		APICallInfoCompactizer(AbstractRestTemplateInvoker<T> invoker, String apiUrl, Object[] arguments) {
			this.invoker = invoker;
			this.sourceApiUrl = apiUrl;
			this.sourceArguments = arguments;

			this.compactize();
		}

		void compactize() {
			String newApiUrl = this.sourceApiUrl;
			List<Object> newArguments = Lists.newArrayList();
			List<String> bindings = invoker.extractBindings(newApiUrl);
			for (int index = 0; index < bindings.size(); index++) {
				String bindingKey = bindings.get(index);
				Object bindingValue = sourceArguments[index];

				if (!bindingKey.startsWith(COMPACTIZE_TARGET_PREFIX)) {
					newArguments.add(bindingValue);
					continue;
				}

				if (bindingValue != null && StringUtils.isNotBlank(bindingValue.toString())) {
					newArguments.add(bindingValue);
					continue;
				}

				newApiUrl = newApiUrl.replaceFirst("\\{\\s*" + bindingKey + "\\s*\\}", "");
			}

			this.apiUrl = invoker.makeUrlValid(newApiUrl);
			this.arguments = newArguments.toArray();
		}

		public AbstractRestTemplateInvoker<T> getInvoker() {
			return this.invoker;
		}

		public String getSourceApiUrl() {
			return this.sourceApiUrl;
		}

		public Object[] getSourceArguments() {
			return this.sourceArguments;
		}

		public String getApiUrl() {
			return this.apiUrl;
		}

		public Object[] getArguments() {
			return this.arguments;
		}
	}

	String makeUrlValid(String sample) {
		Pattern protocol = Pattern.compile("(http[s]{0,1}://)");
		Matcher matcher = protocol.matcher(sample);
		if (matcher.find()) {
			MatchResult matchResult = matcher.toMatchResult();
			return matchResult.group(0) + matcher.replaceAll("").replaceAll("[/]{2}", "/");
		}

		return sample.replaceAll("[/]{2}", "/");
	}

	@Override
	public void addAPIArgumentNormalizer(Class<?> supportType, APIArgumentNormalizer<?> normalizer) {
		if (!this.argumentNormalizers.containsKey(supportType)) {
			synchronized (cacheLock) {
				if (!this.argumentNormalizers.containsKey(supportType)) {
					this.argumentNormalizers.put(supportType, normalizer);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private APIArgumentNormalizer findCachedArgumentNormalizer(Class<? extends APIArgumentNormalizer> normalizerType) {
		if (normalizerType == APIArgumentNormalizer.class) {
			return null;
		}

		if (!this.argumentNormalizers.containsKey(normalizerType)) {
			synchronized (cacheLock) {
				if (!this.argumentNormalizers.containsKey(normalizerType)) {
					try {
						Constructor<? extends APIArgumentNormalizer> declaredConstructor = normalizerType.getDeclaredConstructor();
						declaredConstructor.setAccessible(true);

						addAPIArgumentNormalizer(normalizerType, declaredConstructor.newInstance());
					} catch (Exception e) {
						addAPIArgumentNormalizer(normalizerType, null);
						throw new IllegalStateException("cannot create " + normalizerType + " instance. - No Default Constructor exists.", e);
					}
				}
			}
		}

		return this.argumentNormalizers.get(normalizerType);
	}

	Object[] normalizeArguments(String apiUrl, Method method, Object[] args)
		throws NoSuchFieldException, IllegalAccessException, JsonGenerationException,
		JsonMappingException, IOException {
		if (ArrayUtils.isEmpty(args)) {
			return args;
		}

		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		List<Object> result = Lists.newArrayList();
		for (int index = 0; index < args.length; index++) {
			if (AnnotationUtilsExt.contains(parameterAnnotations[index], RequestURI.class)) {
				continue;
			}

			Form formAnnotation = AnnotationUtilsExt.find(parameterAnnotations[index], Form.class);
			if (formAnnotation != null) {
				return this.normalizeArgumentsFromForm(extractBindings(apiUrl), args);
			}
			result.add(normalizeArgument(findArgumentAnnotatedNormalizer(parameterAnnotations[index]), args[index]));
		}

		return result.toArray(new Object[0]);
	}

	private List<String> extractBindings(String apiUrl) {
		List<String> result = Lists.newArrayList();
		Matcher matcher = BINDING_PATTERN.matcher(apiUrl);
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}

	private Object[] normalizeArgumentsFromForm(List<String> parameterNames, Object[] args) throws NoSuchFieldException, IllegalAccessException,
		JsonGenerationException, JsonMappingException, IOException {
		if (args.length != 1) {
			raiseFormArgumentValidationFailException();
		}

		if (ArrayUtils.isEmpty(args)) {
			return new Object[0];
		}

		List<Object> argsFromField = Lists.newArrayList();
		Object formInstance = args[0];
		Class<?> formClass = formInstance.getClass();
		for (String fieldName : parameterNames) {
			fieldName = getActualFieldName(fieldName);

			Field field = ReflectionUtils.findField(formClass, fieldName);
			if (field.isAnnotationPresent(RequestURI.class)) {
				continue;
			}

			if (field.isAnnotationPresent(Param.class) || field.isAnnotationPresent(PathParam.class)) {
				field.setAccessible(true);
				Object parameterValue = field.get(formInstance);
				argsFromField.add(normalizeArgument(findArgumentAnnotatedNormalizer(field.getAnnotations()), parameterValue));
			}
		}
		return argsFromField.toArray(new Object[0]);
	}

	private String getActualFieldName(String fieldName) {
		if (fieldName.startsWith(COMPACTIZE_TARGET_PREFIX)) {
			return fieldName.substring(1);
		}
		return fieldName;
	}

	private void raiseFormArgumentValidationFailException() {
		throw new IllegalStateException("when use @Form, only 1 argument is allowed");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object normalizeArgument(APIArgumentNormalizer normalizer, Object parameterValue) throws JsonGenerationException, JsonMappingException,
		IOException {
		if (parameterValue == null) {
			return null;
		}

		if (normalizer != null) {
			return normalizer.normalize(parameterValue);
		}

		if (this.argumentNormalizers.containsKey(parameterValue.getClass())) {
			APIArgumentNormalizer apiArgumentNormalizer = this.argumentNormalizers.get(parameterValue.getClass());
			return apiArgumentNormalizer.normalize(parameterValue);
		}

		for (Class<?> supportingType : this.argumentNormalizers.keySet()) {
			if (supportingType.isAssignableFrom(parameterValue.getClass())) {
				APIArgumentNormalizer apiArgumentNormalizer = this.argumentNormalizers.get(supportingType);
				return apiArgumentNormalizer.normalize(parameterValue);
			}
		}

		return parameterValue;
	}

	@SuppressWarnings("rawtypes")
	private APIArgumentNormalizer findArgumentAnnotatedNormalizer(Annotation[] annotations) {
		Param param = AnnotationUtilsExt.find(annotations, Param.class);
		if (param != null) {
			return findCachedArgumentNormalizer(param.normalizer());
		}

		PathParam pathParam = AnnotationUtilsExt.find(annotations, PathParam.class);
		if (pathParam != null) {
			return findCachedArgumentNormalizer(pathParam.normalizer());
		}

		return null;
	}

	protected String getBaseUrl(T annotation, Method annotatedMethod, Object[] arguments) {
		Optional<String> fromMethod = RequestURIExtractors.fromArguements.apply(new APIMethodInvocation(annotatedMethod, arguments));
		if (fromMethod.isPresent()) {
			return fromMethod.get();
		}

		Optional<String> fromAnnotation = RequestURIExtractors.fromAnnotation.apply(annotation);
		if (fromAnnotation.isPresent()) {
			return fromAnnotation.get();
		}

		throw new IllegalStateException("Can not find any valid RequestURI annotated value.");
	}

	protected abstract Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<T> compactizer, Type returnType, T annotation)
		throws Exception;
}
