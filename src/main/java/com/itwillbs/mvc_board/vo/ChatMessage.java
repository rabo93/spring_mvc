package com.itwillbs.mvc_board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 웹소켓 채팅 메세지를 자동 파싱하기 위한 클래스 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	private String type;
	private String message;
	private String sender_id;
	
	// type 변수에 사용될 값을 상수로 생성
	public static final String TYPE_ENTER = "ENTER";
	public static final String TYPE_LEAVE = "LEAVE";
	public static final String TYPE_TALK = "TALK";
}












