package com.itwillbs.mvc_board.controller;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.itwillbs.mvc_board.handler.RsaKeyGenerateor;
import com.itwillbs.mvc_board.service.BankService;
import com.itwillbs.mvc_board.service.MailService;
import com.itwillbs.mvc_board.service.MemberService;
import com.itwillbs.mvc_board.vo.BankToken;
import com.itwillbs.mvc_board.vo.MailAuthInfo;
import com.itwillbs.mvc_board.vo.MemberVO;

@Controller
public class MemberController {
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private BankService bankService;	
	// =======================================================
	// "MemberJoin" 매핑 - GET
	// => 회원가입 폼 페이지 포워딩
	@GetMapping("MemberJoin")
	public String joinForm(HttpSession session, Model model) {
		// RSA 알고리즘을 사용항 공개키/개인키 생성
		Map<String, String> rsaKey = RsaKeyGenerateor.generateKey();
//		System.out.println("공개키 : " + rsaKey.get("publicKey"));
//		System.out.println("개인키 : " + rsaKey.get("privateKey"));
		
		// 개인키/공개키 셋은 세션에 저장해 두고(현재 실제로는 개인키만 저장해도 무관함)
		// 공개키는 클라이언트측에 전송할 수 있도록 Model 객체에 추가
		session.setAttribute("rsaKey", rsaKey);
		model.addAttribute("publicKey", rsaKey.get("publicKey"));
		
		return "member/member_join_form";
	}
	
	// "MemberJoin" 매핑 - POST
	// => 회원가입 비즈니스 로직 처리
	// => 전송되는 폼 파라미터 데이터는 MemberVO 타입 객체에 저장하도록 파라미터 타입 MemberVO 선언
	@PostMapping("MemberJoin")
	public String join(MemberVO member, 
			HttpSession session, Model model, BCryptPasswordEncoder passwordEncoder,
			String encryptedData) { // 암호화 된 회원가입 정보 JSON 문자열 전달받기
//		System.out.println(member);
		// ==========================================================================
		// 회원 가입 정보를 JSON 문자열로 묶어 암호화하여 전송했을 때 복호화 작업
//		System.out.println("encryptedData : " + encryptedData);
//		
//		// 개인키 가져오기
//		Map<String, String> rsaKey = (Map<String, String>)session.getAttribute("rsaKey");
//		// 암호문 복호화
//		String decryptedData = RsaKeyGenerateor.decrypt(rsaKey.get("privateKey"), encryptedData);
//		
//		// Gson 객체 활용하여 JSON 데이터 -> MemberVO 타입 객체로 변환
//		Gson gson = new Gson();
//		member = gson.fromJson(decryptedData, MemberVO.class);
//		
//		System.out.println("복호화 된 MemberVO 객체 : " + member);
		// ==========================================================================
		/*
		 * [ BCryptPasswordEncoder 클래스를 활용한 패스워드 단방향 암호화 ]
		 * - org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder 클래스 활용
		 *   => spring-security-web 또는 spring-security-crypto 라이브러리 필요
		 */
		// 1. BCryptPasswordEncoder 클래스 인스턴스 생성(기본 생성자 활용) - DI 활용
//		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		// 2. BCryptPasswordEncoder 객체의 encode() 메서드 호출하여
		//    평문(원본) 패스워드에 대한 해싱 수행 후 결과값을 문자열로 리턴받아 저장
		//    => 파라미터 : 평문 암호(MemberVO 객체의 passwd 멤버변수값)
		//    => 리턴타입 : String(암호문)
		String securePasswd = passwordEncoder.encode(member.getPasswd());
		System.out.println("평문 : " + member.getPasswd()); // 1234
		System.out.println("암호문 : " + securePasswd); // $2a$10$vRPGUxAa.oZpPcHqYnvtK.v2KiaRssBLCvy.LODUzqIAoHajXPeqm
		// => 단, 매번 생성되는 암호문은 솔팅에 의해(Salt 값에 의해) 항상 달라진다!
		
		// 3. 암호화 된 패스워드를 다시 MemberVO 객체의 passwd 값에 저장(덮어쓰기)
		member.setPasswd(securePasswd);
		// -----------------------------------------------------------------
		// MemberService - registMember() 메서드 호출하여 회원가입 작업 요청
		// => 파라미터 : MemberVO 객체   리턴타입 : int(insertCount)
		int insertCount = memberService.registMember(member);
		// -----------------------------------------------------------------
		// 회원가입 결과 판별하여 페이징 처리
		// 성공 시 : MemberJoinSuccess 서블릿 주소 리다이렉트
		// 실패 시 : result/fail.jsp 페이지 포워딩("msg" 속성값으로 "회원가입 실패!" 전달)
		if(insertCount > 0) {
			// --------------------- 인증 메일 발송 작업 추가 -----------------------
			// MailService - sendAuthMail() 메서드 호출하여 인증메일 발송 요청
			// => 파라미터 : MemberVO 객체   리턴타입 : MailAuthInfo(mailAuthInfo)
			MailAuthInfo mailAuthInfo = mailService.sendAuthMail(member);
			System.out.println("인증 정보 : " + mailAuthInfo);
			// ----------------------------------------------------------------------
			// MemberService - registMailAuthInfo() 메서드 호출하여 인증 정보 등록 요청
			// => 파라미터 : MailAuthInfo 객체   리턴타입 : void
			memberService.registMailAuthInfo(mailAuthInfo);
			// ----------------------------------------------------------------------
			return "redirect:/MemberJoinSuccess";
		} else {
			model.addAttribute("msg", "회원가입 실패!");
			return "result/fail";
		}
		
	}
	
