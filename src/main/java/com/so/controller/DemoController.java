package com.so.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "web/demo")
public class DemoController {

	@RequestMapping(value = {"/login"}, method = RequestMethod.GET)
	public String login(Model model) {
		return "demo/login";
	}
	
	@RequestMapping(value = {"/main"}, method = RequestMethod.GET)
	public String main(Model model) {
		return "demo/main";
	}
}
