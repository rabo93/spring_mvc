package com.itwillbs.mvc_board.aop;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.itwillbs.mvc_board.service.BoardService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
//@Order(value = Ordered.LOWEST_PRECEDENCE) // AOP 빈들간의 우선순위 설정(값이 낮을수록 우선순위 높음. 기본값 : Ordered.LOWEST_PRECEDENCE)
public class LoginCheckAspect {
	// 포인트컷 설정
	// com.itwillbs.mvc_board.aop.LoginCheck 인터페이스에 해당하는 어노테이션 지정된 Target 실행전
	// => @annotation 지정 시 특정 어노테이션이 붙은 Target 탐색
	// => 메서드 정의 시 리턴타입은 상황에 맞게 지정(void 도 가능)
	// => 단, 메서드 파라미터로 스프링 빈으로 관리되는 객체 지정 불가능(컴파일에러는 없지만 실행 X)
	@Before("@annotation(com.itwillbs.mvc_board.aop.LoginCheck)")
	public void loginCheck() throws Exception {
	// 만약, advice 메서드 내에서 해당 어노테이션 내의 메서드 및 enum 에 접근하려면
	// 포인트컷 설정 시 && 연산자 뒤에 @annotation(변수명) 을 기술하고
	// 메서드 파라미터로 어노테이션 타입 변수를 설정하되, 변수명을 @annotation(변수명)과 동일하게 지정
//	@Before("@annotation(com.itwillbs.mvc_board.aop.LoginCheck) && @annotation(loginCheck)")
//	public void loginCheck(LoginCheck loginCheck) {
//		log.info(">>>>>>>>> 로그인 체크");
		
		// HttpSession 객체가 없으므로 HttpServletRequest 객체를 통해서 가져와야하며
		// HttpServletRequest 객체도 별도의 추가 작업을 통해 가져와야한다!
		// => RequestContextHolder.currentRequestAttributes() 메서드 리턴값을 ServletRequestAttributes 타입으로 형변환 후
		//    다시 getRequest() 메서드를 호출하면 HttpServletRequest 객체를 얻어올 수 있다!
		// => 참고) RequestContextHolder 클래스는 스프링 전역에서 Request 정보를 관리하는 유틸 클래스로
		//          컨트롤러 외의 비즈니스 로직 상에서 Request 객체에 접근해야할 때 사용
		//    만약, Request 객체의 속성에만 접근하려면 RequestContextHolder.getRequestAttributes() 메서드(또는 currentRequestAttributes()) 활용 가능
//		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
//		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if(attrs == null) {
			return;
		}
		
		HttpServletRequest request = ((ServletRequestAttributes)attrs).getRequest();
		// HttpServletResponse 객체도 동일한 방법으로 가져올 수 있다!
		HttpServletResponse response = ((ServletRequestAttributes)attrs).getResponse();
		// => 결론! 이 방법보다는 Filter 설정을 통해 객체를 리턴받는 방법이 더 안전하지만
		//    지금 수업에서는 그냥 현재 형태로 사용
		// ----------------------------------------------------------------------------------
		
		// HttpSession 객체 가져오기
		HttpSession session = request.getSession();
//		log.info(">>>>>>>>> 세션 아이디 : " + session.getAttribute("sId"));
		String id = (String)session.getAttribute("sId");
//		log.info(">>>>>>>>> 세션 아이디 : " + id);
		// ==================================================================================
		// 만약, LoginCheck 어노테이션에 memberRole() 메서드와 MemberRole enum 활용하려면 다음과 같이 수행한다.
		// ex) 세션 아이디에 대한 회원 타입(일반사용자, 관리자 등) 판별할 경우
		// => public void loginCheck(LoginCheck loginCheck) {} 형태로 메서드 정의했을 경우
//		log.info(">>>>>>>>> 회원 타입 : " + loginCheck.memberRole()); 
		// ADMIN 또는 USER enum 값 출력됨
		// => @LoginCheck(memberRole = MemberRole.USER) 형태로 지정했던 () 내의 enum 값이 리턴됨
		
