package com.web.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeDataDto {
	private String exnessId;
	private List<RealtimeItemDto> realtimeData;
}
