package com.so.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "web/demo")
public class DemoController {
	
	@RequestMapping(value = {""}, method = RequestMethod.GET)
	public String demo(Model model) {
		return "demo/index";
	}
	
	@RequestMapping(value = {"/login"}, method = RequestMethod.GET)
	public String login(Model model) {
		model.addAttribute("jwt", "sadsadsa");
		return "demo/login";
	}

	@RequestMapping(value = {"/sigup"}, method = RequestMethod.GET)
	public String sigup(Model model) {
		return "demo/signup";
	}
	
	@RequestMapping(value = {"/main"}, method = RequestMethod.GET)
	public String main(Model model) {
		model.addAttribute("jwt", "sadsadsa");
		return "demo/main";
	}
}
