package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDto implements Comparable<NetworkDto> {
	private String email;
    private String refferal;
    private String image;
    private double commission;
    private double profit;
    private int level;
    
    @Override
    public int compareTo(NetworkDto other) {
        return Integer.compare(this.level, other.level);
    }
}
