package com.web.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	@Query(value="select * from transaction where exness_id = ?1 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByExnessId(String exness);

	@Query(value="select * from transaction where exness_id = ?1 and type = ?2 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByExnessIdAndType(String exness, String type);
	
	@Query(value="select * from transaction where amount = ?1 and time = ?2 and exness_id = ?3 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByAmountAndTimeAndExness(double amount, long time, String exness);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Deposit'", nativeQuery = true)
	double getTotalDepositByExnessId(String exnessId);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Withdraw'", nativeQuery = true)
	double getTotalWithdrawByExnessId(String exnessId);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Deposit' and time <= ?2", nativeQuery = true)
	double getTotalDepositPixiuByExnessId(String exnessId, long time);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Withdraw' and time <= ?2", nativeQuery = true)
	double getTotalWithdrawPixiuByExnessId(String exnessId, long time);
}
