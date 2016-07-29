package com.diaimm.astronaut.configurer.transaction;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;

public class RestTemplateTransactionManager extends AbstractPlatformTransactionManager {
	private static Logger log = LoggerFactory.getLogger(RestTemplateTransactionManager.class);
	private static final long serialVersionUID = -143377730767700027L;
	@Autowired
	private ApplicationContext applicationContext;
	private ThreadLocal<RestTemplateTransactionObject> transactionObject = new ThreadLocal<RestTemplateTransactionObject>();

	public RestTemplateTransactionObject getTransactionObject() {
		return transactionObject.get();
	}

	@Override
	protected Object doGetTransaction() throws TransactionException {
		if (transactionObject.get() == null) {
			transactionObject.set(new RestTemplateTransactionObject());
		}
		return transactionObject.get();
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		executeTransactionCommandsAll(this.transactionObject.get().getCommits());
		log.debug("commit : {} ", this.transactionObject.get());
		this.transactionObject.remove();

	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
		executeTransactionCommandsAll(this.transactionObject.get().getRollbacks());
		log.debug("rollback : {} ", this.transactionObject.get());
		this.transactionObject.remove();
	}

	private void executeTransactionCommandsAll(List<TransactionCommand> commands) {
		Exception hasException = null;
		for (TransactionCommand command : commands) {
			try {
				command.execute();
			} catch (Exception e) {
				// all commands must be called
				log.error(e.getMessage(), e);
				hasException = e;
			}
		}
		if (hasException != null) {
			throw new HeuristicCompletionException(HeuristicCompletionException.STATE_MIXED, hasException);
		}
	}
}
