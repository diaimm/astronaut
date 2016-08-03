package com.diaimm.astronaut.configurer.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;

import com.diaimm.astronaut.configurer.AbstractRestTemplateInvoker;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.RequestURI;
import com.google.common.base.Supplier;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@APIMapping(handler = GetForObject.RestTemplateInvoker.class)
public @interface GetForObject {
	@RequestURI
	String url() default "";

	Class<? extends Supplier<?>> dummySupplier();

	class RestTemplateInvoker extends AbstractRestTemplateInvoker<GetForObject> {
		@Override
		protected Object doInvoke(TypeHandlingRestTemplate restTemplate, APICallInfoCompactizer<GetForObject> compactizer, Type returnType,
			GetForObject annotation)
			throws Exception {
			return restTemplate.getForObject(compactizer.getApiUrl(), returnType, compactizer.getArguments());
		}

		private Object getDummyValue(Type returnType, GetForObject annotation) throws IllegalAccessException, InstantiationException {
			Class<?> rawClass = toRawClass(returnType);
			return rawClass.cast(annotation.dummySupplier().newInstance().get());
		}

		private Class<?> toRawClass(Type returnType) {
			if (returnType instanceof ParameterizedType) {
				return (Class<?>) ((ParameterizedType) returnType).getRawType();
			}
			return (Class<?>) returnType;
		}
	}

	class DummyBooleanSupplier implements Supplier<Boolean> {
		@Override
		public Boolean get() {
			return false;
		}
	}

	class DummyLongSupplier implements Supplier<Long> {
		@Override
		public Long get() {
			return 0L;
		}
	}

	class DummyObjectSupplier implements Supplier<Object> {
		public static final Object DUMMY = new Object();

		@Override
		public Object get() {
			return DUMMY;
		}
	}

	class DummyMapSupplier implements Supplier<Map<?, ?>> {
		@Override
		public Map<?, ?> get() {
			return MapUtils.EMPTY_MAP;
		}
	}

	class DummySetSupplier implements Supplier<Set<?>> {
		@Override
		public Set<?> get() {
			return SetUtils.EMPTY_SET;
		}
	}

	class DummyListSupplier implements Supplier<List<?>> {
		@Override
		public List<?> get() {
			return ListUtils.EMPTY_LIST;
		}
	}

	class DummyCollectionSupplier implements Supplier<Collection<?>> {
		@Override
		public Collection<?> get() {
			return CollectionUtils.EMPTY_COLLECTION;
		}
	}

	class DummyPageResponseSupplier implements Supplier<PageResponse<?>> {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private static final PageResponse<?> DUMMY = new PageResponse(ListUtils.EMPTY_LIST, 0, 0, 0);

		@Override
		public PageResponse<?> get() {
			return DUMMY;
		}
	}

	/**
	 * @author diaimm
	 *
	 * @param <T>
	 */
	public static class PageResponse<T> {
		private List<T> content;
		private long page;
		private long size;
		private long total;

		public PageResponse() {
		}

		public PageResponse(List<T> content, long page, long size, long total) {
			this.content = content;
			this.page = page;
			this.size = size;
			this.total = total;
		}

		public int getTotalPages() {
			return size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
		}

		public boolean hasNextPage() {
			return page + 1 < getTotalPages();
		}

		public long getPage() {
			return this.page;
		}

		public void setPage(long page) {
			this.page = page;
		}

		public long getSize() {
			return this.size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public long getTotal() {
			return this.total;
		}

		public void setTotal(long total) {
			this.total = total;
		}

		public List<T> getContent() {
			return this.content;
		}

		public void setContent(List<T> content) {
			this.content = content;
		}
	}
}
