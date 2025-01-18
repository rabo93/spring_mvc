package com.itwillbs.mvc_board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
회원 이메일 인증 정보를 관리할 mail_auth_info 테이블 정의
------------------------------------------------------------------------------------
이메일(email) - 50글자, PK, FK(참조 : member 테이블의 email 컬럼 - DELETE 옵션 추가)
인증코드(auth_code) - 50글자, NN
------------------------------------------------------------------------------------
CREATE TABLE mail_auth_info (
	email VARCHAR(50) PRIMARY KEY,
	auth_code VARCHAR(50) NOT NULL,
	FOREIGN KEY (email) REFERENCES member(email) ON DELETE CASCADE
);
*/
// 인증 정보 1개를 관리하는 MailAuthInfo 클래스 정의
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailAuthInfo {
	private String email;
	private String auth_code;
}
















