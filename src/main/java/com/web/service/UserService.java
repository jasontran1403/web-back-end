package com.web.service;

import java.util.List;

import com.web.dto.AdminPixiuResponse;
import com.web.dto.InfoLisaResponse;
import com.web.dto.InfoResponse;
import com.web.dto.ManagerPixiuResponse;
import com.web.user.Commission;
import com.web.user.Exness;
import com.web.user.Transaction;
import com.web.user.User;

public interface UserService {
	InfoResponse getInfoByExnessId(String exnessId, long from, long to);
	InfoResponse getInfoFromTimeRangeByExnessId(String exnessId, long from, long to);
	AdminPixiuResponse getInfoFromTimeRangeByExnessIdByAdmin(String exnessId, long from, long to);
	ManagerPixiuResponse getInfoFromTimeRangeByExnessIdByManager(String exnessId, long from, long to);
	InfoResponse getAllInfoByEmail(String email, long from, long to);
	InfoLisaResponse getAllInfoByEmailLisa(String email, long from, long to);
	InfoLisaResponse getInfoByExnessLisa(String exnessId, long from, long to);
	boolean saveProfit(String exnessId, double amount, long time);
	Commission saveCommission(String exnessId, double amount, long time);
	boolean saveBalance(String exnessId, double amount, long time);
	Transaction saveTransaction(String exnessId, double amount, long time);
	Exness updateBalanceExness(String exness, double amount);
	void updateTotalProfit(String exnessId, double amount);
	void updateTotalCommission(User user, double amount);
	List<User> getUsersByBranchName(String branchName);
	List<User> findByRefferal(String refferalEmail);
}
