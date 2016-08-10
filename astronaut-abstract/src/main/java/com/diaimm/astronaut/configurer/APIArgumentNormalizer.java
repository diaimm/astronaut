package com.diaimm.astronaut.configurer;

/**
 * Normalize the values of Arguments for API
 * 
 * @author diaimm
 *
 * @param <T>
 */
public interface APIArgumentNormalizer<T> {
	Object normalize(T value);
}
