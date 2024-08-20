package com.web.dto;

import java.util.List;

import com.web.user.AdminPixiu;
import com.web.user.Balance;
import com.web.user.Profit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPixiuResponse {
	private double profit;
	private double commission;
	private List<Profit> profits;
	private List<AdminPixiu> commissions;
	private List<Balance> balances;
}
