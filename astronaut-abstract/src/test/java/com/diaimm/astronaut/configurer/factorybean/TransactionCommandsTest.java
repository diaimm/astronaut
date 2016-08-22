package com.diaimm.astronaut.configurer.factorybean;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.annotations.method.PostForObject;
import com.diaimm.astronaut.configurer.factorybean.TransactionCommands.TransactionAPICaller;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

@RunWith(MockitoJUnitRunner.class)
public class TransactionCommandsTest {
	@Test
	public void createTest() {
		RestTemplateRepositoryInvocationHandler invocationHandler = Mockito.mock(RestTemplateRepositoryInvocationHandler.class);
		Transaction transaction = Mockito.mock(Transaction.class);
		Method method = null;
		Object[] args = new Object[0];

		Optional<TransactionCommands> commands = TransactionCommands.create(invocationHandler, method, args, Optional.fromNullable(transaction));
		Assert.assertNotNull(commands);
		Assert.assertTrue(commands.isPresent());

		Optional<TransactionCommands> commandsOfNullTransaction = TransactionCommands.create(invocationHandler, method, args,
			Optional.<Transaction> absent());
		Assert.assertNotNull(commandsOfNullTransaction);
		Assert.assertTrue(!commandsOfNullTransaction.isPresent());
	}

	@Test
	public void withActualMethodTest() throws NoSuchMethodException, SecurityException {
		Method method = SampleClass.class.getDeclaredMethod("testMethod");
		method.setAccessible(true);
		RestTemplateRepositoryInvocationHandler invocationHandler = Mockito.mock(RestTemplateRepositoryInvocationHandler.class);
		Transaction transaction = method.getAnnotation(Transaction.class);
		Object[] args = new Object[0];
		TransactionAPICaller transactionAPICaller = Mockito.mock(TransactionAPICaller.class);

		Mockito.when(invocationHandler.getApiURLPrefix()).thenReturn("http://this.is.the.prefix");

		Optional<TransactionCommands> commands = TransactionCommands.create(invocationHandler, method, args, Optional.fromNullable(transaction));
		Assert.assertNotNull(commands);
		Assert.assertTrue(commands.isPresent());

		TransactionCommands transactionCommands = commands.get();
		transactionCommands.setTransactionAPICaller(transactionAPICaller);
		transactionCommands.commit(new APIResponse<SomeSampleResponse>() {
		}).execute();
		transactionCommands.rollback(new APIResponse<SomeSampleResponse>() {
		}).execute();
		
		Mockito.verify(transactionAPICaller).callTransactionAPI((RestTemplateRepositoryInvocationHandler) Mockito.anyObject(),
			Mockito.eq("http://this.is.the.prefix/this/is/to/commit"), Mockito.anyObject());
		Mockito.verify(transactionAPICaller).callTransactionAPI((RestTemplateRepositoryInvocationHandler) Mockito.anyObject(),
			Mockito.eq("http://this.is.the.prefix/and/for/rollback"), Mockito.anyObject());
	}

	private static interface SampleClass {
		@PostForObject(dummySupplier = SomeSmapleResponseDummySupplier.class)
		@Transaction(commit = "/this/is/to/commit", rollback = "/and/for/rollback")
		public APIResponse<SomeSampleResponse> testMethod();
	}

	private static class SomeSampleResponse {

	}

	public static class SomeSmapleResponseDummySupplier implements Supplier<SomeSampleResponse> {
		@Override
		public SomeSampleResponse get() {
			return new SomeSampleResponse();
		}
	}
}
