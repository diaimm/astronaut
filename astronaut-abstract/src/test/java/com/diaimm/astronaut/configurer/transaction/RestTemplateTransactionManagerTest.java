package com.diaimm.astronaut.configurer.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;

public class RestTemplateTransactionManagerTest {
	@Test
	public void testAll() {
		RestTemplateTransactionManager target = new RestTemplateTransactionManager();

		TransactionCommand commitCommand = Mockito.mock(TransactionCommand.class);
		TransactionCommand rollbackCommand = Mockito.mock(TransactionCommand.class);

		target.doBegin(null, null);
		target.doGetTransaction();
		RestTemplateTransactionObject transactionObject = target.getTransactionObject();
		transactionObject.pushToCallStack(commitCommand, rollbackCommand);

		target.doCommit(Mockito.mock(DefaultTransactionStatus.class));
		Mockito.verify(commitCommand).execute();
		Mockito.verify(rollbackCommand, Mockito.never()).execute();
		Mockito.reset(commitCommand, rollbackCommand);

		target.doGetTransaction();
		transactionObject = target.getTransactionObject();
		transactionObject.pushToCallStack(commitCommand, rollbackCommand);
		target.doRollback(Mockito.mock(DefaultTransactionStatus.class));
		Mockito.verify(rollbackCommand).execute();
		Mockito.verify(commitCommand, Mockito.never()).execute();
	}

	@Test
	public void executeTransactionCommandsAllTest() {
		RestTemplateTransactionManager target = new RestTemplateTransactionManager();
		RestTemplateTransactionObject transactionObject = (RestTemplateTransactionObject) target.doGetTransaction();
		transactionObject.getCommits();
		
		target.doCommit(null);
		
		TransactionCommand withException = Mockito.mock(TransactionCommand.class);
		try {
			transactionObject.getCommits().add(withException);
		} catch(Exception e){
			Assert.fail();
		}
		
		Mockito.doThrow(new IllegalStateException("test")).when(withException).execute();
		try {
			transactionObject.getCommits().add(withException);
		} catch(Exception e){
			Assert.assertEquals(HeuristicCompletionException.class, e.getClass());
		}
	}
}
