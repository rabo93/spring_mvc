package com.itwillbs.mvc_board.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.itwillbs.mvc_board.service.ChatService;
import com.itwillbs.mvc_board.service.MemberService;
import com.itwillbs.mvc_board.vo.ChatMessage;
import com.itwillbs.mvc_board.vo.ChatMessage2;
import com.itwillbs.mvc_board.vo.ChatRoom;

public class MyWebSocketHandler2 extends TextWebSocketHandler {
	// 접속한 클라이언트(사용자)들에 대한 정보를 저장할 용도의 Map 객체(userSessionList) 생성
	private Map<String, WebSocketSession> userSessionList = new ConcurrentHashMap<String, WebSocketSession>();
	
	// 접속한 클라이언트(사용자)들의 사용자 아이디(HttpSession)와 WebSocketSession 객체의 아이디를
	// 관리할 용도의 Map 객체(userList) 생성
	// => 사용자마다 갱신되는 WebSocketSession 객체를 HttpSession 객체의 sId 속성값과 연결하여
	//    WebSocketSession 객체가 갱신되더라도 갱신된 정보를 세션 아이디를 통해 구별(= 유지) 목록
	// => userList 객체의 key 에 해당하는 value 와 userSession 객체의 key 가 연결됨
	//    (= 사용자 아이디를 통해 상대방의 WebSocketSession 객체에 접근 가능)
	private Map<String, String> userList = new ConcurrentHashMap<String, String>();
	// -----------------------------------------------------------------------------------------
	// JSON 데이터 파싱 작업을 처리할 Gson 객체 생성
	private final Gson gson = new Gson();
	// -----------------------------------------------------------------------------------------
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private ChatService chatService;
	// =========================================================================================
	// 1. afterConnectionEstablished - 웹소켓 최초 연결 시 자동으로 호출되는 메서드
	// => 이 과정에서 스프링에서도 WebSocket 관련 객체가 자동으로 생성됨
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("웹소켓 연결됨!(afterConnectionEstablished)");
		
		// 사용자의 WebSocketSession 객체를 Map 객체에 저장 => 클라이언트(웹소켓) 정보 관리 용도
		// => Key : 웹소켓 세션 아이디(문자열)   Value : 웹소켓 세션 객체(WebSocketSession 타입)
		userSessionList.put(session.getId(), session);
		// --------------------------------------------------------------------------------
		// HttpSession 객체에 저장된 사용자 아이디와 WebSocketSession 객체의 아이디를 userList 에 저장
		userList.put(getHttpSessionId(session), getWebSocketSessionId(session));
		// => 이 때, 사용자가 새로운 WebSocketSession 객체를 갖더라도
		//    userList 객체의 key(사용자 아이디)가 같으면 WebSocketSession 객체 아이디만 덮어씀
		// --------------------------------------------------------------------------------
		System.out.println("클라이언트 세션 목록(" + userSessionList.keySet().size() + " 명) : " + userSessionList);
		System.out.println("사용자 목록(" + userList.keySet().size() + " 명) : " + userList);
	}
	// =========================================================================================
	// 3. afterConnectionClosed - 웹소켓 연결 해제 시 자동으로 호출되는 메서드
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("웹소켓 연결 해제됨!(afterConnectionClosed)");
		
		// 클라이언트 정보를 관리하는 Map 객체(userSessionList) 에서
		// 연결 해제 요청이 발생한 웹소켓 세션 객체 제거(키 : WebSocketSession 객체의 ID)
		userSessionList.remove(session.getId());
		System.out.println("연결 해제 후 세션 목록 : " + userSessionList);
		// ------------------------------------------------------------------------------
		// 사용자 아이디가 저장된 Map 객체(useList) 에서
		// 종료 요청이 발생한 웹소켓 세션 아이디 제거(= 널스트링으로 변경)
		// HttpSession 객체의 아이디(= 사용자 아이디)는 유지
