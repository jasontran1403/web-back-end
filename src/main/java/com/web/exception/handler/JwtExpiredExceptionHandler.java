package com.web.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.web.dto.Response;
import com.web.exception.JwtExpiredException;

@ControllerAdvice
public class JwtExpiredExceptionHandler {
	@ExceptionHandler
	public ResponseEntity<Response> handlerNotFoundException(JwtExpiredException jwtExpiredException) {
		Response response = new Response();
		response.setMessage(jwtExpiredException.getMessage());
		response.setStatus(HttpStatus.REQUEST_TIMEOUT.value());
		response.setTimeStamp(System.currentTimeMillis());
		
		return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
	}
}
