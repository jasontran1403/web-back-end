package com.web.service;

import java.util.List;

import com.web.dto.RealtimeDataDto;
import com.web.dto.RealtimeDataProjection;
import com.web.dto.RealtimeDto;
import com.web.dto.UpcomingCandleDto;

public interface Mq4DataService {
	void saveData(RealtimeDto realtimeDto);
	
	List<RealtimeDataProjection> getRealtimeData();
	String getRealtimeCandle(String exnessId, String currencyName);
	String getUpcomingCandle(String exnessId, String currencyName);
	RealtimeDataDto getRealtimeDataByExnessId(String exnessId);
	void saveUpcomingCandle(UpcomingCandleDto data);
	long getLatestTransaction();
}
