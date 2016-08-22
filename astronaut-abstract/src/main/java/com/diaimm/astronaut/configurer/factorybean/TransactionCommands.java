package com.diaimm.astronaut.configurer.factorybean;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction.TransactionIdFrom;
import com.diaimm.astronaut.configurer.annotations.method.Put;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;
import com.google.common.base.Optional;

class TransactionCommands {
	private static final Logger log = LoggerFactory.getLogger(TransactionCommands.class);
	private final RestTemplateRepositoryInvocationHandler invocationHandler;
	private final Transaction transaction;
	private final Method method;
	private final Object[] args;
	private TransactionAPICaller transactionAPICaller = new TransactionAPICaller();

	TransactionCommands(RestTemplateRepositoryInvocationHandler invocationHandler, Transaction transaction, Method method, Object[] args) {
		this.invocationHandler = invocationHandler;
		this.transaction = transaction;
		this.method = method;
		this.args = args;
	}

	static Optional<TransactionCommands> create(RestTemplateRepositoryInvocationHandler invocationHandler, Method method, Object[] args,
		Optional<Transaction> transaction) {
		if (!transaction.isPresent()) {
			return Optional.absent();
		}

		return Optional.of(new TransactionCommands(invocationHandler, transaction.get(), method, args));
	}

	TransactionCommand commit(final Object apiCallResult) {
		return new TransactionCommand() {
			@Override
			public void execute() {
				callTransactionFinishAPI(transaction, method, args, apiCallResult, transaction.commit());
			}
		};
	}

	TransactionCommand rollback(final Object apiCallResult) {
		return new TransactionCommand() {
			@Override
			public void execute() {
				callTransactionFinishAPI(transaction, method, args, apiCallResult, transaction.rollback());
			}
		};
	}

	private void callTransactionFinishAPI(final Transaction transaction, final Method method, final Object[] args, Object result, String apiURI) {
		TransactionIdFrom transactionIdFrom = transaction.transactionIdFrom();
		Object extractTransactionInfo = transactionIdFrom.extractTransactionInfo(transaction.transactionId(), method, args, result);
		transactionAPICaller.callTransactionAPI(invocationHandler, invocationHandler.getApiURLPrefix() + apiURI, extractTransactionInfo);
	}
	
	void setTransactionAPICaller(TransactionAPICaller transactionAPICaller){
		this.transactionAPICaller = transactionAPICaller;
	}

	static class TransactionAPICaller {
		void callTransactionAPI(RestTemplateRepositoryInvocationHandler invocationHandler, String apiFullURL, Object extractTransactionInfo) {
			Put.RestTemplateInvoker restTemplateInvoker = new Put.RestTemplateInvoker();
			restTemplateInvoker.doInvoke(invocationHandler.getRestTemplate(), apiFullURL, extractTransactionInfo);
			log.debug("finishing transaction... with {}", apiFullURL);
		}
	}
}
