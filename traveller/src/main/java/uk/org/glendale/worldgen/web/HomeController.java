/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * MVC Controller which provides links to the basic top level web pages.
 * 
 * @author Samuel Penn
 */
@Controller
public class HomeController {
	@RequestMapping("/")
	public String homePage() {
		return "home";
	}
	
	@RequestMapping("/help")
	public String help() {
		return "help";
	}
}