	// "MemberJoinSuccess" 매핑 - GET
	// => 회원가입 완료 뷰페이지 포워딩
	@GetMapping("MemberJoinSuccess")
	public String joinSuccess() {
		return "member/member_join_success";
	}
	// --------------------------------------------------------------------
	// 회원 아이디 중복 체크 폼
	// "MemberCheckIdForm" 매핑 - GET
//	@GetMapping("MemberCheckIdForm")
//	public String memberCheckIdForm() {
//		return "member/member_check_id";
//	}
	
	// 회원 아이디 중복 체크 비즈니스 로직
	// "MemberCheckId" 매핑 - GET
//	@GetMapping("MemberCheckId")
//	public String memberCheckId(MemberVO member, Model model) {
//		String id = member.getId();
//		System.out.println("입력받은 아이디 : " + id);
//		
//		// MemberService - getMember() 메서드 호출하여 아이디에 대한 회원 정보 조회(재사용)
//		member = memberService.getMember(member);
////		System.out.println("조회 결과 : " + member);
//		
//		// 아이디 조회 결과를 알려주기 위한 변수 선언
//		boolean isDuplicate = false;
//		
//		// 아이디 조회 결과(존재 여부) 판별
//		if(member != null) { // 조회 결과가 있을 경우(= 아이디 중복)
//			isDuplicate = true;
//		}
//		
//		// 리다이렉트 시에도 Model 객체에 속성 저장 시 자동으로 URL 파라미터 형식으로 변환됨
//		model.addAttribute("id", id);
//		// => 주의! 조회 결과가 없을 경우 MemberVO 객체가 null 이므로 별도의 변수에 아이디 저장 필요
//		model.addAttribute("isDuplicate", isDuplicate);
//		
//		// MemberCheckIdForm 서블릿 주소 리다이렉트
//		return "redirect:/MemberCheckIdForm";
//		// http://localhost:8081/mvc_board/MemberCheckIdForm?id=admin&isDuplicate=true
//		// http://localhost:8081/mvc_board/MemberCheckIdForm?id=abcde&isDuplicate=false
//	}
	// --------------------------------------------------------
	// 위의 비즈니스 로직을 AJAX 로 요청받았을 경우
	// 응답 페이지를 응답하지 않고 작업 결과를 직접 응답 데이터로 생성하여 응답해야할 경우
	// (ex. 처리 결과를 true/false 등의 데이터만 전송해야할 경우)
	// 매핑 메서드에 @ResponseBody 어노테이션을 지정 후
	// return 문 뒤에 실제 전송할 응답 데이터를 지정하면 해당 데이터가 그대로 body 에 담겨 전송됨
	// => 즉, DispatcherServlet 이 동작하지 않고 직접 응답을 수행하게된다!
	// => 기본적으로 응답 데이터를 문자열로 리턴하기 위해 그대로 리턴타입 String 사용
	@ResponseBody
	@GetMapping("MemberCheckId")
	public String memberCheckId(MemberVO member, Model model) {
		// MemberService - getMember() 메서드 호출하여 아이디에 대한 회원 정보 조회(재사용)
		member = memberService.getMember(member);
//		System.out.println("조회 결과 : " + member);
		
		// 아이디 조회 결과를 알려주기 위한 변수 선언
		boolean isDuplicate = false;
		
		// 아이디 조회 결과(존재 여부) 판별
		if(member != null) { // 조회 결과가 있을 경우(= 아이디 중복)
			isDuplicate = true;
		}
		
		System.out.println("isDuplicate : " + isDuplicate);
		
		// 판별 결과 리턴(문자열로 변환)
		return isDuplicate + "";
	}
	
