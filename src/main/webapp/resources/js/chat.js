// =======================================================================
// 1:1 채팅방에서 사용할 chat2.js 와의 중복 피하기 위해 임시로 사용 중지
// =======================================================================
// 자바스크립트 WebSocket 객체를 저장할 변수 선언
let ws;
// ---------------------------------------------------------
// 채팅 메세지 타입을 구분하기 위한 상수 설정
const TYPE_ENTER = "ENTER"; // 입장
const TYPE_LEAVE = "LEAVE"; // 퇴장
const TYPE_TALK = "TALK"; // 대화메세지

// 채팅 메세지 정렬 위치를 구분하기 위한 상수 설정(뷰페이지에서 표시할 때 위치 선정)
const ALIGN_CENTER = "center";
const ALIGN_LEFT = "left";
const ALIGN_RIGHT = "right";
// =========================================================

// 페이지 로딩 완료 시 채팅 시작
$(function() {
	// 채팅방 입장을 위한 웹소켓 연결 수행하는 connect() 함수 호출
	connect();
	// ---------------------------------------------
	// 채팅메세지 입력창(id 선택자 "chatMessage")에 메세지 입력 후 엔터키를 누르거나
	// 전송 버튼(id 선택자 "btnSend") 클릭 시 입력된 메세지 가져와서 
	// 입력 내용이 비어있지 않을 경우 sendMessage() 메서드 호출하여 메세지 전송 요청
	// 1) 버튼 클릭 이벤트
	$("#btnSend").on("click", function() {
		sendInputMessage();
	});
	
	// 2) 키 누름 이벤트(엔터키 판별)
	// => 익명함수 파라미터로 이벤트 발생 대상 정보를 이벤트 객체로 전달받기
	$("#chatMessage").on("keypress", function(event) {
//		console.log(event);
		// KeyboardEvent 객체의 keyCode 값 활용하여 누른 키의 정보(= 아스키코드값) 판별
		// => 참고) 엔터키의 아스키코드값 : 13
		let keyCode = event.keyCode;
//		console.log(keyCode);
		if(keyCode == 13) {
			sendInputMessage();
		}
	});
	
});
// ============================================================================
// 입력된 채팅 메세지 판별 후 전송 및 출력 작업 수행하는 sendInputMessage() 함수 정의
function sendInputMessage() {
	// 채팅메세지 입력창 내용 가져오기
	let message = $("#chatMessage").val();
	
	// 메세지 미입력 시 경고창 출력 및 함수 실행 종료 또는 그냥 함수 실행 종료
	if(message == "") {
		return;
	}
	
	// sendMessage() 함수 호출하여 메세지타입과 채팅 메세지 전달
	// => 이 때, 입력받은 채팅 메세지 전송이므로 메세지 타입을 TYPE_TALK 로 설정
	sendMessage(TYPE_TALK, message);
	
	// 자신의 채팅창에도 메세지 출력을 위해 appendMessage() 함수 호출
	// => 자신의 메세지는 우측 정렬을 위해 "right" 값을 두번째 파라미터로 전달
	appendMessage(message, ALIGN_RIGHT);
	
	// 메세지 전송 요청 후 입력창 초기화 및 입력창 커서 요청
	$("#chatMessage").val("");	
	$("#chatMessage").focus();	
}
// ============================================================================
// 최초 1회 웹소켓 연결을 수행하는 connect() 함수 정의
function connect() {
	// 요청 주소 생성(웹소켓 기본 프로토콜은 ws, 보안 프로토콜은 wss)
	let ws_base_url = "ws://localhost:8081/mvc_board";
//	let ws_base_url = "ws://192.168.3.200:8081/mvc_board";
	// 다른 클라이언트도 접속하여 체크하려면 localhost 대신 PC 주소 정확히 입력
	
	// 자바스크립트의 WebSocket 객체 생성하여 서버측에 웹소켓 통신 연결 요청(Handshaking 수행)
	// => 웹소켓 연결 시 최초 1회는 HTTP 프로토콜을 사용하여 통신을 수행
	// => 파라미터 : 웹소켓 요청을 위한 프로토콜 및 URL(ws://요청주소:포트번호/매핑주소)
	ws = new WebSocket(ws_base_url + "/echo");
	// => 개발자 도구의 Network 탭에서 요청 확인 가능(일반 HTTP 요청과 다른 형태로 구분됨)
	// => 반드시 /echo 요청에 대한 웹소켓 매핑 작업을 서버측에서 수행해야한다!
	//    (컨트롤러가 아닌 별도의 설정 파일(XML - ws-context.xml 파일)에서 매핑 수행)
	//    (스프링 설정 파일(XML) 생성 시 New - Spring Bean Configuration File 메뉴로 생성)
	// => 또한, 웹소켓 연결 시 자동으로 서버측(스프링)의 WebSocketHandler 클래스 구현체의
	//    afterConnectionEstablished() 메서드 자동 호출됨
	// -----------------------------------------------------------------------------------
	// WebSocket 객체의 onxxx 이벤트에 핸들러 함수 연결하여 특정 웹소켓 이벤트 발생 시 작업 처리
	// => 웹소켓을 통해 각종 정보를 주고 받으므로 이벤트 정보도 웹소켓을 통해 수신함
	//    따라서, 첫번째 연결을 제외한 지금부터 수행하는 모든 통신은 브라우저 개발자 도구를 통해 확인 불가
	// -----------------------------------------------------------------------------------
	// ws.onxxx 이벤트에 핸들러 함수 연결(주의! 함수 호출이 아닌 함수 전달)
	ws.onopen = onOpen;
	ws.onclose = onClose;
	ws.onmessage = onMessage;
	ws.onerror = onError;
	
}

