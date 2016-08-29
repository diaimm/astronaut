package com.diaimm.astronaut.scanner.samples.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mapping3")
public class ControllerSample3 {
	@RequestMapping("path1/{pathVariable1}/{pathVariable2}")
	public void method1(@PathVariable String pathVariable1, @PathVariable String pathVariable2) {
	}
}
