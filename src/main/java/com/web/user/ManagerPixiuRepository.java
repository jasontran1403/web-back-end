package com.web.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ManagerPixiuRepository extends JpaRepository<ManagerPixiu, Long>{
	@Query(value="select * from manager_pixiu where exness_id = ?1", nativeQuery=true)
	List<ManagerPixiu> getCommissionByExnessId(String exnessId);
	
	@Query(value="select * from manager_pixiu where exness_id = ?1 order by time asc", nativeQuery=true)
	List<ManagerPixiu> getCommissionByExnessIdAndTime(String exnessId, long from, long to);
	
	@Query(value="select * from manager_pixiu where exness_id = ?1 and time >= ?2 and time <= ?3 order by time asc", nativeQuery=true)
	List<ManagerPixiu> getCommissionByExnessIdAndTimeRange(String exnessId, long from, long to);
	
	@Query(value="select COALESCE(SUM(amount), 0) from manager_pixiu where exness_id = ?1", nativeQuery=true)
	double getTotalCommissionByExnessId(String exnessId);

}
