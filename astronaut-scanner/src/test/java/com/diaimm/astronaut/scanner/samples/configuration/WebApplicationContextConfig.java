package com.diaimm.astronaut.scanner.samples.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.diaimm.astronaut.scanner.samples.controllers.SampleControllerBaseClass;

@Configuration
@ComponentScan(basePackageClasses = { SampleControllerBaseClass.class })
public class WebApplicationContextConfig {
	
}
