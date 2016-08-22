package com.diaimm.astronaut.configurer.factorybean;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diaimm.astronaut.configurer.AnnotationUtilsExt;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject;
import com.google.common.base.Optional;

class TransactionalRestTemplateInvocationHandler implements InvocationHandler {
	private static final Logger log = LoggerFactory.getLogger(TransactionalRestTemplateInvocationHandler.class);
	private RestTemplateRepositoryInvocationHandler invocationHandler;
	private RestTemplateTransactionManager transactionManger;

	TransactionalRestTemplateInvocationHandler(TypeHandlingRestTemplate restTemplate, URI apiURI, String pathPrefix,
		RestTemplateTransactionManager transactionManger) {
		this.invocationHandler = new RestTemplateRepositoryInvocationHandler(restTemplate, apiURI, pathPrefix);
		this.transactionManger = transactionManger;
	}

	@Override
	public Object invoke(Object instance, Method method, Object[] args) throws Throwable {
		Annotation[] annotations = method.getAnnotations();

		Optional<Annotation> apiMappingAnnotation = AnnotationUtilsExt.findAnyAnnotationAnnotatedWith(annotations, APIMapping.class);
		if (!apiMappingAnnotation.isPresent()) {
			return null;
		}

		Object result = null;
		RuntimeException exception = null;
		Optional<TransactionCommands> transactionCommands = TransactionCommands.create(invocationHandler, method, args,
			AnnotationUtilsExt.find(annotations, Transaction.class));
		try {
			result = invocationHandler.invoke(instance, method, args);
			result = addCallInfoForTransaction(transactionCommands, result);
			return result;
		} catch (RuntimeException e) {
			exception = e;
			throw e;
		} finally {
			endTransactionIfNeed(transactionCommands, result, exception);
		}
	}

	private Object addCallInfoForTransaction(Optional<TransactionCommands> commands, final Object apiCallResult) {
		if (!commands.isPresent()) {
			return apiCallResult;
		}

		RestTemplateTransactionObject transactionObject = this.transactionManger.getTransactionObject();
		if (transactionObject == null) {
			return apiCallResult;
		}

		transactionObject.pushToCallStack(commands.get().commit(apiCallResult), commands.get().rollback(apiCallResult));
		return apiCallResult;
	}

	private void endTransactionIfNeed(Optional<TransactionCommands> transactionCommands, Object result, RuntimeException exception) {
		if (!transactionCommands.isPresent()) {
			return;
		}

		RestTemplateTransactionObject transactionObject = this.transactionManger.getTransactionObject();
		// if transactionObject is not null, commits or rollbacks will happen in transaction manager.
		if (transactionObject != null) {
			return;
		}

		handleSingleTransaction(transactionCommands, result, exception);
	}

	private void handleSingleTransaction(Optional<TransactionCommands> transactionCommands, Object result, RuntimeException exception) {
		if (exception != null) {
			try {
				transactionCommands.get().rollback(result).execute();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			return;
		}

		try {
			transactionCommands.get().commit(result).execute();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}