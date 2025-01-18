package com.itwillbs.mvc_board.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.mvc_board.handler.GenerateRandomCode;
import com.itwillbs.mvc_board.handler.SendMailClient;
import com.itwillbs.mvc_board.vo.MailAuthInfo;
import com.itwillbs.mvc_board.vo.MemberVO;

@Service
public class MailService {
	@Autowired
	private SendMailClient sendMailClient;
	
	// 인증 메일 발송 요청 메서드
	public MailAuthInfo sendAuthMail(MemberVO member) {
		// 인증 메일에 포함시킬 난수 생성
		// GenerateRandomCode 클래스의 static 메서드 getRandomCode() 메서드 호출하여 난수 요청
		// => 파라미터 : 생성할 난수 길이(정수)
		String auth_code = GenerateRandomCode.getRandomCode(50);
		System.out.println("생성된 인증코드 : " + auth_code);
		// ==============================================================
		// [ 인증 메일 발송 ]
		// 인증 메일에 포함할 제목과 메일 본문 생성
		String subject = "[아이티윌] 가입 인증 메일입니다.";
		// 인증코드만 메일로 발송할 경우
//		String content = "인증코드 : " + auth_code;
		// 인증 링크를 메일로 발송할 경우(이메일 본문에는 HTML 태그 사용 가능)
		String content = "<a href=\"http://localhost:8081/mvc_board/MemberEmailAuth?email=" + member.getEmail() + "&auth_code=" + auth_code + "\">이메일 인증을 수행하려면 이 링크를 클릭하세요!</a>";
		// -------------------------------------------------------------
		// SendMailClient - sendMail() 메서드(static 메서드) 호출하여 메일 발송 요청
		// => 파라미터 : 메일주소, 제목, 본문   리턴타입 : void
//		SendMailClient.sendMail(member.getEmail(), subject, content);
		// 단, 메일 발송 과정에서 메일 전송 상황에 따라 시간 지연이 발생할 수 있는데
		// 이 과정에서 다음 작업이 실행되지 못하고 발송 완료 시점까지 대기하게 된다.
		// (ex. 사용자 입장에서 가입 완료 화면이 표시되지 않고 요청 화면이 그대로 유지됨)
		// 따라서, 메일 발송 작업과 나머지 작업을 별도로 분리하여 동작시키기 위해
		// 메일 발송 메서드 호출 작업을 하나의 쓰레드(Thread)로 동작시키면 별도로 분리가 가능하다!
		// 즉, 메일 발송이 완료되지 않더라도 다음 작업 진행이 가능하다!
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// 별도의 쓰레드에서 수행할 작업
//				SendMailClient.sendMail(member.getEmail(), subject, content);
				sendMailClient.sendMail(member.getEmail(), subject, content);
				System.out.println("메일 발송 쓰레드 작업 완료 - " + new Date());
			}
		}).start(); // start() 메서드 호출 필수!
		
		System.out.println("메일 발송 쓰레드 시작 - " + new Date());
		// ============================================================================
		// MailAuthInfo 객체 생성 후 인증에 사용될 이메일 주소와 인증 코드 저장 후 리턴
		MailAuthInfo mailAuthInfo = new MailAuthInfo(member.getEmail(), auth_code);
		
		return mailAuthInfo;
	}

	
	
}

















