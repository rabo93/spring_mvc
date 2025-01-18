// ---------------------------------------------------------
// 채팅 메세지 타입을 구분하기 위한 상수 설정
const TYPE_ENTER = "ENTER"; // 입장
const TYPE_LEAVE = "LEAVE"; // 퇴장
const TYPE_TALK = "TALK"; // 대화메세지
const TYPE_INIT = "INIT"; // 채팅페이지 초기화
const TYPE_INIT_COMPLETE = "INIT_COMPLETE"; // 채팅페이지 초기화 완료

// 채팅 메세지 정렬 위치를 구분하기 위한 상수 설정(뷰페이지에서 표시할 때 위치 선정)
const ALIGN_CENTER = "center";
const ALIGN_LEFT = "left";
const ALIGN_RIGHT = "right";
// =========================================================
// 자바스크립트 WebSocket 객체를 저장할 변수 선언
var ws;

// 채팅창 정보를 저장할 변수 선언
var chatWindow;

// 페이지 로딩 완료 시 채팅 시작
$(function() {
	// 채팅방 입장을 위한 웹소켓 연결 수행하는 connect() 함수 호출
	connect();
});

// ============================================================================
// 최초 1회 웹소켓 연결을 수행하는 connect() 함수 정의
function connect() {
	// 요청 주소 생성(웹소켓 기본 프로토콜은 ws, 보안 프로토콜은 wss)
	let ws_base_url = "ws://localhost:8081/mvc_board";
	ws = new WebSocket(ws_base_url + "/echo2");
	console.log("chat2_top.js - WebSocket 객체 : " + ws);
	console.log("chat2_top.js - 웹소켓 연결 상태 : " + ws.readyState);
	
	ws.onopen = onOpen;
	ws.onclose = onClose;
	ws.onmessage = onMessage;
	ws.onerror = onError;
}

// =====================================================================
// [ 채팅창 열기 함수 정의 ]
function openChatWindow(receiver_id) {
	let url = "ChatMain2";
	
	// 새 창 열어서 chatWindow 변수에 저장
	chatWindow = window.open(url, "chat_window", "width=400,height=500");
	
	// 새 창 열고 난 후 새 창의 receiver_id 변수에 부모창에 전달된 receiver_id 저장
	chatWindow.receiver_id = receiver_id;
}
// =====================================================================
// [ 웹소켓 이벤트를 처리할 핸들러 함수 정의 ]
function onOpen() {
	console.log("onOpen()");
} 

