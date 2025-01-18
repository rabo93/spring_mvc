package com.itwillbs.mvc_board.handler;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.log4j.Log4j2;

// 실제 인터셉터 기능을 처리할 클래스 정의 => HandlerInterceptor 인터페이스 구현체로 정의
@Log4j2
public class LoginInterceptor implements HandlerInterceptor {
	// HandlerInterceptor 인터페이스의 추상메서드 오버라이딩
	// - preHandle(), postHandle(), afterCompletion() 메서드 중 필요한 메서드 오버라이딩
	// - 컨트롤러로 요청이 전달되기 전 세션 가로채기를 수행하기 위해서는 preHandle() 메서드 필요
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		log.info(">>>>>>>>> Intercepter - preHandle()");
		// ---------------------------------------------------------
		// 세션 객체를 통해 세션 아이디가 존재하지 않을 경우 fail.jsp 페이지로 포워딩 처리하고
		// 아니면, 기존 요청대로 글쓰기 폼 페이지로 포워딩 처리(BoardController 에 이미 기술되어 있음)
		// ---------------------------------------------------------
		// HttpSession 객체 얻기
		// => 주의! 현재 오버라이딩 된 메서드에 HttpSession 타입 파라미터 추가가 불가능하므로
		//    HttpServletRequest 객체로부터 얻어야함!
		HttpSession session = request.getSession();
		
		// HttpSession 객체에서 로그인 세션 아이디(sId) 꺼내서 null 값(미 로그인)인지 판별
		String id = (String)session.getAttribute("sId");
		if(id == null) {
			log.info(">>>>>>>> Interceptor : 세션 아이디 없음!");
			
			// result/fail.jsp 페이지로 전달할 파라미터 값을 Model 객체에 저장했었지만
			// 현재 메서드 내에서는 Model 객체가 없고 대신 HttpServletRequest 객체가 존재하므로
			// request 객체에 직접 데이터 저장해도 동일함!
			// => model.addAttribute() 대신 request.setAttribute() 메서드 사용
			request.setAttribute("msg", "로그인 필수!\\n로그인 페이지로 이동합니다.");
			request.setAttribute("targetURL", "MemberLogin");
			// -------------------------------------------------------------------------------
			// 이 부분부터는 기존 코드와 동일
			// -------------------------------------------------------------------------------
//			// 로그인 성공 후 다시 현재 페이지로 돌아오기 위해 prevURL 세션 속성값 설정
//			// => 경로를 직접 입력하지 않고 request 객체의 getServletPath() 메서드로 서블릿 주소 추출 가능
			String prevURL = request.getServletPath();
			String queryString = request.getQueryString(); // URL 파라미터 가져오기(없으면 null)
			
			// URL 파라미터(쿼리)가 null 이 아닐 경우 prevURL 에 결합(? 포함)
			if(queryString != null) {
				prevURL += "?" + queryString;
			}
			
//			// 세션 객체에 prevURL 값 저장
			session.setAttribute("prevURL", prevURL);
			// ---------------------------------------------------------------------
//			return "result/fail";
			// 기존 컨트롤러에서 뷰페이지(JSP)로 포워딩을 위해서는 
			// 메서드 리턴타입 String 지정하고 return 문에 뷰페이지명을 기술하여 포워딩 수행했으나
			// 인터셉터의 preHandle() 메서드는 Controller 로 요청이 전달되기 전이며
			// 컨트롤러의 메서드처럼 ViewSolver 를 통해 뷰페이지 포워딩 처리 설정이 불가능하다!
			// ---------------------------------------------------------------------
			// 인터셉터 내의 preHandle() 메서드 리턴타입이 boolean 인 이유는
			// preHandle() 메서드에서 작업 처리 후 컨트롤러로 요청을 전달할지 여부를 결정하는 용도
			// => true 리턴 시 요청이 컨트롤러로 전달되고, false 리턴 시 컨트롤러로 전달되지 않음
			// ---------------------------------------------------------------------
			// preHandle() 메서드에서 인터셉트 작업 처리 후 컨트롤러로 요청을 전달하지 않고
			// 직접 뷰페이지 포워딩 or 리다이렉트 작업을 수행하기 위해서는
			// 기존 JSP 에서 사용하던 방법을 다시 활용해야함(request, response 객체 직접 다루기)
			// 1) 디스패치 방식의 포워딩 수행할 경우
//			RequestDispatcher dispatcher = request.getRequestDispatcher("이동할페이지");
//			dispatcher.forward(request, response);
			// => 위의 코드를 한 줄로 압축
			// => 주의! 이동할 뷰페이지 지정 시 ViewResolver 의 prefix, suffix 값을 활용하지 못하므로
			//    뷰페이지 경로를 webapp 디렉토리(= 컨텍스트루트) 뒤의 경로를 모두 기술해야한다!
			//    (ex. /WEB-INF/views/xxx.jsp)
			request.getRequestDispatcher("/WEB-INF/views/result/fail.jsp").forward(request, response);
			
			// 2) 리다이렉트 수행할 경우
//			response.sendRedirect("MemberLogin");
			
			return false; // 컨트롤러로 요청을 전달하지 않음
		}
		
		// 세션 아이디가 있을 경우에는 컨트롤러로 요청을 전달해야하므로 true 값을 리턴하면
		// 요청에 대한 정상적인 핸들러 작업이 수행된다!
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		log.info(">>>>>>>>> Intercepter - postHandle()");
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		log.info(">>>>>>>>> Intercepter - afterCompletion()");
	}

}



















