package com.diaimm.astronaut.configurer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

public class AsyncAPIResponseTest {
	@Test
	public void testByPassCalls() throws InterruptedException, ExecutionException, TimeoutException {
		ListenableFuture<ResponseEntity<String>> implicit = Mockito.mock(ListenableFuture.class);
		AsyncAPIResponse<String> response = new AsyncAPIResponse<String>(implicit);

		response.cancel(true);
		Mockito.verify(implicit).cancel(true);

		response.isCancelled();
		Mockito.verify(implicit).isCancelled();

		response.isDone();
		Mockito.verify(implicit).isDone();

		ResponseEntity<String> responseEntity = Mockito.mock(ResponseEntity.class);
		Mockito.when(implicit.get()).thenReturn(responseEntity);
		response.get();
		Mockito.verify(responseEntity).getBody();
		Mockito.reset(responseEntity);

		Mockito.when(implicit.get(Mockito.anyLong(), (TimeUnit) Mockito.anyObject())).thenReturn(responseEntity);
		response.get();
		Mockito.verify(responseEntity).getBody();
		Mockito.reset(responseEntity);

		response.addCallback((ListenableFutureCallback<? super ResponseEntity<String>>) Mockito.anyObject());
		Mockito.verify(implicit).addCallback((ListenableFutureCallback<? super ResponseEntity<String>>) Mockito.anyObject());

		response.addCallback((SuccessCallback<? super ResponseEntity<String>>) Mockito.anyObject(), (FailureCallback) Mockito.anyObject());
		Mockito.verify(implicit).addCallback((ListenableFutureCallback<? super ResponseEntity<String>>) Mockito.anyObject(),
			(FailureCallback) Mockito.anyObject());
	}
}