// =====================================================================
// [ 웹소켓 이벤트를 처리할 핸들러 함수 정의 ]
function onOpen() {
//	console.log("onOpen()");
	
	// 채팅방에 입장 메세지 출력 => appendMessage() 함수 호출하여 메세지 전달
	appendMessage(">> 채팅방에 입장하였습니다 <<", ALIGN_CENTER);
	
	// 채팅방 입장 정보를 다른 사용자에게 전송(=> 서버측으로 입장 정보 전송)
	// => sendMessage() 함수 호출하여 메세지 타입과 전송할 메세지를 전달
	// => 전송할 메세지 타입 : 입장(TYPE_ENTER 상수)   전송할 메세지 : 없음(널스트링)
	sendMessage(TYPE_ENTER, "");
} 

// ================================================================
// 자신의 채팅창에 메세지를 표시(추가)하는 appendMessage() 함수
function appendMessage(message, align) {
//	console.log("message : " + message);
	$("#chatMessageArea").append("<div class='message " + align + "'>" + message + "</div>");
}

// ================================================================
// 전달받은 메세지를 웹소켓 서버측으로 전송하는 sendMessage() 함수
// => 파라미터 : 전송할 메세지타입, 메세지
function sendMessage(type, message) {
//	console.log("전송할 메세지 : " + message + ", 메세지 타입 : " + type);
	
	// toJsonString() 함수 호출하여 전송할 메세지를 JSON 형식으로 변환하여 리턴받기
	// => 파라미터 : 메세지타입, 메세지
	console.log("전송할 메세지(JSON) : " + toJsonString(type, message));
	
	// WebSocket 객체(ws)의 send() 메서드 호출하여 서버측으로 웹소켓 메세지 전송
	ws.send(toJsonString(type, message));
	// => 웹소켓을 통해 서버로 메세지가 전송되면 
	//    핸들러 객체(MyWebSocketHandler)의 handleTextMessage() 메서드가 자동 호출됨
}
// ================================================================
// 전달받은 메세지타입과 메세지를 JSON 형식 문자열로 변환하는 함수
function toJsonString(type, message) {
	// 전달받은 파라미터들을 하나의 객체로 묶기
	let data = {
		type : type,
		message : message
	}
//	console.log(data);
	
	// JSON.stringify() 메서드 호출하여 자바스크립트객체 -> JSON 문자열로 변환
	return JSON.stringify(data); // ex) {"type":"TALK","message":"1234"}
}

// ================================================================
// onMessage() 메서드는 이벤트 객체를 전달받아 사용
function onMessage(event) {
//	console.log("onMessage()");
//	console.log(event);
	// => MessageEvent {isTrusted: true, data: '{"type":"TALK","message":"11111"}', origin: 'ws://192.168.3.200:8081', lastEventId: '', source: null, …}
	
	// 전달받은 메세지는 event 객체의 data 속성에 저장되어 있음
//	console.log("event.data : " + event.data);
	// 주의! 전달받은 JSON 데이터(event.data)는 문자열이므로 JSON 객체 접근 방법으로 접근 불가!
//	console.log("event.data.type : " + event.data.type); // 오류는 아니나, undefined 출력됨
	// -------------------------------------------------------
	// JSON.parse() 메서드 활용하여 JSON 문자열 -> 자바스크립트 객체로 변환
	let data = JSON.parse(event.data);
//	console.log("data.type : " + data.type + ", data.message : " + data.message);
	
	// 채팅창에 메세지를 출력하는 appendMessage() 함수 호출하여 출력할 메세지 전달
//	appendMessage(data.message);
	// -------------------------------------------------------
	// 메세지 타입 판별(ENTER or LEAVE or TALK)
	// 1) ENTER 또는 LEAVE 일 경우 입/퇴장 메세지이므로 메세지만 출력
	console.log("ALIGN_CENTER : " + ALIGN_CENTER);
	if(data.type == TYPE_ENTER || data.type == TYPE_LEAVE) {
		// 시스템 메세지의 경우 appendMessage() 함수 두번째 파라미터로 "center" 값 전달(가운데 정렬 요청)
		appendMessage(data.message, ALIGN_CENTER);
	} else if(data.type == TYPE_TALK) {
		// 사용자 메세지의 경우 appendMessage() 함수 두번째 파라미터로 "left" 값 전달(가운데 정렬 요청)
		// => 자신의 메세지는 전송되지 않으므로 항상 좌측 정렬(다른 사용자의 메세지)
		appendMessage(data.sender_id + " : " + data.message, ALIGN_LEFT);
	}
	
} 

// ================================================================
function onClose() {
//	console.log("onClose()");
} 
// ================================================================
function onError() {
	console.log("onError()");
} 










