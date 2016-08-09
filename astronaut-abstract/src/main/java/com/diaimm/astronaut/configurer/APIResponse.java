package com.diaimm.astronaut.configurer;

import java.util.Arrays;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker.APICallInfoCompactizer;

/**
 * <pre>
 * APIResponse<?>가 return type인 method는 exception을 throw 하지 않습니다.
 * 
 * api call에 대한 성공 여부 판단은 isSuccess() 메소드로 확인하실 수 있고, 
 * 1. 실패시의 exception내용을 받고 싶으면 getRestApiClientException()를 호출하시거나 getMessage() 등을 확인하실 수 있습니다.
 * 2. 발생한 exception을 그대로 위로 throw 하시려면 #orThrow()를
 * 3. exception시 default 값을 리턴하고 싶으시면 #or({value})를 호출 하실 수 있습니다.
 * </pre>
 * 
 * @author diaimm
 *
 * @param <T>
 */
public class APIResponse<T> {
	private int code;
	private String message;
	private T contents;
	private RestAPIException restAPIException;
	private String apiUrl;
	private Object[] args;
	private APICallInfoCompactizer<?> compactizer;

	public APICallInfoCompactizer<?> getCompactizer() {
		return this.compactizer;
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	public T getContents() {
		return this.contents;
	}

	public RestAPIException getRestApiClientException() {
		return this.restAPIException;
	}

	public String getApiUrl() {
		return this.apiUrl;
	}

	public Object[] getArgs() {
		return Arrays.copyOf(args, args.length);
	}

	protected APIResponse() {
	}

	public static <S> APIResponse<S> getInstance(Exception e, APICallInfoCompactizer<?> compactizer) {
		return getInstance("", new Object[0], e, compactizer);
	}

	public static <S> APIResponse<S> getInstance(String apiUrl, Object[] args, Exception e, APICallInfoCompactizer<?> compactizer) {
		if (e instanceof RestAPIException) {
			return getInstance(apiUrl, args, (RestAPIException) e, compactizer);
		}

		return getInstance(apiUrl, args, new RestAPIException(503, e.getMessage()), compactizer);
	}

	public static <S> APIResponse<S> getInstance(String apiUrl, Object[] args, S contents, APICallInfoCompactizer<?> compactizer) {
		APIResponse<S> result = getInstance(apiUrl, args, 200, "SUCCESS", compactizer);
		result.contents = contents;
		return result;
	}

	private static <S> APIResponse<S> getInstance(String apiUrl, Object[] args, RestAPIException exception, APICallInfoCompactizer<?> compactizer) {
		APIResponse<S> result = getInstance(apiUrl, args, exception.getStatus(), exception.getMessage(), compactizer);
		result.restAPIException = exception;
		return result;
	}

	private static <S> APIResponse<S> getInstance(String apiUrl, Object[] args, int code, String messgae, APICallInfoCompactizer<?> compactizer) {
		APIResponse<S> result = new APIResponse<S>();
		result.code = code;
		result.message = messgae;
		result.apiUrl = apiUrl;
		if (args != null) {
			result.args = Arrays.copyOf(args, args.length);
		}
		result.compactizer = compactizer;
		return result;
	}

	public T or(T defaultValue) {
		if (this.contents == null) {
			return defaultValue;
		}
		return this.contents;
	}

	public T orThrow() {
		if (this.restAPIException != null) {
			throw this.restAPIException;
		}

		return this.contents;
	}

	public boolean isSuccess() {
		return restAPIException == null;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public static class RestAPIException extends IllegalStateException {
		private int status;
		private String message;

		public RestAPIException(int status, String message) {
			this.status = status;
			this.message = message;
		}

		public int getStatus() {
			return this.status;
		}

		public String getMessage() {
			return this.message;
		}
	}
}
