package com.itwillbs.mvc_board.handler;

import java.net.URI;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.itwillbs.mvc_board.vo.BankToken;

import lombok.extern.log4j.Log4j2;

// 금융결제원 오픈뱅킹 API 요청에 사용할 클래스 정의
@Log4j2
@Component
public class BankApiClient {
	// 오픈뱅킹 API 요청에 사용할 값들을 properties 파일로부터 읽어서 변수에 자동 저장
	@Value("${bank.base_url}")
	private String base_url;
	
	@Value("${bank.redirect_uri}")
	private String redirect_uri;
	
	@Value("${bank.client_id}")
	private String client_id;
	
	@Value("${bank.client_secret}")
	private String client_secret;
	
	@Value("${bank.client_use_code}")
	private String client_use_code;
	
	// 이용기관 등록계좌번호
	@Value("${bank.cntr_account_num}")
	private String cntr_account_num;
	
	// 이용기관 등록계좌 개설기관 표준코드
	@Value("${bank.cntr_account_bank_code}")
	private String cntr_account_bank_code;
	
	// 이용기관 등록계좌 예금주명
	@Value("${bank.cntr_account_holder_name}")
	private String cntr_account_holder_name;
	
	private String grant_type = "authorization_code";
	// -----------------------------------------------

	// 2.1.2. 토큰발급 API - 사용자 토큰발급 API (3-legged) 요청
	// => 요청 URL 은 base_url 변수에 저장된 기본 주소 + 토큰발급 API 상세주소 결합하여 사용
	//    https://testapi.openbanking.or.kr/oauth/2.0/token
	//                                     ~~~~~~~~~~~~~~~~ 이 부분만 기본주소에 결합하여 사용
	// => 요청 파라미터 : code, client_id, client_secret, redirect_uri, grant_type
	public BankToken requestAccessToken(Map<String, String> authResponse) {
		// 금융결제원 오픈API 토큰 발급 API 요청 작업 수행 및 결과 처리
		// ------------------------------------------------------------
		// 자바 클래스 내에서 HTTP 요청(통신)을 수행하는 여러 라이브러리 중
		// REST API 요청을 처리할 수 있는 RestTemplate 객체 활용하여 HTTP 요청
		// ------------------------------------------------------------
		// 1. POST 방식 요청을 수행할 URL 정보를 URI 타입 객체 또는 문자열로 생성
//		String url = base_url + "/oauth/2.0/token";
		URI uri = UriComponentsBuilder
					.fromUriString(base_url + "/oauth/2.0/token") // 요청 주소 생성
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. POST 방식 요청 수행 시 파라미터를 URL 에 결합할 수 없으므로 body 에 포함시켜야 함
		//    따라서, 포함시킬 파라미터 데이터를 별도의 객체를 통해 전달 필요
		//    => Map 객체 대신 MultiValueMap 타입 객체 활용(HttpEntity 객체에서 필요로 함)
		//       (참고. 자바 라이브러리가 아닌 스프링 라이브러리이며, 구현체로 LinkedMultiValueMap 클래스 활용)
		//    => 주의! 업캐스팅 없이 LinkedMultiValueMap 타입 변수로 선언
		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		// 일반 Map 과 달리 put() 메서드 대신 add() 메서드 사용하여 데이터 추가
		parameters.add("code", authResponse.get("code"));
		parameters.add("client_id", client_id);
		parameters.add("client_secret", client_secret);
		parameters.add("redirect_uri", redirect_uri);
		parameters.add("grant_type", grant_type);
		
		// 3. 요청 정보로 사용할 헤더(지금은 불필요)와 바디 정보를 관리하는 HttpEntity 객체 생성
		//    => 제네릭타입으로 파라미터 관리 객체 타입(Map<String, String>) 지정 후
		//       생성자에 파라미터 관리 객체 전달
		HttpEntity<LinkedMultiValueMap<String, String>> httpEntity = 
				new HttpEntity<LinkedMultiValueMap<String,String>>(parameters);
		System.out.println("요청 정보 : " + httpEntity);
		
		// 4. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		// 4-1) RestTemplate 객체 생성
		RestTemplate restTemplate = new RestTemplate();
		
		// 4-2). RestTemplate 객체의 XXX() 메서드를 호출하여 API 요청 수행(실제 요청이 발생하는 지점)
		//    => 다양한 요청 메서드 중 exchange() 메서드 사용
		//       (POST 방식 요청을 사용하기 위해 postForObject() 메서드 활용도 가능함)
		//    => 파라미터 : 요청 URL, HTTP 요청 메서드, HttpEntity 객체, 응답데이터타입
		//    => 이 때, 응답데이터타입으로 클래스 타입 지정 시 XXX.class 형태로 지정
		//       단, 응답되는 JSON 데이터를 자동으로 파싱하려면 ParameterizedTypeReference 타입 활용
		//    => 리턴타입 : ResponseEntity<T> 이며, 제네릭타입은 마지막 파라미터로 지정한 클래스 타입
		ResponseEntity<BankToken> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.POST,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				BankToken.class); // 응답 데이터를 파싱하여 관리할 클래스(.class 필수!)
		// 주의! 응답 데이터로 전달되는 JSON 타입 데이터를 String 타입이 아닌 
		// 특정 클래스 타입으로 자동 파싱하려면 Gson 또는 Jackson 라이브러리가 필요함
		// 이 라이브러리가 존재하지 않을 경우 자동 파싱이 불가능하여 실행 시 예외 발생함
		// org.springframework.web.client.UnknownContentTypeException: Could not extract response: no suitable HttpMessageConverter found for response type [class com.itwillbs.mvc_board.vo.BankToken] and content type [application/json;charset=UTF-8]
		// -------------------------------------------------------------------------------------
		// 추가사항> 요청 과정에서 오류 발생 시 오류 코드(rsp_code)와 오류 메세지(rsp_message)가 전달되는데
		//           VO 클래스를 통해 파싱할 경우 코드와 메세지에 해당하는 변수가 없으면 오류 확인이 힘들다!
		//           => VO 클래스에 오류 코드 및 메세지에 해당하는 멤버변수를 추가하거나
		//              VO 클래스 대신 Map 타입을 응답 데이터 저장용으로 지정하면 된다!
		// -------------------------------------------------------------------------------------

