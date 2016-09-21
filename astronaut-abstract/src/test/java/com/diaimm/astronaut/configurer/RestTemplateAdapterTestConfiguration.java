package com.diaimm.astronaut.configurer;

import org.apache.http.Header;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.diaimm.astronaut.configurer.RestTemplateAdapterLoader.Version;
import com.diaimm.astronaut.configurer.repositoriesToScan.RepositoryToScanBase;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.google.common.collect.Lists;

@Configuration
@PropertySource({ "classpath:properties/project-astronaut.xml" })
public class RestTemplateAdapterTestConfiguration {
	@Bean
	public static RestTemplateAdapterLoader restTemplateAdapterLoader(RestTemplateTransactionManager restTemplateTransactionManager) {
		return new RestTemplateAdapterLoader(new Version() {
			@Override
			public Version latest() {
				return this;
			}

			@Override
			public String getApiPrefix() {
				return "/v1";
			}
		}, "apiURIPropertyKey", "resourceName", "sampleRestTemplate", "sampleAsyncRestTemplate", restTemplateTransactionManager,
			RepositoryToScanBase.class);
	}

	@Bean
	public static RestTemplateTransactionManager restTemplateTransactionManager() {
		return new RestTemplateTransactionManager();
	}

	@Bean
	public DefaultTypeHandlingRestTemplate sampleRestTemplate() {
		return new DefaultTypeHandlingRestTemplate(300, 100, 100, 10, Lists.<Header> newArrayList());
	}

	@Bean
	public TypeHandlingAsyncRestOperations sampleAsyncRestTemplate(DefaultTypeHandlingRestTemplate sampleRestTemplate) {
		return new DefaultTypeHandlingAsyncRestTemplate(sampleRestTemplate);
	}
}
