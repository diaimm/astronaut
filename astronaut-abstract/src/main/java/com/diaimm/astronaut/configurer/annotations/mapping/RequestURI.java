package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.google.common.base.Function;
import com.google.common.base.Optional;

@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestURI {
	static class RequestURIExtractorsUtils {
		private static Logger logger = LoggerFactory.getLogger(RequestURI.class);

		private RequestURIExtractorsUtils() {
			throw new UnsupportedOperationException("initiation is not allowed");
		}

		static Optional<String> findRequestURIFromFields(Object instance, boolean accessToField) {
			if (instance == null) {
				return Optional.absent();
			}

			Field[] declaredFields = instance.getClass().getDeclaredFields();
			for (Field field : declaredFields) {
				if (accessToField) {
					field.setAccessible(true);
				}
				if (field.isAnnotationPresent(RequestURI.class)) {
					if (Void.class.equals(field.getType())) {
						continue;
					}

					try {
						Object object = field.get(instance);
						if (object == null) {
							continue;
						}

						return Optional.fromNullable(object.toString());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						logger.info("An exception occured while getting RequestURI from param : {}", e.getMessage());
					}
				}
			}

			return Optional.absent();
		}

		static Optional<String> findRequestURIFromMethod(Object instance, boolean accessUnexposedMethod) {
			if (instance == null) {
				return Optional.absent();
			}

			Class<?> clazz = instance.getClass();
			Method[] declaredMethods = clazz.getDeclaredMethods();
			for (Method method : declaredMethods) {
				if (accessUnexposedMethod) {
					method.setAccessible(true);
				}

				RequestURI requestUri = AnnotationUtilsExt.findAnnotation(method, RequestURI.class);
				if (requestUri != null) {
					if (void.class.equals(method.getReturnType())) {
						continue;
					}

					try {
						Object invoked = method.invoke(instance);
						if (invoked == null) {
							continue;
						}

						return Optional.fromNullable(invoked.toString());
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						logger.info("An exception occured while getting RequestURI from param : {}", e.getMessage());
					}
				}
			}
			return Optional.absent();
		}
	}

	public static class RequestURIExtractors {
		private RequestURIExtractors(){
			throw new UnsupportedOperationException("initiation is not allowed");
		}
		
		public static Function<Annotation, Optional<String>> fromAnnotation = new Function<Annotation, Optional<String>>() {
			@Override
			public Optional<String> apply(Annotation input) {
				return RequestURIExtractorsUtils.findRequestURIFromMethod(input, true);
			}
		};

		public static Function<APIMethodInvocation, Optional<String>> fromArguements = new Function<APIMethodInvocation, Optional<String>>() {
			@Override
			public Optional<String> apply(APIMethodInvocation invocation) {
				Method apiMethod = invocation.getApiMethod();
				Object[] args = invocation.getArgs();

				Optional<Integer> candidateIndex = Optional.absent();
				Annotation[][] parameterAnnotations = apiMethod.getParameterAnnotations();
				for (int index = 0; index < parameterAnnotations.length; index++) {
					Annotation[] argAnnotations = parameterAnnotations[index];
					if (!AnnotationUtilsExt.contains(argAnnotations, RequestURI.class)) {
						if (AnnotationUtilsExt.contains(argAnnotations, Form.class)) {
							candidateIndex = Optional.of(index);
						}
						continue;
					}

					Object targetArgument = args[index];
					if (targetArgument == null) {
						continue;
					}

					return Optional.fromNullable(targetArgument.toString());
				}

				if (!candidateIndex.isPresent()) {
					return Optional.absent();
				}

				Object requestParamArg = args[candidateIndex.get()];
				Optional<String> fromFields = RequestURIExtractorsUtils.findRequestURIFromFields(requestParamArg, true);
				if (fromFields.isPresent()) {
					return fromFields;
				}

				return RequestURIExtractorsUtils.findRequestURIFromMethod(requestParamArg, true);
			}
		};

		public static class APIMethodInvocation {
			private Method apiMethod;
			private Object[] args;

			public APIMethodInvocation(Method apiMethod, Object[] args) {
				this.apiMethod = apiMethod;
				this.args = args;
			}

			public Method getApiMethod() {
				return this.apiMethod;
			}

			public Object[] getArgs() {
				return this.args;
			}
		}
	}
}
