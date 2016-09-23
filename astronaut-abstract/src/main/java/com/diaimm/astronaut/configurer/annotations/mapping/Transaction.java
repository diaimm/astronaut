package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ReflectionUtils;

import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.google.common.base.Optional;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transaction {
	String commit();

	String rollback();

	TransactionIdFrom transactionIdFrom()

	default TransactionIdFrom.RESPONSE;

	String transactionId() default "";

	enum TransactionIdFrom {
		RESPONSE {
			@Override
			public Object extractTransactionInfo(String transactionIdFieldName, Method method, Object[] args, Object result) {
				try {
					if (StringUtils.isBlank(transactionIdFieldName)) {
						return result;
					}

					return getFieldValue(transactionIdFieldName, result);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

		},
		REQUEST {
			@Override
			public Object extractTransactionInfo(String transactionIdFieldName,
				Method method, Object[] args, Object result) {
				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				int targetParamterIndex = findTargetParameterIndex(parameterAnnotations);
				if (targetParamterIndex < 0) {
					return null;
				}

				Object target = args[targetParamterIndex];
				if (StringUtils.isBlank(transactionIdFieldName)) {
					return target;
				}

				return getFieldValue(transactionIdFieldName, target);
			}

			private int findTargetParameterIndex(Annotation[][] parameterAnnotations) {
				for (int parameterIndex = 0; parameterIndex < parameterAnnotations.length; parameterIndex++) {
					Optional<TransactionId> found = AnnotationUtilsExt.find(parameterAnnotations[parameterIndex], TransactionId.class);
					if (found.isPresent()) {
						return parameterIndex;
					}
				}

				return -1;
			}
		};

		Object getFieldValue(String transactionIdFieldName, Object target) {
			if (target == null) {
				return null;
			}

			Field targetField = ReflectionUtils.findField(target.getClass(), transactionIdFieldName);
			if (targetField == null) {
				return null;
			}

			targetField.setAccessible(true);
			return ReflectionUtils.getField(targetField, target);
		}

		public abstract Object extractTransactionInfo(String transactionIdFieldName, Method method, Object[] args, Object result);
	}
}
