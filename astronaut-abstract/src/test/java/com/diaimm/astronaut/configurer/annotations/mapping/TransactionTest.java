package com.diaimm.astronaut.configurer.annotations.mapping;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.diaimm.astronaut.configurer.annotations.mapping.Transaction.TransactionIdFrom;

public class TransactionTest {
	@Test
	public void RESPONSETest() throws NoSuchMethodException, SecurityException {
		TransactionIdFrom response = Transaction.TransactionIdFrom.RESPONSE;
		Assert.assertEquals("result", response.extractTransactionInfo(null, null, null, "result"));

		Assert.assertNull(response.extractTransactionInfo("sampleValue", null, null, null));
		Assert.assertNull(response.extractTransactionInfo("unknownField", null, null, "result"));

		Method method = SampleClass.class.getDeclaredMethod("sampleMethod");
		SampleClass sampleClass = new SampleClass();
		sampleClass.sampleValue = "result";
		Assert.assertEquals("result", response.extractTransactionInfo("sampleValue", method, null, sampleClass));
	}

	@Test
	public void REQUESTTest() throws NoSuchMethodException, SecurityException {
		TransactionIdFrom request = Transaction.TransactionIdFrom.REQUEST;
		SampleClass sampleClass = new SampleClass();
		sampleClass.sampleValue = "result";
		Assert.assertEquals("transactionId",
			request.extractTransactionInfo(null, SampleClass.class.getDeclaredMethod("sampleMethod2", SampleClass.class),
				new Object[] { "transactionId" }, sampleClass));

		Assert.assertNull(
			request.extractTransactionInfo("unknownField", SampleClass.class.getDeclaredMethod("sampleMethod2", SampleClass.class),
				new Object[] { sampleClass }, sampleClass));

		Assert.assertEquals("result",
			request.extractTransactionInfo("sampleValue", SampleClass.class.getDeclaredMethod("sampleMethod2", SampleClass.class),
				new Object[] { sampleClass }, sampleClass));

	}

	private static class SampleClass {
		private String sampleValue;

		public void sampleMethod() {

		}

		public void sampleMethod2(@TransactionId SampleClass requestBody) {

		}
	}
}
