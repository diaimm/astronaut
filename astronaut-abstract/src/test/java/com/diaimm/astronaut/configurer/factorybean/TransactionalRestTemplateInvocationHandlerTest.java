package com.diaimm.astronaut.configurer.factorybean;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.diaimm.astronaut.configurer.TypeHandlingRestOperations;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalRestTemplateInvocationHandlerTest {
	@Test
	public void invokeTest() throws Throwable {
		RestTemplateTransactionManager transactionManager = Mockito.mock(RestTemplateTransactionManager.class);
		RestTemplateRepositoryInvocationHandler invocationHandler = Mockito.mock(RestTemplateRepositoryInvocationHandler.class);
		
		Mockito.when(invocationHandler.getRestTemplate()).thenReturn(Mockito.mock(TypeHandlingRestOperations.class));

		Method method = SampleClass.class.getDeclaredMethod("testMethod");
		method.setAccessible(true);
		Object[] args = new Object[0];

		Object expected = new Object();
		Mockito.when(invocationHandler.invoke(Mockito.anyObject(), (Method) Mockito.anyObject(), (Object[]) Mockito.anyObject())).thenReturn(
			expected);

		RestTemplateTransactionObject transactionObject = Mockito.mock(RestTemplateTransactionObject.class);

		// with transaction
		Mockito.when(transactionManager.getTransactionObject()).thenReturn(transactionObject);
		TransactionalRestTemplateInvocationHandler handler = new TransactionalRestTemplateInvocationHandler(invocationHandler, transactionManager);
		Object result = handler.invoke(new Object(), method, args);
		Assert.assertNotNull(result);
		Assert.assertEquals(expected, result);

		Mockito.verify(transactionObject, Mockito.timeout(1)).pushToCallStack((TransactionCommand) Mockito.anyObject(), (TransactionCommand) Mockito.anyObject());
	
		// no transaction
		Mockito.when(transactionManager.getTransactionObject()).thenReturn(null);
		handler = new TransactionalRestTemplateInvocationHandler(invocationHandler, transactionManager);
		result = handler.invoke(new Object(), method, args);
		Assert.assertNotNull(result);
		Assert.assertEquals(expected, result);
		Mockito.verify(invocationHandler, Mockito.timeout(1)).getApiURLPrefix();
		Mockito.reset(invocationHandler);
		
		try{
			// no transaction and an exception
			Mockito.when(invocationHandler.invoke(Mockito.anyObject(), (Method) Mockito.anyObject(), (Object[]) Mockito.anyObject())).thenThrow(RuntimeException.class);
			Mockito.when(transactionManager.getTransactionObject()).thenReturn(null);
			handler = new TransactionalRestTemplateInvocationHandler(invocationHandler, transactionManager);
			result = handler.invoke(new Object(), method, args);
			Assert.fail();
		} catch (RuntimeException e){
			Assert.assertNotNull(e);
			Mockito.verify(invocationHandler, Mockito.times(1)).getApiURLPrefix();
		}
	}
}
