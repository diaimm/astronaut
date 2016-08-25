package com.diaimm.astronaut.configurer.transaction;

import org.junit.Test;
import org.mockito.Mockito;

import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;

import junit.framework.Assert;

public class RestTemplateTransactionObjectTest {
	@Test
	public void testAll() {
		TransactionCommand commitCommand = Mockito.mock(TransactionCommand.class);
		TransactionCommand rollbackCommand = Mockito.mock(TransactionCommand.class);
		RestTemplateTransactionObject target = new RestTemplateTransactionObject();
		target.pushToCallStack(commitCommand, rollbackCommand);

		Assert.assertEquals(1, target.getCommits().size());
		Assert.assertEquals(commitCommand, target.getCommits().get(0));
		
		Assert.assertEquals(1, target.getRollbacks().size());
		Assert.assertEquals(rollbackCommand, target.getRollbacks().get(0));
	}
}
