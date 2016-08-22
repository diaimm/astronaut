package com.diaimm.astronaut.configurer.factorybean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.diaimm.astronaut.configurer.RestTemplateAdapterLoader.Version;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;

public class RestTemplateAdapterFactoryBean<T> implements FactoryBean<T> {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private Environment environment;
	private InvocationHandler invocationHandler;
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
		this.invocationHandler = new TransactionalRestTemplateInvocationHandler(restTemplate, getAPIUrl(), version.getApiPrefix(), transactionManger);
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
}
