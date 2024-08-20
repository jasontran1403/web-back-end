package com.web.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.web.dto.Response;
import com.web.exception.NotFoundException;

@ControllerAdvice
public class NotFoundExceptionHandler {
	@ExceptionHandler
	public ResponseEntity<Response> handlerNotFoundException(NotFoundException notFoundException) {
		Response response = new Response();
		response.setMessage(notFoundException.getMessage());
		response.setStatus(HttpStatus.NOT_FOUND.value());
		response.setTimeStamp(System.currentTimeMillis());
		
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
}
