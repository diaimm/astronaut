package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.diaimm.astronaut.configurer.RestTemplateAdapterLoader.Version;
import com.diaimm.astronaut.configurer.annotations.mapping.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction.TransactionIdFrom;
import com.diaimm.astronaut.configurer.annotations.method.Put;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;
import com.google.common.collect.Maps;

public class RestTemplateAdapterFactoryBean<T> implements FactoryBean<T> {
	@Autowired
	private ApplicationContext applicationContext;
	private RestTemplateRepositoryInvocationHandler invocationHandler;
	private final String apiURIPropertyKey;
	private final Version version;
	private final Class<T> target;
	private final String restTemplateName;
	private RestTemplateTransactionManager transactionManger;

	public RestTemplateAdapterFactoryBean(Version version, String apiURIPropertyKey, String restTemplateName, Class<T> target,
		RestTemplateTransactionManager transactionManger) {
		this.version = version;
		this.apiURIPropertyKey = apiURIPropertyKey;
		this.restTemplateName = restTemplateName;
		this.target = target;
		this.transactionManger = transactionManger;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(applicationContext.getClassLoader(), new Class<?>[] { target }, invocationHandler);
	}

	@Override
	public Class<T> getObjectType() {
		return target;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@PostConstruct
	private void init() throws URISyntaxException {
		String[] propertyKeySplitted = apiURIPropertyKey.split(":");
		Properties properties = applicationContext.getBean(propertyKeySplitted[0], Properties.class);
		URI apiURI = getApiURI(properties, propertyKeySplitted[1]);

		TypeHandlingRestTemplate restTemplate = applicationContext.getBean(restTemplateName, TypeHandlingRestTemplate.class);
		this.invocationHandler = new RestTemplateRepositoryInvocationHandler(restTemplate, apiURI, version.getApiPrefix(), transactionManger);
	}

	private URI getApiURI(Properties properties, String key) throws URISyntaxException {
		if (properties.containsKey(key)) {
			return new URI(properties.getProperty(key));
		}

		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static class RestTemplateRepositoryInvocationHandler implements InvocationHandler {
		private static Logger log = LoggerFactory.getLogger(RestTemplateRepositoryInvocationHandler.class);
		private final Map<Class<? extends Annotation>, RestTemplateInvoker<?>> restTemplateInvokerCache = Maps.newConcurrentMap();
		private Object cacheLock = new Object();
		private TypeHandlingRestTemplate restTemplate;
		private String apiURLPrefix;
		private RestTemplateTransactionManager transactionManger;
		private static ObjectMapper mapper = new ObjectMapper();
		{
			mapper.disable(Feature.FAIL_ON_UNKNOWN_PROPERTIES);
		}

		private RestTemplateRepositoryInvocationHandler(TypeHandlingRestTemplate restTemplate, URI apiURI, String pathPrefix,
			RestTemplateTransactionManager transactionManger) {
			this.apiURLPrefix = apiURI + pathPrefix;
			this.restTemplate = restTemplate;
			this.transactionManger = transactionManger;
		}

		@Override
		public Object invoke(Object instance, Method method, Object[] args) {
			Annotation[] annotations = method.getAnnotations();
			Transaction transaction = getTransactionAnnotaion(annotations);

			for (Annotation annotation : annotations) {
				if (annotation.annotationType().isAnnotationPresent(APIMapping.class)) {
					Object result = null;
					RuntimeException exception = null;
					try {
						result = addCallInfoForTransaction(transaction, method, annotation, args, invokeAPICall(method, annotation, args));

						return result;
					} catch (RuntimeException e) {
						exception = e;
						throw e;
					} finally {
						endTransactionIfNeed(transaction, method, annotation, args, result, exception);
					}
				}
			}

			return null;
		}

		private Object addCallInfoForTransaction(final Transaction transaction, final Method method, final Annotation annotation,
			final Object[] args, final Object result) {
			if (transaction == null) {
				return result;
			}

			RestTemplateTransactionObject transactionObject = this.transactionManger.getTransactionObject();
			if (transactionObject == null) {
				return result;
			}

			transactionObject.pushToCallStack(new TransactionCommand() {
				@Override
				public void execute() {
					callCommitApi(transaction, method, annotation, args, result);
				}
			}, new TransactionCommand() {
				@Override
				public void execute() {
					callRollbackApi(transaction, method, annotation, args, result);
				}
			});

			return result;
		}

		private void endTransactionIfNeed(Transaction transaction, Method method, Annotation annotation, Object[] args, Object result,
			RuntimeException exception) {
			if (transaction == null) {
				return;
			}

			RestTemplateTransactionObject transactionObject = this.transactionManger.getTransactionObject();
			if (transactionObject != null) {
				return;
			}

			// @Transactional 없이 실행된 상황
			if (exception != null) {
				try {
					callRollbackApi(transaction, method, annotation, args, result);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				return;
			}

			try {
				callCommitApi(transaction, method, annotation, args, result);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		private void callCommitApi(final Transaction transaction, final Method method,
			final Annotation annotation, final Object[] args, Object result) {
			callTransactionFinishAPI(transaction, method, args, result, transaction.commit());
		}

		private void callRollbackApi(final Transaction transaction, final Method method, final Annotation annotation, final Object[] args,
			Object result) {
			callTransactionFinishAPI(transaction, method, args, result, transaction.rollback());
		}

		private void callTransactionFinishAPI(final Transaction transaction, final Method method, final Object[] args, Object result, String apiURI) {
			TransactionIdFrom transactionIdFrom = transaction.transactionIdFrom();
			Object extractTransactionInfo = transactionIdFrom.extractTransactionInfo(transaction.transactionId(), method, args, result);
			Put.RestTemplateInvoker restTemplateInvoker = new Put.RestTemplateInvoker();

			String apiFullURL = apiURLPrefix + apiURI;
			restTemplateInvoker.doInvoke(restTemplate, apiFullURL, extractTransactionInfo);
			log.debug("finishing transaction... with {}", apiFullURL);
		}

		private Transaction getTransactionAnnotaion(Annotation[] annotations) {
			Transaction transaction = null;
			for (Annotation annotation : annotations) {
				if (annotation instanceof Transaction) {
					transaction = (Transaction) annotation;
				}
			}
			return transaction;
		}

		private Object invokeAPICall(Method method, Annotation annotation, Object[] args) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			APIMapping apiMapping = annotationType.getAnnotation(APIMapping.class);

			try {
				RestTemplateInvoker<?> invokerInstance = getInvokerInstance(annotationType, apiMapping);
				Object result = getInvokerInstance(annotationType, apiMapping).invoke(restTemplate,
					getAPIUrl(invokerInstance, annotation, method, args),
					method, annotation, args);
				return result;
			} catch (Exception e) {
				log.debug(e.getMessage(), e);
				throw new IllegalStateException(e);
			}
		}

		private String getAPIUrl(RestTemplateInvoker invokerInstance, Annotation annotation, Method method, Object[] args) {
			return apiURLPrefix + invokerInstance.extractAPIUrl(annotation, method, args);
		}

		private RestTemplateInvoker getInvokerInstance(Class<? extends Annotation> annotationType, APIMapping apiMapping)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			synchronized (cacheLock) {
				if (!restTemplateInvokerCache.containsKey(annotationType)) {
					Class<? extends RestTemplateInvoker> handler = apiMapping.handler();
					Constructor<? extends RestTemplateInvoker> declaredConstructor = handler.getDeclaredConstructor();
					declaredConstructor.setAccessible(true);

					restTemplateInvokerCache.put(annotationType, addArgumentNormalizers(declaredConstructor.newInstance()));
				}

				return restTemplateInvokerCache.get(annotationType);
			}
		}

		private RestTemplateInvoker addArgumentNormalizers(RestTemplateInvoker newInstance) {
			newInstance.addAPIArgumentNormalizer(DateTime.class, new APIArgumentNormalizer<DateTime>() {
				@Override
				public Object normalize(DateTime value) {
					return value.toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
				}
			});

			newInstance.addAPIArgumentNormalizer(Collection.class, new APIArgumentNormalizer<Collection>() {
				@Override
				public Object normalize(Collection value) {
					try {
						return mapper.writeValueAsString(value);
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
				}
			});

			newInstance.addAPIArgumentNormalizer(Map.class, new APIArgumentNormalizer<Map>() {
				@Override
				public Object normalize(Map value) {
					try {
						return mapper.writeValueAsString(value);
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
				}
			});

			return newInstance;
		}
	}
}
