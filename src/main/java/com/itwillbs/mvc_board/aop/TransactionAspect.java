package com.itwillbs.mvc_board.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
public class TransactionAspect {
	// 트랜잭션 확인을 위한 AOP Advice 메서드 정의(트랜잭션 용어는 중요하지 않음)
	// 1) 트랜잭션 작업(Service 클래스의 메서드)이 수행되기 전 포인트 컷 설정
	//    (=> 모든 Service 클래스의 모든 메서드)
	@Before("execution(* com.itwillbs.mvc_board.service.*Service.*(..))")
	public void beforeTransaction() {
//		System.out.println("======== 트랜잭션 작업 전 ========");
	}
	
	// 2) 트랜잭션 작업 성공 시(예외 발생하지 않았을 경우) 로그 출력을 위해
	//    After Returning Advice 사용(@AfterReturning)하여 포인트컷 설정
	//    => Target 호출 후 예외가 발생하지 않으면 실행됨
	@AfterReturning("execution(* com.itwillbs.mvc_board.service.*Service.*(..))")
	public void commitTransaction() {
//		System.out.println("======== 트랜잭션 커밋!!!! ========");
	}
	
	// 3) 트랜잭션 작업 실패 시(예외 발생했을 경우) 로그 출력을 위해
	//    After Throwing Advice 사용(@AfterThrowing)하여 포인트컷 설정
	//    => Target 호출 후 예외가 발생하면 실행됨
	@AfterThrowing("execution(* com.itwillbs.mvc_board.service.*Service.*(..))")
	public void rollbackTransaction() {
		System.out.println("======== 트랜잭션 롤백!!!! ========");
	}
	
	
	
}




















