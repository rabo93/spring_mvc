package com.itwillbs.mvc_board.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect // AOP 로 동작
@Component // 스프링 빈으로 등록(root-context.xml 에서 스캔 대상에 포함됨)
public class LogAdvisor {
	// Advice(분리된 관심사)에 해당하는 메서드 정의
	// => 이 때, Advice 의 종류와 함께 Target 의 JoinPoint 에 결합되도록 Pointcut 도 설정해야함
	// 1) Advice 종류 지정
	//    => showLog() 메서드를 Target 의 JoinPoint 호출 전에 실행되도록 @Before 지정
	// 2) Advice 어노테이션 내부에 Pointcut 설정
	//    => Pointcut 표현식 활용하여 Target 의 JoinPoint 지정
//	@Before("execution(* com.itwillbs.mvc_board.service.MemberService.*(..))")
	// => com.itwillbs.mvc_board.service.MemberService 클래스의 모든 메서드를 대상으로 Pointcut 설정
//	@Before("execution(* com.itwillbs.mvc_board.service.BoardService.registBoard(com.itwillbs.mvc_board.vo.BoardVO))")
	// => BoardService 의 registBoard(BoardVO) 메서드를 대상으로 Pointcut 설정
//	@Before("execution(* com.itwillbs.mvc_board.service.BoardService.*(..))")
	// => BoardService 의 모든 메서드를 대상으로 Pointcut 설정
	public void showLog() {
//		log.info("================= AOP 로 출력되는 로그 메세지 ==============");
//		log.info("================= AOP 로 출력되는 로그 메세지 ==============");
//		log.info("================= AOP 로 출력되는 로그 메세지 ==============");
//		log.info("================= AOP 로 출력되는 로그 메세지 ==============");
//		log.info("================= AOP 로 출력되는 로그 메세지 ==============");
	}
	
	
}


















