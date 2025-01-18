package com.itwillbs.mvc_board.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itwillbs.mvc_board.handler.BankApiClient;
import com.itwillbs.mvc_board.handler.BankValueGenerator;
import com.itwillbs.mvc_board.mapper.BankMapper;
import com.itwillbs.mvc_board.vo.BankToken;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class BankService {
	@Autowired
	private BankMapper mapper;

	@Autowired
	private BankApiClient bankApiClient;
	
	// API - 엑세스토큰 발급 요청 	
	public BankToken getAccessToken(Map<String, String> authResponse) {
		// BankApiClient - requestAccessToken() 메서드 호출하여 엑세스토큰 발급 요청
		return bankApiClient.requestAccessToken(authResponse);
	}

	// DB - 엑세스토큰 정보 저장 요청
	public void registAccessToken(Map<String, Object> map) {
		// BankMapper - selectTokenInfo() 메서드 호출하여 사용자 일련번호에 해당하는 엑세스토큰 존재 여부 판별
		// => 파라미터 : Map 객체   리턴타입 : String(access_token)
		String access_token = mapper.selectTokenInfo(map);
		System.out.println("토큰 정보 : " + access_token);
		
		// -------------------------------------------------------
		// 기존 토큰이 존재하지 않을 경우 새 엑세스 토큰 정보 추가(INSERT) - insertAccessToken()
		// 기존 토큰이 존재할 경우 새 엑세스 토큰 정보 갱신(UPDATE) - updateAccessToken()
		if(access_token == null) {
			// BankMapper - insertAccessToken() 메서드 호출하여 엑세스토큰 정보 저장 요청
			mapper.insertAccessToken(map);
		} else {
			// BankMapper - updateAccessToken() 메서드 호출하여 엑세스토큰 정보 저장 요청
			mapper.updateAccessToken(map);
		}
		
	}

	// DB - 엑세스토큰 정보 조회 요청
	public BankToken getBankTokenInfo(String id) {
		// BankMapper - selectBankTokenInfo()
		return mapper.selectBankTokenInfo(id);
	}

	// API - 핀테크 사용자 정보 조회 요청
	public Map<String, Object> getBankUserInfo(BankToken token) {
		// BankApiClient - requestBankUserInfo()
		return bankApiClient.requestBankUserInfo(token);
	}

	// API - 핀테크 계좌 잔액 조회 요청
	public Map<String, String> getAccountDetail(Map<String, Object> map) {
		// BankApiClient - requestAccountDetail()
		return bankApiClient.requestAccountDetail(map);
	}

	// DB - 대표계좌 등록 요청
	public int registRepresentAccount(Map<String, Object> map) {
		// BankMapper - selectReprensetAccount() 메서드 호출하여 사용자번호에 대한 대표계좌 조회
		// => 파라미터 : Map 객체   리턴타입 : String(user_seq_no)
		String user_seq_no = mapper.selectReprensetAccount(map);
		
		// 대표계좌 존재여부 판별
		if(user_seq_no == null) { // 대표계좌 정보 없을 경우
			return mapper.insertRepresentAccount(map); // 대표계좌 정보 등록
		} else { // 대표계좌 정보 있을 경우
			return mapper.updateRepresentAccount(map); // 대표계좌 정보 변경
		}
	}

	// =====================================================================
	// API - 출금이체 요청
	public Map<String, String> requestWithdraw(Map<String, Object> map) {
		// BankApiClient - requestWithdraw()
		return bankApiClient.requestWithdraw(map);
	}

	// DB - 출금이체 결과 저장 요청
	public void registWithdrawResult(Map<String, String> withdrawResult) {
		// BankMapper - insertTransactionResult()
		// => 파라미터 : 출금이체 결과, 거래타입("WI" : 출금이체)
		mapper.insertTransactionResult(withdrawResult, "WI");
	}
	
	// DB - 출금이체 결과 조회 요청
	public Map<String, String> getWithdrawResult(String bank_tran_id) {
		// BankMapper - selectTransactionResult()
		return mapper.selectTransactionResult(bank_tran_id);
	}
	// =====================================================================
	// API - 이용기관 엑세스토큰 발급 요청 	
	public BankToken getAdminAccessToken() {
		// BankApiClient - requestAdminAccessToken() 메서드 호출하여 이용기관 엑세스토큰 발급 요청
		return bankApiClient.requestAdminAccessToken();
	}

	// =====================================================================
	// API - 입금이체 요청
	public Map<String, Object> requestDeposit(Map<String, Object> map) {
		// BankMapper - selectAdminAccessToken() 메서드 호출하여 이용기관 엑세스토큰 조회
		// => 파라미터 : 없음   리턴타입 : BankToken(adminToken)
		// => 단, 기존 사용자의 엑세스 토큰 정보 조회와 작업은 동일하고
		//    전달할 아이디값만 "admin" 으로 고정하여 메서드 재사용 가능
		// -----------------------------------
		// BankMapper - selectBankTokenInfo() 메서드 호출하여 이용기관 엑세스토큰 조회
		// => 파라미터 : 관리자 아이디("admin")   리턴타입 : BankToken(adminToken)
		BankToken adminToken = mapper.selectBankTokenInfo("admin");
		
		// Map 객체에 "adminToken" 이라는 속성명으로 이용기관 토큰 정보 추가
		map.put("adminToken", adminToken);
		
		// BankApiClient - requestDeposit()
		return bankApiClient.requestDeposit(map);
	}

	// =====================================================================
	@Value("${bank.client_use_code}")
	private String client_use_code;
	
	public Map<String, Object> transfer(Map<String, Object> map) {
		// [ 송금 작업 순서 ]
		// 1단계. 보내는 사람 -> 이용기관(아이티윌 - 이연태) 출금이체 수행
		// 2단계. 이용기관(아이티윌 - 이연태) -> 받는 사람   입금이체 수행
		// ---------------------------------------------------------------
		// BankApiClient - requestWithdrawForTransfer() 메서드 호출하여 출금이체 요청
		// => 기존 requestWithdraw() 메서드 재사용해도 되지만 구분을 위해 별도로 정의
		// => 파라미터 : Map 객체   리턴타입 : Map<String, String>(withdrawResult)
		Map<String, String> withdrawResult = bankApiClient.requestWithdrawForTransfer(map);
		log.info(">>>>> 송금(출금) 결과 : " + withdrawResult);
		
		// 출금이체 성공/실패 상관없이 결과를 Map 객체에 저장
		Map<String, Object> transferResult = new HashMap<String, Object>();
		transferResult.put("withdrawResult", withdrawResult);
		
		// 출금이체 성공시에만 입금이체 작업 수행
		// => 출금이체 요청 결과 응답데이터 중 "rsp_code" 값이 "A0000" 일 경우만 작업 수행
		if(withdrawResult.get("rsp_code").equals("A0000")) {
			// BankMapper - selectBankTokenInfo() 메서드 호출하여 이용기관 엑세스토큰 조회
			// => 파라미터 : 관리자 아이디("admin")   리턴타입 : BankToken(adminToken)
			BankToken adminToken = mapper.selectBankTokenInfo("admin");
			
			// Map 객체에 "adminToken" 이라는 속성명으로 이용기관 토큰 정보 추가
			map.put("adminToken", adminToken);
			
			// BankApiClient - requestDepositForTransfer() 메서드 호출하여 입금이체 요청
			Map<String, Object> depositResult =  bankApiClient.requestDepositForTransfer(map);
			log.info(">>>>> 송금(입금) 결과 : " + depositResult);
			
			// 출금이체 성공/실패 상관없이 결과를 Map 객체에 저장
			transferResult.put("depositResult", depositResult);
			
			// 입금이체 실패 시
			if(!depositResult.get("rsp_code").equals("A0000")) {
				// 출금이체를 되돌려야하므로 아이티윌(이용기관) -> 출금이체계좌로 다시 입금이체 필요(생략)
			}
		}
		
		return transferResult;
	}

	// DB - 사용자 계좌정보 조회
	public Map<String, String> getBankAccountInfo(String user_seq_no) {
		return mapper.selectBankAccountInfo(user_seq_no);
	}
}