		// switch 문을 통해(if 도 가능) memberRole() 메서드 리턴값을 식으로 전달 시
		// case 문에서 enum 값 판별 가능
//		switch(loginCheck.memberRole()) {
////			case MemberRole.ADMIN : // 문법 오류!!!!
//			// 주의! switch 문에 enum 타입 전달 시 case 문에서 enum 값만 지정(enum 이름 생략)
//			case ADMIN : 
//				// 관리자일 때 체크를 위한 코드....
//				break;
//			case USER :
//				// 일반 사용자일 때 체크를 위한 코드....
//		}
		// ==================================================================================
		// 세션 아이디가 존재하지 않을 경우 로그인 페이지로 리다이렉트
		// => fail.jsp 페이지로 포워딩하여 targetURL 속성에 "MemberLogin" 문자열 전달
		if(id == null) {
//			log.info(">>>>>>>>> 세션 아이디 없음!!!!");
//			model.addAttribute("msg", "로그인 필수!\\n로그인 페이지로 이동합니다.");
//			model.addAttribute("targetURL", "MemberLogin");
			// => Model 객체 사용 불가능하므로 대신 HttpServletRequest 객체 사용하여 속성값 저장
//			request.setAttribute("msg", "로그인 필수!\\n로그인 페이지로 이동합니다.");
//			request.setAttribute("targetURL", "MemberLogin");
			// => 강제 예외 발생을 통해 처리할 경우 불필요(주석 처리)
			
//			// 로그인 성공 후 다시 현재 페이지로 돌아오기 위해 prevURL 세션 속성값 설정
//			// => 경로를 직접 입력하지 않고 request 객체의 getServletPath() 메서드로 서블릿 주소 추출 가능
			String prevURL = request.getServletPath();
			String queryString = request.getQueryString(); // URL 파라미터 가져오기(없으면 null)
//			System.out.println("prevURL : " + prevURL);
//			System.out.println("queryString : " + queryString);
//			
//			// URL 파라미터(쿼리)가 null 이 아닐 경우 prevURL 에 결합(? 포함)
			if(queryString != null) {
				prevURL += "?" + queryString;
			}
//			
//			// 세션 객체에 prevURL 값 저장
			session.setAttribute("prevURL", prevURL);
			
//			return "result/fail"; // 스프링 기본 포워딩 문법 사용 불가!!!!
			// 스프링의 DispatcherServlet 을 통한 포워딩이 동작하지 않으므로
			// 원래 사용하던 RequestDispatcher 객체를 통한 포워딩 수행하면 된다!!!!
			// => 주의! servlet-context.xml 의 prefix, suffix 값 결합이 불가능하므로 경로 직접 명시!
//			request.getRequestDispatcher("/WEB-INF/views/result/fail.jsp").forward(request, response);
			// ------------------------------------------------------------------------------
			// 포워딩 또는 리다이렉트를 직접 기술할 경우 해당 코드들에 대한 중복이 또 발생할 수 있다!
			// 이 때, Exception 처리 기능을 응용하여 아이디 없을 때 강제로 특정 예외를 발생시킨 후
			// 해당 예외 처리 시 포워딩이나 리다이렉트를 수행하도록 할 수도 있다!
			// => throw new XXXException() 형태로 강제로 예외 발생 가능
			// => 이 때, HttpStatusCodeException 클래스 활용 시 특정 HTTP 상태 코드를 예외로 발생 가능
			//    (인가받지 못한 사용자일 때 HttpStatus.UNAUTHORIZED 상수가 딱 맞아떨어짐(HTTP 401 Unauthorized)
			throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, "회원만 이용 가능합니다!\\n로그인 페이지로 이동합니다./MemberLogin") {};
			// => 예외 처리를 담당하는 클래스에서 HttpStatusCodeException 예외를 별도로 처리하는 작업 추가
		}
		// ==================================================================================
	}
	
	// 일반 사용자용 세션과 관리자용 세션(추가됨)을 구분하여 체크할 경우
	@Before("@annotation(com.itwillbs.mvc_board.aop.LoginCheck) && @annotation(loginCheck)")
	public void loginCheck(LoginCheck loginCheck) {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if(attrs == null) {
			return;
		}
		
		HttpServletRequest request = ((ServletRequestAttributes)attrs).getRequest();
		HttpServletResponse response = ((ServletRequestAttributes)attrs).getResponse();
		
		HttpSession session = request.getSession();
		String id = (String)session.getAttribute("sId");
		
		
//		String prevURL = request.getServletPath();
//		String queryString = request.getQueryString();
//		
//		if(queryString != null) {
//			prevURL += "?" + queryString;
//		}
////		
//		session.setAttribute("prevURL", prevURL);
//		throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, "회원만 이용 가능합니다!\\n로그인 페이지로 이동합니다./MemberLogin") {};
		
		
		
		switch(loginCheck.memberRole()) {
			case USER :
				if(id == null) {
					String prevURL = request.getServletPath();
					String queryString = request.getQueryString();
					
					if(queryString != null) {
						prevURL += "?" + queryString;
					}
//					
					session.setAttribute("prevURL", prevURL);
					throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, "회원만 이용 가능합니다!\\n로그인 페이지로 이동합니다./MemberLogin") {};
				}
				
				break;
			case ADMIN : 
				if(id == null) {
					String prevURL = request.getServletPath();
					String queryString = request.getQueryString();
					
					if(queryString != null) {
						prevURL += "?" + queryString;
					}
//					
					session.setAttribute("prevURL", prevURL);
					throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, "회원만 이용 가능합니다!\\n로그인 페이지로 이동합니다./MemberLogin") {};
				} else if(!id.equals("admin")) {
					String prevURL = request.getServletPath();
					String queryString = request.getQueryString();
					
					if(queryString != null) {
						prevURL += "?" + queryString;
					}
//					
					session.setAttribute("prevURL", prevURL);
					throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다!") {};
				}
				
		}
		
	}
	
}















