package com.itwillbs.mvc_board.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatGPTMapper {

	// 클래스 등록
	int insertClass(Map<String, String> map);

	// 클래스 목록 조회
	List<Map<String, String>> selectClassList();

	// 클래스 상세정보 조회
	Map<String, String> selectClassInfo(String class_id);


}












