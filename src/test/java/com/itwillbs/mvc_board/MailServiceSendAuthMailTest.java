package com.itwillbs.mvc_board;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.itwillbs.mvc_board.handler.SendMailClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/config/root-context.xml", "file:src/main/resources/config/servlet-context.xml"}) // 실제 원본 파일 위치 탐색(복사 불필요)
public class MailServiceSendAuthMailTest {
	@Value("${SENDER_MAIL_ADDRESS}")
	private String sender_mail_address;
	
	@Test
	public void testSendAuthMail() {
		System.out.println("test : " + sender_mail_address);
//		SendMailClient.sendMail("ytlee@itwillbs.co.kr", "확인용", "확인용");
//		SendMailClient.sendMail(sender_mail_address, "확인용", "확인용");
	}

}















