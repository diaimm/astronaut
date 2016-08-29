package com.diaimm.astronaut.scanner.samples.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mapping2")
public class ControllerSample2 {
	@RequestMapping("path1/{pathVariable1}/{pathVariable2}")
	public void method1(@PathVariable String pathVariable1, @PathVariable String pathVariable2) {
	}
}
