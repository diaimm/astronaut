package com.diaimm.astronaut.configurer.normalizer;

import org.apache.commons.lang.StringUtils;

import com.diaimm.astronaut.configurer.APIArgumentNormalizer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class IterableStringsToStringNormalizer implements APIArgumentNormalizer<Object> {
	private final String separator;

	protected IterableStringsToStringNormalizer(String separator) {
		this.separator = separator;
	}

	@Override
	public Object normalize(Object value) {
		if (Iterable.class.isAssignableFrom(value.getClass()) || value.getClass().isArray()) {
			return Joiner.on(this.separator).skipNulls().join(getTrimmedIterable(value));
		}
		return value.toString();
	}

	private Iterable<String> getTrimmedIterable(Object value) {
		if (value.getClass().isArray()) {
			return getTrimmedIterable(Lists.newArrayList((Object[]) value));
		}
		return Iterables.transform((Iterable<?>) value, new Function<Object, String>() {
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
		});
	}
}
