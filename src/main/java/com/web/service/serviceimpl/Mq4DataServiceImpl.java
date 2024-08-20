package com.web.service.serviceimpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.dto.RealtimeDataDto;
import com.web.dto.RealtimeDataProjection;
import com.web.dto.RealtimeDto;
import com.web.dto.RealtimeItemDto;
import com.web.dto.UpcomingCandleDto;
import com.web.service.Mq4DataService;
import com.web.user.Mq4Data;
import com.web.user.Mq4DataRepository;

@Service
public class Mq4DataServiceImpl implements Mq4DataService {
	@Autowired
	Mq4DataRepository mq4Repo;

	@Override
	public void saveData(RealtimeDto realtimeDto) {
		// TODO Auto-generated method stub
		Optional<Mq4Data> data = mq4Repo.findExistedData(realtimeDto.getExnessId(), realtimeDto.getCurrencyName());

		Date currentDateTime = new Date();
		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);
		// Lấy timestamp sau khi đặt thời gian
		long time = calendar.getTimeInMillis() / 1000;

		if (data.isPresent()) {
			data.get().setTotalEquity(realtimeDto.getTotalEquity());
			data.get().setCurrencyEquity(realtimeDto.getCurrencyEquity());
			data.get().setCurrentBalance(realtimeDto.getCurrentBalance());
			data.get().setCurrentCandle(realtimeDto.getCurrentCandle());
			data.get().setLot(realtimeDto.getLot());
			data.get().setLastestUpdated(time);

			mq4Repo.save(data.get());
		} else {
			Mq4Data newData = new Mq4Data();
			newData.setExnessId(realtimeDto.getExnessId());
			newData.setCurrencyName(realtimeDto.getCurrencyName());
			newData.setTotalEquity(realtimeDto.getTotalEquity());
			newData.setCurrencyEquity(realtimeDto.getCurrencyEquity());
			newData.setCurrentBalance(realtimeDto.getCurrentBalance());
			newData.setCurrentCandle(realtimeDto.getCurrentCandle());
			newData.setLot(realtimeDto.getLot());
			newData.setLastestUpdated(time);

			mq4Repo.save(newData);
		}
	}

	@Override
	public List<RealtimeDataProjection> getRealtimeData() {
		// TODO Auto-generated method stub
		return mq4Repo.getAllRealtimeData();
	}

	@Override
	public RealtimeDataDto getRealtimeDataByExnessId(String exnessId) {
		// TODO Auto-generated method stub
		List<Mq4Data> listDataFromExness = mq4Repo.getRealtimeDataByExnessId(exnessId);
		RealtimeDataDto results = new RealtimeDataDto();
		List<RealtimeItemDto> dataItem = new ArrayList<>();
		for (Mq4Data item : listDataFromExness) {
			RealtimeItemDto itemConvert = new RealtimeItemDto();
			itemConvert.setName(item.getCurrencyName());
			itemConvert.setValue(Math.abs(item.getCurrencyEquity()));
			itemConvert.setCandle(item.getCurrentCandle());

			if (item.getCurrencyEquity() > 0) {
				itemConvert.setType(0);
			} else {
				double ratio = Math.abs(item.getCurrencyEquity()) / item.getCurrentBalance();

				if (ratio < 0.1) {
					itemConvert.setType(1);
				} else if (ratio >= 0.1 && ratio <= 0.2) {
					itemConvert.setType(2);
				} else {
					itemConvert.setType(3);
				}
			}

			dataItem.add(itemConvert);
		}

		results.setExnessId(exnessId);
		results.setRealtimeData(dataItem);

		return results;
	}

	@Override
	public String getRealtimeCandle(String exnessId, String currencyName) {
		// TODO Auto-generated method stub
		Optional<Mq4Data> data = mq4Repo.findExistedData(exnessId, currencyName);
		if (data.isPresent()) {
			String result = "";
			int number = Integer.parseInt(data.get().getCurrentCandle());
			if (number == 30) {
				result = "M30";
			} else if (number == 60) {
				result = "H1";
			} else if (number == 240) {
				result = "H4";
			}else if (number == 1440) {
				result = "D1";
			}
			return result;
		}
		throw new RuntimeException("Không có dữ liệu của cặp " + currencyName + " từ ExnessId#" + exnessId);
	}

	@Override
	public void saveUpcomingCandle(UpcomingCandleDto data) {
		// TODO Auto-generated method stub
		Optional<Mq4Data> result = mq4Repo.findExistedData(data.getExnessId(), data.getCurrencyName());
		if (result.isPresent()) {
			result.get().setUpcomingCandle(data.getUpcomingCandle());
			mq4Repo.save(result.get());
		} else {
			throw new RuntimeException("Không có dữ liệu của cặp " + data.getCurrencyName() + " từ ExnessId#" + data.getExnessId());
		}
	}

	@Override
	public String getUpcomingCandle(String exnessId, String currencyName) {
		// TODO Auto-generated method stub
		Optional<Mq4Data> result = mq4Repo.findExistedData(exnessId, currencyName);
		if (result.isPresent()) {
			return result.get().getUpcomingCandle();
		}
		throw new RuntimeException("Không có dữ liệu của cặp " + currencyName + " từ ExnessId#" + exnessId);
	}

	@Override
	public long getLatestTransaction() {
		// TODO Auto-generated method stub
		return mq4Repo.getLatestRealtimeData();
	}

}
