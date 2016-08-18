package com.diaimm.astronaut.configurer.normalizer;

public class CommanJoningNormalizer extends IterableStringsJoiningNormalizer {
	private static final String SEPARATOR = ",";
	@Override
	String getSeparator() {
		return SEPARATOR;
	}
}