		log.info("응답 코드 : " + responseEntity.getStatusCode());
		log.info("응답 헤더 : " + responseEntity.getHeaders());
		log.info("응답 본문 : " + responseEntity.getBody());
		
		// 5. 응답데이터가 저장된 ResponseEntity 객체의 getBody() 메서드 호출하여
		//    응답 데이터 중 본문만 추출하여 지정된 제네릭타입 객체로 리턴
//				System.out.println(responseEntity.getBody()); // 응답데이터 중 본문(ChatGPT 응답 내용)만 추출
		return responseEntity.getBody();
	}

	// ===================================================================
	// 2.2. 사용자/서비스 관리 - 2.2.1. 사용자정보조회 API (GET)
	// https://testapi.openbanking.or.kr/v2.0/user/me
	public Map<String, Object> requestBankUserInfo(BankToken token) {
		// 1. POST 방식 요청을 수행할 URL 정보를 URI 타입 객체 또는 문자열로 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url) // 기본 요청 주소 생성
					.path("/v2.0/user/me") // 상세 요청 주소(세부 경로) 생성
//					.path("/v2.0") // path() 메서드 복수개 순차적 연결도 가능
//					.path("/user")
//					.path("/me")
					.queryParam("user_seq_no", token.getUser_seq_no()) // GET 방식 요청 파라미터
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		// => https://testapi.openbanking.or.kr/v2.0/user/me?user_seq_no=1101002290
		
		// 2. 사용자 정보 조회 API 요청에 사용될 헤더정보를 관리할 HttpHeaders 객체 생성
		// 2-1) HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		// 2-2) HttpHeaders 객체에 엑세스토큰 추가(속성명 : "Authorization", add() 메서드 활용)
//		headers.add("Authorization", token.getAccess_token());
		// 또는 인증용 Bearer 토큰 정보를 설정하는 setBearerAuth() 메서드 통해 토큰값 설정도 가능
		headers.setBearerAuth(token.getAccess_token());
		
		// 3. 요청 정보로 사용할 헤더와 바디(지금은 불필요) 정보를 관리하는 HttpEntity 객체 생성
		//    => 바디 정보 없이 헤더 정보만 전달(생성자에 HttpHeaders 객체 전달)
		//    => 모든 헤더 정보가 문자열로 관리되므로 제네릭타입으로 String 타입 지정
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		System.out.println("요청 정보 : " + httpEntity);
		
		// 4. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		// 4-1) RestTemplate 객체 생성
		RestTemplate restTemplate = new RestTemplate();
		
		// 4-2). RestTemplate 객체의 exchange() 메서드를 HTTP(REST API) 요청 수행 - GET
		//       (GET 방식 요청을 사용하기 위해 getForObject() 메서드 활용도 가능함)
		//    => 파라미터 : 요청 URL(URI 객체), HTTP 요청 메서드, HttpEntity 객체, 응답데이터타입
		//    => 리턴타입 : ResponseEntity<T>
		//    => 이 때, 응답데이터타입으로 클래스 타입 지정 시 ResponseEntity<Map<String, Object>> 로 지정
		//       단, Map 타입 지정을 통해 응답되는 JSON 데이터를 자동으로 파싱하려면 
		//       ParameterizedTypeReference 객체를 통해 관리되어야 함
		// -------------------
		// Map 타입 지정 시 제네릭타입을 사용하기 위해 ResponseEntity<Map<K, V>> 를 지정하더라도
		// exchange() 메서드에서 Map.class 지정 시 Map<K, V>.class 형태로 지정 불가능하다!
		// 따라서, ParameterizedTypeReference 타입을 활용하여 파싱될 클래스에 제네릭타입을 지정한
		// 별도의 객체를 생성한 후 ParameterizedTypeReference 객체를 파싱 타입 클래스로 지정하는 방법 사용
		// => 이 때, 객체 생성 시 추상클래스 뒤의 구현체 중괄호 블럭{} 표기
		ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<Map<String,Object>>() {};
		// => 응답데이터 중 res_list(계좌목록) 값이 리스트 형태의 "객체" 이므로
		//    제네릭타입을 <String, String> 대신 <String, Object> 타입으로 지정
		
		// exchange() 메서드 마지막 파라미터로 파싱 클래스 지정 시
		// ParameterizedTypeReference 객체를 지정하고
		// 리턴값을 지정하는 ResponseEntity 의 제네릭타입은 실제 파싱될 제네릭타입을 그대로 기술
		ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.GET,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				responseType); // 응답 데이터를 파싱하여 관리할 클래스
		// -------------------------------------------------------------------------------------

		log.info("응답 코드 : " + responseEntity.getStatusCode());
		log.info("응답 헤더 : " + responseEntity.getHeaders());
		log.info("응답 본문 : " + responseEntity.getBody());
		
		// 5. 응답데이터가 저장된 ResponseEntity 객체의 getBody() 메서드 호출하여
		//    응답 데이터 중 본문만 추출하여 지정된 제네릭타입 객체로 리턴
		return responseEntity.getBody();
	}

	// ===================================================================
	// 2.3. 계좌조회 서비스(사용자) - 2.3.1. 잔액조회 API (GET)
	// https://testapi.openbanking.or.kr/v2.0/account/balance/fin_num
	// => 주의! 잔액조회 테스트 데이터가 등록되어 있을 경우에만 정상적인 응답 처리됨
	//    (테스트 데이터가 없거나 일치하지 않을 경우 응답 데이터로 에러가 전송됨)
	// => 테스트 데이터 등록 : 로그인 후 MY PAGE - 테스트 정보 관리 - 응답정보관리 - API명 : 잔액조회 선택
	//    (사용자일련번호 선택 -> 핀테크이용번호 선택 시 계좌정보 자동 입력됨)
	public Map<String, String> requestAccountDetail(Map<String, Object> map) {
		BankToken token = (BankToken)map.get("token");
		// -----------------------------------------------------------
		// 잔액조회 요청에 사용될 bank_tran_id, tran_dtime 값 생성하기
		// BankValueGenerator - getBankTranId() 메서드 호출하여 거래고유번호 리턴받기
		// => 파라미터 : 이용기관코드(String)   리턴타입 : String(bank_tran_id)
		String bank_tran_id = BankValueGenerator.getBankTranId(client_use_code);
		
		// BankValueGenerator - getTranDTime() 메서드 호출하여 거래일시 리턴받기
		// => 파라미터 : 없음   리턴타입 : String(tran_dtime)
		String tran_dtime = BankValueGenerator.getTranDTime();
		
		System.out.println("거래고유번호 : " + bank_tran_id);
		System.out.println("거래일시 : " + tran_dtime);
		// -----------------------------------------------------------
		// 1. POST 방식 요청을 수행할 URL 정보를 URI 타입 객체 또는 문자열로 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url) // 기본 요청 주소 생성
					.path("/v2.0/account/balance/fin_num") // 상세 요청 주소(세부 경로) 생성
					.queryParam("fintech_use_num", map.get("fintech_use_num")) // GET 방식 요청 파라미터
					.queryParam("bank_tran_id", bank_tran_id) // GET 방식 요청 파라미터
					.queryParam("tran_dtime", tran_dtime) // GET 방식 요청 파라미터
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. 사용자 정보 조회 API 요청에 사용될 헤더정보를 관리할 HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccess_token());
		
		// 3. 요청 정보로 사용할 헤더와 바디(지금은 불필요) 정보를 관리하는 HttpEntity 객체 생성
		//    => 바디 정보 없이 헤더 정보만 전달(생성자에 HttpHeaders 객체 전달)
		//    => 모든 헤더 정보가 문자열로 관리되므로 제네릭타입으로 String 타입 지정
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		System.out.println("요청 정보 : " + httpEntity);
		
		// 4. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		RestTemplate restTemplate = new RestTemplate();
		ParameterizedTypeReference<Map<String, String>> responseType = 
				new ParameterizedTypeReference<Map<String, String>>() {};
		// => 응답데이터 중 "객체" 가 존재하지 않으므로 제네릭타입을 <String, String> 사용 가능
		
		// exchange() 메서드 마지막 파라미터로 파싱 클래스 지정 시
		// ParameterizedTypeReference 객체를 지정하고
		// 리턴값을 지정하는 ResponseEntity 의 제네릭타입은 실제 파싱될 제네릭타입을 그대로 기술
		ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.GET,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				responseType); // 응답 데이터를 파싱하여 관리할 클래스
		// -------------------------------------------------------------------------------------

		log.info("응답 코드 : " + responseEntity.getStatusCode());
		log.info("응답 헤더 : " + responseEntity.getHeaders());
		log.info("응답 본문 : " + responseEntity.getBody());
		
		// 5. 응답데이터가 저장된 ResponseEntity 객체의 getBody() 메서드 호출하여
		//    응답 데이터 중 본문만 추출하여 지정된 제네릭타입 객체로 리턴
		return responseEntity.getBody();
	}

	// =======================================================================
	// 2.6. 계좌이체 서비스 - 2.6.1. 출금이체 API 서비스(POST)
	// https://testapi.openbanking.or.kr/v2.0/transfer/withdraw/fin_num
	public Map<String, String> requestWithdraw(Map<String, Object> map) {
		BankToken token = (BankToken)map.get("token");
		// -----------------------------------------------------------
		// 세션 아이디 가져오기
		String id = (String)map.get("id");
		// -----------------------------------------------------------
		// 잔액조회 요청에 사용될 bank_tran_id, tran_dtime 값 생성하기
		String bank_tran_id = BankValueGenerator.getBankTranId(client_use_code);
		String tran_dtime = BankValueGenerator.getTranDTime();
		// -----------------------------------------------------------
		// 1. HTTP 요청에 필요한 URI 정보를 관리할 URI 객체 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url) // 기본 요청 주소 생성
					.path("/v2.0/transfer/withdraw/fin_num") // 상세 요청 주소(세부 경로) 생성
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. 사용자 정보 조회 API 요청에 사용될 헤더정보를 관리할 HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccess_token());
		// 단, API 요청 파라미터 타입이 "application/json; charset=UTF-8" 타입을 요구하므로
		// 헤더 정보를 관리하는 HttpHeaders 객체의 setContentType() 메서드를 호출하여
		// 헤더 정보에 컨텐츠 타입을 JSON 타입으로 설정
		// => 메서드 파라미터로 JSON 타입 지정을 위해 MediaType.APPLICATION_JSON 상수 활용
		//    (주의! APPLICATION_JSON_UTF8 상수는 Deprecated)
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// ---------------------------------------------------------------------------------
		// 3. API 요청 파라미터를 JSON 형식으로 생성
		// => org.json.JSONObject 클래스 또는 com.google.code.gson.JsonObject 클래스 활용
		// 3-1) JSONObject 클래스 활용 => put() 메서드로 데이터 추가(자동으로 JSON 형식으로 변환)
