package com.web.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.web.service.ExnessService;
import com.web.service.TransactionService;
import com.web.user.Exness;
import com.web.user.Transaction;
import com.web.user.TransactionRepository;
import com.web.user.User;
import com.web.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{
	private final TransactionRepository transactionRepo;
	private final UserRepository userRepo;
	private final ExnessService exService;

	@Override
	public List<Transaction> findTransactionByEmail(String email) {
		// TODO Auto-generated method stub
		
		if (email.equalsIgnoreCase("all")) {
			List<Exness> listExness = new ArrayList<>();
			List<User> users = userRepo.getUsersByBranchName("PixiuGroup");
			for (User userItem : users) {
				List<Exness> listExnessNew = userItem.getExnessList();
				for (Exness exness : listExnessNew) {
					listExness.add(exness);
				}
			}
			
			List<Transaction> listResult = new ArrayList<>();
			for (Exness exness : listExness) {
				List<Transaction> listTransactionByExness = transactionRepo.findTransactionByExnessId(exness.getExness());
				if (listTransactionByExness.size() > 0) {
					listResult.addAll(listTransactionByExness);
				}
			}
			
			listResult.sort((transaction1, transaction2) -> Long.compare(transaction2.getTime(), transaction1.getTime()));

			
			return listResult;
		} else {
			User user = userRepo.findByEmail(email).get();
			List<Exness> listExness = user.getExnessList();
			List<Transaction> listResult = new ArrayList<>();
			for (Exness exness : listExness) {
				List<Transaction> listTransactionByExness = transactionRepo.findTransactionByExnessId(exness.getExness());
				if (listTransactionByExness.size() > 0) {
					listResult.addAll(listTransactionByExness);
				}
			}
			
			listResult.sort((transaction1, transaction2) -> Long.compare(transaction2.getTime(), transaction1.getTime()));

			
			return listResult;
		}
		
	}

	@Override
	public Transaction saveTransaction(Transaction transaction) {
		// TODO Auto-generated method stub
		return transactionRepo.save(transaction);
	}

	@Override
	public List<Transaction> findByAmountAndTimeAndExness(double amount, long time, String exness) {
		// TODO Auto-generated method stub
		return transactionRepo.findTransactionByAmountAndTimeAndExness(amount, time, exness);
	}

	@Override
	public double getTotalDepositByExnessId(String exnessId) {
		// TODO Auto-generated method stub
		double totalDeposit = transactionRepo.getTotalDepositByExnessId(exnessId);
		double totalWithdraw = transactionRepo.getTotalWithdrawByExnessId(exnessId);
		return totalDeposit - totalWithdraw;
	}

	@Override
	public double getTotalDepositFromPixiu(long timestamp) {
		// TODO Auto-generated method stub
		List<Exness> listExnessFromPixiu = exService.findAllByBranchName("PixiuGroup");
		
		double result = 0.0;
		
		for (Exness exness : listExnessFromPixiu) {
			double deposit = transactionRepo.getTotalDepositPixiuByExnessId(exness.getExness(), timestamp);
			double withdraw = transactionRepo.getTotalWithdrawPixiuByExnessId(exness.getExness(), timestamp);
			
			result = result + deposit - withdraw;
		}
		
		return result;
	}

}
