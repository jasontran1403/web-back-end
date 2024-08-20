package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeDto {
	private String exnessId;
	private String currencyName;
	private double totalEquity;
	private double currencyEquity;
	private String currentCandle;
	private double lot;
	private double currentBalance;
	private long lastestUpdated;
}
