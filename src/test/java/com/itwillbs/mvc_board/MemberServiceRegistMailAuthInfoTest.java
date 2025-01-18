package com.itwillbs.mvc_board;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.itwillbs.mvc_board.mapper.MemberMapper;
import com.itwillbs.mvc_board.vo.MailAuthInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/config/root-context.xml"}) // 실제 원본 파일 위치 탐색(복사 불필요)
public class MemberServiceRegistMailAuthInfoTest {
	@Autowired
	private MemberMapper mapper;
	
	@Test
	public void testRegistMailAuthInfo() {
		MailAuthInfo mailAuthInfo = new MailAuthInfo("ytlee@itwillbs.co.kr", "3333");
		
		// 기존 인증 정보 존재 여부 확인
		// MemberMapper - selectMailAuthInfo() 메서드 호출
		// => 파라미터 : MailAuthInfo 객체   리턴타입 : MailAuthInfo(dbMailAuthInfo)
		MailAuthInfo dbMailAuthInfo = mapper.selectMailAuthInfo(mailAuthInfo);
		System.out.println("조회된 인증 정보 : " + dbMailAuthInfo);
		
		// 인증정보 조회 결과 판별
		if(dbMailAuthInfo == null) { // 기존 인증정보 없음(인증메일 발송 이력 없음)
			// 새 인증정보 등록 위해 insertMailAuthInfo() 메서드 호출하여 등록 작업 요청(INSERT)
			mapper.insertMailAuthInfo(mailAuthInfo);
		} else { // 기존 인증정보 있음(인증메일 발송 이력 있음)
			// 기존 인증정보 갱신 위해 updateMailAuthInfo() 메서드 호출하여 수정 작업 요청(UPDATE)
			mapper.updateMailAuthInfo(mailAuthInfo);
		}
	}

}
















