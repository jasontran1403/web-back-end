package com.web.service.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.web.dto.PreviousMonthResponse;
import com.web.service.ExnessService;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.Transaction;
import com.web.user.TransactionRepository;
import com.web.user.User;
import com.web.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExnessServiceImpl implements ExnessService {
	private final ExnessRepository exRepo;
	private final UserRepository userRepo;
	private final TransactionRepository tranRepo;

	@Override
	public User findUserByExness(String exnessId) {
		// TODO Auto-generated method stub
		return exRepo.findByExness(exnessId).get().getUser();
	}

	@Override
	public PreviousMonthResponse findByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.findByEmail(email).get();
		PreviousMonthResponse result = new PreviousMonthResponse();
		double withdrawAmounts = 0.0, depositAmounts = 0.0, profit = 0.0;
		
		for (Exness exness : user.getExnessList()) {
			result.setBalance(result.getBalance() + exness.getPrevBalance());
			profit += exness.getTotalProfit();
			List<Transaction> transactions = tranRepo.findTransactionByExnessId(exness.getExness());
			for (Transaction tran : transactions) {
				if (tran.getType().equals("Deposit")) {
					depositAmounts += tran.getAmount();
				} else if (tran.getType().equals("Withdraw")) {
					withdrawAmounts += tran.getAmount();
				}
			}
		}
		result.setProfit(profit);
		result.setDeposit(depositAmounts);
		result.setWithdraw(withdrawAmounts);
		return result;
	}

	@Override
	public PreviousMonthResponse findByExness(String exness) {
		// TODO Auto-generated method stub
		PreviousMonthResponse result = new PreviousMonthResponse();
		Exness item = exRepo.findByExness(exness).get();
		double withdrawAmounts = 0.0, depositAmounts = 0.0, profit = 0.0;
		profit += item.getTotalProfit();
		List<Transaction> transactions = tranRepo.findTransactionByExnessId(item.getExness());
		for (Transaction tran : transactions) {
			if (tran.getType().equals("Deposit")) {
				depositAmounts += tran.getAmount();
			} else if (tran.getType().equals("Withdraw")) {
				withdrawAmounts += tran.getAmount();
			}
		}
		result.setProfit(profit);
		result.setDeposit(depositAmounts);
		result.setWithdraw(withdrawAmounts);
		result.setBalance(result.getBalance() + item.getPrevBalance());
		return result;
	}

	@Override
	public void updateTotalProfit(String exnessId, double amount) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		exness.setTotalProfit(exness.getTotalProfit() + amount);
		exRepo.save(exness);
	}

	@Override
	public Optional<Exness> findByExnessId(String exnessId) {
		// TODO Auto-generated method stub
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		return exness;
	}

	@Override
	public double getBalanceByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.getByEmail(email);
		List<Exness> exnesses = exRepo.findByUser(user);
		double balance = 0;
		for (Exness item : exnesses) {
			balance += item.getBalance();
		}
		return balance;
	}

	@Override
	public double getProfitByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.getByEmail(email);
		List<Exness> exnesses = exRepo.findByUser(user);
		double profit = 0;
		for (Exness item : exnesses) {
			profit += item.getTotalProfit();
		}
		return profit;
	}

	@Override
	public List<Exness> findAllExness() {
		// TODO Auto-generated method stub
		return exRepo.findAll();
	}

	@Override
	public void fixTotalProfit(String exnessId, double amount) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		exness.setTotalProfit(amount);
		exRepo.save(exness);
	}

	@Override
	public List<Exness> getListExnessByBranchName(String branchName) {
		// TODO Auto-generated method stub
		List<Exness> listExness = exRepo.findAll();
		List<Exness> result = new ArrayList<>();
		
		for (Exness exness : listExness) {
			if (exness.getUser().getBranchName().equalsIgnoreCase(branchName)) {
				result.add(exness);
			}
		}
		
		return result;
	}

	@Override
	public List<Exness> findAllByBranchName(String branchName) {
		// TODO Auto-generated method stub
		List<Exness> results = new ArrayList<>();
		
		List<User> allUsers = userRepo.getUsersByBranchName(branchName);
		
		for (User user : allUsers) {
			for (Exness exness : user.getExnessList()) {
				results.add(exness);
			}
		}
		
		return results;
	}

	@Override
	public List<Exness> findListExnessByRootUser(User rootUser) {
		// TODO Auto-generated method stub
		List<Exness> listExnesses = new ArrayList<>();
		
		return listExnesses;
	}

}
