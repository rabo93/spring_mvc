package com.itwillbs.mvc_board.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itwillbs.mvc_board.vo.ChatMessage;

// 웹소켓 핸들링을 수행할 클래스 정의 - TextWebSocketHandler 클래스 상속
// => 이 클래스는 컨트롤러처럼 개발자가 직접 객체 생성을 제어하지 않음(자동으로 관리됨)
// => 서버에서는 단 하나의 유일한 객체(싱글톤)로 관리됨
// => 클래스 내에는 TextWebSocketHandler 클래스의 메서드를 오버라이딩하여 각 요청에 대한 처리 구현
//    (afterConnectionEstablied, afterConnectionClosed, handleTextMessage, handleTransportError)
public class MyWebSocketHandler extends TextWebSocketHandler {
	// 접속한 클라이언트(사용자)들에 대한 정보를 저장할 용도의 Map 객체(userSessionList) 생성
	// => 제네릭타입 : String, WebSocketSession
	// => Key : 웹소켓 세션 아이디(문자열)   Value : 웹소켓 세션 객체(WebSocketSession 타입)
	// => Map 객체의 구현체 클래스로 HashMap 타입 대신 ConcurrentHashMap 타입 사용 시
	//    멀티쓰레딩 환경에서 동시 접근시에도 락(Lock)을 통해 안전(Thread-safe) 하게 구현 가능
	//    (단, 추가/수정 등의 작업에서는 HashMap 보다 성능 느림. 읽기는 동일함)
	private Map<String, WebSocketSession> userSessionList = new ConcurrentHashMap<String, WebSocketSession>();
	// ============================================================================
	// JSON 데이터 파싱 작업을 처리할 Gson 객체 생성
	private final Gson gson = new Gson();
	// ============================================================================
	// 1. afterConnectionEstablished - 웹소켓 최초 연결 시 자동으로 호출되는 메서드
	// => 이 과정에서 스프링에서도 WebSocket 관련 객체가 자동으로 생성됨
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("웹소켓 연결됨!(afterConnectionEstablished)");
		
		// 웹소켓 통신을 수행하는 클라이언트(접속자)가 웹소켓 연결을 요청하면
		// 최초 1회 HTTP 통신을 수행하여 연결을 수립하고
		// 이 과정에서 해당 클라이언트에 대한 정보가 WebSocketSession 객체로 관리됨
		// => WebSocketSession 객체의 ID 값이 자동으로 클라이언트마다 부여됨(각 클라이언트 구별)
		// => 주의! HTTP 통신에서 사용하는 세션 객체(HttpSession)와 다르다!
		// => 참고) ROOT 로그 레벨을 DEBUG 로 낮추면 WebSocketSession 객체 생성 과정 확인 가능함
		//    (ex. 2024-12-24 14:54:27 DEBUG [org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator] New StandardWebSocketSession[id=37768563-2c4d-d847-36fe-4edb316a1cc2, uri=ws://localhost:8081/mvc_board/echo])
		System.out.println("웹소켓 세션 아이디(session.getId()) : " + session.getId());
		// => 웹소켓 세션 아이디(session.getId()) : 5eac6dfd-fd09-8922-7a39-874596bb5f0a
		System.out.println("웹소켓 세션 IP 주소(session.getRemoteAddress()) : " + session.getRemoteAddress());
		// => 웹소켓 세션 IP 주소(session.getRemoteAddress()) : /192.168.1.40:57843
		// --------------------------------------------------------------------------------
		// 사용자의 WebSocketSession 객체를 Map 객체에 저장 => 클라이언트(웹소켓) 정보 관리 용도
		// => Key : 웹소켓 세션 아이디(문자열)   Value : 웹소켓 세션 객체(WebSocketSession 타입)
		userSessionList.put(session.getId(), session);
		System.out.println("연결 후 세션 목록 : " + userSessionList);
		// --------------------------------------------------------------------------------
		// HttpSession 객체에 저장된 정보 확인
		// => 웹소켓 설정 정보에서 HttpSessionHandshakeInterceptor 클래스 설정을 통한 인터셉터 설정 필수!
		// => 웹소켓 최초 연결 시 수행하는 HTTP 통신 과정에서 HttpSession 객체 인터셉트 => WebSocketSession 객체에 저장
		// => WebSocketSession 객체의 getAttributes() 메서드 활용하여 HttpSession 객체의 속성들에 접근
		System.out.println("세션(HttpSession) 아이디 : " + session.getAttributes().get("sId"));
	}

	
	// ======================================================================================
	// 2. handleTextMessage - 클라이언트로부터 메세지를 수신할 경우 자동으로 호출되는 메서드
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println("메세지 수신됨!(handleTextMessage)");
		// --------------------------------------------------------------
		System.out.println("메세지 전송한 클라이언트 : " + session.getId());