//		userList.put(getHttpSessionId(session), "");
		
		// HttpSession 아이디도 제거
		userList.remove(getHttpSessionId(session));
		// ------------------------------------------------------------------------------
		System.out.println("클라이언트 세션 목록(" + userSessionList.keySet().size() + " 명) : " + userSessionList);
		System.out.println("사용자 목록(" + userList.keySet().size() + " 명) : " + userList);
	}
	// ======================================================================================
	// 2. handleTextMessage - 클라이언트로부터 메세지를 수신할 경우 자동으로 호출되는 메서드
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println("메세지 수신됨!(handleTextMessage)");
		// --------------------------------------------------------------
		System.out.println("메세지 전송한 사용자 : " + getHttpSessionId(session));
		
		String jsonMsg = message.getPayload();
		System.out.println("전송된 메세지 : " + jsonMsg);
		
		// 수신된 메세지(JSON 형식 문자열)를 ChatMessage2 타입 객체로 변환
		ChatMessage2 chatMessage = gson.fromJson(jsonMsg, ChatMessage2.class);
		System.out.println("파싱된 메세지 : " + chatMessage);
		
		// 송신자 아이디와 수신자 아이디를 변수에 저장
		String sender_id = getHttpSessionId(session);
		String receiver_id = chatMessage.getReceiver_id();
		System.out.println("송신자 아이디 : " + sender_id + ", 수신자 아이디 : " + receiver_id);
		// =======================================================================
		// 수신된 메세지 타입 판별
		if(chatMessage.getType().equals(ChatMessage2.TYPE_INIT)) { // 채팅페이지 초기 진입메세지
			// 자신이 참여중인 채팅방 목록(= 자신의 아이디가 포함된 채팅방) 조회 요청
			// ChatService - getChatRoomList() 메서드 호출
			// => 파라미터 : 자신(송신자)의 아이디   리턴타입 : List<ChatRoom>(chatRoomList)
//			List<ChatRoom> chatRoomList = chatService.getChatRoomList(sender_id);
			
			// 해당 채팅방에서 수신된 메세지 중 읽지 않은 메세지 갯수를 포함하여 조회 요청
			// => 파라미터 : 자신(송신자)의 아이디   리턴타입 : List<Map<String, String>>(chatRoomList)
			List<Map<String, String>> chatRoomList = chatService.getChatRoomList(sender_id);
			System.out.println("기존 채팅방 목록 : " + chatRoomList);
			
			// 채팅방 목록 조회 결과를 JSON 형식으로 변환하여 메세지로 설정
//			chatMessage.setMessage(gson.toJson(chatRoomList));
			// 조회된 목록이 없을 경우 리스트 객체는 [] 로 표기되고
			// JSON 문자열로 변환 시 그대로 "[]" 형태로 변환됨
			// 따라서, 리스트가 비어있을 경우 null 값을 저장하여 변환되도록 처리
			chatMessage.setMessage(gson.toJson(chatRoomList == null || chatRoomList.size() == 0 ? null : chatRoomList));
			System.out.println("채팅방 목록 JSON 변환 결과 : " + chatMessage.getMessage());
			
			// sendMesasge() 메서드 호출하여 클라이언트측으로 초기화 정보 전송
			sendMessage(session, chatMessage);
		} else if(chatMessage.getType().equals(ChatMessage2.TYPE_INIT_COMPLETE)) {
			// 초기화 완료 메세지에서 수신자 아이디 포함 여부 판별
			if(receiver_id != null && !receiver_id.equals("")) {
				System.out.println("수신자 아이디 있음 - " + receiver_id);
				
				// -----------------------------------------------------
				// 사용자 접속 여부 판별(userList 객체의 receiver_id 확인)
				if(userList.get(receiver_id) == null) { // 현재 접속중인 사용자가 아닐 경우
					// MemberService - getMemberId() 메서드 호출하여 DB 에서 아이디 검색 요청
					// => 파라미터 : 수신자 아이디   리턴타입 : String(dbReceiverId)
					String dbReceiverId = memberService.getMemberId(receiver_id);
//					System.out.println("dbReceiverId : " + dbReceiverId);
					
					// DB 에서도 상대방 아이디가 존재하지 않을 경우 판별
					if(dbReceiverId == null) {
						// 메세지 송신자에게 오류 메세지 전송 후 작업 종료
						ChatMessage2 errorMessage = new ChatMessage2(0, ChatMessage2.TYPE_ERROR, "", sender_id, "", "존재하지 않는 사용자입니다!", "", 0);
						sendMessage(session, errorMessage);
						return;
					}
					
				}
				// 사용자 접속 여부 판별 끝
				// --------------------------------------------------------
				// 상대방이 회원일 경우(접속 여부 무관함) 채팅창 표시(신규 채팅방 or 기존 채팅방)
				// ChatService - getChatRoom() 메서드 호출하여 
				// 상대방과의 기존 채팅방 존재 여부 확인 요청
				// => 파라미터 : 송신자 아이디, 수신자 아이디   리턴타입 : ChatRoom(chatRoom)
				ChatRoom chatRoom = chatService.getChatRoom(sender_id, receiver_id);
				
				if(chatRoom == null) { // 기존 채팅방 없음
					System.out.println("채팅방 없음 - 새 채팅방 생성");
					
					// 1. 새 채팅방의 방번호(room_id) 생성
					// => generateRoomId() 메서드 호출하여 room_id 값 리턴받아 ChatMessage 객체에 저장
					chatMessage.setRoom_id(generateRoomId());
					System.out.println(chatMessage);
					
					// 2. ChatService - addChatRoom() 메서드 호출하여 새 채팅방 정보 DB 저장 요청
					// => 파라미터 : ChatRoom 객체(chatRoom)   리턴타입 : void
//					chatRoom = new ChatRoom(chatMessage.getRoom_id(), " 님과의 대화", sender_id, receiver_id, 1);
//					chatService.addChatRoom(chatRoom);
					
					// 만약, 1개 채팅방에 해당하는 2개의 레코드 정보를 각각 ChatRoom 객체로 생성할 경우
					// => 파라미터 : List<ChatRoom> 객체(chatRoomList)   리턴타입 : void
					// => 1개의 채팅방 정보에서 송신자와 수신자를 각각 반대로 설정하여 2개의 객체 생성
					List<ChatRoom> chatRoomList = new ArrayList<ChatRoom>();
					chatRoomList.add(new ChatRoom(chatMessage.getRoom_id(), receiver_id + " 님과의 대화", sender_id, receiver_id, 1));
					chatRoomList.add(new ChatRoom(chatMessage.getRoom_id(), sender_id + " 님과의 대화", receiver_id, sender_id, 1));
					chatService.addChatRoom2(chatRoomList);
					// -------------------------------------
					// 3. ChatMessage2 객체 정보 설정
					// => 2번 과정에서 List 객체의 0번 인덱스에 저장된 ChatRoom 객체 활용
					//    (1번 과정 진행 시 ChatRoom 객체 그대로 활용 가능)
					// 3-1) 송신자 화면에 새 채팅방 목록 추가를 위한 메세지 타입 TYPE_START 지정
					chatMessage.setType(ChatMessage2.TYPE_START);
					
					// 3-2) 채팅 메세지에 채팅방 정보를 JSON 문자열로 변환하여 저장(gson.toJson() 활용)
					// => 2번 과정에서 List 객체를 사용했을 경우 ChatRoom 객체에 0번 인덱스 꺼내서 저장 후 변환
					chatRoom = chatRoomList.get(0);
					chatMessage.setMessage(gson.toJson(chatRoom));
					
					// 4. 송신자에게 메세지 전송
					sendMessage(session, chatMessage);
				} else {
					System.out.println("채팅방 있음 - 새 채팅방 생성 불필요");
					
					// 기존 채팅방이 존재하므로 DB 에 채팅방 정보 추가 작업은 불필요
					// 채팅 메세지에 기존 채팅방 정보(ChatRoom 객체) 설정
					chatMessage.setRoom_id(chatRoom.getRoom_id()); // 조회된 룸 아이디 저장
					chatMessage.setType(ChatMessage2.TYPE_START); // 채팅 시작을 위한 START 타입 설정
					// 조회된 채팅방 정보(ChatRoom 객체)를 JSON 문자열로 변환하여 메세지로 저장
					chatMessage.setMessage(gson.toJson(chatRoom));
					
					// 송신자에게 메세지 전송
					sendMessage(session, chatMessage);
				}
				
			} else { // 단순 확인용으로 실제 else 블럭은 불필요
				System.out.println("수신자 아이디 없음");
			}
			
		} else if(chatMessage.getType().equals(ChatMessage2.TYPE_REQUEST_CHAT_LIST)) { // 기존 대화내역 요청
			// 현재 HttpSession 객체의 세션 아이디를 ChatMessage2 객체의 sender_id 로 저장
//			chatMessage.setSender_id(getHttpSessionId(session));
			// => 클라이언트에서 요청 시에 포함하여 전송해도 됨
			
			// ChatService - getChatMessageList() 메서드 호출하여 기존 채팅 내역 조회 요청
			// => 파라미터 : ChatMessage2 객체   리턴타입 : List<ChatMessage2>(chatMessageList)
			List<ChatMessage2> chatMessageList = chatService.getChatMessageList(chatMessage);
			
			// 기존 채팅 내역 존재할 경우에만 클라이언트측으로 전송
			if(chatMessageList != null && chatMessageList.size() > 0) {
				// 채팅 내역을 message 변수에 JSON 문자열로 변환하여 저장
				chatMessage.setMessage(gson.toJson(chatMessageList));
				// 채팅 내역 전송
				sendMessage(session, chatMessage);
			}
			
		} else if(chatMessage.getType().equals(ChatMessage2.TYPE_TALK)) { // 채팅 메세지 수신
			System.out.println("채팅메세지 수신됨!");
			
			// 채팅 메세지에 현재 시스템 날짜 및 시각 정보 전달
			// => getDateTimeForNow() 함수 정의하여 현재 시스템 날짜 및 시각 정보 리턴받기
			chatMessage.setSend_time(getDateTimeForNow());
			
			// ChatService - addChatMessage() 메서드 호출하여 채팅 메세지 DB 저장 요청
			// => 파라미터 : ChatMessage2 객체
			chatService.addChatMessage(chatMessage);
			
			// 채팅 메세지 전송할 사용자 확인(userList 객체의 receiver_id 존재 여부 확인)
			// => 현재 접속중인 사용자만 체크(이미 탈퇴한 회원 체크는 생략)
			if(userList.get(receiver_id) != null) { // 접속중일 경우
				System.out.println("상대방에게 메세지 전송 - " + chatMessage);
				// 상대방 아이디를 활용하여 WebSocketSession 객체 가져오기
				// => userList 에서 아이디에 해당하는 웹소켓 세션 아이디를 꺼낸 후
				//    다시 userSessionList 객체에서 웹소켓 세션 아이디에 해당하는 WebSocketSession 객체 꺼내기
				WebSocketSession receiver_session = userSessionList.get(userList.get(receiver_id));
				// sendMessage() 메서드 호출하여 채팅 메세지 전송
				sendMessage(receiver_session, chatMessage);
			}
			
			// 자신에게도 자신의 채팅 메세지 표시를 위해 전송
			sendMessage(session, chatMessage);
		} else if(chatMessage.getType().equals(ChatMessage2.TYPE_FILE_UPLOAD_COMPLETE)) { // 파일 업로드 완료
			// 메세지 타입을 FILE 로 변경한 후 현재 시각 정보도 설정한 후
			// DB 에 메세지 추가 요청 및 클라이언트(상대방 & 자신)로 전송
			// 채팅 메세지에 현재 시스템 날짜 및 시각 정보 전달(TALK 작업과 거의 동일함)
			// ----------------------------------------------------------
			chatMessage.setType(ChatMessage2.TYPE_FILE);
			chatMessage.setSend_time(getDateTimeForNow());
			
			chatService.addChatMessage(chatMessage);
			
			if(userList.get(receiver_id) != null) { // 접속중일 경우
				WebSocketSession receiver_session = userSessionList.get(userList.get(receiver_id));
				sendMessage(receiver_session, chatMessage);	
			}
			
			sendMessage(session, chatMessage);
		} else if(chatMessage.getType().equals(ChatMessage2.TYPE_READ)) { // 메세지 읽음
			// ChatService - updateMessageReadState() 메서드 호출하여 메세지 읽음 표시 처리 요청
			// => 파라미터 : ChatMessage2 객체   리턴타입 : void
			chatService.updateChatMessageReadState2(chatMessage);
		}
		
	}
	
	// ======================================================================================
	// 4. handleTransportError - 웹소켓 통신 과정에서 오류 발생 시 자동으로 호출되는 메서드
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		exception.printStackTrace();
		System.out.println("웹소켓 오류 발생!(handleTransportError)");
		
	}
	
	// 5. handleBinaryMessage - 웹소켓 상에서 파일이 전송되면 자동으로 호출되는 메서드
	// => 단, 파일 크기가 일정 크기(기본 최대 크기는 65KB)를 넘어서면 전송이 불가능함
	//    별도 설정을 추가하여 전송 가능 크기를 조정하거나 
	//    HTTP 프로토콜을 통해 전송하고 주소만 전달받아 사용(우리가 사용할 기능)
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		System.out.println("handleBinaryMessage");
		// 텍스트와 마찬가지로 XXXMessage 객체의 getPayload() 메서드를 호출하여 전송 데이터 가져오기
		System.out.println("BinaryMessage : " + message.getPayload());
	}
	// ======================================================================================
	// ======================================================================================
	// ======================================================================================
	// ======================================================================================
	// ======================================================================================
	// ======================================================================================
	// 각 웹소켓 세션(채팅방 사용자)들에게 메세지를 전송하는 메서드
	private void sendMessage(WebSocketSession session, ChatMessage2 chatMessage) throws Exception {
		session.sendMessage(new TextMessage(toJson(chatMessage)));
	}
	
	// ChatMessage 객체 정보를 JSON 문자열로 변환하여 리턴하는 메서드
	private String toJson(ChatMessage2 chatMessage) {
		// Gson 객체의 toJson() 메서드 호출하여 파라미터로 변환할 객체 전달
		return gson.toJson(chatMessage);
	}
	
	// =========================================================================================
	// HttpSession 객체에 저장된 세션 아이디 리턴하는 메서드
	private String getHttpSessionId(WebSocketSession session) {
		return session.getAttributes().get("sId").toString();
	}
	
	// WebSocketSession 객체의 아이디 리턴하는 메서드
	private String getWebSocketSessionId(WebSocketSession session) {
		return session.getId();
	}
	// =========================================================================================
	// 채팅방번호(room_id) 값 생성하는 메서드
	private String generateRoomId() {
		// UUID 활용하여 랜덤ID 값 리턴
		return UUID.randomUUID().toString();
	}
	
	// 현재 시스템의 날짜 및 시각 정보 리턴하는 메서드
	// => 표현 형식 : yyyy-MM-dd HH:mm:ss
	private String getDateTimeForNow() {
		// LocalXXX 클래스의 포맷팅을 위해 DateTimeFormatter 활용
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return LocalDateTime.now().format(dtf);
	}
	
	
	
}













