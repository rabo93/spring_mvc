package com.itwillbs.mvc_board.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.mvc_board.mapper.MemberMapper;
import com.itwillbs.mvc_board.vo.MailAuthInfo;
import com.itwillbs.mvc_board.vo.MemberVO;

@Service
public class MemberService {
	@Autowired
	private MemberMapper mapper;

	// 회원 가입 요청
	public int registMember(MemberVO member) {
		// MemberMapper - insertMember()
		return mapper.insertMember(member);
	}

	// 회원 패스워드 조회 요청
	public String getMemberPasswd(String id) {
		// MemberMapper - selectMemberPasswd()
		return mapper.selectMemberPasswd(id);
	}

	// 회원 상세정보 조회 요청
	public MemberVO getMember(MemberVO member) {
		// MemberMapper - selectMember()
		return mapper.selectMember(member);
	}

	// 회원 정보 수정 요청
	public int modifyMember(Map<String, String> map) { 
		// MemberMapper - updateMember()
		return mapper.updateMember(map);
	}

	// 회원 탈퇴 요청
	public int withdrawMember(String id) {
		// MemberMapper - updateMemberStatus()
		// => 파라미터 : 아이디, 회원상태값(정수, 1 : 정상, 2 : 휴면, 3 : 탈퇴)
		return mapper.updateMemberStatus(id, 3);
	}

	// 이메일 인증 정보 등록 요청
	public void registMailAuthInfo(MailAuthInfo mailAuthInfo) {
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

	// 이메일 인증 처리 요청
	public boolean requestEmailAuth(MailAuthInfo mailAuthInfo) {
		boolean isAuthSuccess = false;
		
		// MemberMapper - selectMailAuthInfo() 메서드 호출하여 인증정보 조회 수행(재사용)
		// => 파라미터 : MailAuthInfo 객체   리턴타입 : MailAuthInfo(dbMailAuthInfo)
		MailAuthInfo dbMailAuthInfo = mapper.selectMailAuthInfo(mailAuthInfo);
		System.out.println("조회된 인증 정보 : " + dbMailAuthInfo);
		
		// 인증정보 조회 결과 판별
		if(dbMailAuthInfo != null) { // 이메일에 대한 인증 코드가 존재할 경우
			// 하이퍼링크를 통해 전달받은 인증코드와 조회된 인증코드 문자열 비교 수행
			if(mailAuthInfo.getAuth_code().equals(dbMailAuthInfo.getAuth_code())) { // 인증코드 일치
				// 1) MemberMapper - updateMailAuthStatus() 메서드 호출하여
				//    member 테이블의 이메일 인증 상태(mail_auth_status) 값을 "Y" 로 변경(UPDATE)
				//    => 파라미터 : MailAuthInfo 객체
				mapper.updateMailAuthStatus(mailAuthInfo);
				
				// 2) MemberMapper - deleteMailAuthInfo() 메서드 호출하여
				//    mail_auth_info 테이블의 인증 정보(레코드) 삭제(DELETE)
				//    => 파라미터 : MailAuthInfo 객체
				mapper.deleteMailAuthInfo(mailAuthInfo);
				
				// 3) 인증 수행 결과 처리를 위해 isAuthSuccess 변수값을 true 로 변경
				isAuthSuccess = true;
			}
		} 
		
		return isAuthSuccess;
	}

	// ==============================================================
	// 채팅 - 사용자 아이디 조회 요청
	public String getMemberId(String id) {
		return mapper.selectMemberId(id);
	}
}

























