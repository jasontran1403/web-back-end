package com.web.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.service.ProfitService;
import com.web.user.Profit;
import com.web.user.ProfitRepository;

@Service
public class ProfitServiceImpl implements ProfitService {
	@Autowired
	ProfitRepository proRepo;

	@Override
	public List<Profit> findByAmountAndTimeAndExness(double amount, long time, String exness) {
		// TODO Auto-generated method stub
		return proRepo.findByAmountAndTimeAndExness(amount, time, exness);
	}

	@Override
	public List<Profit> findAmountOfProfitsByTime(long time) {
		// TODO Auto-generated method stub
		return proRepo.findAmountOfProfitsByTime(time);
	}

	@Override
	public double sumTotalProfit(String exnessId) {
	    try {
	        double result = proRepo.sumTotalProfit(exnessId);
	        return result;
	    } catch (Exception e) {
	        return 0;
	    }
	}

}
