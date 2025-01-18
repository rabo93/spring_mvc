package com.itwillbs.mvc_board.handler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/*
 * [ 서버에서 특정 작업에 대한 일괄 처리를 수행하는 방법 3가지 ]
 * 1) Batch : 여러 작업들을 순차적으로 처리
 * 2) Quartz 또는 Scheduler : 특정 작업을 특정 시점(시간)에 처리
 *    => Quartz 는 Scheduler 에 비해 구현 과정이 복합하고, 대신 세밀한 제어가 가능함
 *       Scheduler 는 구현 과정이 간편하므로 단순 스케쥴링에 많이 사용됨
 * ---------------------------------------------------------------
 * [ Scheduler 구현 방법 ]
 * 1. root-context.xml 또는 servlet-context.xml 등 필요한 위치에 Scheduler 설정
 * 2. 스케쥴링으로 작업을 수행할 메서드 상단에 @Scheduled 어노테이션을 지정하여 스케쥴러 동작
 *    => 패키지 주의! (org.springframework.scheduling.annotation.Scheduled)
 * => 스케쥴러 사용하려면 XML 설정 필요하며, 별도의 라이브러리 추가 불필요함(기본 라이브러리로 제공됨)
 * ---------------------------------------------------------------
 * [ @Scheduled 어노테이션에 사용 가능한 대표적인 옵션 ]
 * 1) fixedRate : 밀리초 단위로 설정한 시간 간격으로 스케쥴링 실행됨
 *    ex) @Schduled(fixedRate = 1000) : 1초 간격으로 스케쥴링
 * 2) cron : 가장 많이 사용하는 방법으로, 특정 시점에 스케쥴링 실행됨
 *    기본 문법 : @Schduled(cron = "초 분 시 일 월 요일 [연도(옵션)]")
 *    => 초 : 0 ~ 59, 분 : 0 ~ 59, 시 : 0 ~ 23, 일 : 1 ~ 31, 
 *    => 월 : 1 ~ 12 또는 JAN ~ DEC
 *    => 요일 : 0(일요일) ~ 6(토요일) 또는 SUN ~ SAT
 *    => 연도 : 1970 ~ 2099
 *    
 *    [ 특수문자 ]
 *    * : 모든 값(Wildcard)  => ex) 0 * * * * * : 매 분마다 0초에 실행
 *    , : OR 조건(하나의 항목에 복수개의 값 지정)  => ex) 0,30 * * * * * : 30초 간격으로 0초, 30초에 실행 
 *    - : 범위
 *    / : 간격(주기)
 *    L : 마지막(Last)(주로, "일" 또는 "요일" 항목에 사용)`
 *    W : 가장 가까운 평일(Week)
 */
@Log4j2
@Component
public class MyJobScheduler {
	
	// 스케쥴링으로 수행할 메서드 정의
	// 메서드 상단에 @Scheduled 어노테이션을 적용하여 스케쥴링 적용
//	@Scheduled(fixedRate = 1000) // fixedRate = 1000 지정 시 1000ms(1초) 마다 작업이 실행됨
//	@Scheduled(cron = "0 * * * * *") // 매일 매시 매분 0초마다 작업이 실행됨
//	@Scheduled(cron = "30 31 * * * *") // 매일 매시 31분 30초마다 작업이 실행됨
	@Scheduled(cron = "0,30 * * * * *") // 매일 매시 31분 30초마다 작업이 실행됨
//	@Scheduled(cron = "0 0 10 5 * *") // 매월 5일 오전 10시마다 작업이 실행됨
//	@Scheduled(cron = "0 0 10 5W * *") // 매월 5일에 가장 가까운 평일 오전 10시마다 작업이 실행됨
//	@Scheduled(cron = "0 0 9-18/1 * * *") // 매일 9시~18시까지 1시간 간격으로 작업이 실행됨
	public void testScheduler() {
		log.info("=========스케쥴러 작업=========");
//		log.info("=========스케쥴러 작업=========");
//		log.info("=========스케쥴러 작업=========");
	}
	
	
}











