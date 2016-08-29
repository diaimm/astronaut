package com.diaimm.astronaut.scanner;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import com.diaimm.astronaut.scanner.samples.configuration.WebApplicationContextConfig;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, CassandraDataAutoConfiguration.class,
	FreeMarkerAutoConfiguration.class, XADataSourceAutoConfiguration.class })
@Import(WebApplicationContextConfig.class)
public class SpringBootApplication {
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(SpringBootApplication.class, args);
		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}
	}
}