//		JSONObject jsonObject = new JSONObject(); // 기본 생성자로 객체 생성(또는 파라미터 생성자 사용 가능)
//		jsonObject.put("bank_tran_id", bank_tran_id); // 거래고유번호(참가기관)
//		jsonObject.put("cntr_account_type", "N"); // 약정 계좌/계정 구분("N" : 계좌)
//		jsonObject.put("cntr_account_num", cntr_account_num); // 약정 계좌/계정 번호(서비스 이용기관 - 아이티윌 계좌)
		// --------------------------------------------------
		// 3-2) Gson 라이브러리의 JsonObject 클래스 활용(클래스명 주의! JSONObject 와 다르다!!)
		//      => addProperty() 메서드로 데이터 추가(자동으로 JSON 형식으로 변환)
		JsonObject jsonObject = new JsonObject(); // 기본 생성자로 객체 생성
		// ------------- 핀테크 이용기관(아이티윌) 정보 ------------
		jsonObject.addProperty("bank_tran_id", bank_tran_id); // 거래고유번호(참가기관)
		jsonObject.addProperty("cntr_account_type", "N"); // 약정 계좌/계정 구분("N" : 계좌)
		jsonObject.addProperty("cntr_account_num", cntr_account_num); // 약정 계좌/계정 번호(서비스 이용기관 - 아이티윌 계좌)
		jsonObject.addProperty("dps_print_content", id); // 입금계좌인자내역(= 입금되는 계좌에 출력할 메세지)
		// => 임의로 현재 사용자의 세션 아이디를 내역으로 활용(사용자가 정하지 않음)
		
		// ------------- 요청 고객(출금계좌) 정보 ------------
		jsonObject.addProperty("fintech_use_num", (String)map.get("withdraw_client_fintech_use_num")); // 출금계좌핀테크이용번호
		jsonObject.addProperty("wd_print_content", "아이티윌"); // 출금계좌인자내역(기본값 : 받는 계좌 예금주명 => 임의로 아이티윌로 고정)
		jsonObject.addProperty("tran_amt", (String)map.get("tran_amt")); // 거래금액
		jsonObject.addProperty("tran_dtime", tran_dtime); // 거래요청일시
		jsonObject.addProperty("req_client_name", (String)map.get("withdraw_client_name")); // 요청고객성명(출금계좌)
		jsonObject.addProperty("req_client_fintech_use_num", (String)map.get("withdraw_client_fintech_use_num")); // 요청고객 핀테크이용번호(출금계좌)
		// => 요청고객 계좌번호&개설기관 표준코드 미사용 시 핀테크 이용번호 설정 필수!
		//    (단, 동시에 두 가지 다 설정 시 오류 발생!)
		jsonObject.addProperty("req_client_num", id.toUpperCase()); // 요청고객회원번호(아이디처럼 사용)
		// => 별도의 회원 번호가 없으므로 세션 아이디 활용(영문자 알파벳 대문자 필수!)
		jsonObject.addProperty("transfer_purpose", "ST"); // 이체용도(송금 : TR, 결제 : ST 등) => 3.17. 이체 용도 안내 참조
		// => 고객 계좌에서 출금을 하지만 다른 고객에게 송금하지 않고
		//    이용기관 계좌에 전달되므로 "결제" 형태의 거래가 됨
		
		// ------------- 수취 고객(실제 최종 입금 대상) 정보 ------------
		// 최종적으로 이 금액을 수신하는 계좌에 대한 정보
		// => 이 정보(3가지)는 피싱 등의 사고 발생 시 지급 정지 등을 위한 정보로 실제 검증 수행X
		// => 현재 수행하는 거래는 사용자 -> 이용기관(아이티윌) 계좌로 입금되므로 이용기관의 계좌 정보를 세팅
