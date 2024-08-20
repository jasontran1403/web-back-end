package com.web.service;

import java.util.List;

import com.web.user.Transaction;

public interface TransactionService {
	List<Transaction> findTransactionByEmail(String email);
	Transaction saveTransaction(Transaction transaction);
	List<Transaction> findByAmountAndTimeAndExness(double amount, long time, String exness);
	double getTotalDepositByExnessId(String exnessId);
	double getTotalDepositFromPixiu(long timestamp);
}
