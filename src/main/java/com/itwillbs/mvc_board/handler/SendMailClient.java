package com.itwillbs.mvc_board.handler;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 메일 발송을 처리하는 SendMailClient 클래스
@Component
public class SendMailClient {
	@Autowired
	private MailAuthenticator authenticator;
	
	private final String HOST = "smtp.gmail.com"; // 메일 서버 주소
	// => 메일 송신 프로토콜 : SMTP(Simple Mail Transfer Protocol) <-> 수신 프로토콜 : POP3, IMAP
	private final String PORT = "587"; // GMail 서비스 포트번호(SMTP 기본 포트번호는 25)
	// => 각 메일 서버마다 포트번호 다름
	private final String SENDER_ADDRESS = "ytlee@itwillbs.co.kr"; // 발신자 메일 주소

	public void sendMail(String receiver, String subject, String content) {
		try {
			// --------------- 메일 전송에 필요한 정보 설정 -----------------
			// 1. 시스템(= 톰캣 서버)의 속성 정보(= 서버 정보)를 java.util.Properties 객체로 리턴받기
			Properties props = System.getProperties();
			
			// 2. Properties 객체를 활용하여 메일 전송에 필요한 기본 설정 정보를 서버 정보에 추가
			// => Properties 객체의 put() 메서드 활용(Properties 객체는 Map 객체와 유사함)
			// => 이 때, 키(Key)는 고정
			// 메일 전송에 사용할 메일 서버에 대한 정보 설정(구글, 네이버, 네이트 등)
			props.put("mail.smtp.host", HOST); // SMTP 서버 주소
			props.put("mail.smtp.port", PORT); // SMTP 서비스 포트번호
			props.put("mail.smtp.auth", "true"); // SMTP 서비스 이용 시 접근 과정에서 인증(로그인) 여부 설정
			// 메일 서버 인증 관련 추가 정보 설정(설정 내용에 따라 위의 정보 중 포트번호가 바뀔 수 있음)
			props.put("mail.smtp.starttls.enable", "true"); // 인증 프로토콜로 TLS 프로토콜 사용 여부 설정
			props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // TLS 프로토콜의 버전 설정
			props.put("mail.smtp.ssl.trust", HOST); // SSL 인증에 사용할 신뢰 가능한 서버 주소 지정
			
			// 3. 메일 서버에 대한 인증(로그인) 정보 관리하는 사용자 정의 클래스 타입 인스턴스 생성
			// => MailAuthenticator -> Authenticator 타입 업캐스팅
//			Authenticator authenticator = new MailAuthenticator();
			// => @Autowired 어노테이션을 통해 스프링 빈으로 자동 주입 활용하기 위해 주석 처리
			
			// 4. 자바 메일 전송 작업을 하나의 객체로 다룰 때 jakartra.mail.Session 객체 활용
			//    => Session 클래스의 getDefaultInstance() 메서드 호출하여 Session 타입 객체 리턴받기
			Session mailSession = Session.getDefaultInstance(props, authenticator);
			
			// 5. 서버 정보와 인증 정보를 포함하여 전송할 메일 정보를 하나의 묶음으로 관리할
			// jakarta.mail.internet.MimeMessage 객체 생성(파라미터 : Session 객체)
			Message message = new MimeMessage(mailSession);
			// ---------------------------------------------------------------------------------------
			// 6. 전송할 메일에 대한 상세 정보 설정
			// 1) 발신자 정보 설정
			//    => InternetAddress 객체 활용(Address 타입으로 업캐스팅)
			Address senderAddress = new InternetAddress(SENDER_ADDRESS, "아이티윌");
			// => UnsupportedEncodingException 예외 발생
			// => 기본적으로 상용 메일 서비스에서는 발신자 메일 주소 변경이 불가능하므로(스팸메일 정책 때문)
			//    다른 메일 주소를 입력하더라도 실제 SMTP 서버에 로그인하는 계정으로 발송됨
			// => 주의! SMTP 서버를 네이버로 사용 시 메일 주소 수정 자체가 불가능하며
			//    메일 주소 강제 변경 시에는 예외 발생한다!
			
			// 2) 수신사 정보 설정
			Address receiverAddress = new InternetAddress(receiver);
			// => AddressException 예외 발생
			
			// 3) 5번 과정에서 생성한 Message 객체를 활용하여 전송할 메일의 내용 설정
			// 3-1) 메일 헤더 정보 설정(생략 가능)
			message.setHeader("content-type", "text/html; charset=UTF-8");
			
			// 3-2) 발신자 설정
			message.setFrom(senderAddress);
			
			// 3-3) 수신자 설정
			// => Message 객체의 setRecipient() 메서드는 단일 수신자에게 발송 시 사용하고
			//    setRecipients() 메서드는 다중 수신자에게 발송 시 사용(파라미터가 다름)
			// => 첫번째 파라미터로 전달할 수신 타입(RecipientType 객체)은 상수 활용
			//    RecipientType.TO : 수신자에게 직접 전송(메일을 직접 수신할 수신자 = 업무 담당자)
			//    RecipientType.CC : 참조(Carbon Copy 약자). 직접 수신자는 아니나 업무 참조용으로 수신(= 업무 관계자)
			//    RecipientType.BCC : 숨은 참조(Blid CC 약자). 메일 수신자가 CC 여부를 알 수 없게 참조 수신자를 숨김
			message.setRecipient(RecipientType.TO, receiverAddress);
			
			// 3-4) 메일 제목 설정
			message.setSubject(subject);
			
			// 3-5) 메일 본문 설정
			// => 파라미터 : 메일 본문, 본문의 컨텐츠 타입
			// => 만약, 파라미터 타입을 Multipart 타입으로 전달 시 첨부 파일 기능 활용 가능
			message.setContent("<h3>" + content + "</h3>", "text/html; charset=UTF-8");
			
			// 3-6) 메일 전송 날짜 및 시각 설정
			message.setSentDate(new Date()); // 현재 시스템의 날짜 및 시각 정보 Date 객체로 설정
			// ---------------------------------------------------------------------------------
			// 7. 메일 전송
			// => jakarta.mail.Transport 클래스의 static 메서드 send() 호출
			// => 파라미터 : Message 객체
			Transport.send(message);
			System.out.println("메일 발송 성공!");
		} catch (Exception e) {
			System.out.println("메일 발송 실패!");
			e.printStackTrace();
		}
	}

}











