package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpcomingCandleDto {
	private String exnessId;
	private String currencyName;
	private String upcomingCandle;
}