	// ====================================================================
	// "MemberLogin" 매핑 - GET
	// => 로그인 폼 뷰페이지 포워딩
//	@GetMapping("MemberLogin")
//	public String memberLoginForm() {
//		return "member/member_login_form";
//	}
	
	// 로그인 폼 요청 시 공개키 전송하는 기능 추가
	@GetMapping("MemberLogin")
	public String memberLoginForm(HttpSession session, Model model) {
		// RSA 알고리즘을 사용항 공개키/개인키 생성
		Map<String, String> rsaKey = RsaKeyGenerateor.generateKey();
//		System.out.println("공개키 : " + rsaKey.get("publicKey"));
//		System.out.println("개인키 : " + rsaKey.get("privateKey"));
		
		// 개인키/공개키 셋은 세션에 저장해 두고(현재 실제로는 개인키만 저장해도 무관함)
		// 공개키는 클라이언트측에 전송할 수 있도록 Model 객체에 추가
		session.setAttribute("rsaKey", rsaKey);
		model.addAttribute("publicKey", rsaKey.get("publicKey"));
		
		return "member/member_login_form";
	}
	
	// "MemberLogin" 매핑 - POST
	// => 로그인 비즈니스 로직 처리
	// => 로그인 폼 파라미터 저장용 MemberVO, 아이디 기억하기 값을 저장할 파라미터 rememberId
	//    암호화 처리를 위한 BCryptPasswordEncoder 타입 파라미터,
	//    세션 정보를 관리하는 HttpSession, 응답 페이지로 데이터 전송할 Model 타입 파라미터 필요
	// 임시) 저장된 쿠키값을 컨트롤러에서 접근하려면 
	//       1) request.getCookies() 메서드를 호출하여 Cookie[] 타입 리턴받아 쿠키 처리하기
	//       2) @CookieValue 어노테이션을 사용하여 쿠키값 바로 변수에 저장하기
	//          => @CookieValue(value = "쿠키명", required = 필수여부(true/false)) 데이터타입 변수명
	//          => 쿠키명과 변수명이 같으면 value 속성 생략, 다르면 value 속성 필수
	//          => required 생략 또는 true 지정 시 해당 쿠키 없으면 오류 발생!
	@PostMapping("MemberLogin")
	public String memberLogin(MemberVO member, String rememberId, 
			@CookieValue(value = "userId", required = false) String userId2,
			String encryptedData, // 아이디&패스워드를 JSON 으로 묶어서 암호화했을 경우
			BCryptPasswordEncoder passwordEncoder, HttpSession session, Model model,
			HttpServletResponse response) {
//		System.out.println("저장된 userId2 쿠키값 : " + userId2);
		// -------------------------------------------------------------------
//		System.out.println("암호화 된 아이디 : " + member.getId());
//		System.out.println("암호화 된 패스워드 : " + member.getPasswd());
//		System.out.println("암호화 데이터 : " + encryptedData);
		
		// 암호화 된 데이터 복호화
		Map<String, String> rsaKey = (Map<String, String>)session.getAttribute("rsaKey");
		// RsaKeyGenerator.decrypt() 메서드 호출하여 복호화 후 결과값 리턴받기
		// => 파라미터 : 개인키, 암호문   리턴타입 : String
//		String decryptedData = RsaKeyGenerateor.decrypt(rsaKey.get("privateKey"), member.getId());
//		System.out.println("복호화 된 아이디 : " + decryptedData);
//		
//		decryptedData = RsaKeyGenerateor.decrypt(rsaKey.get("privateKey"), member.getPasswd());
//		System.out.println("복호화 된 패스워드 : " + decryptedData);

		// id 와 passwd 값을 별도로 전송하여 복호화했을 경우 MemberVO 객체에 복호화 된 값 저장
//		member.setId(RsaKeyGenerateor.decrypt(rsaKey.get("privateKey"), member.getId()));
//		member.setPasswd(RsaKeyGenerateor.decrypt(rsaKey.get("privateKey"), member.getPasswd()));
		
		// id 와 passwd 값을 JSON 으로 묶어서 전송했을 경우
		String decryptedData = RsaKeyGenerateor.decrypt(rsaKey.get("privateKey"), encryptedData);
		System.out.println("복호화 된 데이터 : " + decryptedData);
		// JSONObject 또는 Gson 의 JsonObject 등을 활용하여 파싱을 통해 id 와 passwd 추출
		JSONObject jo = new JSONObject(decryptedData); // JSON 문자열 파싱
		// 파싱된 각각의 아이디와 패스워드를 MemberVO 객체에 저장
		member.setId(jo.getString("id"));
		member.setPasswd(jo.getString("passwd"));
		
		// -------------------------------------------------------------------
		// [ BCryptPasswordEncoder 클래스를 활용하여 암호화 된 패스워드 비교 작업을 위한 패스워드 조회 ]
		// MemberService - getMember() 메서드 호출하여 회원 상세정보 조회 요청(회원 상세정보 재사용)
		// => 파라미터 : MemberVO 객체(아이디 필요)   리턴타입 : MemberVO(dbMember)
		// => 기존 MemberVO 객체에 덮어써도 되지만, 기존 패스워드 유지 위해 별도의 변수 사용
		MemberVO dbMember = memberService.getMember(member);
		// ------------------------------------------------------------------- 또는
		// MemberService - getMemberPasswd() 메서드 호출하여 회원 아이디에 대한 패스워드 조회
		// => 파라미터 : 아이디(String)   리턴타입 : String(dbPasswd)
//		String dbPasswd = memberService.getMemberPasswd(member.getId());
//		System.out.println("DB 에 저장된 패스워드 : " + dbPasswd);
//		System.out.println("입력받은 패스워드 : " + member.getPasswd());
		/*
		 * [ BCryptPasswordEncoder 객체를 활용한 패스워드 비교 ]
		 * - 입력받은 패스워드(= 평문)와 DB에 저장된 패스워드(= 암호문) 간의 
		 *   직접적인 문자열 비교 시 무조건 두 문자열은 다름
		 * - 일반적인 해싱의 경우 새 패스워드도 해싱을 통해 암호문으로 변환하여 비교하면 되지만
		 *   현재, BCryptPasswordEncoder 객체를 통해 기존 패스워드를 암호화했기 때문에
		 *   솔팅값에 의해 두 암호는 서로 다른 문자열이 되어 
		 *   DB 에서 WHERE 절로 두 패스워드 비교 또는 String 클래스의 equals() 로 비교가 불가능하다!
		 * - BCryptPasswordEncoder 객체의 matches() 메서드를 활용하여 비교 필수!
		 *   (내부적으로 암호문으로부터 솔팅값을 추출하여 평문을 암호화하여 비교)
		 * 
		 * < 기본 문법 >
		 * 객체명.matches(평문, 암호문) 호출 시 boolean 타입 결과 리턴
		 * ------------------------------------------------------------------------
		 * 검색 아이디가 존재하지 않을 경우 리턴되는 결과값 : null
		 * => null 값일 경우(= 아이디 없음) 또는 패스워드가 일치하지 않을 경우
		 *    "result/fail.jsp" 페이지 포워딩(전달 메세지 : "로그인 실패!")
		 */
		// 패스워드만 조회했을 경우
//		if(dbPasswd == null || !passwordEncoder.matches(member.getPasswd(), dbPasswd)) { // 로그인 실패
//		}
		
		// 회원 상세정보를 조회했을 경우
		// 1) 로그인 성공/실패 여부 판별
		// 2) 회원 상태(휴면(생략)/탈퇴) 판별
		if(dbMember == null || !passwordEncoder.matches(member.getPasswd(), dbMember.getPasswd())) { // 로그인 실패
			model.addAttribute("msg", "로그인 실패!");
			return "result/fail";
		} else if(dbMember.getMember_status() == 3) { // 로그인 성공이지만, 탈퇴 회원일 경우
			model.addAttribute("msg", "탈퇴한 회원입니다!");
			return "result/fail";
		} else if(dbMember.getMail_auth_status().equals("N")) { // 이메일 미인증 회원일 경우
			model.addAttribute("msg", "이메일 인증 후 로그인 가능합니다!");
			return "result/fail";
		} else { // 로그인 성공
			// 로그인 성공한 아이디를 세션에 저장(속성명 "sId")
			session.setAttribute("sId", member.getId());
			// 세션 타이머 1시간으로 변경(클라이언트로부터 1시간동안 요청이 없을 경우 세션 제거됨)
//			session.setMaxInactiveInterval(60 * 60); // 60초 * 60분 = 1시간
			// ------------------------------------------------------------------------------
			// [ 핀테크 엑세스토큰 정보 조회하여 세션에 저장하는 기능 추가 ]
			// BankService - getBankUserInfo() 메서드 호출하여 핀테크 사용자 정보 조회(DB 의 엑세스토큰 조회 목적)
			// => 파라미터 : 아이디   리턴타입 : BankToken(token)
			BankToken token = bankService.getBankTokenInfo(member.getId());
			
			// 조회 결과를 세션에 저장
			session.setAttribute("token", token);
			// ------------------------------------------------------------------------------
			// [ 로그인 폼에서 "아이디 기억" 항목 체크박스에 대한 쿠키 처리 ]
//			System.out.println("아이디 기억하기 여부 : " + rememberId);
			// 파라미터로 전달받은 rememberId 값 null 여부 체크
//			if(rememberId != null) { // 변수값이 null 이 아닐 때(= 아이디 기억하기 체크박스 체크)
//				// javax.servlet.http.Cookie 타입 객체 생성
//				// => 파라미터 : 쿠키명("rememberId"), 쿠키값(로그인에 성공한 아이디)
//				Cookie cookie = new Cookie("rememberId", member.getId());
//				
//				// 쿠키 유효기간 설정(초 단위)
//				cookie.setMaxAge(60 * 60 * 24 * 30); // 30일(= 60초 * 60분 * 24시간 * 30일)
//				
//				// 클라이언트측으로 쿠키 정보를 전송하기 위해서
//				// 응답 정보를 관리하는 HttpServletResponse 객체의 addCookie() 메서드를 호출하여
//				// 응답 정보에 쿠키 추가
//				response.addCookie(cookie);
//			} else { // 변수값이 null 이 아닐 때(= 아이디 기억하기 체크박스 미체크)
//				// 기존 쿠키에 아이디 저장 여부와 관계없이 무조건 rememberId 라는 쿠키 삭제
//				// -----------------
//				// Cookie 객체 생성
//				// => 이 때, 쿠키명은 반드시 삭제할 쿠키의 이름을 정확히 입력하고 값은 무관
////				Cookie cookie = new Cookie("rememberId", member.getId());
//				Cookie cookie = new Cookie("rememberId", null); // null 값 전달해도 됨
//				
//				// 쿠키의 유효기간을 0 으로 설정(클라이언트가 수신하는 즉시 해당 쿠키 삭제)
//				cookie.setMaxAge(0);
//				
//				// 응답 데이터에 쿠키 포함시키기
//				response.addCookie(cookie);
//			}
			// ---------- 쿠키 관리 코드 중복 제거 ------------
			// 1. javax.servlet.http.Cookie 타입 객체 생성
			// => 쿠키값으로 로그인 성공한 아이디 전달
			Cookie cookie = new Cookie("userId", member.getId());
			
			// 2. 쿠키 만료 기간 설정
			// => 저장 시 30일로 설정, 삭제 시 0 으로 설정
			if(rememberId != null) {
				cookie.setMaxAge(60 * 60 * 24 * 30);
			} else {
				cookie.setMaxAge(0);
			}
			
			// 3. 응답 정보에 쿠키 추가
			response.addCookie(cookie);
			// ------------------------------------------------------------------------------
			// [ 특정 페이지 로그인 필수 처리를 위한 로그인 완료 시 원래 페이지로 이동 처리 ]
			// - 세션 객체에 저장된 "prevURL" 속성이 null 이 아닐 경우 해당 주소로 리다이렉트 하고
			//   null 일 경우(로그인 링크 눌렀을 때) 기존과 동일하게 메인페이지로 리다이렉트
//			System.out.println("prevURL : " + session.getAttribute("prevURL"));
			if(session.getAttribute("prevURL") == null) {
				return "redirect:/"; // 메인페이지로 리다이렉트
			} else {
				// request.getServletPath() 메서드를 통해 이전 요청 URL 을 저장할 경우
				// "/요청URL" 형식으로 저장되므로 redirect:/ 에서 / 제외하고 결합하여 사용
				return "redirect:" + session.getAttribute("prevURL");
			}
			
		}
		
	} // 로그인 처리 끝
	// ---------------------------------------------------------------------
	// 로그아웃("MemberLogout" - GET)
	@GetMapping("MemberLogout")
	public String logout(HttpSession session) {
		// 세션 초기화
		session.invalidate();
		
		// 메인페이지로 리다이렉트
		return "redirect:/";
	}
	// ======================================================================
	// "ReSendAuthMail" 매핑 - GET
	// => 인증메일 재발송 폼 뷰페이지 포워딩
	@GetMapping("ReSendAuthMail")
	public String reSendAuthMailForm() {
		return "member/resend_auth_mail_form";
	}
	
