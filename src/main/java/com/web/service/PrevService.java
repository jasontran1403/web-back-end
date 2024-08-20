package com.web.service;

import com.web.dto.PrevRequest;
import com.web.dto.PreviousMonthResponse;
import com.web.user.Prev;

public interface PrevService {
	PreviousMonthResponse findPrevByEmail(String email);
	void updatePrev(PrevRequest request);
	Prev findByExnessId(String exnessid);
	Prev initPrev(String email);
	void updatePrevData(String exnessId, double balance, double profit, double deposit, double withdraw);
}
