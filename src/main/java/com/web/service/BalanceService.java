package com.web.service;

import java.util.List;

import com.web.user.Balance;

public interface BalanceService {
	List<Balance> findByAmountAndTimeAndExness(double amount, long time, String exness);
	List<Balance> findAmountOfBalanceByTime(long time);
}