	// "ReSendAuthMail" 매핑 - POST
	// => 인증메일 재발송 비즈니스 로직 처리
	@PostMapping("ReSendAuthMail")
	public String reSendAuthMail(MemberVO member, Model model) {
//		System.out.println("member : " + member);
		
		// 아이디, 이메일 일치 여부 확인
		// MemberService - getMember() 메서드 재사용하여 회원 상세정보 조회
		MemberVO dbMember = memberService.getMember(member);
//		System.out.println("dbMember : " + dbMember);
		
		if(dbMember == null) { // 아이디 없음
			model.addAttribute("msg", "존재하지 않는 아이디!");
			return "result/fail";
		} else if(!member.getEmail().equals(dbMember.getEmail())) { // 이메일 불일치
			model.addAttribute("msg", "존재하지 않는 메일주소!");
			return "result/fail";
		} else if(dbMember.getMail_auth_status().equals("Y")) { // 이미 메일 인증 완료한 회원
			model.addAttribute("msg", "이미 인증을 완료한 회원입니다!");
			return "result/fail";
		}
		// ---------------------------------------
		// MailService - sendAuthMail() 메서드 호출하여 인증 메일 발송 요청(재사용)
		MailAuthInfo mailAuthInfo = mailService.sendAuthMail(member);
		
		// MemberSerivce - registMailAuthInfo() 메서드 호출하여 인증 정보 등록 요청(재사용)
		memberService.registMailAuthInfo(mailAuthInfo);
		
		model.addAttribute("msg", "인증 메일 발송 성공!\\n인증 메일을 확인해 주세요.\\n로그인 페이지로 이동합니다.");
		model.addAttribute("targetURL", "MemberLogin");
		return "result/success";
	}
	
