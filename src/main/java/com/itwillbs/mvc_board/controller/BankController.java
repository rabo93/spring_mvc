package com.itwillbs.mvc_board.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.mvc_board.aop.BankTokenCheck;
import com.itwillbs.mvc_board.aop.LoginCheck;
import com.itwillbs.mvc_board.aop.LoginCheck.MemberRole;
import com.itwillbs.mvc_board.service.BankService;
import com.itwillbs.mvc_board.vo.BankToken;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class BankController {
	@Autowired
	private BankService service;
	// ===============================================================
	// 계좌관리 메인페이지 포워딩
//	@LoginCheck(memberRole = MemberRole.USER)
	// 세션 체크 시 일반 로그인 체크 or 관리자 세션 체크를 위해 Enum 값 활용
	@LoginCheck(memberRole = MemberRole.USER)
	@GetMapping("BankMain")
	public String bankMain() {
		return "bank/bank_main";
	}
	
	// ---------------------------------------------------------------
	/*
	 * 2.1.1. 사용자인증 API (3-legged)
	 * - 요청을 통해 사용자 인증 및 계좌 등록까지 수행 후
	 *   API 서버로부터 지정된 콜백(Callback) 주소로 리다이렉트가 발생(새로운 주소로 요청)되고
	 *   해당 요청을 컨트롤러에서 매핑하여 전달된 응답 파라미터(code, scope, client_info(생략됨), state)를
	 *   Map 타입으로 전달받아 처리
	 * - 콜백 주소 : http://localhost:8081/mvc_board/callback 
	 *   => 주의! 해당 콜백 주소로 응답받기 위해서는 등록이 필요하며    
	 *      금융결제원 오픈API 개발자 사이트 - 마이페이지 - API Key 관리에서 설정
	 *      URL 등록 후 저장 버튼 클릭 -> 아래쪽 Callback URL 등록 항목의 [등록] 버튼 클릭 필수!
	 */
	
	// 사용자 인증 요청에 대한 콜백 처리
	// => 엑세스토큰 발급 요청까지 연쇄적으로 수행 후 계좌관리 메인페이지로 리다이렉트
	@LoginCheck(memberRole = MemberRole.USER)
	@GetMapping("callback")
	public String callback(@RequestParam Map<String, String> authResponse, HttpSession session, Model model) {
		System.out.println(authResponse);
		/*
		 * 사용자 인증 요청에 대한 콜백 응답 데이터 예시
		 * {
		 * code=MtosPAeK8VusYqqVFqi1Ns2oARUxPZ,  => Authorization Code (엑세스토큰 요청 시 필요)
		 * scope=inquiry login transfer, 
		 * state=12345678901234567890123456789012
		 * }
		 */
		// 임시) 메인페이지에서 엑세스토큰 요청을 별도로 수행하기 위해 세션에 인증코드 저장
//		session.setAttribute("code", authResponse.get("code"));
		// -------------------------------------------------------------------------------------
		// 2.1.2. 토큰발급 API - 사용자 토큰발급 API (3-legged) 요청
		// BankService - getAccessToken() 메서드 호출하여 엑세스토큰 발급 요청
		// => 파라미터 : 토큰 발급에 필요한 정보(인증코드 요청 결과가 포함된 Map 객체)
		// => 리턴타입 : BankToken 또는 Map<String, String)
		BankToken token = service.getAccessToken(authResponse);
		log.info(">>>>>> 엑세스토큰 정보 : " + token);
		
		// 요청 결과 판별
		// => BankToken 객체가 null 이가 엑세스토큰 값이 null 일 경우 요청 에러 처리
		if(token == null || token.getAccess_token() == null) {
			model.addAttribute("msg", "토큰 발급 실패! 재인증 필요!");
			// 인증 화면이 새 창에 표시되어 있으며, 해당 창 닫기 위해 "isClose" 속성값에 true 저장
			model.addAttribute("isClose", true);
			return "result/fail";
		}
		// -------------------------------------------------------------------------------------
		// BankService - registAccessToken() 메서드 호출하여 엑세스토큰 관련 정보 저장 요청
		// => 파라미터 : 세션아이디, BankToken 객체   리턴타입 : void
		// => 세션 아이디와 BankToken 객체를 Map<String, Object> 타입으로 묶어서 전달 가능
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", session.getAttribute("sId"));
		map.put("token", token);
		service.registAccessToken(map);
		// -------------------------------------------------------------------------------------
		// 세션에 엑세스토큰 관리 객체(BankToken) 객체 저장
		session.setAttribute("token", token);
		
		// success.jsp 페이지로 포워딩
		model.addAttribute("msg", "계좌 연결 완료!");
//		model.addAttribute("targetURL", "BankMain");
		model.addAttribute("isClose", true);
		return "result/success";
	}
	
	// -----------------------------------------------------------------------
	// 2.2. 사용자/서비스 관리 - 2.2.1. 사용자정보조회 API (GET)
	// https://testapi.openbanking.or.kr/v2.0/user/me
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck // 엑세스토큰 존재 여부 체크하는 사용자 어노테이션
	@GetMapping("BankUserInfo")
	public String bankUserInfo(HttpSession session, Model model) {
		// 엑세스토큰 관련 정보가 저장된 BankToken 객체(token)를 세션에서 꺼내기
		BankToken token = (BankToken)session.getAttribute("token");
		System.out.println("토큰 정보 : " + token);
		
		// 엑세스토큰(세션의 token 속성값(객체) 또는 token 객체의 access_token 값) 정보가 없을 경우
		// "fail.jsp" 페이지 포워딩("계좌 인증 필수!" 메세지 및 "BankMain" 페이지 전달)
//		if(token == null || token.getAccess_token() == null) {
//			model.addAttribute("msg", "계좌 인증 필수!");
//			model.addAttribute("targetURL", "BankMain");
//			return "result/fail";
//		}
		// => @BankTokenCheck 어노테이션으로 처리
		
		// BankService - getBankUserInfo() 메서드 호출하여 핀테크 사용자 정보 조회
		// => 파라미터 : BankToken 객체(user_seq_no 와 access_token 필요)   
		//    리턴타입 : Map<String, Object>(bankUserInfo)
		Map<String, Object> bankUserInfo = service.getBankUserInfo(token);
		log.info(">>>>> 핀테크 사용자 정보 : " + bankUserInfo);
		
		// ----------------------------------------------------------------------
		// API 응답코드(rsp_code)가 "A0000" 이 아닐 경우 요청 처리 실패이므로
		// (단, 이체과정에서는 "A0001"(이체처리중) 이 응답으로 전송될 수 있음)
		// API 응답메세지(rsp_message) 를 Model 객체에 저장 후 fail.jsp 포워딩
		if(!bankUserInfo.get("rsp_code").equals("A0000")) {
			model.addAttribute("msg", bankUserInfo.get("rsp_message"));
			return "result/fail";
		}
		// ----------------------------------------------------------------------
		
		// 조회 결과를 Model 에 저장
		model.addAttribute("bankUserInfo", bankUserInfo);
		
		// 사용자 정보 조회 뷰페이지(bank/bank_user_info.jsp) 포워딩
		return "bank/bank_user_info";
	}
	// -----------------------------------------------------------------------
	// 2.3. 계좌조회 서비스(사용자) - 2.3.1. 잔액조회 API (GET)
	// https://testapi.openbanking.or.kr/v2.0/account/balance/fin_num
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck // 엑세스토큰 존재 여부 체크하는 사용자 어노테이션
	@PostMapping("BankAccountDetail")
	public String bankAccountDetail(@RequestParam Map<String, Object> map, HttpSession session, Model model) {
		log.info(" >>>>>> 계좌 잔액조회 요청 파라미터 : " + map);
		
		// 엑세스토큰 정보가 저장된 BankToken 객체를 세션에서 꺼내기
		BankToken token = (BankToken)session.getAttribute("token");
		// => @BankTokenCheck 어노테이션으로 사전 검사 완료됐기 때문에 별도의 체크 불필요
		// ------------------------------------------------------------------------------
		// 파라미터가 저장된 Map 객체에 토큰 정보 저장
		map.put("token", token); // Map<String, Object> 필요
		
		// BankService - getAccountDetail() 메서드 호출하여 핀테크 계좌 잔액조회 요청
		// => 파라미터 : Map 객체   
		//    리턴타입 : Map<String, String> 또는 Map<String, Object> 사용(accountDetail)
		Map<String, String> accountDetail = service.getAccountDetail(map);
		
		// ----------------------------------------------------------------------
		// API 응답코드(rsp_code)가 "A0000" 이 아닐 경우 요청 처리 실패이므로
		// (단, 이체과정에서는 "A0001"(이체처리중) 이 응답으로 전송될 수 있음)
		// API 응답메세지(rsp_message) 를 Model 객체에 저장 후 fail.jsp 포워딩
		if(!accountDetail.get("rsp_code").equals("A0000")) {
			model.addAttribute("msg", accountDetail.get("rsp_message"));
			return "result/fail";
		}
		// ----------------------------------------------------------------------
		// Model 객체에 응답데이터가 저장된 Map 객체(accountDetail)와
		// 요청 파라미터로 전달받은(Map 객체 - map) 예금주명, 계좌번호(마스킹) 파라미터로 저장
		model.addAttribute("accountDetail", accountDetail);
		model.addAttribute("account_holder_name", map.get("account_holder_name"));
		model.addAttribute("account_num_masked", map.get("account_num_masked"));
		
		return "bank/bank_account_detail";
	}
	// -----------------------------------------------------------------------------------------
	// 사용자 조회 화면에서 계좌 목록 중 대표계좌로설정 버튼 클릭 시 해당 정보를 DB 에 등록 요청
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck
	@PostMapping("BankRegistRepresentAccount")
	public String registRepresentAccount(@RequestParam Map<String, Object> map, HttpSession session, Model model) {
		// 엑세스토큰 정보가 저장된 BankToken 객체를 세션에서 꺼내기
		BankToken token = (BankToken)session.getAttribute("token");
		// => @BankTokenCheck 어노테이션으로 사전 검사 완료됐기 때문에 별도의 체크 불필요
		// ------------------------------------------------------------------------------
		// 파라미터가 저장된 Map 객체에 토큰 정보 저장
		map.put("token", token); // Map<String, Object> 필요
		
		log.info(">>>>>>>> 대표 계좌 정보 : " + map);
		// BankService - registRepresentAccount() 메서드 호출하여 대표계좌정보 DB 등록 요청
		// => 파라미터 : Map 객체   리턴타입 : int(count)
		int count = service.registRepresentAccount(map);
		
		if(count > 0) {
			return "redirect:/BankMain"; // 임시) 계좌관리 메인페이지로 이동
		} else {
			model.addAttribute("msg", "대표 계좌 등록 실패\\n관리자에게 문의 바랍니다.");
			return "result/fail";
		}
		
	}
	
	// =========================================================================================
	// 2.6. 계좌이체 서비스 - 2.6.1. 출금이체 API 서비스(POST)
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck
	@PostMapping("BankWithdraw")
	public String bankWithdraw(@RequestParam Map<String, Object> map, HttpSession session, Model model) {
		// 엑세스토큰 정보가 저장된 BankToken 객체를 세션에서 꺼내기
		BankToken token = (BankToken)session.getAttribute("token");
		// => @BankTokenCheck 어노테이션으로 사전 검사 완료됐기 때문에 별도의 체크 불필요
		// ------------------------------------------------------------------------------
		// 파라미터가 저장된 Map 객체에 토큰 정보 저장
		map.put("token", token); // Map<String, Object> 필요
		
		// 임시) 세션 아이디도 Map 객체에 저장
		map.put("id", session.getAttribute("sId"));
		
		log.info(">>>>>>>> 출금 이체 요청 파라미터 정보 : " + map);
		
		// BankService - requestWithdraw() 메서드 호출하여 출금이체 요청
		// => 파라미터 : Map 객체   리턴타입 : Map<String, String>(withdrawResult)
		Map<String, String> withdrawResult = service.requestWithdraw(map);
		log.info(">>>>>>>> 출금이체 요청 결과 : " + withdrawResult);
		
		// 출금이체가 성공했을 경우 "BankWithdrawResult" 페이지로 리다이렉트
		// 아니면, "출금 실패!" 처리(fail.jsp)
		if(!withdrawResult.get("rsp_code").equals("A0000") || !withdrawResult.get("bank_rsp_code").equals("000")) { // 출금 실패
			model.addAttribute("msg", "출금 실패! - " + withdrawResult.get("rsp_message"));
			return "result/fail";
		}
		
		// 사용자번호를 출금이체 결과 객체에 추가
		withdrawResult.put("user_seq_no", token.getUser_seq_no());
		// => 주의! 사용자번호 대신 BankToken 객체를 통째로 저장하려면 Map<String, Object> 필요
		
		// 임시) withdrawResult 객체의 api_tran_dtm 속성값(문자열)의 뒷자리 3자리(밀리초) 제거
//		withdrawResult.put(
//				"api_tran_dtm", 
//				withdrawResult.get("api_tran_dtm").substring(0, withdrawResult.get("api_tran_dtm").length() - 3));
		
		// 출금이체 성공 시 결과를 DB 에 저장
		// BankService - registWithdrawResult() 메서드 호출하여 DB 저장 요청
		// => 파라미터 : 출금이체 결과(withdrawResult)   리턴타입 : void
		service.registWithdrawResult(withdrawResult);
		
		// 출금이체 결과 정보 Map 객체 중 bank_tran_id 값을 Model 에 저장
		model.addAttribute("bank_tran_id", withdrawResult.get("bank_tran_id"));
		
		return "redirect:/BankWithdrawResult";
	}
	// ----------------------------------------
	// 출금이체 결과 출력 비즈니스 로직("BankWithdrawResult" - GET)
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck
	@GetMapping("BankWithdrawResult")
	public String bankWithdrawResult(String bank_tran_id, Model model) {
		// BankService - getWithdrawResult() 메서드 호출하여 출금이체 결과 조회
		// => 파라미터 : 거래고유번호(참가기관, bank_tran_id)   
		//    리턴타입 : Map<String, String>(withdrawResult)
		Map<String, String> withdrawResult = service.getWithdrawResult(bank_tran_id);
		
		model.addAttribute("withdrawResult", withdrawResult);
		
		return "bank/bank_withdraw_result";
	}
	// =========================================================================================
	// 2.6. 계좌이체 서비스 - 2.6.2. 입금이체 API 서비스(POST)
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck
	@PostMapping("BankDeposit")
	public String bankDeposit(@RequestParam Map<String, Object> map, HttpSession session, Model model) {
		// 엑세스토큰 정보가 저장된 BankToken 객체를 세션에서 꺼내기
		BankToken token = (BankToken)session.getAttribute("token");
		// => @BankTokenCheck 어노테이션으로 사전 검사 완료됐기 때문에 별도의 체크 불필요
		// ------------------------------------------------------------------------------
		// 파라미터가 저장된 Map 객체에 토큰 정보 저장
//		map.put("token", token); // 입금 과정에서 사용자의 엑세스토큰 정보는 불필요
		map.put("id", session.getAttribute("sId"));
		log.info(">>>>>>>> 입금 이체 요청 파라미터 정보 : " + map);
		
		// BankService - requestDeposit() 메서드 호출하여 입금이체 요청
		// => 파라미터 : Map 객체   리턴타입 : Map<String, Object>(depositResult)
		// => 리턴 데이터 중 List 객체 포함되므로 String, Object 타입 필요
		Map<String, Object> depositResult = service.requestDeposit(map);
		log.info(">>>>>>>> 입금이체 요청 결과 : " + depositResult);
		
		// 입금이체가 성공했을 경우 "BankDepositResult" 페이지로 리다이렉트
		// 아니면, "입금 실패!" 처리(fail.jsp)
//		if(!withdrawResult.get("rsp_code").equals("A0000") || !withdrawResult.get("bank_rsp_code").equals("000")) { // 출금 실패
//			model.addAttribute("msg", "출금 실패! - " + withdrawResult.get("rsp_message"));
//			return "result/fail";
//		}
		
		// 사용자번호를 출금이체 결과 객체에 추가
//		withdrawResult.put("user_seq_no", token.getUser_seq_no());
		// => 주의! 사용자번호 대신 BankToken 객체를 통째로 저장하려면 Map<String, Object> 필요
		
		// 임시) withdrawResult 객체의 api_tran_dtm 속성값(문자열)의 뒷자리 3자리(밀리초) 제거
//			withdrawResult.put(
//					"api_tran_dtm", 
//					withdrawResult.get("api_tran_dtm").substring(0, withdrawResult.get("api_tran_dtm").length() - 3));
		
		// 출금이체 성공 시 결과를 DB 에 저장
		// BankService - registWithdrawResult() 메서드 호출하여 DB 저장 요청
		// => 파라미터 : 출금이체 결과(withdrawResult)   리턴타입 : void
//		service.registWithdrawResult(withdrawResult);
		
		// 출금이체 결과 정보 Map 객체 중 bank_tran_id 값을 Model 에 저장
//		model.addAttribute("bank_tran_id", withdrawResult.get("bank_tran_id"));
		
		return "redirect:/BankDepositResult";
	}
	
	// ==========================================================================
	// 사용자간(P2P) 계좌이체(송금) = 출금이체 -> 입금이체 연속으로 요청
	@LoginCheck(memberRole = MemberRole.USER)
	@BankTokenCheck // 자신(송금하는 사람)의 엑세스토큰 검증
	@PostMapping("BankTransfer")
	public String bankTransfer(@RequestParam Map<String, Object> map, HttpSession session, Model model) {
		// 엑세스토큰 정보가 저장된 BankToken 객체를 세션에서 꺼내기
		BankToken senderToken = (BankToken)session.getAttribute("token");
		// => @BankTokenCheck 어노테이션으로 사전 검사 완료됐기 때문에 별도의 체크 불필요
		// ------------------------------------------------------------------------------
		// 이체에 필요한 사용자 계좌(입금받는 상대방) 관련 정보(토큰) 조회
		// BankService - getBankUserInfo() 메서드 재사용하여 사용자 계좌 관련 정보 조회 요청
		// => 파라미터 : 아이디(상대방)   리턴타입 : BankToken(receiverToken)
		BankToken receiverToken = service.getBankTokenInfo((String)map.get("receiver_id"));
		log.info(">>>>>>>> 상대방(입금받는사람) 토큰 정보 : " + receiverToken);
		log.info(">>>>>>>> 자신(송금하는사람) 토큰 정보 : " + senderToken);
		
		// 수신자(상대방)의 토큰이 존재하지 않을 경우(= 계좌 등록이 되어있지 않음 등)
		// "상대방 계좌 정보가 존재하지 않습니다!" 출력 처리(fail.jsp)
		if(receiverToken == null || receiverToken.getAccess_token() == null) {
			model.addAttribute("msg", "상대방 계좌 정보가 존재하지 않습니다!");
			return "result/fail";
		}
		
		// 각 사용자(송금인, 수신인) 의 토큰 정보를 Map 객체에 추가
		map.put("receiverToken", receiverToken);
		map.put("senderToken", senderToken);
		// => 각 토큰 정보에 아이디도 포함되어 있으므로 세션 아이디를 별도로 저장하는 작업 생략
		// ---------------------------------------------------------------------
		// 송금인과 수신인의 계좌 정보 조회
		// BankService - getAccountInfo() 메서드 호출하여 각각의 계좌 정보 조회
		// => 파라미터 : 사용자일련번호(user_seq_no) => BankToken 객체에 저장되어 있음
		// => 리턴타입 : Map<String, String>
		Map<String, String> senderAccount = service.getBankAccountInfo(senderToken.getUser_seq_no());
		Map<String, String> receiverAccount = service.getBankAccountInfo(receiverToken.getUser_seq_no());
		
		// 각 계좌정보도 Map 객체(map)에 추가
		map.put("senderAccount", senderAccount);
		map.put("receiverAccount", receiverAccount);
		
		log.info(">>>>>>>> 송금 요청 정보 : " + map);
		// =====================================================================
		// BankService - transfer() 메서드 호출하여 송금 작업 요청
		// => 파라미터 : Map 객체   리턴타입 : Map<String, Object>(transferResult)
		Map<String, Object> transferResult = service.transfer(map);
		
		// 송금 요청 결과에 따른 처리
		// 1) 출금이체결과 또는 입금이체결과 객체가 null 일 경우
		// 2) 출금이체결과의 "rsp_code" 값이 "A0000" 이 아닐 경우
		// 3) 입금이체결과의 "rsp_code" 값이 "A0000" 이 아닐 경우
		Map<String, String> withdrawResult = (Map<String, String>)transferResult.get("withdrawResult");
		Map<String, Object> depositResult = (Map<String, Object>)transferResult.get("depositResult");
		
		if(withdrawResult == null || depositResult == null) {
			model.addAttribute("msg", "송금 과정에서 시스템 오류 발생!\\n관리자에게 문의바랍니다.");
			return "result/fail";
		} else if(!withdrawResult.get("rsp_code").equals("A0000")) {
			model.addAttribute("msg", withdrawResult.get("rsp_message") + "(" + withdrawResult.get("bank_rsp_message") + ")");
			return "result/fail";
		} else if(!depositResult.get("rsp_code").equals("A0000")) {
			model.addAttribute("msg", depositResult.get("rsp_message") + "(" + depositResult.get("bank_rsp_message") + ")");
			return "result/fail";
		}
		
		log.info(">>>>> 이체결과 : " + transferResult);
		
		// DB 저장과정 생략
		// => 세션에 이체결과 객체 담아서 리다이렉트
		session.setAttribute("transferResult", transferResult);
		
		return "redirect:/BankTransferResult";
	}
	
	// ---------------------------------
	// P2P 송금(이체) 결과 뷰페이지 처리
	@GetMapping("BankTransferResult")
	public String bankTransferResult(HttpSession session, Model model) {
		// 세션에 저장된 이체 결과 객체(transferResult) 꺼내기
		Map<String, Object> transferResult = (Map<String, Object>)session.getAttribute("transferResult");
		
		// 세션에서 객체 꺼낸 후 세션 내의 객체는 제거
		session.removeAttribute("transferResult");
		
		// 이체 결과 객체를 Model 객체에 저장
		model.addAttribute("transferResult", transferResult);
		
		return "bank/bank_transfer_result";
	}
	 
	
	
}

















