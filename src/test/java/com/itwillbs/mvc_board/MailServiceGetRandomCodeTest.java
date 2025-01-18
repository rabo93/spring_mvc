package com.itwillbs.mvc_board;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.itwillbs.mvc_board.handler.GenerateRandomCode;
import com.itwillbs.mvc_board.handler.MailAuthenticator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/config/root-context.xml", "file:src/main/resources/config/servlet-context.xml"}) // 실제 원본 파일 위치 탐색(복사 불필요)
public class MailServiceGetRandomCodeTest {

	@Test
	public void testGetRandomCode() {
		// 인증 메일에 포함시킬 난수 생성 테스트
		// GenerateRandomCode 클래스의 static 메서드 getRandomCode() 메서드 호출하여 난수 요청
		// => 파라미터 : 생성할 난수 길이(정수)   리턴타입 : String(auth_code)
//		String auth_code = GenerateRandomCode.getRandomCode(50);
//		System.out.println("생성된 인증코드 : " + auth_code);
		
		for(int i = 1; i <= 10; i++) {
			String auth_code = GenerateRandomCode.getRandomCode(50);
			System.out.println("생성된 인증코드 : " + auth_code);
		}
		
		MailAuthenticator mailAuthenticator = new MailAuthenticator(); 
	}

}