//		jsonObject.addProperty("recv_client_name", cntr_account_holder_name); // 최종수취고객성명(입금대상)
		jsonObject.addProperty("recv_client_name", "이연태"); // 최종수취고객성명(입금대상)
		jsonObject.addProperty("recv_client_bank_code", cntr_account_bank_code); // 최종수취고객계좌 개설기관 표준코드(입금대상)
		jsonObject.addProperty("recv_client_account_num", cntr_account_num); // 최종수취고객계좌번호(입금대상)

		System.out.println("요청 JSON 데이터 : " + jsonObject.toString());
		
		/*
		 * [ 테스트 데이터 등록 방법 - 출금이체 ]
		 * - 사용자 일련번호, 핀테크 이용번호 : 출금 계좌 고객 정보 선택(셀렉트박스)
		 *   => 출금기관 대표코드, 출금 계좌번호(출력용 포함) 자동으로 입력됨
		 * - 송금인 실명 : 출금계좌 예금주명(고객 성명)
		 * - 거래금액 : tran_amt 값 입력
		 * - 입금계좌 인자내역 : dps_print_content 값 입력(현재는 사용자의 아이디 입력되어 있음)
		 *   => 주의! 실제 사용 가능한 길이보다 짧게 입력받음(10글자만 입력 가능)
		 *   => 실제 전송하는 내용과 다르게 테스트 데이터가 입력되어도 오류 발생 X
		 * - 수취인 성명 : 핀테크 이용기관 계좌 예금주명 입력(최종 수취인 아님!!)
		 * ---------------------------------------
		 * [ 주의사항 ]
		 * 출금 기능에서는 실제 잔액 계산 기능이 제공되지 않음(원래는 금융기관에서 제공하는 정보)
		 * 따라서, 출금이 성공하더라도 잔액 확인이 불가능하므로 
		 * DB 에 잔액 저장해두고 수동으로 처리 필요
		 */
		
		// 임시) 테스트 데이터 - 입금계좌 인자내역 잘못된 정보 설정하고 확인
		// ---------------------------------------------------------------
		// 4. 헤더와 바디를 묶어서 관리하는 HttpEntity 객체 생성
		// => 생성자에 바디 정보(JSON 문자열)와 헤더 정보(HttpHeaders)를 모두 전달
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonObject.toString(), headers);
		
		// 5. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		RestTemplate restTemplate = new RestTemplate();
		ParameterizedTypeReference<Map<String, String>> responseType = 
				new ParameterizedTypeReference<Map<String, String>>() {};
		// => 응답데이터 중 "객체" 가 존재하지 않으므로 제네릭타입을 <String, String> 사용 가능
		
		// exchange() 메서드 마지막 파라미터로 파싱 클래스 지정 시
		// ParameterizedTypeReference 객체를 지정하고
		// 리턴값을 지정하는 ResponseEntity 의 제네릭타입은 실제 파싱될 제네릭타입을 그대로 기술
		ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.POST,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				responseType); // 응답 데이터를 파싱하여 관리할 클래스
		
		// 6. 응답데이터를 관리하는 ResponseEntity 객체의 getBody() 메서드 호출하여 응답 데이터 본문 리턴
		return responseEntity.getBody();
	}

	// =========================================================
	// 2.1.2. 토큰발급 API - 센터인증 이용기관 토큰발급 API (2-legged) (관리자(이용기관) 엑세스토큰 발급용)
	// => 요청 URL 은 일반 토큰 발급과 동일함
	public BankToken requestAdminAccessToken() {
		// 1. POST 방식 요청을 수행할 URL 정보를 URI 타입 객체 또는 문자열로 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url + "/oauth/2.0/token") // 요청 주소 생성
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. POST 방식 요청 수행 시 파라미터를 URL 에 결합할 수 없으므로 body 에 포함시켜야 함
		// => 일반 사용자 토큰 발급 요청 파라미터와 다름
		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		parameters.add("client_id", client_id);
		parameters.add("client_secret", client_secret);
		parameters.add("scope", "oob"); // 고정값
		parameters.add("grant_type", "client_credentials"); // 고정값
		
		// 3. 요청 정보로 사용할 헤더(지금은 불필요)와 바디 정보를 관리하는 HttpEntity 객체 생성
		HttpEntity<LinkedMultiValueMap<String, String>> httpEntity = 
				new HttpEntity<LinkedMultiValueMap<String,String>>(parameters);
		
		// 4. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		RestTemplate restTemplate = new RestTemplate();
		
		ResponseEntity<BankToken> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.POST,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				BankToken.class); // 응답 데이터를 파싱하여 관리할 클래스(.class 필수!)
		
		log.info("응답 코드 : " + responseEntity.getStatusCode());
		log.info("응답 헤더 : " + responseEntity.getHeaders());
		log.info("응답 본문 : " + responseEntity.getBody());
		
		// 응답 정보 리턴
		return responseEntity.getBody();
	}

	// =====================================================================
	// 2.6. 계좌이체 서비스 - 2.6.2. 입금이체 API(POST)
	// https://testapi.openbanking.or.kr/v2.0/transfer/deposit/fin_num
	public Map<String, Object> requestDeposit(Map<String, Object> map) {
		BankToken token = (BankToken)map.get("adminToken");
		String id = (String)map.get("id");
		// -----------------------------------------------------------
		// 요청에 사용될 bank_tran_id, tran_dtime 값 생성하기
		String bank_tran_id = BankValueGenerator.getBankTranId(client_use_code);
		String tran_dtime = BankValueGenerator.getTranDTime();
		// -----------------------------------------------------------
		// 1. HTTP 요청에 필요한 URI 정보를 관리할 URI 객체 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url) // 기본 요청 주소 생성
					.path("/v2.0/transfer/deposit/fin_num") // 상세 요청 주소(세부 경로) 생성
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. 사용자 정보 조회 API 요청에 사용될 헤더정보를 관리할 HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token.getAccess_token());
		// => 주의! 입금 대상(고객)의 엑세스토큰이 아닌 이용기관(아이티윌 - 이연태)의 엑세스토큰!
		headers.setContentType(MediaType.APPLICATION_JSON);
		// ---------------------------------------------------------------------------------
		// 3. API 요청 파라미터를 JSON 형식으로 생성
		// 3-1) 1건의 입금 이체 정보를 저장할 객체 생성
		JsonObject joReq = new JsonObject(); // 기본 생성자로 객체 생성
		
		joReq.addProperty("tran_no", 1); // 거래순번(2020년부터 단건이체만 가능하므로 무조건 1 고정)
		joReq.addProperty("bank_tran_id", bank_tran_id); // 거래고유번호(참가기관)
		
		// ------------- 요청 고객(입금계좌) 정보 ------------
		joReq.addProperty("fintech_use_num", (String)map.get("deposit_client_fintech_use_num")); // 출금계좌핀테크이용번호
		joReq.addProperty("print_content", "아이티윌_입금"); // 입금계좌인자내역(기본값 : 임의로 아이티윌_입금 으로 고정)
		joReq.addProperty("tran_amt", (String)map.get("tran_amt")); // 거래금액
		joReq.addProperty("req_client_name", (String)map.get("deposit_client_name")); // 요청고객성명(입금계좌)
		joReq.addProperty("req_client_fintech_use_num", (String)map.get("deposit_client_fintech_use_num")); // 요청고객 핀테크이용번호(입금계좌)
		// => 요청고객 계좌번호&개설기관 표준코드 미사용 시 핀테크 이용번호 설정 필수!
		//    (단, 동시에 두 가지 다 설정 시 오류 발생!)
		joReq.addProperty("req_client_num", id.toUpperCase()); // 요청고객회원번호(아이디처럼 사용)
		// => 별도의 회원 번호가 없으므로 세션 아이디 활용(영문자 알파벳 대문자 필수!)
		joReq.addProperty("transfer_purpose", "TR"); // 이체용도(송금 : TR, 결제 : ST 등) => 3.17. 이체 용도 안내 참조
		
		 
		// 3-2) 입금 이체 1건의 정보를 배열(리스트)로 관리할 JsonArray 객체 생성
		JsonArray jaReq_list = new JsonArray();
		// JsonArray 객체의 add() 메서드를 호출하여 1건 이체 정보가 담긴 JsonObject 객체 추가
		jaReq_list.add(joReq);
		
		
		// 3-3) 기본 입금 이체 정보를 저장할 JsonObject 객체 생성
		JsonObject jsonObject = new JsonObject();
		// ------------- 핀테크 이용기관(아이티윌) 정보 ------------
		jsonObject.addProperty("cntr_account_type", "N"); // 약정 계좌/계정 구분("N" : 계좌)
		jsonObject.addProperty("cntr_account_num", cntr_account_num); // 약정 계좌/계정 번호(서비스 이용기관 - 아이티윌 계좌)
		
		jsonObject.addProperty("wd_pass_phrase", "NONE"); // 입금이체용 암호문구(테스트 시 "NONE" 값 고정)
		jsonObject.addProperty("wd_print_content", map.get("deposit_client_name") + "_송금"); // 출금계좌(이용기관 - 아이티윌)인자내역
		// => 임의로 현재 사용자의 이름을 내역으로 활용(입금이체 버튼 클릭 시 전송됨)
		// => 아이디의 경우 길이가 길면 길이제한으로 인한 오류가 발생할 수 있음
		
		jsonObject.addProperty("name_check_option", "on"); // 수취인 성명 검증여부("on" : 검증, "off" 미검증, 생략 시 "on")
		jsonObject.addProperty("tran_dtime", tran_dtime); // 거래요청일시
		jsonObject.addProperty("req_cnt", 1); // 입금요청건수(2020년부로 다건이체 중단으로 단건이체용 1 고정)
	
		
		// 3-4) 기본 입금 이체 정보 JsonObject 객체에 1건의 이체 정보 JsonArray 객체 추가
		jsonObject.add("req_list", jaReq_list);

		System.out.println("요청 JSON 데이터 : " + jsonObject.toString());
		
		/*
		 * [ 테스트 데이터 등록 방법 - 입금이체 ]
		 * - 사용자 일련번호, 핀테크 이용번호 : 출금 계좌 고객 정보 선택(셀렉트박스)
		 *   => 입금기관 대표코드, 입금 계좌번호(출력용 포함) 자동으로 입력됨
		 * - 송금인 실명 : 이용기관 계좌 예금주명(입금 요청 고객 정보가 아님!!) => 이연태
		 * - 거래금액 : tran_amt 값 입력
		 * - 입금계좌 인자내역 : print_content 값 입력(아이티윌_입금)
		 * - 수취인 성명 : req_client_name 값 입력(입금(송금) 대상 고객 계좌 예금주명(최종 수취인))
		 * ---------------------------------------
		 * [ 주의사항 ]
		 * 입금 기능에서는 실제 잔액 계산 기능이 제공되지 않음(원래는 금융기관에서 제공하는 정보)
		 * 따라서, 입금이 성공하더라도 잔액 확인이 불가능하므로 
		 * DB 에 잔액 저장해두고 수동으로 처리 필요
		 */
		
		// 임시) 테스트 데이터 - 입금계좌 인자내역 잘못된 정보 설정했음!!!
		// ---------------------------------------------------------------
		// 4. 헤더와 바디를 묶어서 관리하는 HttpEntity 객체 생성
		// => 생성자에 바디 정보(JSON 문자열)와 헤더 정보(HttpHeaders)를 모두 전달
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonObject.toString(), headers);
		
		// 5. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		RestTemplate restTemplate = new RestTemplate();
		ParameterizedTypeReference<Map<String, Object>> responseType = 
				new ParameterizedTypeReference<Map<String, Object>>() {};
		// => 응답데이터 중 "객체" 가 존재하지 않으므로 제네릭타입을 <String, String> 사용 가능
		
		// exchange() 메서드 마지막 파라미터로 파싱 클래스 지정 시
		// ParameterizedTypeReference 객체를 지정하고
		// 리턴값을 지정하는 ResponseEntity 의 제네릭타입은 실제 파싱될 제네릭타입을 그대로 기술
		ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.POST,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				responseType); // 응답 데이터를 파싱하여 관리할 클래스
		
		// 6. 응답데이터를 관리하는 ResponseEntity 객체의 getBody() 메서드 호출하여 응답 데이터 본문 리턴
		return responseEntity.getBody();
	}

	// =============================================================================
	// [ P2P(개인간) 송금 ]
	// 출금이체
	public Map<String, String> requestWithdrawForTransfer(Map<String, Object> map) {
		BankToken senderToken = (BankToken)map.get("senderToken");
		BankToken receiverToken = (BankToken)map.get("receiverToken");
		Map<String, String> senderAccount = (Map<String, String>)map.get("senderAccount");
		Map<String, String> receiverAccount = (Map<String, String>)map.get("receiverAccount");
		// -----------------------------------------------------------
		// 세션 아이디 가져오기
		String id = (String)senderToken.getId();
		// -----------------------------------------------------------
		// 잔액조회 요청에 사용될 bank_tran_id, tran_dtime 값 생성하기
		String bank_tran_id = BankValueGenerator.getBankTranId(client_use_code);
		String tran_dtime = BankValueGenerator.getTranDTime();
		// -----------------------------------------------------------
		// 1. HTTP 요청에 필요한 URI 정보를 관리할 URI 객체 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url) // 기본 요청 주소 생성
					.path("/v2.0/transfer/withdraw/fin_num") // 상세 요청 주소(세부 경로) 생성
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. 사용자 정보 조회 API 요청에 사용될 헤더정보를 관리할 HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(senderToken.getAccess_token()); // 보내는 사람의 엑세스토큰
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// ---------------------------------------------------------------------------------
		// 3. API 요청 파라미터를 JSON 형식으로 생성
		JsonObject jsonObject = new JsonObject(); // 기본 생성자로 객체 생성
		// ------------- 핀테크 이용기관(아이티윌) 정보 ------------
		jsonObject.addProperty("bank_tran_id", bank_tran_id); // 거래고유번호(참가기관)
		jsonObject.addProperty("cntr_account_type", "N"); // 약정 계좌/계정 구분("N" : 계좌)
		jsonObject.addProperty("cntr_account_num", cntr_account_num); // 약정 계좌/계정 번호(서비스 이용기관 - 아이티윌 계좌)
		jsonObject.addProperty("dps_print_content", id); // 입금계좌인자내역(= 입금되는 계좌에 출력할 메세지)
		// => 임의로 현재 사용자의 세션 아이디를 내역으로 활용(사용자가 정하지 않음)
		
		// ------------- 요청 고객(출금계좌) 정보 ------------
		jsonObject.addProperty("fintech_use_num", senderAccount.get("fintech_use_num")); // 출금계좌핀테크이용번호
		jsonObject.addProperty("wd_print_content", receiverAccount.get("account_holder_name")); // 출금계좌인자내역(기본값 : 받는 계좌 예금주명 => 임의로 아이티윌로 고정)
		jsonObject.addProperty("tran_amt", (String)map.get("tran_amt")); // 거래금액
		jsonObject.addProperty("tran_dtime", tran_dtime); // 거래요청일시
		jsonObject.addProperty("req_client_name", senderAccount.get("account_holder_name")); // 요청고객성명(출금계좌)
		jsonObject.addProperty("req_client_fintech_use_num", senderAccount.get("fintech_use_num")); // 요청고객 핀테크이용번호(출금계좌)
		// => 요청고객 계좌번호&개설기관 표준코드 미사용 시 핀테크 이용번호 설정 필수!
		//    (단, 동시에 두 가지 다 설정 시 오류 발생!)
		jsonObject.addProperty("req_client_num", senderToken.getUser_seq_no()); // 요청고객회원번호(사용자번호 활용)
		// => 별도의 회원 번호가 없으므로 세션 아이디 활용(영문자 알파벳 대문자 필수!)
		jsonObject.addProperty("transfer_purpose", "TR"); // 이체용도(송금 : TR, 결제 : ST 등) => 3.17. 이체 용도 안내 참조
		
		// ------------- 수취 고객(실제 최종 입금 대상) 정보 ------------
		// 최종적으로 이 금액을 수신하는 계좌에 대한 정보
		// => 이 정보(3가지)는 피싱 등의 사고 발생 시 지급 정지 등을 위한 정보로 실제 검증 수행X
		// => 현재 수행하는 거래는 사용자 -> 이용기관(아이티윌) 계좌로 입금되므로 이용기관의 계좌 정보를 세팅
//		jsonObject.addProperty("recv_client_name", cntr_account_holder_name); // 최종수취고객성명(입금대상)
		jsonObject.addProperty("recv_client_name", receiverAccount.get("account_holder_name")); // 최종수취고객성명(입금대상)
		jsonObject.addProperty("recv_client_bank_code", receiverAccount.get("account_bank_code")); // 최종수취고객계좌 개설기관 표준코드(입금대상)
		jsonObject.addProperty("recv_client_account_num", receiverAccount.get("account_num")); // 최종수취고객계좌번호(입금대상)
		// => 마스킹 되어있는 계좌번호지만 실제 검증 수행을 하지 않으므로 일단 사용
		System.out.println("요청 JSON 데이터 : " + jsonObject.toString());
		
		/*
		 * [ 테스트 데이터 등록 방법 - 출금이체 ]
		 * - 사용자 일련번호, 핀테크 이용번호 : 출금 계좌 고객 정보 선택(셀렉트박스)
		 *   => 출금기관 대표코드, 출금 계좌번호(출력용 포함) 자동으로 입력됨
		 * - 송금인 실명 : 출금계좌 예금주명(고객 성명)
		 * - 거래금액 : tran_amt 값 입력
		 * - 입금계좌 인자내역 : dps_print_content 값 입력(현재는 사용자의 아이디 입력되어 있음)
		 *   => 주의! 실제 사용 가능한 길이보다 짧게 입력받음(10글자만 입력 가능)
		 *   => 실제 전송하는 내용과 다르게 테스트 데이터가 입력되어도 오류 발생 X
		 * - 수취인 성명 : 핀테크 이용기관 계좌 예금주명 입력(최종 수취인 아님!!)
		 * ---------------------------------------
		 * [ 주의사항 ]
		 * 출금 기능에서는 실제 잔액 계산 기능이 제공되지 않음(원래는 금융기관에서 제공하는 정보)
		 * 따라서, 출금이 성공하더라도 잔액 확인이 불가능하므로 
		 * DB 에 잔액 저장해두고 수동으로 처리 필요
		 */
		
		// ---------------------------------------------------------------
		// 4. 헤더와 바디를 묶어서 관리하는 HttpEntity 객체 생성
		// => 생성자에 바디 정보(JSON 문자열)와 헤더 정보(HttpHeaders)를 모두 전달
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonObject.toString(), headers);
		
		// 5. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		RestTemplate restTemplate = new RestTemplate();
		ParameterizedTypeReference<Map<String, String>> responseType = 
				new ParameterizedTypeReference<Map<String, String>>() {};
		// => 응답데이터 중 "객체" 가 존재하지 않으므로 제네릭타입을 <String, String> 사용 가능
		
		// exchange() 메서드 마지막 파라미터로 파싱 클래스 지정 시
		// ParameterizedTypeReference 객체를 지정하고
		// 리턴값을 지정하는 ResponseEntity 의 제네릭타입은 실제 파싱될 제네릭타입을 그대로 기술
		ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.POST,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				responseType); // 응답 데이터를 파싱하여 관리할 클래스
		
		// 6. 응답데이터를 관리하는 ResponseEntity 객체의 getBody() 메서드 호출하여 응답 데이터 본문 리턴
		return responseEntity.getBody();
	}

	// 입금이체
	public Map<String, Object> requestDepositForTransfer(Map<String, Object> map) {
		BankToken senderToken = (BankToken)map.get("senderToken");
		BankToken receiverToken = (BankToken)map.get("receiverToken");
		BankToken adminToken = (BankToken)map.get("adminToken");
		Map<String, String> senderAccount = (Map<String, String>)map.get("senderAccount");
		Map<String, String> receiverAccount = (Map<String, String>)map.get("receiverAccount");
		// -----------------------------------------------------------
		// 세션 아이디 가져오기
		String id = (String)senderToken.getId();
		// -----------------------------------------------------------
		// 잔액조회 요청에 사용될 bank_tran_id, tran_dtime 값 생성하기
		String bank_tran_id = BankValueGenerator.getBankTranId(client_use_code);
		String tran_dtime = BankValueGenerator.getTranDTime();
		// -----------------------------------------------------------
		// 1. HTTP 요청에 필요한 URI 정보를 관리할 URI 객체 생성
		URI uri = UriComponentsBuilder
					.fromUriString(base_url) // 기본 요청 주소 생성
					.path("/v2.0/transfer/deposit/fin_num") // 상세 요청 주소(세부 경로) 생성
					.encode() // 주소 인코딩
					.build() // UriComponents 타입 객체 생성
					.toUri(); // URI 타입 객체로 변환
		System.out.println("요청 주소 : " + uri.toString());
		
		// 2. 사용자 정보 조회 API 요청에 사용될 헤더정보를 관리할 HttpHeaders 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(adminToken.getAccess_token());
		// => 주의! 입금 대상(고객)의 엑세스토큰이 아닌 이용기관(아이티윌 - 이연태)의 엑세스토큰!
		headers.setContentType(MediaType.APPLICATION_JSON);
		// ---------------------------------------------------------------------------------
		// 3. API 요청 파라미터를 JSON 형식으로 생성
		// 3-1) 1건의 입금 이체 정보를 저장할 객체 생성
		JsonObject joReq = new JsonObject(); // 기본 생성자로 객체 생성
		
		joReq.addProperty("tran_no", 1); // 거래순번(2020년부터 단건이체만 가능하므로 무조건 1 고정)
		joReq.addProperty("bank_tran_id", bank_tran_id); // 거래고유번호(참가기관)
		
		// -------------------------------------------------------------------------------
		// 위에서 작업한 입금은 이용기관 -> 사용자 계좌로 입금을 수행했지만
		// 이번 작업은 사용자 -> 이용기관 -> 사용자의 과정을 거쳐 송금을 수행하므로
		//                 (출금이체)  (입금이체)
		// 고객의 정보 부분이 두 종류(받는사람, 보내는사람)로 분리 설정 필요
		// ------------- 받는 사람(입금계좌) 정보 ------------
		joReq.addProperty("fintech_use_num", receiverAccount.get("fintech_use_num")); // 출금계좌핀테크이용번호
		joReq.addProperty("print_content", senderAccount.get("account_holder_name")); // 입금계좌인자내역(보내는사람 이름)
		joReq.addProperty("tran_amt", (String)map.get("tran_amt")); // 거래금액
		
		// ------------- 보내는 사람(요청 고객) 정보 ---------------
		joReq.addProperty("req_client_name", senderAccount.get("account_holder_name")); // 요청고객성명(입금계좌)
		joReq.addProperty("req_client_fintech_use_num", senderAccount.get("fintech_use_num")); // 요청고객 핀테크이용번호(입금계좌)
		// => 요청고객 계좌번호&개설기관 표준코드 미사용 시 핀테크 이용번호 설정 필수!
		//    (단, 동시에 두 가지 다 설정 시 오류 발생!)
		joReq.addProperty("req_client_num", id.toUpperCase()); // 요청고객회원번호(아이디처럼 사용)
		// => 별도의 회원 번호가 없으므로 세션 아이디 활용(영문자 알파벳 대문자 필수!)
		joReq.addProperty("transfer_purpose", "TR"); // 이체용도(송금 : TR, 결제 : ST 등) => 3.17. 이체 용도 안내 참조
		
		 
		// 3-2) 입금 이체 1건의 정보를 배열(리스트)로 관리할 JsonArray 객체 생성
		JsonArray jaReq_list = new JsonArray();
		// JsonArray 객체의 add() 메서드를 호출하여 1건 이체 정보가 담긴 JsonObject 객체 추가
		jaReq_list.add(joReq);
		
		
		// 3-3) 기본 입금 이체 정보를 저장할 JsonObject 객체 생성
		JsonObject jsonObject = new JsonObject();
		// ------------- 핀테크 이용기관(아이티윌) 정보 ------------
		jsonObject.addProperty("cntr_account_type", "N"); // 약정 계좌/계정 구분("N" : 계좌)
		jsonObject.addProperty("cntr_account_num", cntr_account_num); // 약정 계좌/계정 번호(서비스 이용기관 - 아이티윌 계좌)
		
		jsonObject.addProperty("wd_pass_phrase", "NONE"); // 입금이체용 암호문구(테스트 시 "NONE" 값 고정)
		jsonObject.addProperty("wd_print_content", receiverAccount.get("account_holder_name") + "_송금"); // 출금계좌(이용기관 - 아이티윌)인자내역
		// => 임의로 현재 사용자의 이름을 내역으로 활용(입금이체 버튼 클릭 시 전송됨)
		// => 아이디의 경우 길이가 길면 길이제한으로 인한 오류가 발생할 수 있음
		
		jsonObject.addProperty("name_check_option", "on"); // 수취인 성명 검증여부("on" : 검증, "off" 미검증, 생략 시 "on")
		jsonObject.addProperty("tran_dtime", tran_dtime); // 거래요청일시
		jsonObject.addProperty("req_cnt", 1); // 입금요청건수(2020년부로 다건이체 중단으로 단건이체용 1 고정)
	
		
		// 3-4) 기본 입금 이체 정보 JsonObject 객체에 1건의 이체 정보 JsonArray 객체 추가
		jsonObject.add("req_list", jaReq_list);

		System.out.println("요청 JSON 데이터 : " + jsonObject.toString());
		
		/*
		 * [ 테스트 데이터 등록 방법 - 입금이체 ]
		 * - 사용자 일련번호, 핀테크 이용번호 : 입금 계좌 고객(받는 사람) 정보 선택(셀렉트박스)
		 *   => 입금기관 대표코드, 입금 계좌번호(출력용 포함) 자동으로 입력됨
		 * - 송금인 실명 : 이용기관 계좌 예금주명(입금 요청 고객 정보가 아님!!) => 이연태
		 * - 거래금액 : tran_amt 값 입력
		 * - 입금계좌 인자내역 : print_content 값 입력(상대방이름)
		 * - 수취인 성명 : req_client_name 값 입력(입금(송금) 대상 고객 계좌 예금주명(최종 수취인))
		 * ---------------------------------------
		 * [ 주의사항 ]
		 * 입금 기능에서는 실제 잔액 계산 기능이 제공되지 않음(원래는 금융기관에서 제공하는 정보)
		 * 따라서, 입금이 성공하더라도 잔액 확인이 불가능하므로 
		 * DB 에 잔액 저장해두고 수동으로 처리 필요
		 */
		
		// ---------------------------------------------------------------
		// 4. 헤더와 바디를 묶어서 관리하는 HttpEntity 객체 생성
		// => 생성자에 바디 정보(JSON 문자열)와 헤더 정보(HttpHeaders)를 모두 전달
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonObject.toString(), headers);
		
		// 5. REST API(RESTful API) 요청에 사용할 RestTemplate 객체 활용
		RestTemplate restTemplate = new RestTemplate();
		ParameterizedTypeReference<Map<String, Object>> responseType = 
				new ParameterizedTypeReference<Map<String, Object>>() {};
		// => 응답데이터 중 "객체" 가 존재하지 않으므로 제네릭타입을 <String, String> 사용 가능
		
		// exchange() 메서드 마지막 파라미터로 파싱 클래스 지정 시
		// ParameterizedTypeReference 객체를 지정하고
		// 리턴값을 지정하는 ResponseEntity 의 제네릭타입은 실제 파싱될 제네릭타입을 그대로 기술
		ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
				uri, // 요청 URL 관리하는 URI 타입 객체(또는 문자열로 된 URL 도 전달 가능) 
				HttpMethod.POST,  // 요청 메서드(HttpMethod.XXX 상수 활용)
				httpEntity, // 요청 정보를 관리하는 HttpEntity 객체
				responseType); // 응답 데이터를 파싱하여 관리할 클래스
		
		// 6. 응답데이터를 관리하는 ResponseEntity 객체의 getBody() 메서드 호출하여 응답 데이터 본문 리턴
		return responseEntity.getBody();
	}
	
}














