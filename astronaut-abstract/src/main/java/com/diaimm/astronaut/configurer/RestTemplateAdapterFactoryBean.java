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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.diaimm.astronaut.configurer.RestTemplateAdapterLoader.Version;
import com.diaimm.astronaut.configurer.annotations.APIMapping;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction;
import com.diaimm.astronaut.configurer.annotations.mapping.Transaction.TransactionIdFrom;
import com.diaimm.astronaut.configurer.annotations.method.Put;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionObject.TransactionCommand;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class RestTemplateAdapterFactoryBean<T> implements FactoryBean<T> {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private Environment environment;
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
		TypeHandlingRestTemplate restTemplate = applicationContext.getBean(restTemplateName, TypeHandlingRestTemplate.class);
		this.invocationHandler = new RestTemplateRepositoryInvocationHandler(restTemplate, getAPIUrl(), version.getApiPrefix(), transactionManger);
	}

	private URI getAPIUrl() throws URISyntaxException {
		String[] propertyKeySplitted = apiURIPropertyKey.split(":");
		if (propertyKeySplitted.length >= 2 && applicationContext.containsBean(propertyKeySplitted[0])) {
			Properties properties = applicationContext.getBean(propertyKeySplitted[0], Properties.class);
			if (properties.containsKey(propertyKeySplitted[1])) {
				return new URI(properties.getProperty(propertyKeySplitted[1]));
			}
		}

		if (this.environment.containsProperty(apiURIPropertyKey)) {
			return new URI(environment.getProperty(apiURIPropertyKey));
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
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
			Optional<Transaction> transaction = getTransactionAnnotaion(annotations);

			for (Annotation annotation : annotations) {
				if (!annotation.annotationType().isAnnotationPresent(APIMapping.class)) {
					continue;
				}

				Object result = null;
				RuntimeException exception = null;
				Optional<TransactionCommands> transactionCommands = makeTransactionCommands(transaction, method, args, annotation);
				try {
					result = invokeAPICall(method, annotation, args);
					result = addCallInfoForTransaction(transactionCommands, result);
					return result;
				} catch (RuntimeException e) {
					exception = e;
					throw e;
				} finally {
					endTransactionIfNeed(transactionCommands, result, exception);
				}
			}

			return null;
		}

		private Optional<TransactionCommands> makeTransactionCommands(Optional<Transaction> transaction, Method method, Object[] args,
			Annotation annotation) {
			Optional<TransactionCommands> transactionCommands = Optional.absent();
			if (transaction.isPresent()) {
				transactionCommands = Optional.of(new TransactionCommands(this, transaction.get(), method, annotation, args));
			}
			return transactionCommands;
		}

		private Object addCallInfoForTransaction(Optional<TransactionCommands> commands, final Object apiCallResult) {
			if (!commands.isPresent()) {
				return apiCallResult;
			}

			RestTemplateTransactionObject transactionObject = this.transactionManger.getTransactionObject();
			if (transactionObject == null) {
				return apiCallResult;
			}

			transactionObject.pushToCallStack(commands.get().commit(apiCallResult), commands.get().rollback(apiCallResult));
			return apiCallResult;
		}

		private void endTransactionIfNeed(Optional<TransactionCommands> transactionCommands, Object result, RuntimeException exception) {
			if (!transactionCommands.isPresent()) {
				return;
			}

			RestTemplateTransactionObject transactionObject = this.transactionManger.getTransactionObject();
			// if transactionObject is not null, commits or rollbacks will happen in transaction manager.
			if (transactionObject != null) {
				return;
			}

			handleSingleTransaction(transactionCommands, result, exception);
		}

		private void handleSingleTransaction(Optional<TransactionCommands> transactionCommands, Object result, RuntimeException exception) {
			if (exception != null) {
				try {
					transactionCommands.get().rollback(result).execute();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				return;
			}

			try {
				transactionCommands.get().commit(result).execute();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		private Optional<Transaction> getTransactionAnnotaion(Annotation[] annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof Transaction) {
					return Optional.of((Transaction) annotation);
				}
			}

			return Optional.absent();
		}

		private Object invokeAPICall(Method method, Annotation annotation, Object[] args) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			APIMapping apiMapping = annotationType.getAnnotation(APIMapping.class);

			try {
				RestTemplateInvoker invokerInstance = getInvokerInstance(annotationType, apiMapping);
				String apiURL = getAPIUrl(invokerInstance, annotation, method, args);
				return invokerInstance.invoke(restTemplate, apiURL, method, annotation, args);
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
					return value.getMillis();
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

	private static class TransactionCommands {
		private final RestTemplateRepositoryInvocationHandler invocationHandler;
		private final Transaction transaction;
		private final Method method;
		private final Annotation annotation;
		private final Object[] args;

		TransactionCommands(RestTemplateRepositoryInvocationHandler invocationHandler, Transaction transaction, Method method,
			Annotation annotation, Object[] args) {
			this.invocationHandler = invocationHandler;
			this.transaction = transaction;
			this.method = method;
			this.annotation = annotation;
			this.args = args;
		}

		TransactionCommand commit(final Object apiCallResult) {
			return new TransactionCommand() {
				@Override
				public void execute() {
					callCommitApi(transaction, method, annotation, args, apiCallResult);
				}
			};
		}

		TransactionCommand rollback(final Object apiCallResult) {
			return new TransactionCommand() {
				@Override
				public void execute() {
					callRollbackApi(transaction, method, annotation, args, apiCallResult);
				}
			};
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
			String apiFullURL = invocationHandler.apiURLPrefix + apiURI;
			restTemplateInvoker.doInvoke(invocationHandler.restTemplate, apiFullURL, extractTransactionInfo);
			RestTemplateRepositoryInvocationHandler.log.debug("finishing transaction... with {}", apiFullURL);
		}
	}
}
