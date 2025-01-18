package com.itwillbs.mvc_board.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChatGPTClient {
	@Value("${gpt.API_KEY}")
	private String apiKey;
	
	private String url = "https://api.openai.com/v1/chat/completions";
	private String model = "gpt-4o-mini"; // GPT 모델
	private double temperature = 0.5; // 답변 온도(메서드별로 다르게 설정해도 됨)

	public String requestHashtag(Map<String, String> classInfo) {
		// [ REST API 요청을 위한 HTTP 통신 ]
		// ------------------------------ 헤더 정보 설정 -----------------------------------
		// 1. HTTP 헤더 정보를 관리할 org.springframework.http.HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		
		// 2. 헤더 정보 설정
		// 2-1) 전송할 요청 정보의 컨텐츠 타입이 JSON 형식이므로 헤더 정보 중 컨텐츠 타입 변경을 위해
		//      HttpHeaders 객체의 setContentType() 메서드 호출하여 JSON 타입 지정
		//      => 전달할 파라미터로 JSON 타입 지정을 위한 MediaType.APPLICATION_JSON 상수 활용
		//         (APPLICATION_JSON_UTF8 상수는 Deprecated)
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// 2-2) API KEY(= 엑세스 토큰) 를 사용하여 인증을 위해 인증 토큰 설정
//		headers.add("Authorization", "Bearer " + apiKey); // 기본 방식
		headers.setBearerAuth(apiKey); // Bearer 토근을 설정하는 전용 메서드
		
		// -----------------------------------------------------------------------------------
		// 3. 요청 파라미터 생성(JSON 형식)
		// => 단, JSON 형식 정보를 생성하기 위해 먼저 각각의 정보들을 Map 또는 List 객체로 생성
		// 3-1) 프롬프트(요청 메세지) 생성(Map 객체 활용하여 Key 와 Value 설정)
		//      => role 은 system 과 user 로 각각 생성하고
		//         system 의 content 는 프리셋(사전 정보) 설정, user 의 content 는 요청 질문 설정
		// 참고) system role 은 필수 요소는 아님! user role 에 프리셋도 포함할 수 있음
		Map<String, String> roleSystem = new HashMap<String, String>();
		roleSystem.put("role", "system");
		roleSystem.put("content", "너는 온라인 교육 사이트 교육 안내원 역할이야."
				+ "우리 사이트에서 교육하는 내용을 제공할테니 해시태그 10개 생성해줘."
				+ "단, 해시태그 1개는 최대 5글자 내에서 주로 한글과 숫자만 사용하되"
				+ "전문용어는 영문자를 섞어도 상관없고, 각 해시태그 사이는 공백없이 콤마(,)로 연결하고."
				+ "설명 제외하고 해시태그만 보여줘.");
		
		Map<String, String> roleUser = new HashMap<String, String>();
		roleUser.put("role", "user");
		// 요청 프롬프트는 Map 객체의 "class_subject", "class_content" 값 결합하여 전달 
		roleUser.put("content", classInfo.get("class_subject") + "\n" + classInfo.get("class_content"));
		
		// 각각의 Map 객체를 하나의 List 객체로 결합(=> array 형식으로 변환하기 위함)
		List<Map<String, String>> messages = new ArrayList<Map<String,String>>();
		messages.add(roleSystem);
		messages.add(roleUser);
		
		// 3-2) 전체 요청 정보를 JSONObject 객체를 활용하여 JSON 형식으로 생성
		JSONObject requestData = new JSONObject();
		requestData.put("model", model);
		requestData.put("temperature", temperature);
		requestData.put("messages", messages);
		// -----------------------------------------------------------------------------
		// 4. HTTP 요청 정보 전체를 관리할 HttpEntity 객체 생성
		// => 모든 정보를 문자열로 관리하기 위해 제네릭타입 String 지정
		// => 파라미터 : body 정보(JSONObject 객체를 문자열로 변환)와 header 정보
		HttpEntity<String> httpEntity = new HttpEntity<String>(requestData.toString(), headers);
		System.out.println("HttpEntity : " + httpEntity);
		
		// 5. RESTful API 요청에 사용할 RestTemplate 객체 생성
		RestTemplate restTemplate = new RestTemplate();
		
		// 6. RestTemplate 객체의 XXX() 메서드를 호출하여 API 요청 수행(실제 요청이 발생하는 지점)
		//    => 다양한 요청 메서드 중 exchange() 메서드 사용
		//       (POST 방식 요청을 사용하기 위해 postForObject() 메서드 활용도 가능함)
		//    => 파라미터 : 요청 URL, HTTP 요청 메서드, HttpEntity 객체, 응답데이터타입
		//    => 이 때, 응답데이터타입으로 클래스 타입 지정 시 XXX.class 형태로 지정
		//       단, 응답되는 JSON 데이터를 자동으로 파싱하려면 ParameterizedTypeReference 타입 활용
		//    => 리턴타입 : ResponseEntity<T> 이며, 제네릭타입은 마지막 파라미터로 지정한 클래스 타입
		ResponseEntity<String> responseEntity = 
				restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
//		System.out.println(responseEntity);
		
		// 7. 응답데이터가 저장된 ResponseEntity 객체의 getBody() 메서드 호출하여
		//    응답 데이터 중 본문만 추출하여 지정된 제네릭타입 객체로 리턴
//		System.out.println(responseEntity.getBody()); // 응답데이터 중 본문(ChatGPT 응답 내용)만 추출
		return responseEntity.getBody();
	}
	
}























