package com.web.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateExnessLisaRequest {
	private String email;
	private String name;
	private String exness;
	private String server;
	private String password;
	private int type;
	private String refferal;
	private String rate;
	private double lot;
	private String teleId;
}
