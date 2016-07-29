package com.diaimm.astronaut.configurer;

public interface APIArgumentNormalizer<T> {
	Object normalize(T value);
}
