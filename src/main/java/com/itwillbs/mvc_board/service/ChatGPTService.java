package com.itwillbs.mvc_board.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itwillbs.mvc_board.handler.ChatGPTClient;
import com.itwillbs.mvc_board.mapper.ChatGPTMapper;

import lombok.extern.log4j.Log4j2;

@Service
public class ChatGPTService {
	@Autowired
	private ChatGPTMapper mapper;
	
	@Autowired
	private ChatGPTClient client;

	// 해시태그 요청
	public String requestHashtag(Map<String, String> classInfo) {
		return client.requestHashtag(classInfo); 
	}

	// 클래스 등록 요청
	public int registClass(Map<String, String> map) {
		return mapper.insertClass(map);
	}

	// 클래스 목록 요청
	public List<Map<String, String>> getClassList() {
		return mapper.selectClassList();
	}

	// 클래스 상세정보 요청
	public Map<String, String> getClassInfo(String class_id) {
		return mapper.selectClassInfo(class_id);
	}
}












