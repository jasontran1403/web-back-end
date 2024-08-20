package com.web.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.web.dto.RealtimeDataProjection;

public interface Mq4DataRepository extends JpaRepository<Mq4Data, Long>{
	@Query(value="select * from mq4data where exness_id = ?1 and currency_name = ?2", nativeQuery=true)
	Optional<Mq4Data> findExistedData(String exnessId, String currencyName);

	@Query(value = "SELECT exness_id as exnessid, MAX(lot) as lot, MAX(current_balance) AS balance, MAX(total_equity) AS equity FROM mq4data GROUP BY exness_id", nativeQuery = true)
    List<RealtimeDataProjection> getAllRealtimeData();
	
	@Query(value = "select * from mq4data where exness_id = ?1", nativeQuery=true)
	List<Mq4Data> getRealtimeDataByExnessId(String exnessId);
	
	@Query(value = "select MIN(lastest_updated) from mq4data", nativeQuery=true)
	long getLatestRealtimeData();
}
