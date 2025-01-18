package com.itwillbs.mvc_board.vo;

import java.io.Serializable;

import lombok.Data;

/*
 * [ spring_mvc_board3.bank_access_token 테이블 정의 ]
 * ---------------------------------------------------
 * 사용자 아이디(id) - 문자 16자, PK
 * ----------
 * 사용자일련번호(user_seq_no) - 문자+숫자 10자, UN, NN
 * 엑세스토큰(access_token) - 문자 400자, UN, NN
 * 토큰타입(token_type) - 문자 10자, NN
 * 토큰만료시간(expire_in) - 정수(초), NN
 * 갱신토큰(refresh_token) - 문자 400자, UN, NN
 * 토큰권한범위(scope) - 문자 100자, NN
 * ---------------------------------------------------
	CREATE TABLE bank_access_token (
		id VARCHAR(16) PRIMARY KEY,
		user_seq_no VARCHAR(10) UNIQUT NOT NULL,
		access_token VARCHAR(400) UNIQUE NOT NULL,
		token_type VARCHAR(10) NOT NULL,
		expire_in INT NOT NULL,
		refresh_token VARCHAR(400) UNIQUE NOT NULL,
		scope VARCHAR(100) NOT NULL
	);
 */

// 2.1.2. 토큰발급 API  - 사용자 토큰발급 API (3-legged) 요청에 대한 응답 데이터를 관리할 클래스
// => 멤버변수로 응답 데이터 파라미터명과 1:1 로 대응하는 멤버변수 선언
//    (주의! 자동으로 응답 데이터를 파싱하기 때문에 멤버변수명이 완벽하게 일치해야함)
// => BankToken 객체를 세션 객체에 저장해야할 경우 
//    톰캣 서버 재시작(또는 Reload) 시 세션 정보를 서버상에 저장 후 다시 복원하는 과정을 거치는데
//    이 과정에서 세션 객체를 직렬화를 통해 내보내기가 수행되고, 역직렬화로 복원시킨다.
//    이 때, 세션에 저장된 모든 클래스타입 객체들은 직렬화가 가능한 객체여야하며
//    일반적으로 자바에 내장된 클래스들은 직렬화가 가능한 클래스로 정의되어 있지만
//    사용자 정의 클래스는 개발자가 직접 직렬화 가능한 클래스로 지정해야한다! (implements Serialzable)
//    (또는 각각의 데이터를 VO 등의 객체가 아닌 각각의 String 타입 등의 단일 값으로 저장하면 됨)
//    (이 작업은 개발 과정에서만 필요하며 실제 서비스 중에는 재시작 시 모든 정보가 제거되도록 함)
@Data
public class BankToken implements Serializable {
	// -------------------------------------------------------
	// 사용자 아이디 정보
	private String id;
	// -------------------------------------------------------
	// 엑세스토큰 관련 정보
	private String access_token;
	private String token_type;
	private int expires_in;
	private String refresh_token;
	private String scope;
	private String user_seq_no;
	// -------------------------------------------------------
	// 토큰 발급 요청 과정에서 오류 발생 시 응답 메세지의 오류 정보
	private String rsp_code;
	private String rsp_message;
	// -------------------------------------------------------
//	private String fintech_use_num;
	// -------------------------------------------------------
}












