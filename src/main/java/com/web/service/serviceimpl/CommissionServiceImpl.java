package com.web.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.service.CommissionService;
import com.web.user.AdminPixiu;
import com.web.user.AdminPixiuRepository;
import com.web.user.Commission;
import com.web.user.CommissionRepository;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.ManagerPixiu;
import com.web.user.ManagerPixiuRepository;
import com.web.user.User;
import com.web.user.UserRepository;

@Service
public class CommissionServiceImpl implements CommissionService {
	@Autowired
	CommissionRepository commissRepo;
	
	@Autowired
	AdminPixiuRepository adminRepo;
	
	@Autowired
	ManagerPixiuRepository managerRepo;

	@Autowired
	ExnessRepository exRepo;

	@Autowired
	UserRepository userRepo;

	@Override
	public Commission saveCommission(Commission commission) {
		// TODO Auto-generated method stub
		return commissRepo.save(commission);
	}

	@Override
	public double getTotalCommission(String branchName) {
		// TODO Auto-generated method stub
		return userRepo.getTotalCommissionByBranchName(branchName);
	}

	@Override
	public List<Commission> getAllCommissionByBranchName(String branchName) {
		// TODO Auto-generated method stub
		List<Commission> result = new ArrayList<>();

		List<User> users = userRepo.getUsersByBranchName(branchName);

		for (User user : users) {
			List<Exness> exnesses = user.getExnessList();

			for (Exness exness : exnesses) {
				List<Commission> resultByExness = commissRepo.getCommissionByExnessId(exness.getExness());
				for (Commission commission : resultByExness) {
					result.add(commission);
				}
			}
		}
		return result;
	}

	@Override
	public void deleteAllCommission() {
		// TODO Auto-generated method stub
		List<Commission> commissions = commissRepo.findAll();
		for (Commission item : commissions) {
			commissRepo.delete(item);
		}
		
		List<User> users = userRepo.findAll();
		for (User user : users) {
			user.setCommission(0);
			userRepo.save(user);
		}
	}

	@Override
	public List<AdminPixiu> getAllCommissionByBranchNameAdmin(String branchName, String rootEmail) {
		// TODO Auto-generated method stub
		List<AdminPixiu> result = new ArrayList<>();

		List<User> users = userRepo.getUsersByBranchName(branchName);
		List<Exness> allExnesses = new ArrayList<>();

		for (User user : users) {
			List<Exness> exnesses = user.getExnessList();
			if (exnesses != null) {
				allExnesses.addAll(exnesses);
			}
		}
		
		User rootUserSubBranch1 = userRepo.getByEmail(rootEmail);
		List<Exness> exnessSubBranch = filterForSubBranch(allExnesses, rootUserSubBranch1);
		for (Exness exness : exnessSubBranch) {
			List<AdminPixiu> resultByExness = adminRepo.getCommissionByExnessId(exness.getExness());
			for (AdminPixiu commission : resultByExness) {
				result.add(commission);
			}
		}
		return result;
	}

	@Override
	public List<ManagerPixiu> getAllCommissionByBranchNameManager(String branchName) {
		// TODO Auto-generated method stub
		List<ManagerPixiu> result = new ArrayList<>();

		List<User> users = userRepo.getUsersByBranchName(branchName);

		for (User user : users) {
			List<Exness> exnesses = user.getExnessList();

			for (Exness exness : exnesses) {
				List<ManagerPixiu> resultByExness = managerRepo.getCommissionByExnessId(exness.getExness());
				for (ManagerPixiu commission : resultByExness) {
					result.add(commission);
				}
			}
		}
		return result;
	}
	
	public List<Exness> filterForSubBranch(List<Exness> listExness, User rootUser) {
		List<Exness> exnessFiltered = new ArrayList<>();
		
		User userLevel1 = userRepo.findByRefferal(rootUser.getEmail()).get(0);
		findSubBranchExness(userLevel1, exnessFiltered);
		return exnessFiltered;
	}

	private void findSubBranchExness(User user, List<Exness> exnessFiltered) {
	    if (user != null) {
	        // Kiểm tra xem user có danh sách Exness không
	        List<Exness> userExnessList = user.getExnessList();
	        if (userExnessList != null) {
	            // Thêm Exness của user vào danh sách lọc
	            exnessFiltered.addAll(userExnessList);
	        }

	        // Tìm kiếm chiều sâu với tất cả người giới thiệu của user
	        List<User> refferalUsers = userRepo.findByRefferal(user.getEmail());
	        for (User userRefferal : refferalUsers) {
	            // Gọi đệ quy cho mỗi người giới thiệu
	            findSubBranchExness(userRefferal, exnessFiltered);
	        }
	    }
	}

}
