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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.mapping.PathParam;
import com.diaimm.astronaut.configurer.annotations.mapping.PostBody;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI.RequestURIExtractors.APIMethodInvocation;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

		List<ParamConfig> parameterNames = getParamAnnotatedParameters(method);
		for (ParamConfig paramterName : parameterNames) {
			if (baseUrl.indexOf("?") == -1) {
				baseUrl.append("?");
			} else {
				baseUrl.append("&");
			}

			baseUrl.append(paramterName.paramKey).append("={").append(paramterName.fieldName).append("}");
		}
		return baseUrl.toString();
	}

	private static class ParamConfig {
		private String paramKey;
		private String fieldName;

		public ParamConfig(String paramKey, String fieldName) {
			this.paramKey = paramKey;
			this.fieldName = fieldName;
		}
	}

	private List<ParamConfig> getParamAnnotatedParameters(Method methodIn) {
		Class<?>[] parameterTypes = methodIn.getParameterTypes();
		Annotation[][] parameterAnnotations = methodIn.getParameterAnnotations();

		List<ParamConfig> result = Lists.newArrayList();
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

	private List<ParamConfig> processParamAnnotatedArgumentFromForm(Class<?> parameterType, Annotation[] annotations) {
		List<ParamConfig> result = Lists.newArrayList();

		for (Field field : parameterType.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Param.class)) {
				continue;
			}

			Param paramAnnotation = field.getAnnotation(Param.class);
			String configuredName = paramAnnotation.value();
			result.add(new ParamConfig(StringUtils.isBlank(configuredName) ? field.getName() : configuredName.trim(), field.getName()));
		}

		return result;
	}

	private void processParamAnnotatedArgument(List<ParamConfig> result, Annotation[] annotations) {
		Param param = AnnotationUtilsExt.find(annotations, Param.class);
		if (param == null) {
			return;
		}
		result.add(new ParamConfig(param.value(), param.value()));
	}

	@Override
	public Object invoke(TypeHandlingRestTemplate restTemplate, String apiUrl, Method method, T annotation, Object[] args) {
		APICallInfoCompactizer<T> compactizer = null;
		try {
			compactizer = new APICallInfoCompactizer<T>(this, apiUrl, method, args);
			Class<?> returnType = method.getReturnType();
			if (APIResponse.class.isAssignableFrom(returnType)) {
				Type genericReturnType = method.getGenericReturnType();
				if (ParameterizedType.class.isAssignableFrom(genericReturnType.getClass())) {
					ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
					Object forEntity = processDoInvoke(restTemplate, ((ParameterizedType) parameterizedType).getActualTypeArguments()[0], annotation,
						compactizer);
					return APIResponse.getInstance(apiUrl, args, forEntity, compactizer);
				}
			}

			return processDoInvoke(restTemplate, method.getGenericReturnType(), annotation, compactizer);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (APIResponse.class.isAssignableFrom(method.getReturnType())) {
				return APIResponse.getInstance(apiUrl, args, e, compactizer);
			}

			throw new IllegalStateException(e);
		}
	}

	private Object processDoInvoke(TypeHandlingRestTemplate restTemplate, Type returnType, T annotation, APICallInfoCompactizer<T> compactizer)
		throws Exception {
		Object result = doInvoke(restTemplate, compactizer, returnType, annotation);
		if (result instanceof ResponseEntity) {
			return ((ResponseEntity<?>) result).getBody();
		}
		return result;
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

	public static class APICallInfoCompactizer<T extends Annotation> {
		private final AbstractRestTemplateInvoker<T> invoker;
		private final String sourceApiUrl;
		private final Object[] sourceArguments;
		private String apiUrl;
		private Object postBody;
		private Method method;
		private Object[] arguments;

		APICallInfoCompactizer(AbstractRestTemplateInvoker<T> invoker, String apiUrl, Method method, Object[] args)
			throws NoSuchFieldException, IllegalAccessException, JsonGenerationException, JsonMappingException, IOException {
			this.invoker = invoker;
			this.sourceApiUrl = apiUrl;
			this.method = method;
			this.postBody = this.findPostBody(args);
			this.sourceArguments = invoker.normalizeArguments(apiUrl, method, args);

			this.compactize();
		}

		Object findPostBody(Object[] args) throws IllegalArgumentException, IllegalAccessException {
			if (ArrayUtils.isEmpty(args)) {
				return null;
			}

			Annotation[][] parameterAnnotations = method.getParameterAnnotations();
			Object found = null;
			for (int index = 0; index < args.length; index++) {
				if (AnnotationUtilsExt.contains(parameterAnnotations[index], PostBody.class)) {
					if (found != null) {
						raiseMoreThan1PostBodyFoundException();
					}

					found = args[index];
				}

				Form formAnnotation = AnnotationUtilsExt.find(parameterAnnotations[index], Form.class);
				if (formAnnotation != null) {
					Object fromForm = this.findPostBody(args[index]);
					if (fromForm != null) {
						if (found != null) {
							raiseMoreThan1PostBodyFoundException();
						}

						found = fromForm;
					}
				}
			}

			return found;
		}

		private void raiseMoreThan1PostBodyFoundException() {
			throw new IllegalStateException("found more than 1 PostBody annotated fields(or params)");
		}

		Object findPostBody(Object formInstance) throws IllegalArgumentException, IllegalAccessException {
			if (formInstance == null) {
				return null;
			}

			Class<?> formClass = formInstance.getClass();
			Object found = null;
			for (Field field : formClass.getDeclaredFields()) {
				if (field.isAnnotationPresent(PostBody.class)) {
					if (found != null) {
						raiseMoreThan1PostBodyFoundException();
					}

					field.setAccessible(true);
					found = field.get(formInstance);
				}
			}
			return found;
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

		public Object getPostBody() {
			return this.postBody;
		}
	}

	protected abstract Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<T> compactizer, Type returnType, T annotation)
		throws Exception;
}
