package com.diaimm.astronaut.configurer;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;

import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.transaction.RestTemplateTransactionManager;
import com.google.common.collect.Sets;

public class RestTemplateAdapterLoader implements BeanFactoryPostProcessor {
	private final String apiURIPropertyKey;
	private final Version version;
	private final String restTemplateBeanName;
	private final String name;
	private final Class<?> baseClassToScan;
	private final RestTemplateTransactionManager transactionManger;

	public interface Version {
		Version latest();

		String getApiPrefix();
	}

	public RestTemplateAdapterLoader(Version version, String apiURIPropertyKey, String name, String restTemplateBeanName,
		RestTemplateTransactionManager transactionManger, Class<?> baseClassToScan) {
		this.apiURIPropertyKey = apiURIPropertyKey;
		this.name = name;
		this.restTemplateBeanName = restTemplateBeanName;
		this.baseClassToScan = baseClassToScan;
		this.version = version;
		this.transactionManger = transactionManger;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		final DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(defaultListableBeanFactory) {
			@Override
			public Set<BeanDefinition> findCandidateComponents(String basePackage) {
				String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage) + "/**/*.class";
				try {
					Resource[] resources = ((ResourcePatternResolver) this.getResourceLoader()).getResources(packageSearchPath);
					for (Resource resource : resources) {
						if (resource.isReadable()) {
							MetadataReader metadataReader = this.getMetadataReaderFactory().getMetadataReader(resource);
							if (isCandidateComponent(metadataReader)) {
								ClassMetadata classMetadata = metadataReader.getClassMetadata();
								Class<?> currentClass = Class.forName(classMetadata.getClassName());

								BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(RestTemplateAdapterFactoryBean.class.getName());
								beanDefinitionBuilder.addConstructorArgValue(version);
								beanDefinitionBuilder.addConstructorArgValue(apiURIPropertyKey);
								beanDefinitionBuilder.addConstructorArgValue(restTemplateBeanName);
								beanDefinitionBuilder.addConstructorArgValue(currentClass);
								beanDefinitionBuilder.addConstructorArgValue(transactionManger);

								logger.debug("Identified candidate component class: " + resource);
								AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
								defaultListableBeanFactory.registerBeanDefinition(currentClass.getName(), beanDefinition);
							}
						}
					}
				} catch (Exception e) {
					throw new BeanCreationException("bean 생성 실패 ", e);
				}

				return Sets.newHashSet();
			}

			protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
				ClassMetadata classMetadata = metadataReader.getClassMetadata();
				try {
					Class<?> currentClass = Class.forName(classMetadata.getClassName());
					if (isTargetAnnotationPresent(currentClass) && currentClass.isInterface()) {
						return true;
					}
				} catch (ClassNotFoundException e) {
					// never happens
					logger.debug(e.getMessage(), e);
				}
				return false;
			}

			private boolean isTargetAnnotationPresent(Class<?> currentClass) {
				if (currentClass.isAnnotationPresent(RestAPIRepository.class)) {
					return isValueMatch(currentClass.getAnnotation(RestAPIRepository.class).value());
				}

				return false;
			}

			private boolean isValueMatch(String annotationValue) {
				String resourceName = RestTemplateAdapterLoader.this.name;
				if (StringUtils.isBlank(resourceName)) {
					return StringUtils.isBlank(annotationValue);
				}
				return resourceName.equals(annotationValue.trim());
			}
		};
		scanner.scan(baseClassToScan.getPackage().getName());
	}
}
