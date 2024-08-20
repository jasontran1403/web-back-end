package com.web.service;

import java.util.List;

import com.web.dto.MessageRequest;
import com.web.user.Message;

public interface MessageService {
	Message saveMessage(MessageRequest message);
	List<Message> findMessagesByEmail(String email);
	void toggleMessageStatus(long id);
}
