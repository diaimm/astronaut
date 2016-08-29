package com.diaimm.astronaut.scanner.samples.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ControllerSample4 {
	@RequestMapping("/mapping4/path1/{pathVariable1}/{pathVariable2}")
	public void method1(@PathVariable String pathVariable1, @PathVariable String pathVariable2) {
	}
}
