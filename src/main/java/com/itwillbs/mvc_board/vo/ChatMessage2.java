package com.itwillbs.mvc_board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
[ 채팅 메세지를 저장할 chat_message 테이블 정의 ]
idx - INT PK AI
room_id - 50글자 NN
sender_id - 16글자 NN
receiver_id - 16글자 NN
message - 2000글자 NN
type - 20글자 NN
send_time - 날짜 및 시각(DATETIME) NN
read_state - 메세지 읽음(1)/읽지않음(0) 여부
--------------------------------------
CREATE TABLE chat_message (
	idx INT PRIMARY KEY AUTO_INCREMENT,
	room_id VARCHAR(50) NOT NULL,
	sender_id VARCHAR(16) NOT NULL,
	receiver_id VARCHAR(16) NOT NULL,
	message VARCHAR(2000) NOT NULL,
	type VARCHAR(20) NOT NULL,
	send_time DATETIME NOT NULL,
	read_state INT NOT NULL
);
*/
// 웹소켓 채팅 메세지를 자동 파싱하기 위한 클래스 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage2 {
	private int idx;
	private String type;
	private String sender_id;
	private String receiver_id;
	private String room_id;
	private String message;
	private String send_time;
	private int read_state;
	
	// type 변수에 사용될 값을 상수로 생성
	public static final String TYPE_ENTER = "ENTER";
	public static final String TYPE_LEAVE = "LEAVE";
	public static final String TYPE_TALK = "TALK";
	public static final String TYPE_INIT = "INIT";
	public static final String TYPE_INIT_COMPLETE = "INIT_COMPLETE";
	public static final String TYPE_ERROR = "ERROR";
	public static final String TYPE_START = "START";
	public static final String TYPE_REQUEST_CHAT_LIST = "REQUEST_CHAT_LIST";
	public static final String TYPE_FILE_UPLOAD_COMPLETE = "FILE_UPLOAD_COMPLETE";
	public static final String TYPE_FILE = "FILE";
	public static final String TYPE_READ = "READ"; // 메세지 읽음
}












