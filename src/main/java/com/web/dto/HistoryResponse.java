package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryResponse {
	private String sender;
	private String receiver;
	private double amount;
	private String message;
	private String time;
	private String transaction;
}
