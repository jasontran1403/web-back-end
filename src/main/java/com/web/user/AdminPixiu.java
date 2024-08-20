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
@Table(name = "admin_pixiu")
@IdClass(AdminPixiu.class)
public class AdminPixiu {
	@Id
	private String exnessId;
	@Id
	private long time;
	@Id
	private double amount;
	@Column(columnDefinition = "TEXT")
	private String message;
}

