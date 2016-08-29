package com.diaimm.astronaut.scanner.samples.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerSample1 {
	@RequestMapping("/mapping1/path1/{pathVariable1}/{pathVariable2}")
	public void method1(@PathVariable String pathVariable1, @PathVariable String pathVariable2) {
	}
}
