package com.itwillbs.mvc_board.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwillbs.mvc_board.service.ChatGPTService;

@Controller
public class ChatGPTController {
	@Autowired
	private ChatGPTService chatGPTService;
	
	@GetMapping("ChatGPTMain")
	public String main() {
		return "gpt/gpt_main";
	}
	
	@GetMapping("ClassRegist")
	public String classRegist() {
		return "gpt/class_regist_form";
	}
	
	// AJAX 요청을 통해 해시코드 생성 후 응답 처리
	@ResponseBody
	@PostMapping("ClassRequestHashtag")
	public String classRequestHashtag(@RequestParam Map<String, String> classInfo) {
		System.out.println("classInfo : " + classInfo);
		
		// ChatGPTService - requestHashtag() 메서드 호출하여 해시태그 생성 요청
		// => 파라미터 : Map 객체(classInfo)   리턴타입 : String(responseData)
		String responseData = chatGPTService.requestHashtag(classInfo);
		
		// 임시) 실제 응답데이터를 responseData 변수에 저장하여 요청없이 활용(뷰페이지 요청 위해)
//		String responseData = "{"
//				+ "  \"id\": \"chatcmpl-AYNaKv9yRghetA7YeNHudgol3bQNL\","
//				+ "  \"object\": \"chat.completion\","
//				+ "  \"created\": 1732757176,"
//				+ "  \"model\": \"gpt-4o-mini-2024-07-18\","
//				+ "  \"choices\": ["
//				+ "    {"
//				+ "      \"index\": 0,"
//				+ "      \"message\": {"
//				+ "        \"role\": \"assistant\","
//				+ "        \"content\": \"#교육,#온라인,#학습,#자기계발,#기술교육,#코딩,#전문지식,#성장,#1대1,#스킬업\","
//				+ "        \"refusal\": null"
//				+ "      },"
//				+ "      \"logprobs\": null,"
//				+ "      \"finish_reason\": \"stop\""
//				+ "    }"
//				+ "  ],"
//				+ "  \"usage\": {"
//				+ "    \"prompt_tokens\": 120,"
//				+ "    \"completion_tokens\": 44,"
//				+ "    \"total_tokens\": 164,"
//				+ "    \"prompt_tokens_details\": {"
//				+ "      \"cached_tokens\": 0,"
//				+ "      \"audio_tokens\": 0"
//				+ "    },"
//				+ "    \"completion_tokens_details\": {"
//				+ "      \"reasoning_tokens\": 0,"
//				+ "      \"audio_tokens\": 0,"
//				+ "      \"accepted_prediction_tokens\": 0,"
//				+ "      \"rejected_prediction_tokens\": 0"
//				+ "    }"
//				+ "  },"
//				+ "  \"system_fingerprint\": \"fp_0705bf87c0\""
//				+ "}";
		System.out.println("ChatGPT 응답 결과 : " + responseData);
		
		// 응답 정보 그대로 응답 데이터로 전송(@ResponseBody 필수)
		return responseData;
	}
	
	// "ClassRegist" 요청(POST)에 대한 클래스 정보 등록 비즈니스 로직
	@PostMapping("ClassRegist")
	public String classRegist(@RequestParam Map<String, String> map, Model model) {
		System.out.println(map);
		// ChatGPTService - registClass() 메서드 호출
		// => 파라미터 : Map 객체   리턴타입 : int
		int insertCount = chatGPTService.registClass(map);
		
		// 작업 요청 결과 판별
		// 성공 시 "ClassList" 서블릿 주소 리다이렉트
		// 실패 시 "fail.jsp" 페이지 포워딩 처리("클래스 등록 실패!")
		if(insertCount > 0) {
			return "redirect:/ClassList";
		} else {
			model.addAttribute("msg", "클래스 등록 실패!");
			return "result/fail";
		}
		
	}
	
	@GetMapping("ClassList")
	public String classList(Model model) {
		// ChatGPTService - getClassList() 메서드 호출하여 클래스 목록 조회 요청
		// => 파라미터 : 없음   리턴타입 : List<Map<String, String>>(classList)
		List<Map<String, String>> classList = chatGPTService.getClassList();
		
		// Model 객체에 목록 저장
		model.addAttribute("classList", classList);
		
		return "gpt/class_list";
	}
	
	@GetMapping("ClassInfo")
	public String classInfo(String class_id, Model model) {
		// ChatGPTService - getClassInfo() 메서드 호출하여 클래스 목록 조회 요청
		// => 파라미터 : 클래스ID   리턴타입 : Map<String, String>(classInfo)
		Map<String, String> classInfo = chatGPTService.getClassInfo(class_id);
		
		// Model 객체에 목록 저장
		model.addAttribute("classInfo", classInfo);
		
		return "gpt/class_info";
	}
	
}












