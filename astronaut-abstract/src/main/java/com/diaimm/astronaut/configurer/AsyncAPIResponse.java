package com.diaimm.astronaut.configurer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

/**
 * <pre>
 * When the return type of a method is APIResponse<?>, it doesn't throw any exceptions. 
 * 
 * You can check if the call has been successed with {@link #isSuccess()}, and 
 * 1. if it has failed, you can get the exception information through {@link #getRestApiClientException()} or get the message through {@link #getMessage()}.
 * 2. if you just throw the same exception occurred inside the module just call {@link #orThrow()}.
 * 3. if you have a default value for the failures the use {@link #or(defaultValue)}
 * </pre>
 * 
 * @author diaimm
 *
 * @param <T>
 */
public class AsyncAPIResponse<T> {
	private ListenableFuture<ResponseEntity<T>> implict;

	public AsyncAPIResponse(ListenableFuture<ResponseEntity<T>> implict){
		this.implict = implict;
	}
	
	public boolean cancel(boolean mayInterruptIfRunning) {
		return implict.cancel(mayInterruptIfRunning);
	}

	public boolean isCancelled() {
		return implict.isCancelled();
	}

	public boolean isDone() {
		return implict.isDone();
	}

	public T get() throws InterruptedException, ExecutionException {
		return implict.get().getBody();
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return implict.get(timeout, unit).getBody();
	}

	public void addCallback(ListenableFutureCallback<? super ResponseEntity<T>> callback) {
		implict.addCallback(callback);
	}

	public void addCallback(SuccessCallback<? super ResponseEntity<T>> successCallback, FailureCallback failureCallback) {
		implict.addCallback(successCallback, failureCallback);
	}
}
