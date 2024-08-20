package com.web.service.serviceimpl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.web.dto.PrevRequest;
import com.web.dto.PreviousMonthResponse;
import com.web.service.PrevService;
import com.web.user.Exness;
import com.web.user.ExnessRepository;
import com.web.user.Prev;
import com.web.user.PrevRepository;
import com.web.user.User;
import com.web.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrevServiceImpl implements PrevService{
	private final UserRepository userRepo;
	private final PrevRepository prevRepo;
	private final ExnessRepository exRepo;
	
	@Override
	public PreviousMonthResponse findPrevByEmail(String email) {
		// TODO Auto-generated method stub
		User user = userRepo.findByEmail(email).get();
		Optional<Prev> prev = prevRepo.getPrevByUser(user);
		if (prev.isEmpty()) {
			return new PreviousMonthResponse();
		}
		PreviousMonthResponse result = new PreviousMonthResponse();
		result.setBalance(prev.get().getBalance());
		result.setCommission(prev.get().getCommission());
		result.setDeposit(prev.get().getDeposit());
		result.setWithdraw(prev.get().getWithdraw());
		return result;
	}

	@Override
	public void updatePrev(PrevRequest request) {
		// TODO Auto-generated method stub
		Prev prev = new Prev();
		User user = userRepo.findByEmail(request.getEmail()).get();
		prev.setUser(user);
		prev.setBalance(request.getBalance());
		prev.setCommission(request.getCommission());
		prev.setDeposit(request.getDeposit());
		prev.setWithdraw(request.getWithdraw());
		prevRepo.save(prev);
		
	}

	@Override
	public Prev findByExnessId(String exnessid) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessid).get();
		Prev prev = prevRepo.getPrevByUser(exness.getUser()).get();
		return prev;
	}

	@Override
	public Prev initPrev(String email) {
		// TODO Auto-generated method stub
		Prev prev = new Prev();
		User user = userRepo.findByEmail(email).get();
		prev.setUser(user);
		return prevRepo.save(prev);
	}

	@Override
	public void updatePrevData(String exnessId, double balance, double profit, double deposit, double withdraw) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		Prev prev = prevRepo.getPrevByUser(exness.getUser()).get();
		prev.setBalance(balance);
		prev.setCommission(profit);
		prev.setDeposit(deposit);
		prev.setWithdraw(withdraw);
		prevRepo.save(prev);
	}

}
