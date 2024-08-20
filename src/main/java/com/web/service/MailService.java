package com.web.service;

import com.web.dto.EmailDto;

import jakarta.mail.MessagingException;

public interface MailService {
	void send(EmailDto mail) throws MessagingException;
}
