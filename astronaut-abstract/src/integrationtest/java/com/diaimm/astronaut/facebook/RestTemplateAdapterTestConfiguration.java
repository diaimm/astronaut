package com.diaimm.astronaut.facebook;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;

import com.diaimm.astronaut.configurer.DefaultTypeHandlingRestTemplateImpl;
import com.diaimm.astronaut.configurer.RestTemplateAdapterLoader;
import com.diaimm.astronaut.configurer.RestTemplateAdapterLoader.Version;
import com.diaimm.astronaut.configurer.TypeHandlingRestTemplate;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.diaimm.astronaut.facebook.repositories.FaceBookMeBase;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

@Configuration
@PropertySource({ "classpath:properties/project-facebook.xml" })
public class RestTemplateAdapterTestConfiguration {

	/**
	 * https://graph.facebook.com/v2.7/me?fields=id%2Cname%2Cinterested_in&access_token=EAACEdEose0cBALbJZAvvW6IqaYrmzXJVzHALvOc9KKYMNsAEwIlDZBTNWYgFr95NaqVB7VY9OqR1AIM5rtH3dSIITm3dZCRgEznpaKz9ZCGfmfRx6sgy7IzRbqj6sdsM41i6ZA0raXBYH7AWFHukqdZCUeovUZAPmzw7GUlsWgsOF7lL5vTyKI6"
	 * @param restTemplateTransactionManager
	 * @return
	 */
	@Bean
	public static RestTemplateAdapterLoader restTemplateAdapterLoader(RestTemplateTransactionManager restTemplateTransactionManager) {
		return new RestTemplateAdapterLoader(new Version() {
			@Override
			public Version latest() {
				return this;
			}

			@Override
			public String getApiPrefix() {
				return "/v2.7";
			}
		}, "apiURIPropertyKey", "facebook2.7", "sampleRestTemplate", restTemplateTransactionManager, FaceBookMeBase.class);
	}

	@Bean
	public static RestTemplateTransactionManager restTemplateTransactionManager() {
		return new RestTemplateTransactionManager();
	}

	@Bean
	public TypeHandlingRestTemplate sampleRestTemplate() {
		List<Header> defaultHeaders = Lists.newArrayList();
		defaultHeaders.add(new BasicHeader("user-agent",
			"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36"));
		defaultHeaders.add(new BasicHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));

		DefaultTypeHandlingRestTemplateImpl result = new DefaultTypeHandlingRestTemplateImpl(300, 5000, 100, 10, defaultHeaders);
		result.setMessageConverters(httpMessageConverters());

		return result;
	}

	public List<HttpMessageConverter<?>> httpMessageConverters() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new StringHttpMessageConverter());
		converters.add(new Jaxb2RootElementHttpMessageConverter());
		converters.add(jsonHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		return converters;
	}

	public AbstractHttpMessageConverter jsonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = jacksonConverter.getObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return jacksonConverter;
	}
}
