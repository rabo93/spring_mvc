package com.itwillbs.mvc_board.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itwillbs.mvc_board.mapper.ChatMapper;
import com.itwillbs.mvc_board.vo.ChatMessage2;
import com.itwillbs.mvc_board.vo.ChatRoom;

@Component
public class ChatService {
	@Autowired
	private ChatMapper mapper;
	
	// 기존 자신의 채팅방 목록 조회 요청
//	public List<ChatRoom> getChatRoomList(String sender_id) {
	public List<Map<String, String>> getChatRoomList(String sender_id) {
		return mapper.selectChatRoomList(sender_id);
	}

	// 기존 상대방과의 채팅방 조회 요청
	public ChatRoom getChatRoom(String sender_id, String receiver_id) {
		return mapper.selectChatRoom(sender_id, receiver_id);
	}

	// --------------------------------
	// 새 채팅방 정보 저장 요청
	public void addChatRoom(ChatRoom chatRoom) {
		mapper.insertChatRoom(chatRoom);
	}
	
	// 새 채팅방 정보 저장 요청2(List 타입 파라미터)
	public void addChatRoom2(List<ChatRoom> chatRoomList) {
		mapper.insertChatRoom2(chatRoomList);
	}
	// --------------------------------
	// 채팅방 메세지 저장 요청
	public void addChatMessage(ChatMessage2 message) {
		mapper.insertChatMessage(message);
	}

	// 기존 채팅 내역 조회 요청
	public List<ChatMessage2> getChatMessageList(ChatMessage2 chatMessage) {
		// 1) 채팅 내역 조회
		// Mapper - selectChatMessageList()
		List<ChatMessage2> chatMessageList = mapper.selectChatMessageList(chatMessage);
		
		// 2) 조회된 채팅 내역 읽음 표시 처리(단, 채팅 내역이 존재할 경우에만 수행)
		// Mapper - updateChatMessageReadState()
		if(chatMessageList != null && chatMessageList.size() > 0) {
			mapper.updateChatMessageReadState(chatMessage);
		}
		
		return chatMessageList;
	}

	// 메세지 읽음 표시 처리 요청
	public void updateChatMessageReadState2(ChatMessage2 chatMessage) {
		mapper.updateChatMessageReadState2(chatMessage);
	}

}