	// 이메일 인증("MemberEmailAuth" - GET)
	@GetMapping("MemberEmailAuth")
	public String emailAuth(MailAuthInfo mailAuthInfo, Model model) {
		System.out.println("mailAuthInfo : " + mailAuthInfo);
		
		// MemberService - requestEmailAuth() 메서드 호출하여 이메일 인증 처리 요청
		// => 파라미터 : MailAuthInfo 객체   리턴타입 : boolean(isAuthSuccess)
		boolean isAuthSuccess = memberService.requestEmailAuth(mailAuthInfo);
		
		// 인증 처리 결과 판별
		if(!isAuthSuccess) { // 인증 실패
			model.addAttribute("msg", "메일 인증 실패!");
			return "result/fail";
		} else { // 인증 성공
			model.addAttribute("msg", "메일 인증 성공!\\n로그인 페이지로 이동합니다.");
			model.addAttribute("targetURL", "MemberLogin");
			return "result/success";
		}
		
	}
	
	
	// ======================================================================
	// 회원 상세정보 조회("MemberInfo" - GET)
	@GetMapping("MemberInfo")
	public String memberInfo(MemberVO member, HttpSession session, Model model, HttpServletRequest request) {
		// 세션 아이디 체크하여 세션 아이디가 없을 경우 "result/fail" 페이지 포워딩 처리
		// => "msg" 속성값 : "로그인 필수!"
		// => "targetURL" 속성값 : "MemberLogin" (로그인페이지 URL)
		String id = (String)session.getAttribute("sId");
		if(id == null) {
			model.addAttribute("msg", "로그인 필수!\\n로그인 페이지로 이동합니다.");
			model.addAttribute("targetURL", "MemberLogin");
			
			// ----------------------------------------------------------------
			// 로그인 완료 후 다시 회원 상세정보 조회 페이지로 이동할 수 있도록
			// 세션 객체에 회원 상세정보 조회 페이지의 서블릿 주소를 저장 후
			// 로그인 완료 시 해당 주소로 리다이렉트 수행할 수 있다!
//			session.setAttribute("prevURL", "MemberInfo");
			// => 경로를 직접 입력하지 않고 request 객체의 getServletPath() 메서드로 서블릿 주소 추출 가능
			String prevURL = request.getServletPath();
			String queryString = request.getQueryString(); // URL 파라미터 가져오기(없으면 null)
			
			// URL 파라미터(쿼리)가 null 이 아닐 경우 prevURL 에 결합(? 포함)
			if(queryString != null) {
				prevURL += "?" + queryString;
			}
			
			// 세션 객체에 prevURL 값 저장
			session.setAttribute("prevURL", prevURL);
			// ----------------------------------------------------------------
			
			return "result/fail";
		}
		// -------------------------------------------------------------------------
		// MemberService - getMember() 메서드 호출하여 회원 상세정보 조회
		// => 파라미터 : MemberVO 객체(아이디만 전달해도 무관함)   리턴타입 : MemberVO
		// => 이 때, 아이디 파라미터가 없으므로 세션 아이디 값을 MemberVO 객체에 저장
		// => 조회 결과 저장 시 새로운 MemberVO 타입 변수 선언 대신 기존 변수 member 재사용 가능
		//    (기존 MemberVO 객체와 별개로 구별해야할 정보가 없기 때문에 덮어써도 무관함)
		member.setId(id);
		member = memberService.getMember(member);
		
		// Model 객체에 MemberVO 객체 저장 후 member/member_info.jsp 페이지 포워딩
		model.addAttribute("member", member);
		
		return "member/member_info";
	}
	// ==================================================================================
	// 회원 정보 수정("MemberModify" - POST)
	// => 회원 정보 수정 폼 없이 회원 상세정보 페이지에서 바로 수정 페이지 요청함
	@PostMapping("MemberModify")
//	public String modify(MemberVO member, String oldPasswd, HttpSession session, Model model) {
//	public String modify(@RequestParam Map<String, String> map, MemberVO member, 
	public String modify(@RequestParam Map<String, String> map, String[] hobby,
			BCryptPasswordEncoder passwordEncoder, HttpSession session, Model model, HttpServletRequest request) {
		// 만약, 파라미터 매핑용 매개변수를 Map 타입으로 선언 시 @RequestParam 필수!
		// 만약, Map 타입 파라미터와 MemberVO 타입 파라미터를 동시에 선언 시 두 객체 모두 파라미터가 저장됨
		// => 단, Map 객체에는 기존 패스워드(oldPasswd) 파라미터도 저장됨(MemberVO 에는 없음)
		System.out.println("map : " + map);
//		System.out.println("member : " + member);
		// ----------------------------------------------------------------------------
		String id = (String)session.getAttribute("sId");
		if(id == null) {
			model.addAttribute("msg", "로그인 필수!\\n로그인 페이지로 이동합니다.");
			model.addAttribute("targetURL", "MemberLogin");
			
			// ----------------------------------------------------------------
			// 로그인 완료 후 다시 회원 상세정보 조회 페이지로 이동할 수 있도록
			// 세션 객체에 회원 상세정보 조회 페이지의 서블릿 주소를 저장 후
			// 로그인 완료 시 해당 주소로 리다이렉트 수행할 수 있다!
//			session.setAttribute("prevURL", "MemberInfo");
			// => 경로를 직접 입력하지 않고 request 객체의 getServletPath() 메서드로 서블릿 주소 추출 가능
			String prevURL = request.getServletPath();
			String queryString = request.getQueryString(); // URL 파라미터 가져오기(없으면 null)
			
			// URL 파라미터(쿼리)가 null 이 아닐 경우 prevURL 에 결합(? 포함)
			if(queryString != null) {
				prevURL += "?" + queryString;
			}
			
			// 세션 객체에 prevURL 값 저장
			session.setAttribute("prevURL", prevURL);
			// ----------------------------------------------------------------
			
			return "result/fail";
		}
		// --------------------------------------------------------------------------
		// Map 객체에 세션 아이디 추가(키 : id)
		map.put("id", id);
		
		// MemberService - getMemberPasswd() 메서드 재사용하여 암호화 된 패스워드 조회
		// => 파라미터 : 아이디(String)   리턴타입 : String(dbPasswd)
		String dbPasswd = memberService.getMemberPasswd(id);
		// 조회된 패스워드와 입력받은 기존 패스워드를 비교(Map 객체의 oldPasswd 키 사용)
		if(dbPasswd == null || !passwordEncoder.matches(map.get("oldPasswd"), dbPasswd)) { // 아이디 불일치 또는 패스워드 불일치
			model.addAttribute("msg", "수정 권한이 없습니다!");
			return "result/fail";
		}
		// ------------------------------------------------------------------------
		// 기존 비밀번호 일치 시 회원 정보 수정 전에 새 비밀번호 입력 여부 판별하여
		// 새 비밀번호가 입력되었을 경우 암호화 수행 필요
//		System.out.println("새 비밀번호 : " + map.get("passwd"));
		if(!map.get("passwd").equals("")) {
//			String encodedPasswd = passwordEncoder.encode(map.get("passwd"));
			map.put("passwd", passwordEncoder.encode(map.get("passwd")));
//			System.out.println("암호화 된 새 비밀번호 : " + map.get("passwd"));
		}
		// ------------------------------------------------------------------------
		// Map 타입으로 파라미터 처리 시 동일한 파라미터명이 존재할 경우(ex. 체크박스)
		// 첫번째 파라미터만 Map 객체에 저장되는 문제가 발생함.
		// => MemberVO 객체 사용 시 문자열로 자동으로 결합됨
		// => 또는, String[] 타입 파라미터를 추가로 선언하면 배열로 저장됨
//		System.out.println("취미 : " + Arrays.toString(hobby));
		// => Map 객체에 "hobby" 라는 키로 "A,B,C" 형식으로 문자열 결합하여 취미 저장
		if(hobby != null) { // 취미를 하나라도 체크했을 경우
			String strHobby = "";
			for(int i = 0; i < hobby.length; i++) {
				// 0번 인덱스를 제외한 나머지는 취미 앞에 콤마(,) 결합
				if(i > 0) {
					strHobby += ",";
				}
				
				strHobby += hobby[i];
			}
			
			map.put("hobby", strHobby);
//			System.out.println("취미 : " + map.get("hobby"));
		}
		// ------------------------------------------------------------------------
		// MemberService - modifyMember() 메서드 호출하여 회원정보 수정 요청
		// => 파라미터 : Map 객체   리턴타입 : int(updateCount)
		int updateCount = memberService.modifyMember(map);
		
		// 수정 요청 결과 판별
		// => 성공 시 수정된 정보 확인을 위해 회원 상세정보 페이지("MemberInfo")로 리다이렉트
		//    위의 작업 대신, 실패와 마찬가지로 "회원정보 수정 성공!" 메세지 출력 및
		//    회원 상세정보 페이지로 이동하는 작업 처리(success.jsp)
		// => 실패 시 "회원정보 수정 실패!" 메세지 저장 후 이전 페이지 처리(fail.jsp)
		// => success.jsp 와 fail.jsp 파일을 하나로 통합하여 처리해도 무관함
		if(updateCount > 0) {
//			return "redirect:/MemberInfo";
			model.addAttribute("msg", "회원정보 수정 성공!");
			model.addAttribute("targetURL", "MemberInfo");
			return "result/success";
		} else {
			model.addAttribute("msg", "회원정보 수정 실패!");
			return "result/fail";
		}
		
	}
	// ===============================================================================
	// 회원탈퇴("MemberWithdraw" - GET)
	@GetMapping("MemberWithdraw")
	public String withdrawForm(HttpSession session, Model model, HttpServletRequest request) {
		// ----------------------------------------------------------------------------
		String id = (String)session.getAttribute("sId");
		if(id == null) {
			model.addAttribute("msg", "로그인 필수!\\n로그인 페이지로 이동합니다.");
			model.addAttribute("targetURL", "MemberLogin");
			
			// ----------------------------------------------------------------
			// 로그인 완료 후 다시 회원 상세정보 조회 페이지로 이동할 수 있도록
			// 세션 객체에 회원 상세정보 조회 페이지의 서블릿 주소를 저장 후
			// 로그인 완료 시 해당 주소로 리다이렉트 수행할 수 있다!
//			session.setAttribute("prevURL", "MemberInfo");
			// => 경로를 직접 입력하지 않고 request 객체의 getServletPath() 메서드로 서블릿 주소 추출 가능
			String prevURL = request.getServletPath();
			String queryString = request.getQueryString(); // URL 파라미터 가져오기(없으면 null)
			
			// URL 파라미터(쿼리)가 null 이 아닐 경우 prevURL 에 결합(? 포함)
			if(queryString != null) {
				prevURL += "?" + queryString;
			}
			
			// 세션 객체에 prevURL 값 저장
			session.setAttribute("prevURL", prevURL);
			// ----------------------------------------------------------------
			
			return "result/fail";
		}
		// ----------------------------------------------------------------------------
		// member/member_withdraw_form.jsp 페이지 포워딩
		return "member/member_withdraw_form";
	}
			
