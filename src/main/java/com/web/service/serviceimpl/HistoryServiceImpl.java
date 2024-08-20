package com.web.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.service.HistoryService;
import com.web.user.History;
import com.web.user.HistoryRepository;

@Service
public class HistoryServiceImpl implements HistoryService {
	@Autowired
	HistoryRepository hisRepo;

	@Override
	public List<History> findHistoryByReceiver(String receiver) {
		// TODO Auto-generated method stub
		return hisRepo.findByReceive(receiver);
	}

	@Override
	public History saveHistory(History history) {
		// TODO Auto-generated method stub
		return hisRepo.save(history);
	}

	@Override
	public List<History> findHistoryByReceiverAndTimeRange(String receiver, long from, long to) {
		// TODO Auto-generated method stub
		return hisRepo.findByReceiveAndTimeRange(receiver, from, to);
	}

	@Override
	public double calculateAllIB(String email) {
		// TODO Auto-generated method stub
		List<History> histories = hisRepo.findByReceive(email);
		
		double total = 0;
		for (History history : histories) {
			total += history.getAmount();
		}
		return total;
	}

	@Override
	public List<History> findHistoryByEmail(String email) {
		// TODO Auto-generated method stub
		return hisRepo.findByReceive(email);
	}

}
