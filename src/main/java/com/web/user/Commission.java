package com.web.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "commission")
@IdClass(Commission.class)
public class Commission {
	@Id
	private String exnessId;
	@Id
	private String transactionId;
	@Id
	private long time;
	private double amount;
	@Column(columnDefinition="TEXT")
	private String message;
}
