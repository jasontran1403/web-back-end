package com.web.service;

import java.util.List;
import java.util.Optional;

import com.web.dto.PreviousMonthResponse;
import com.web.user.Exness;
import com.web.user.User;

public interface ExnessService {
	User findUserByExness(String exnessId);
	Optional<Exness> findByExnessId(String exnessId);
	List<Exness> findAllExness();
	List<Exness> getListExnessByBranchName(String branchName);
	PreviousMonthResponse findByEmail(String email);
	PreviousMonthResponse findByExness(String exness);
	void updateTotalProfit(String exnessId, double amount);
	void fixTotalProfit(String exnessId, double amount);
	double getBalanceByEmail(String email);
	double getProfitByEmail(String email);
	List<Exness> findAllByBranchName(String branchName);
	List<Exness> findListExnessByRootUser(User rootUser);
}
