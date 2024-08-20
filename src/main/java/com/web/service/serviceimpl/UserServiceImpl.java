package com.web.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.dto.AdminPixiuResponse;
import com.web.dto.InfoLisaResponse;
import com.web.dto.InfoResponse;
import com.web.dto.ManagerPixiuResponse;
import com.web.exception.NotFoundException;
import com.web.service.UserService;
import com.web.user.AdminPixiu;
import com.web.user.AdminPixiuRepository;
import com.web.user.Balance;
import com.web.user.BalanceRepository;
import com.web.user.Commission;
import com.web.user.CommissionRepository;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.History;
import com.web.user.HistoryRepository;
import com.web.user.ManagerPixiu;
import com.web.user.ManagerPixiuRepository;
import com.web.user.Profit;
import com.web.user.ProfitRepository;
import com.web.user.Transaction;
import com.web.user.TransactionRepository;
import com.web.user.User;
import com.web.user.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	UserRepository userRepo;

	@Autowired
	ProfitRepository proRepo;

	@Autowired
	CommissionRepository commissRepo;

	@Autowired
	AdminPixiuRepository adminRepo;

	@Autowired
	ManagerPixiuRepository managerRepo;

	@Autowired
	ExnessRepository exRepo;

	@Autowired
	BalanceRepository balanceRepo;

	@Autowired
	TransactionRepository transRepo;

	@Autowired
	HistoryRepository hisRepo;

	@Override
	public InfoResponse getInfoByExnessId(String exnessId, long from, long to) {
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This ExnessID " + exnessId + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<Commission> commissions = new ArrayList<>();

		if (from > 0 && to > 0) {
			profits = proRepo.getCommissionByExnessIdAndTime(exnessId, from, to);
			commissions = commissRepo.getCommissionByExnessIdAndTime(exnessId, from, to);
		} else {
			profits = proRepo.getCommissionByExnessId(exnessId);
			commissions = commissRepo.getCommissionByExnessId(exnessId);
		}

		double totalCommission = commissRepo.getTotalCommissionByExnessId(exnessId);
		InfoResponse result = new InfoResponse();
		result.setProfit(exness.get().getBalance());
		result.setCommission(totalCommission);
		result.setProfits(profits);
		result.setCommissions(commissions);
		List<Balance> balances = balanceRepo.findByExnessByTime(exness.get().getExness(), from, to);
		result.setBalances(balances);

		return result;
	}

	@Override
	public InfoResponse getInfoFromTimeRangeByExnessId(String exnessId, long from, long to) {
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This ExnessID " + exnessId + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<Commission> commissions = new ArrayList<>();

		if (from > 0 && to > 0) {
			profits = proRepo.getCommissionByExnessIdAndTimeRange(exnessId, from, to);
			commissions = commissRepo.getCommissionByExnessIdAndTimeRange(exnessId, from, to);
		} else {
			profits = proRepo.getCommissionByExnessId(exnessId);
			commissions = commissRepo.getCommissionByExnessId(exnessId);
		}

		double totalCommission = commissRepo.getTotalCommissionByExnessId(exnessId);
		InfoResponse result = new InfoResponse();
		result.setProfit(exness.get().getBalance());
		result.setCommission(totalCommission);
		result.setProfits(profits);
		result.setCommissions(commissions);
		List<Balance> balances = balanceRepo.findByExnessByTimeRange(exness.get().getExness(), from, to);
		result.setBalances(balances);

		return result;
	}

	@Override
	public boolean saveProfit(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		Profit profit = new Profit();
		profit.setExnessId(exnessId);
		profit.setAmount(amount);
		profit.setTime(time);

		List<Profit> profits = proRepo.findByTimeAndExness(time, exnessId);
		if (profits.size() != 0) {
			return false;
		} else {
			proRepo.save(profit);
			return true;
		}

	}

	@Override
	public Commission saveCommission(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		Commission commission = new Commission();
		commission.setExnessId(exnessId);
		commission.setAmount(amount);
		commission.setTime(time);
		return commissRepo.save(commission);
	}

	@Override
	public boolean saveBalance(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		Balance balance = new Balance();
		Exness exness = exRepo.findByExness(exnessId).get();
		balance.setExnessId(exness.getExness());
		balance.setAmount(amount);
		balance.setTime(time);

		List<Balance> balances = balanceRepo.findByTimeAndExness(time, exnessId);
		if (balances.size() != 0) {
			return false;
		} else {
			balanceRepo.save(balance);
			return true;
		}

	}

	@Override
	public Transaction saveTransaction(String exnessId, double amount, long time) {
		// TODO Auto-generated method stub
		String type = "";
		if (amount > 0) {
			type = "Deposit";
		} else {
			type = "Withdraw";
		}
		Transaction transaction = new Transaction();
		transaction.setExnessId(exnessId);
		transaction.setAmount(amount);
		transaction.setType(type);
		transaction.setTime(time);
		return transRepo.save(transaction);
	}

	@Override
	public InfoResponse getAllInfoByEmail(String email, long from, long to) {
		// TODO Auto-generated method stub
		Optional<User> user = userRepo.findByEmail(email);
		if (user.isEmpty()) {
			throw new NotFoundException("This user with email " + email + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<Commission> commissions = new ArrayList<>();
		List<Balance> balances = new ArrayList<>();
		double balance = 0.0;

		if (from > 0 && to > 0) {
			for (Exness exness : user.get().getExnessList()) {
				balance += exness.getBalance();
				List<Profit> profitsFromCriteria = proRepo.getCommissionByExnessIdAndTime(exness.getExness(), from, to);
				if (profitsFromCriteria.size() > 0) {
					for (Profit profit : profitsFromCriteria) {
						profits.add(profit);
					}
				}

				List<Commission> commissionsFromCriteria = commissRepo
						.getCommissionByExnessIdAndTime(exness.getExness(), from, to);
				if (commissionsFromCriteria.size() > 0) {
					for (Commission commission : commissionsFromCriteria) {
						commissions.add(commission);
					}
				}

				List<Balance> balanceFromCriteria = balanceRepo.findByExnessByTime(exness.getExness(), from, to);
				if (balanceFromCriteria.size() > 0) {
					for (Balance balanceItem : balanceFromCriteria) {
						balances.add(balanceItem);
					}
				}

			}
		}

		InfoResponse result = new InfoResponse();
		result.setProfit(balance);
		result.setCommission(user.get().getCommission());
		result.setProfits(profits);
		result.setCommissions(commissions);
		result.setBalances(balances);

		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public Exness updateBalanceExness(String exness, double amount) {
		// TODO Auto-generated method stub
		Exness item = exRepo.findByExness(exness).get();
		item.setBalance(amount);
		return exRepo.save(item);
	}

	@Override
	public void updateTotalProfit(String exnessId, double amount) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		User user = exness.getUser();
		user.setPrev(user.getPrev() + amount);
		userRepo.save(user);
	}

	@Override
	public void updateTotalCommission(User user, double amount) {
		// TODO Auto-generated method stub
		user.setCommission(user.getCommission() + amount);
		userRepo.save(user);
	}

	@Override
	public List<User> getUsersByBranchName(String branchName) {
		// TODO Auto-generated method stub
		return userRepo.getUsersByBranchName(branchName);
	}

	@Override
	public InfoLisaResponse getAllInfoByEmailLisa(String email, long from, long to) {
		// TODO Auto-generated method stub
		Optional<User> user = userRepo.findByEmail(email);
		if (user.isEmpty()) {
			throw new NotFoundException("This user with email " + email + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<History> histories = new ArrayList<>();
		List<Balance> balances = new ArrayList<>();
		double balance = 0.0;

		if (from > 0 && to > 0) {
			for (Exness exness : user.get().getExnessList()) {
				balance += exness.getBalance();
				List<Profit> profitsFromCriteria = proRepo.getCommissionByExnessIdAndTime(exness.getExness(), from, to);
				if (profitsFromCriteria.size() > 0) {
					for (Profit profit : profitsFromCriteria) {
						profits.add(profit);
					}
				}

				List<Balance> balanceFromCriteria = balanceRepo.findByExnessByTime(exness.getExness(), from, to);
				if (balanceFromCriteria.size() > 0) {
					for (Balance balanceItem : balanceFromCriteria) {
						balances.add(balanceItem);
					}
				}

			}

			List<History> historiesFromCriteria = hisRepo.getHistoryByEmailAndTime(email, from, to);
			if (historiesFromCriteria.size() > 0) {
				for (History history : historiesFromCriteria) {
					histories.add(history);
				}
			}
		}

		InfoLisaResponse result = new InfoLisaResponse();
		result.setProfit(balance);
		result.setCommission(user.get().getCommission());
		result.setProfits(profits);
		result.setHistories(histories);
		result.setBalances(balances);

		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public InfoLisaResponse getInfoByExnessLisa(String exnessId, long from, long to) {
		// TODO Auto-generated method stub
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This ExnessID " + exnessId + " is not existed!");
		}

		List<Profit> profits = new ArrayList<>();
		List<History> histories = new ArrayList<>();
		List<Balance> balances = new ArrayList<>();

		List<History> historiesFromCriteria = hisRepo.getHistoryByEmailAndTime(exness.get().getUser().getEmail(), from,
				to);
		if (historiesFromCriteria.size() > 0) {
			for (History history : historiesFromCriteria) {
				histories.add(history);
			}
		}

		List<Profit> profitsFromCriteria = proRepo.getCommissionByExnessIdAndTime(exness.get().getExness(), from, to);
		if (profitsFromCriteria.size() > 0) {
			for (Profit profit : profitsFromCriteria) {
				profits.add(profit);
			}
		}

		List<Balance> balanceFromCriteria = balanceRepo.findByExnessByTime(exness.get().getExness(), from, to);
		if (balanceFromCriteria.size() > 0) {
			for (Balance balanceItem : balanceFromCriteria) {
				balances.add(balanceItem);
			}
		}

		InfoLisaResponse result = new InfoLisaResponse();
		result.setProfit(exness.get().getBalance());
		result.setCommission(exness.get().getUser().getCommission());
		result.setProfits(profits);
		result.setHistories(histories);
		result.setBalances(balances);

		return result;
	}

	@Override
	public AdminPixiuResponse getInfoFromTimeRangeByExnessIdByAdmin(String exnessId, long from, long to) {
		// TODO Auto-generated method stub
		Optional<Exness> exness = exRepo.findByExness(exnessId);
		if (exness.isEmpty()) {
			throw new NotFoundException("This ExnessID " + exnessId + " is not existed!");
		}
		

		List<AdminPixiu> commissions = new ArrayList<>();

		List<Profit> profits = new ArrayList<>();

		if (from > 0 && to > 0) {
			profits = proRepo.getCommissionByExnessIdAndTimeRange(exnessId, from, to);
			commissions = adminRepo.getCommissionByExnessIdAndTimeRange(exnessId, from, to);
		} else {
			profits = proRepo.getCommissionByExnessId(exnessId);
			commissions = adminRepo.getCommissionByExnessId(exnessId);
		}

		double totalCommission = adminRepo.getTotalCommissionByExnessId(exnessId);
		AdminPixiuResponse result = new AdminPixiuResponse();
		result.setProfit(exness.get().getBalance());
		result.setCommission(totalCommission);
		result.setProfits(profits);
		result.setCommissions(commissions);
		List<Balance> balances = balanceRepo.findByExnessByTimeRange(exness.get().getExness(), from, to);
		result.setBalances(balances);

		return result;
	}

	@Override
	public ManagerPixiuResponse getInfoFromTimeRangeByExnessIdByManager(String exnessId, long from, long to) {
		// TODO Auto-generated method stub
				Optional<Exness> exness = exRepo.findByExness(exnessId);
				if (exness.isEmpty()) {
					throw new NotFoundException("This ExnessID " + exnessId + " is not existed!");
				}
				

				List<ManagerPixiu> commissions = new ArrayList<>();

				List<Profit> profits = new ArrayList<>();

				if (from > 0 && to > 0) {
					profits = proRepo.getCommissionByExnessIdAndTimeRange(exnessId, from, to);
					commissions = managerRepo.getCommissionByExnessIdAndTimeRange(exnessId, from, to);
				} else {
					profits = proRepo.getCommissionByExnessId(exnessId);
					commissions = managerRepo.getCommissionByExnessId(exnessId);
				}

				double totalCommission = managerRepo.getTotalCommissionByExnessId(exnessId);
				ManagerPixiuResponse result = new ManagerPixiuResponse();
				result.setProfit(exness.get().getBalance());
				result.setCommission(totalCommission);
				result.setProfits(profits);
				result.setCommissions(commissions);
				List<Balance> balances = balanceRepo.findByExnessByTimeRange(exness.get().getExness(), from, to);
				result.setBalances(balances);

				return result;
	}

	@Override
	public List<User> findByRefferal(String refferalEmail) {
		// TODO Auto-generated method stub
		return userRepo.findByRefferal(refferalEmail);
	}

}
