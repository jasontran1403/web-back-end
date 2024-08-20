package com.web.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Mq4Data {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String exnessId;
	private String currencyName;
	private double totalEquity;
	private double lot;
	private double currencyEquity;
	private String currentCandle;
	private String upcomingCandle;
	private double currentBalance;
	private long lastestUpdated;
	private int initPoint;
	private int initSpread;
}
