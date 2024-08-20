package com.web.service;

import java.util.List;

import com.web.user.History;

public interface HistoryService {
	List<History> findHistoryByReceiver(String receiver);
	List<History> findHistoryByReceiverAndTimeRange(String receiver, long from, long to);
	History saveHistory(History history);
	double calculateAllIB(String email);
	List<History> findHistoryByEmail(String email);
}
