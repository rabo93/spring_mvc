package com.itwillbs.mvc_board.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.itwillbs.mvc_board.aop.LoginCheck;
import com.itwillbs.mvc_board.aop.LoginCheck.MemberRole;
import com.itwillbs.mvc_board.service.BankService;
import com.itwillbs.mvc_board.vo.BankToken;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class AdminController {
	@Autowired
	private BankService bankService;
	
//	@GetMapping("AdminMain")
//	public String main() {
//		
//	}
	
	// 세션 체크 시 관리자용 세션 체크로 동작하도록 MemberRole 을 ADMIN 으로 전달
	@LoginCheck(memberRole = MemberRole.ADMIN)
	@GetMapping("AdminBankRequestToken")
	public String adminBankRequestToken(Map<String, Object> map, HttpSession session, Model model) {
		// BankService - getAdminAccessToken() 메서드 호출하여 이용기관 엑세스토큰 발급 요청
		// => 파라미터 : 없음   리턴타입 : BankToken(adminToken)
		// => 주의! 이용기관 당 하나의 엑세스토큰만 유효하므로 실제로 한 명만 발급 요청해야함
		BankToken adminToken = bankService.getAdminAccessToken();
		log.info(">>>>>>> adminToken : " + adminToken);
		
		// user_seq_no 와 refresh_token 값이 존재하지 않으므로 널스트링("") 으로 설정
		adminToken.setUser_seq_no("");
		adminToken.setRefresh_token("");
		
		// Map 객체에 세션 아이디(id)와 토큰 정보(BankToken 객체) 저장
		map.put("id", session.getAttribute("sId"));
		map.put("token", adminToken);
		
		// BankService - registAccessToken() 메서드 호출하여 관리자 토큰 관련 정보 저장(재사용)
		bankService.registAccessToken(map);
		
		model.addAttribute("msg", "관리자 토큰 발급 성공!");
		model.addAttribute("targetURL", "BankMain");
		return "result/success";
	}
	
	
}













