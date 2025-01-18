package com.itwillbs.mvc_board.handler;

import com.itwillbs.mvc_board.vo.PageInfo2;

public class PageInfoToJson {
	
	public static String toJson(PageInfo2 pageInfo) {
		// PageInfo2 객체의 멤버변수값을
		// "멤버변수명" : 데이터 형식의 문자열로 결합
		String data = "{"; // 객체의 시작 기호 { 를 기본값으로 저장
		
		data += "\"listCount\":" + pageInfo.getListCount();
		data += ",\"pageListLimit\":" + pageInfo.getPageListLimit();
		data += ",\"maxPage\":" + pageInfo.getMaxPage();
		data += ",\"startPage\":" + pageInfo.getStartPage();
		data += ",\"endPage\":" + pageInfo.getEndPage();
		data += ",\"pageNum\":" + pageInfo.getPageNum();
		data += "}";
		
		return data;
	}
	
}







