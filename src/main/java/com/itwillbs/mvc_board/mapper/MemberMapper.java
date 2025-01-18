package com.itwillbs.mvc_board.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.itwillbs.mvc_board.vo.MailAuthInfo;
import com.itwillbs.mvc_board.vo.MemberVO;

@Mapper
public interface MemberMapper {

	// 회원 가입
	int insertMember(MemberVO member);

	// 회원 패스워드 조회
	String selectMemberPasswd(String id);

	// 회원 상세정보 조회
	MemberVO selectMember(MemberVO member);

	// 회원 정보 수정
	int updateMember(Map<String, String> map);

	// 회원 탈퇴(파라미터가 2개이므로 @Param 어노테이션 필요)
	int updateMemberStatus(@Param("id") String id, @Param("member_status") int member_status);

	// 이메일 인증 정보 조회
	MailAuthInfo selectMailAuthInfo(MailAuthInfo mailAuthInfo);

	// 이메일 인증 정보 등록
	void insertMailAuthInfo(MailAuthInfo mailAuthInfo);

	// 이메일 인증 정보 수정(갱신)
	void updateMailAuthInfo(MailAuthInfo mailAuthInfo);

	// 이메일 인증 상태 수정(갱신)
	void updateMailAuthStatus(MailAuthInfo mailAuthInfo);

	// 이메일 인증 정보 삭제
	void deleteMailAuthInfo(MailAuthInfo mailAuthInfo);

	// ==============================================================
	// 채팅 - 사용자 아이디 조회
	String selectMemberId(String id);

}