//		System.out.println("전송된 메세지 : " + message.getPayload());
		
		String jsonMsg = message.getPayload();
		System.out.println("전송된 메세지 : " + jsonMsg);
		// --------------------------------------------------------------
		// [ JSON 형태의 문자열 파싱(문자열 -> 객체로 변환) ]
		// => Gson 라이브러리 활용(org.json 라이브러리도 가능)
		// => 전송된 메세지는 JSON 형식 문자열이므로 별도의 작업을 통해 원하는 객체 형태로 변환
		// 1. org.json 라이브러리의 JSONObject 객체 활용
//		JSONObject jo = new JSONObject(jsonMsg);
		// => 참고) JSONObject 객체의 getXXX() 메서드 호출하여 각 JSON 객체 접근 가능
//		System.out.println("메세지 타입 : " + jo.getString("type"));
//		System.out.println("메세지 : " + jo.getString("msg"));
//		System.out.println("메세지 발신자 : " + jo.getString("sender_id"));
		// => 주의! sender_id 속성 존재하지 않을 경우 예외 발생
		//    (org.json.JSONException: JSONObject["sender_id"] not found.)
		// ---------------------------------------------------------------
		// 2. Gson 객체 활용(Jackson 도 가능)하여 자동 파싱
		// => Gson 객체의 fromJson() 메서드 호출하여 파싱할 데이터와 파싱 클래스 전달(.class 필수)
		// => Gson 라이브러리 추가와 별개로 VO 클래스 정의도 필요(속성명과 동일한 이름의 멤버변수 필요)
		// => 이 때, 멤버변수에 해당하는 속성이 전달되지 않아도 예외 발생 없음(null 값 허용)
		ChatMessage chatMessage = gson.fromJson(jsonMsg, ChatMessage.class);
		System.out.println("파싱된 메세지 : " + chatMessage);
		// => 파싱된 메세지 : ChatMessage(type=TALK, msg=1234, sender_id=null)
		// ===================================
		// sendMessage() 메서드 호출하여 채팅방의 다른 사용자들에게 메세지 전송
		// => 파라미터 : 웹소켓 세션 객체(WebSocketSession), 채팅 메세지 객체(ChatMessage)
		sendMessage(session, chatMessage);
	}
	// ======================================================================================
	// 각 웹소켓 세션(채팅방 사용자)들에게 메세지를 전송하는 메서드
	public void sendMessage(WebSocketSession session, ChatMessage chatMessage) throws Exception {
		// 메세지 발신자의 세션 아이디 가져오기
		String sender_id = (String)session.getAttributes().get("sId");
		System.out.println("발신자 아이디 : " + sender_id);
		// --------------------------------------------------------------
		// 웹소켓 세션을 관리하는 Map 객체(userSessionList)에서
		// 자신의 세션을 제외한 나머지 세션 객체에 수신된 메세지 전송
		// => WebSocketSession 객체의 sendMessage() 메서드 활용
		// => WebSocketSession 객체는 userSessionList 객체 내의 value 값으로 저장되어 있으며
		//    Map 객체의 values() 메서드를 호출하면 모든 value 값을 꺼내서 컬렉션으로 리턴해준다.
		//    따라서, 리턴값을 반복문을 통해 WebSocketSession 객체에 접근하여 메세지 전송   
		// => 결국, 현재 실습에서는 세션을 Map 객체에 저장하지 않고 Set 또는 List 에 저장해도 됨)
		for(WebSocketSession ws : userSessionList.values()) {
			// 메세지 전송 시 TextMessage 객체 생성자에 전달할 메세지를 지정하여 객체 생성 후
			// sendMessage() 메서드 파라미터로 TextMessage 객체 전달(IOException 핸들링 필요)
//			ws.sendMessage(new TextMessage(chatMessage.toString()));
			// => 클라이언트(웹브라우저)의 WebSocket 객체의 onMessage 이벤트가 발생
			// --------------------------------------------------------------------
			// 단, 클라이언트 목록(userSessions 에서 꺼낸 ws)의 세션 아이디와
			// 메세지 송신자의 세션 아이디가 다를 경우에만 전송 수행
			if(!ws.getId().equals(session.getId())) {
				// 메세지 타입(type) 판별하여 "ENTER" 또는 "LEAVE" 일 경우 입장/퇴장 메세지 설정
				// => 타입이 "TALK" 일 경우에는 추가적인 메세지 설정은 불필요(기존 메세지 사용)
				// => 이 때, 메세지에 발신자 아이디를 포함하여 전송(ChatMessage 객체의 sender_id)
				if(chatMessage.getType().equals(ChatMessage.TYPE_ENTER)) { // 입장메세지
					chatMessage.setMessage(">> " + sender_id + " 님이 입장하셨습니다 <<");
				} else if(chatMessage.getType().equals(ChatMessage.TYPE_LEAVE)) { // 퇴장메세지
					 chatMessage.setMessage(">> " + sender_id + " 님이 퇴장하셨습니다 <<");
				}
				
				// HttpSession 객체의 세션 아이디를 ChatMessage 객체의 sender_id 값으로 저장
				chatMessage.setSender_id(sender_id);

				// WebSocketSession 객체의 sendMessage() 메서드 호출하여 채팅메세지 전송
				// => ChatMessage 객체를 JSON 문자열로 변환 후 전달
				ws.sendMessage(new TextMessage(toJson(chatMessage)));
			}
			
		}
		
	}
	
	// ChatMessage 객체 정보를 JSON 문자열로 변환하여 리턴하는 메서드
	public String toJson(ChatMessage chatMessage) {
		// Gson 객체의 toJson() 메서드 호출하여 파라미터로 변환할 객체 전달
		return gson.toJson(chatMessage);
	}
	
	// ======================================================================================
	// 3. afterConnectionClosed - 웹소켓 연결 해제 시 자동으로 호출되는 메서드
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("웹소켓 연결 해제됨!(afterConnectionClosed)");
		
		// 클라이언트 정보를 관리하는 Map 객체(userSessionList) 에서
		// 연결 해제 요청이 발생한 웹소켓 세션 객체 제거(키 : WebSocketSession 객체의 ID)
		userSessionList.remove(session.getId());
		System.out.println("연결 해제 후 세션 목록 : " + userSessionList);
		// ------------------------------------------------------------------------------
		// ChatMessage 객체 생성하여 해제된 회원의 퇴장 메세지 설정
		// => type 만 "LEAVE" 로 설정
		ChatMessage chatMessage = new ChatMessage(ChatMessage.TYPE_LEAVE, "", "");
		
		// sendMessage() 메서드 호출하여 퇴장 정보 전송
		sendMessage(session, chatMessage);
		
	}
	
	// ======================================================================================
	// 4. handleTransportError - 웹소켓 통신 과정에서 오류 발생 시 자동으로 호출되는 메서드
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.out.println("웹소켓 오류 발생!(handleTransportError)");
		
	}
	
	
	
}







