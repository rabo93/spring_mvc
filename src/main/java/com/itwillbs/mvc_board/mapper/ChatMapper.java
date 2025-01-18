package com.itwillbs.mvc_board.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.itwillbs.mvc_board.vo.ChatMessage2;
import com.itwillbs.mvc_board.vo.ChatRoom;

@Mapper
public interface ChatMapper {
	// 기존 자신의 채팅방 목록 조회
//	List<ChatRoom> selectChatRoomList(String sender_id);
	List<Map<String, String>> selectChatRoomList(String sender_id);

	// 기존 상대방과의 채팅방 조회
	ChatRoom selectChatRoom(@Param("sender_id") String sender_id, @Param("receiver_id") String receiver_id);
	
	// --------------------------------
	// 새 채팅방 정보 저장
	void insertChatRoom(ChatRoom chatRoom);

	// 새 채팅방 정보 저장2(List 타입 파라미터)
	void insertChatRoom2(List<ChatRoom> chatRoomList);
	// --------------------------------
	
	// 채팅 메세지 저장
	void insertChatMessage(ChatMessage2 message);

	// 기존 채팅 내역 조회
	List<ChatMessage2> selectChatMessageList(ChatMessage2 chatMessage);

	// 조회된 채팅 내역 읽음 표시 처리
	void updateChatMessageReadState(ChatMessage2 chatMessage);

	// 메세지 읽음 표시 처리
	void updateChatMessageReadState2(ChatMessage2 chatMessage);


}












