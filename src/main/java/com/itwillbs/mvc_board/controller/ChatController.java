package com.itwillbs.mvc_board.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.itwillbs.mvc_board.aop.LoginCheck;
import com.itwillbs.mvc_board.aop.LoginCheck.MemberRole;
import com.itwillbs.mvc_board.handler.FileHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class ChatController {
	// 파일 처리를 전담하는 FileHandler 객체 주입
	@Autowired
	private FileHandler fileHandler;
	// ==========================================================
	// 통합 채팅방 메인페이지 요청(일반 사용자 세션 체크)
	@LoginCheck(memberRole = MemberRole.USER)
	@GetMapping("ChatMain")
	public String chatMain() {
		return "chat/chat_main";
	}
	
	// 통합 채팅방 메인페이지 요청(일반 사용자 세션 체크)
	@LoginCheck(memberRole = MemberRole.USER)
	@GetMapping("ChatMain2")
	public String chatMain2() {
		return "chat/chat_main_window";
	}
	
	// ==================================================
	// 채팅방 파일 업로드 처리
	@ResponseBody
	@PostMapping("ChatFileUpload")
	public String chatFileUpload(MultipartFile file, HttpSession session) {
		System.out.println("업로드 파일 : " + file);
		System.out.println("파일 종류 : " + file.getContentType());
		
		Map<String, String> uploadResult = new HashMap<String, String>();
		
		// 만약, 이미지 파일이 아닐 경우 업로드 작업 수행하지 않도록 처리
		if(!file.getContentType().startsWith("image/")) { // 이미지 파일이 아닐 경우
			uploadResult.put("result", "fail");
			uploadResult.put("message", "이미지 파일만 업로드 가능합니다!");
		} else { // 이미지 파일일 경우
			// 파일 업로드 처리
			// FileHandler - getRealPath() 메서드 호출하여 가상 경로에 대한 실제 경로 리턴받기
			// => 파라미터 : HttpSession 객체, 가상 업로드 경로   리턴타입 : String(실제 경로)
			String realPath = fileHandler.getRealPath(session, "/resources/upload/chat");
			System.out.println("realPath : " + realPath);
			
			// FileHandler - processDuplicateFileName() 메서드 호출하여 파일 중복 방지 처리
			// => 파라미터 : 원본 파일명   리턴타입 : String(중복방지 처리된 파일명)
			String fileName = fileHandler.processDuplicateFileName(file.getOriginalFilename());
			System.out.println("fileName : " + fileName);
			
			// FileHandler - completeUpload() 메서드 호출하여 파일 업로드 완료(임시파일 이동) 처리
			// => 파라미터 : MultipartFile 객체, 실제 업로드 경로, 중복 처리된 파일명
			// => 리턴타입 : String(실제 업로드 된 파일명)
			String uploadFileName = fileHandler.completeUpload(file, realPath, fileName);
			System.out.println("uploadFileName : " + uploadFileName);
			
			// 파일 업로드 성공 시(uploadFileName 이 널스트링이 아님)
			// 해당 이미지 파일에 대한 썸네일 이미지 생성하고
			// 원본 파일과 썸네일 이미지 파일의 이름을 Map 객체에 저장
			if(!uploadFileName.equals("")) {
				// FileHandler - createThumbnailImage() 메서드 호출하여 썸네일 이미지 생성
				// => 파라미터 : 실제 업로드 경로, 업로드 된 파일명   리턴타입 : String(썸네일 이미지 파일명)
				String thumbnailFileName = fileHandler.createThumbnailImage(realPath, uploadFileName);
				System.out.println("thumbnailFileName : " + thumbnailFileName);
				
				// 원본 파일 이름과 썸네일 파일 이름을 Map 객체에 추가
				uploadResult.put("fileName", uploadFileName);
				uploadResult.put("thumbnailFileName", thumbnailFileName);
				
//				uploadResult.put("result", "success");
			}
		}
		
		// Map 객체(uploadResult)를 JSON 형식으로 변환하여 응답데이터로 전송
		return new Gson().toJson(uploadResult);
	}
	
}










