package com.web.service;

import java.util.List;

import com.web.user.AdminPixiu;
import com.web.user.Commission;
import com.web.user.ManagerPixiu;

public interface CommissionService {
	Commission saveCommission(Commission commission); 
	double getTotalCommission(String branchName);
	List<Commission> getAllCommissionByBranchName(String branchName);
	List<AdminPixiu> getAllCommissionByBranchNameAdmin(String branchName, String rootEmail);
	List<ManagerPixiu> getAllCommissionByBranchNameManager(String branchName);
	void deleteAllCommission();
}
