package com.web.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfitRepository extends JpaRepository<Profit, Long>{
	@Query(value="select * from profit where exness_id = ?1", nativeQuery=true)
	List<Profit> getCommissionByExnessId(String exnessId);
	
	@Query(value="select * from profit where exness_id = ?1 order by time asc", nativeQuery=true)
	List<Profit> getCommissionByExnessIdAndTime(String exnessId, long from, long to);
	
	@Query(value="select * from profit where exness_id = ?1 and time >= ?2 and time <= ?3 order by time asc", nativeQuery=true)
	List<Profit> getCommissionByExnessIdAndTimeRange(String exnessId, long from, long to);
	
	@Query(value="select * from profit where amount = ?1 and time = ?2 and exness_id = ?3", nativeQuery=true)
	List<Profit> findByAmountAndTimeAndExness(double amount, long time, String exnessId);
	
	@Query(value="select * from profit where time = ?1 and exness_id = ?2", nativeQuery=true)
	List<Profit> findByTimeAndExness(long time, String exnessId);
	
	@Query(value="select * from profit where time = ?1", nativeQuery=true)
	List<Profit> findAmountOfProfitsByTime(long time);
	
	@Query(value="select sum(amount) from profit where exness_id = ?1", nativeQuery=true)
	double sumTotalProfit(String exnessId);

}