	// 회원탈퇴("MemberWithdraw" - POST)
	@PostMapping("MemberWithdraw")
	public String withdraw(String passwd, BCryptPasswordEncoder passwordEncoder, 
			HttpSession session, Model model) {
		// 입력받은 패스워드 확인
		String id = (String)session.getAttribute("sId");
		
		// MemberService - getMemberPasswd() 메서드 재사용하여 암호화 된 패스워드 조회
		// => 파라미터 : 아이디(String)   리턴타입 : String(dbPasswd)
		String dbPasswd = memberService.getMemberPasswd(id);
		// 조회된 패스워드와 입력받은 기존 패스워드를 비교
		if(dbPasswd == null || !passwordEncoder.matches(passwd, dbPasswd)) { // 아이디 불일치 또는 패스워드 불일치
			model.addAttribute("msg", "권한이 없습니다!");
			return "result/fail";
		}
		// ----------------------------------------------------------------------------
		// MemberService - withdrawMember() 메서드 호출하여 회원 탈퇴 요청
		// => 파라미터 : 아이디   리턴타입 : int(withdrawResult)
		int withdrawResult = memberService.withdrawMember(id);
		
		// 탈퇴 요청 결과 판별
		if(withdrawResult > 0) {
			// 로그아웃 처리
			session.invalidate();
			
			model.addAttribute("msg", "탈퇴 처리가 완료되었습니다.");
			model.addAttribute("targetURL", "./");
			return "result/success";
		} else {
			model.addAttribute("msg", "탈퇴 실패!\\n관리자에게 문의 바랍니다.");
			return "result/fail";
		}
	}
	
	
}












