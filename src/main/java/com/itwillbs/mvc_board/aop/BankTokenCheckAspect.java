package com.itwillbs.mvc_board.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.itwillbs.mvc_board.vo.BankToken;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
public class BankTokenCheckAspect {
	// Advice : Before Advice
	// Pointcut : BankTokenCheck 어노테이션
	@Before("@annotation(com.itwillbs.mvc_board.aop.BankTokenCheck)")
	public void tokenCheck() throws Exception {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if(attrs == null) {
			return;
		}
		
		HttpServletRequest request = ((ServletRequestAttributes)attrs).getRequest();
		// HttpServletResponse 객체도 동일한 방법으로 가져올 수 있다!
		// ----------------------------------------------------------------------------------
		// HttpSession 객체 가져오기
		HttpSession session = request.getSession();
		BankToken token = (BankToken)session.getAttribute("token");
		log.info(">>>>>>>>> 토큰 정보 : " + token);
		// ==================================================================================
		// BankToken 객체 또는 BankToken 객체 내의 access_token 값이 존재하지 않을 경우 강제 예외 발생
		if(token == null || token.getAccess_token() == null) {
//			// 로그인 성공 후 다시 현재 페이지로 돌아오기 위해 prevURL 세션 속성값 설정
//			// => 경로를 직접 입력하지 않고 request 객체의 getServletPath() 메서드로 서블릿 주소 추출 가능
			String prevURL = request.getServletPath();
			String queryString = request.getQueryString(); // URL 파라미터 가져오기(없으면 null)
//			
//			// URL 파라미터(쿼리)가 null 이 아닐 경우 prevURL 에 결합(? 포함)
			if(queryString != null) {
				prevURL += "?" + queryString;
			}
//			
//			// 세션 객체에 prevURL 값 저장
			session.setAttribute("prevURL", prevURL);
			throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, "계좌 인증 필수!!/BankMain") {};
		}
	}
	
}