// ================================================================
// onMessage() 메서드는 이벤트 객체를 전달받아 사용
function onMessage(event) {
	// JSON.parse() 메서드 활용하여 JSON 문자열 -> 자바스크립트 객체로 변환
	let data = JSON.parse(event.data);
	console.log("data : " + JSON.stringify(data));
//	console.log("data.type : " + data.type + ", data.message : " + data.message);
	// -------------------------------------------------------
	// 윈도우간의 데이터를 주고받기 위해 각자 서로 다른 창의 함수를 호출하는 대신
	// window 객체의 postMessage() 함수를 호출하면 윈도우끼리 데이터 전송이 가능함(문자열 형태)
	// => 실질적으로 위의 변환 코드는 불필요하지만
	//    만약, top.jsp 에서 웹소켓 통신을 별도로 수행할 경우에는 필요함
	// 단, 채팅창(자식창 = chatWindow)이 열려있을 경우에만 postMessage() 메서드로 전송
	if(chatWindow) {
		chatWindow.postMessage(event.data);
	} else { // 자식창이 없을 경우
		// [채팅] 하이퍼링크 옆에 * 표시 추가
		$("#messageStatus").html("*").css("color", "red");
	}
	
	// -------------------------------------------------------
	// 채팅창에 메세지를 출력하는 appendMessage() 함수 호출하여 출력할 메세지 전달
//	appendMessage(data.message);
	// -------------------------------------------------------
	// 메세지 타입 판별(ENTER or LEAVE or TALK)
	// 1) INIT 일 경우 채팅방 초기화이므로 채팅방 관련 정보 초기화
	// 2) ENTER 또는 LEAVE 일 경우 입/퇴장 메세지이므로 메세지만 출력
//	if(data.type == TYPE_INIT) { // 채팅페이지 초기 진입
//		// 채팅방 목록 표시 영역 초기화
//		$("#chatRoomListArea").empty();
//		
//		// 전달받은 message 속성값으로 채팅방 목록이 저장되어 전송됨
//		console.log(data.message + " : " + typeof(data.message));
//		if(data.message == "null") { // 채팅방 없을경우 null 값이 문자열로 전송됨
//			$("#chatRoomListArea").html("채팅중인 채팅방 없음");
//			return;
//		} 
//		
//		// 채팅방 목록을 JSON 객체 형태로 파싱하여 목록 표시
//		
//		
//		// 채팅방 초기화 완료 메세지를 서버측으로 전송
//		// => 메세지 타입 : INIT_COMPLETE, 수신자 아이디도 함께 전송   
//		sendMessage(TYPE_INIT_COMPLETE, "", data.receiver_id, "", "");
//		
//	} else if(data.type == TYPE_ENTER || data.type == TYPE_LEAVE) {
//		// 시스템 메세지의 경우 appendMessage() 함수 두번째 파라미터로 "center" 값 전달(가운데 정렬 요청)
//		appendMessage(data.message, ALIGN_CENTER);
//	} else if(data.type == TYPE_TALK) {
//		// 사용자 메세지의 경우 appendMessage() 함수 두번째 파라미터로 "left" 값 전달(가운데 정렬 요청)
//		// => 자신의 메세지는 전송되지 않으므로 항상 좌측 정렬(다른 사용자의 메세지)
//		appendMessage(data.sender_id + " : " + data.message, ALIGN_LEFT);
//	}
	
} 


// ================================================================
// 자신의 채팅창에 메세지를 표시(추가)하는 appendMessage() 함수
function appendMessage(message, align) {
//	console.log("message : " + message);
	$("#chatMessageArea").append("<div class='message " + align + "'>" + message + "</div>");
}

// ================================================================
// 전달받은 메세지를 웹소켓 서버측으로 전송하는 sendMessage() 함수
// => 파라미터 : 전송할 메세지타입, 송신자아이디, 수신자아이디, 채팅방아이디, 채팅메세지 

function sendMessage(type, sender_id, receiver_id, room_id, message, idx) {
	// toJsonString() 함수 호출하여 전송할 메세지를 JSON 형식으로 변환하여 리턴받기
	console.log("전송할 메세지(JSON) : " + toJsonString(type, sender_id, receiver_id, room_id, message));
	
	// WebSocket 객체(ws)의 send() 메서드 호출하여 서버측으로 웹소켓 메세지 전송
	ws.send(toJsonString(type, sender_id, receiver_id, room_id, message, idx));
	// => 웹소켓을 통해 서버로 메세지가 전송되면 
	//    핸들러 객체(MyWebSocketHandler)의 handleTextMessage() 메서드가 자동 호출됨
}
// ================================================================
// 전달받은 메세지타입과 메세지를 JSON 형식 문자열로 변환하는 함수
function toJsonString(type, sender_id, receiver_id, room_id, message, idx) {
	// 전달받은 파라미터들을 하나의 객체로 묶기
	let data = {
		type : type,
		sender_id : sender_id,
		receiver_id : receiver_id,
		room_id : room_id,
		message : message,
		idx : idx
	}
	
	// JSON.stringify() 메서드 호출하여 자바스크립트객체 -> JSON 문자열로 변환
	return JSON.stringify(data); // ex) {"type":"TALK","message":"1234"}
}

// ================================================================
function onClose() {
//	console.log("onClose()");
} 
// ================================================================
function onError() {
	console.log("onError()");
} 










