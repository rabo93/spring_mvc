package com.itwillbs.mvc_board.handler;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 자바 메일 기능 사용 시 메일 서버(ex. 네이버, Gmail 등) 인증을 위한 정보를 관리하는 클래스 정의
// => jakarta.mail.Authenticator(javax.mail.Authenticator) 클래스 상속받아 정의
@Component
public class MailAuthenticator extends Authenticator {
	// --------------------------------------------------------------------------
	// 특정 클래스에서 중요한 보안 정보(패스워드 등)를 사용할 때
	// 코드에 직접 기술(= 하드 코딩)하지 않고 별도의 외부 파일로 분리하여 관리해야함
	// => src/main/resources/config/appdata.properties 파일 생성하여 관리하고 있음
	// => 해당 파일을 스프링 클래스 내에서 접근하려면 servlet-context.xml 파일 설정 필요
	//    (또는 어노테이션으로 설정도 가능함)
	// => 기본 문법 : @Value("${속성이 설정된 파일 내의 속성명}") 형태로 지정 후 멤버변수 선언
	@Value("${SENDER_MAIL_ADDRESS}")
	private String sender_mail_address;
	
	@Value("${GMAIL_APP_PASSWORD}")
	private String gmail_app_password;
	
	// --------------------------------------------------------------------------
	// 인증정보(아이디, 패스워드)를 관리할 PasswordAuthentication 클래스 타입 멤버변수 선언
	private PasswordAuthentication passwordAuthentication;
	
	// 기본 생성자 정의
	public MailAuthenticator() {
//		System.out.println("MailAuthenticator() 생성자 호출됨!");
		/*
		 * 인증에 사용될 아이디와 패스워드를 파라미터로 전달받는 PasswordAuthentication 객체 생성
		 * - 파라미터 : 메일 서버의 계정명, 패스워드
		 *   => 네이버, Gmail 기준 2단계 인증 미사용 시 : 계정명, 패스워드
		 *   => 네이버, Gmail 기준 2단계 인증 사용 시 : 계정명, 앱 비밀번호
		 *      (단, 사용 메일 계정에 대한 2단계 인증 활성화 및 앱 비밀번호 등록 필요)
		 *      (앱 비밀번호는 로그인 등의 다른 서비스에서는 사용 불가능하며, 특정 서비스에서만 사용)
		 */
//		passwordAuthentication = new PasswordAuthentication("ytlee7066@gmail.com", "cpyjunlriontqmpk");
//		passwordAuthentication = new PasswordAuthentication(sender_mail_address, gmail_app_password);
	}
	// => 스프링 빈으로 관리하면서 자동 주입을 통해 객체를 주입받으므로 생성자 직접 호출하지 않음

	/*
	 * 인증 정보 관리 객체(PasswordAuthentication)를 외부로 리턴하는 getPasswordAuthentication() 메서드 정의
	 * => 주의! Getter 메서드를 직접 정의 시 멤버변수명에 따라 메서드명이 달라지는데
	 *    외부에서 getPasswordAuthentication() 메서드를 직접 호출하는 것이 아니라
	 *    객체 내에서 자동으로 호출되므로 미리 약속된 메서드명으로 정의 필수!
	 * => 슈퍼클래스인 Autheneticator 클래스의 getPasswordAuthentication() 메서드 오버라이딩하자!
	 */
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		System.out.println("MailAuthenticator - sender_mail_address : " + sender_mail_address);
		// 자동 주입을 통해 객체를 주입받을 경우
		// PasswordAuthentication 객체를 현재 메서드 위치에서 생성해야함
		// => 생성자 호출 시점에서는 @Value 어노테이션을 통한 값 불러오기 작업이 수행되기 전임
		passwordAuthentication = new PasswordAuthentication(sender_mail_address, gmail_app_password);
		return passwordAuthentication;
	}
	
}











