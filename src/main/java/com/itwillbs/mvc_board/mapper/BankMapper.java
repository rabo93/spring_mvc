package com.itwillbs.mvc_board.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.itwillbs.mvc_board.vo.BankToken;

@Mapper
public interface BankMapper {

	// 기존 엑세스토큰 정보 조회
	String selectTokenInfo(Map<String, Object> map);
	
	// 엑세스토큰 정보 저장
	void insertAccessToken(Map<String, Object> map);

	// 엑세스토큰 정보 갱신
	void updateAccessToken(Map<String, Object> map);

	// DB 사용자 토큰 정보 조회
	BankToken selectBankTokenInfo(String id);

	// DB 사용자 대표계좌 정보 조회
	String selectReprensetAccount(Map<String, Object> map);

	// DB 사용자 대표계좌 정보 등록
	int insertRepresentAccount(Map<String, Object> map);

	// DB 사용자 대표계좌 정보 변경
	int updateRepresentAccount(Map<String, Object> map);

	// 이체(출금, 입금) 결과 저장
	void insertTransactionResult(
			@Param("withdrawResult") Map<String, String> withdrawResult, 
			@Param("transactionType") String transactionType);

	// 이체(출금, 입금) 결과 조회
	Map<String, String> selectTransactionResult(String bank_tran_id);

	// DB 사용자 계좌 정보 조회
	Map<String, String> selectBankAccountInfo(String user_seq_no);

}












