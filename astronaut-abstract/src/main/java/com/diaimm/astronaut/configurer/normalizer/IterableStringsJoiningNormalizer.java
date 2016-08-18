package com.diaimm.astronaut.configurer.normalizer;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.diaimm.astronaut.configurer.APIArgumentNormalizer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public abstract class IterableStringsJoiningNormalizer implements APIArgumentNormalizer<Object> {
	@Override
	public Object normalize(Object value) {
		if (value.getClass().isArray()) {
			return joinToString(getTrimmedIterable(Lists.newArrayList((Object[]) value)).iterator());
		}

		if (Iterable.class.isAssignableFrom(value.getClass()) || value.getClass().isArray()) {
			return joinToString(getTrimmedIterable(value).iterator());
		}

		if (Iterator.class.isAssignableFrom(value.getClass())) {
			return joinToString(Iterators.transform((Iterator<?>) value, toTrimmedOrNull()));
		}
		
		return value.toString();
	}

	private Object joinToString(Iterator<String> iterator) {
		return Joiner.on(this.getSeparator()).skipNulls().join(iterator);
	}

	private Iterable<String> getTrimmedIterable(Object value) {
		return FluentIterable.from((Iterable<?>) value).transform(toTrimmedOrNull());
	}

	private Function<Object, String> toTrimmedOrNull() {
		return new Function<Object, String>() {
			@Override
			public String apply(Object input) {
				if (input == null) {
					return null;
				}

				String value = input.toString();
				if (StringUtils.isBlank(value)) {
					return null;
				}

				return value.trim();
			}
		};
	}

	abstract String getSeparator();
}
