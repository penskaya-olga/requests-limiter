package app.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import app.test.annotation.CheckRequest;

@RestController
public class TestController {
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseStatus(value=HttpStatus.OK)
	@CheckRequest
	public String get() {		
		return "";
	}

}
