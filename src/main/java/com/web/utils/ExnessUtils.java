package com.web.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.web.service.CommissionService;
import com.web.service.ExnessService;
import com.web.service.UserService;

@RestController
public class ExnessUtils {
	@Autowired
	ExnessService exService;
	@Autowired
	UserService userSerivce;
	@Autowired
	CommissionService commissService;

	public void getIB() throws JsonMappingException, JsonProcessingException {
		
	}
}
