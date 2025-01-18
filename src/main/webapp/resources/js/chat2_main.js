// 채팅 메세지 타입을 구분하기 위한 상수 설정
// => 외부 자바스크립트 파일끼리 자동 완성 불가로 인해 임시로 동일한 상수 중복으로 기술
const TYPE_ENTER = "ENTER"; // 입장
const TYPE_LEAVE = "LEAVE"; // 퇴장
const TYPE_TALK = "TALK"; // 대화메세지
const TYPE_INIT = "INIT"; // 채팅페이지 초기화
const TYPE_INIT_COMPLETE = "INIT_COMPLETE"; // 채팅페이지 초기화 완료
const TYPE_ERROR = "ERROR"; // 채팅페이지 관련 에러
const TYPE_START = "START"; // 채팅창 표시
const TYPE_REQUEST_CHAT_ROOM_LIST = "REQUEST_CHAT_ROOM_LIST"; // 채팅페이지 채팅방 목록
const TYPE_REQUEST_CHAT_LIST = "REQUEST_CHAT_LIST"; // 채팅페이지 채팅방 대화 목록
const TYPE_FILE_UPLOAD_COMPLETE = "FILE_UPLOAD_COMPLETE"; // 파일 업로드 완료
const TYPE_FILE = "FILE"; // 파일메세지
const TYPE_READ = "READ"; // 메세지 읽음


// 채팅 메세지 정렬 위치를 구분하기 위한 상수 설정(뷰페이지에서 표시할 때 위치 선정)
const ALIGN_CENTER = "center";
const ALIGN_LEFT = "left";
const ALIGN_RIGHT = "right";
// =========================================================
var ws;     // 부모창이 관리하는 웹소켓 객체를 저장할 변수
var receiver_id; // 부모창에서 전달받은 수신자 아이디 저장할 변수
var sId; // 부모창의 top.jsp 에 생성된 <input type="hidden" id="sId"> 태그 값(세션아이디) 저장할 변수
// => 자바스크립트 파일에서 JSTL(EL) 등 사용 불가능하므로 세션 아이디 접근을 위해 추가 필요

// 채팅창에서 서버의 리소스(이미지 등) 접근을 위해 EL 사용이 불가능하므로
// 현재 채팅창 주소 정보로부터 서블릿주소를 제외한 나머지 주소 추출
// ex) http://localhost:8081/mvc_board/ChatMain2 => http://localhost:8081/mvc_board
const url = location.href;
var baseUrl = url.substring(0, url.lastIndexOf("/ChatMain2"));

$(function() {
	// 페이지 로딩 시 hidden 속성 세션 아이디 가져오기
	// => 부모창에 있는 요소이므로 $("선택자", opener.document) 형태로 접근 필요
	sId = $("#sId", opener.document).val();
	console.log("sId : " + sId);
	
	if(!sId) {
		alert("로그인이 필요합니다.\n로그인 페이지로 이동합니다.");
		opener.location.href = "MemberLogin"; // 부모창 로그인 페이지로 이동
		window.close(); // 자식창(채팅창) 닫기
	}
	
	// --------------------------------------------
	// 창 크기 기본 사이즈로 리셋(새로고침 시 활용)
	resetWindowSize();
	// --------------------------------------------
	// 채팅목록 옆 채팅창 표시 영역을 최초 페이지 로딩 시 숨김 처리
	$("#chatRoomArea").hide();
	
	
	console.log("부모창에서 전달받은 receiver_id : " + receiver_id);
	// =======================================================
	// 부모창에서 postMessage() 함수로 메세지 전송 시
	// 자식창에서 "message" 이벤트 핸들링
	// => 이벤트 함수 호출 시 event 객체 전달됨
	window.onmessage = (e) => {
		// 부모창에서 전달된 메세지는 이벤트 함수의 data 속성 확인
		console.log("부모창 메세지 : " + e.data);
//		console.log(e.source); // 부모창의 window 객체를 source 속성으로 다룰 수 있음
		
		// 부모로부터 전달받은 JSON 메세지 파싱
		let data = JSON.parse(e.data);
		
		if(data.type == TYPE_INIT) { // 채팅 윈도우 초기화
			showChatList(data);
		} else if(data.type == TYPE_START) { // 채팅창 열기
			startChat(data);
		} else if(data.type == TYPE_REQUEST_CHAT_LIST) { // 채팅창 목록
			// 기존 채팅 내역이 있을 경우에만 수신되는 메세지
			console.log("채팅 내역 수신됨");
			console.log(data.message);
			// 기존 채팅 내역이 저장된 message 내의 리스트 반복(JSON 파싱 필요)하여
			// appendMessage() 메서드 호출하여 메세지 표시 반복
			for(let message of JSON.parse(data.message)) {
				appendMessage(message.type, message.sender_id, message.receiver_id, message.message, message.send_time);
			}
			
		} else if(data.type == TYPE_TALK || data.type == TYPE_FILE) { // 채팅메세지 또는 파일메세지
			console.log("TYPE_TALK - 채팅방 오픈 여부 확인");
			// 채팅방이 열려있는지 & 해당 채팅방이 현재 수신된 메세지의 상대방과의 채팅방인지 체크
			if(isOpenedChatRoom() && getOpenedChatRoomId() == data.room_id) {
				appendMessage(data.type, data.sender_id, data.receiver_id, data.message, data.send_time);
				
				// 수신자가 자신일 때 메세지 읽음 처리
				if(data.receiver_id == sId) {
					// 메세지 읽음 처리를 위해 서버로 읽음 메세지 전송(type 속성 READ 로 설정)
					sendMessage(TYPE_READ, data.sender_id, data.receiver_id, data.room_id, data.message, data.idx);
				}
			} else { // 채팅창이 열려있지 않을 경우
				// 해당 사용자와의 채팅방 존재 여부 판별
				if($(".chatRoomList." + data.room_id).length == 0) { // 또는 if($(".chatRoomList").hasClass(data.room_id)) {} 사용
					// 채팅방 목록에 새 채팅방 정보 1개 추가
					// 대화 수신 시 채팅방 제목과 채팅방 상태가 전달되지 않기 때문에
					// DB 에 저장되는 형식에 맞게 강제로 생성
					// (이 때, 자신이 수신자로 전달되었으므로 채팅방에는 sender_id 와 receiver_id 를 반대로 설정)
					let room = {
						room_id : data.room_id,
						title : data.receiver_id + " 님과의 대화",
						sender_id : data.receiver_id,
						receiver_id : data.sender_id
					}
					
					appendChatRoomToRoomList(room);
				}
				
				// 룸아이디가 동일한 채팅방 목록 제목 옆에 카운트 표시
				// => 기존 카운트 값 가져와서 널스트링일 경우 0으로 설정
				let messageCnt = $(".chatRoomList." + data.room_id).find(".messageStatus").text();
				
				if(!messageCnt) { // undefined 또는 널스트링 판별
					messageCnt = 0;
				}
				
				console.log("메세지 갯수 : " + messageCnt + ", " + typeof(messageCnt));
				
				// 카운트 값 1 증가시키기
				$(".chatRoomList." + data.room_id).find(".messageStatus").html(Number(messageCnt) + 1);
			}
			
		}
	}
	// =======================================================
	// 채팅창 초기화를 수행할 initChat() 함수 호출
	initChat();
	
}); // $(function() {}) 끝
// ============================================================================
// [ 자식창 채팅창 초기화 ]
function initChat() {
	let wsCheckInterval = setInterval(() => {
		// 문서 로딩 시 부모창(window.opener)에 접근 가능 => opener 로 사용(window 생략)
		// ------------------------------------------------------------------------------
		// 부모창의 웹소켓 객체를 가져오기
		ws = opener.ws;
		
		console.log("chat2_main.js - 웹소켓 연결 상태 : " + ws.readyState);
		
		if(ws == null || ws.readyState != ws.OPEN) { // 웹소켓 객체가 없거나 연결 상태가 OPEN 이 아닐 경우
			console.log("웹소켓 연결 안됨!");
			
			// --------------------------------------------------
			// ex) "연결중입니다" 메세지 모달창 표시
			// --------------------------------------------------
			if(ws.readyState == ws.CLOSING || ws.readyState == ws.CLOSED) {
				console.log("부모의 connect() 함수 호출!");
				opener.connect();
			}
		} else {
			console.log("웹소켓 연결 완료!");
			
			// 부모창을 통해 메세지를 전송할 sendMessage() 함수 호출
			sendMessage(TYPE_INIT, "", "", "", "");
			
			// 현재 실행중인 인터벌(반복 작업) 종료하기 위해 clearInterval() 함수 호출
			// => 함수 파라미터로 반복중인 인터벌 객체의 아이디(= 인터벌 객체) 전달
			clearInterval(wsCheckInterval);
		}
	}, 1000); // 1초마다 반복(1000ms = 1초)
	
}
// ============================================================================
// [ 채팅방 목록 표시 ]
function showChatList(data) {
	console.log("receiver_id : " + receiver_id);
	
	// 채팅방 목록 표시 영역 초기화
	$("#chatRoomListArea").empty();
	
	// 전달받은 message 속성값으로 채팅방 목록이 저장되어 전송됨
	console.log(data.message + " : " + typeof(data.message));
	if(data.message == "null") { // 채팅방 없을경우 null 값이 문자열로 전송됨
		$("#chatRoomListArea").html("<div class='chatRoomList empty'>채팅중인 채팅방 없음</div>");
	} else {
		// 전달받은 message 속성값에 저장된 채팅방 목록을 JSON 객체 형태로 파싱하여 목록 표시
		// 이 때, 복수개의 채팅방 목록이 배열 형태로 전달되므로 반복문을 통해 접근
		for(let room of JSON.parse(data.message)) {
			appendChatRoomToRoomList(room);
		}
	}
	
	// 채팅방 초기화 완료 메세지를 서버측으로 전송
	// => 메세지 타입 : INIT_COMPLETE, 수신자 아이디도 함께 전송   
	sendMessage(TYPE_INIT_COMPLETE, "", receiver_id, "", "");
}
// ============================================================================
// [ 채팅창 생성 및 채팅창 목록 추가 ]
function startChat(data) {
	console.log("startChat - 채팅창 생성");
	
	// 기존 채팅방 목록에 "채팅방 없음" 항목 있을 경우 삭제
	$(".chatRoomList.empty").remove();
	// --------------------------------
	// 채팅방 목록 추가(표시) 및 채팅방 생성(표시)
	// 채팅방 목록에 새 채팅방을 추가하기 위해 appendChatRoomToRoomList() 함수 호출
	// (기존 대화내역이 없는 사용자와 채팅 시작 시 채팅방 목록에 새 채팅방 추가 위함)
	console.log("data.message : " + data.message);
	// data.message 항목에 저장된 채팅방 정보가 JSON 문자열로 저장되어 있으므로
	// 다시 한 번 파싱하여 함수 파라미터로 전달 
	// ex) "message":"{\"room_id\":\"260c6215-a081-4afc-9eac-f920c3c1d641\",\"title\":\"hong 님과의 대화\",\"sender_id\":\"admin\",\"receiver_id\":\"hong\",\"status\":1}
	appendChatRoomToRoomList(JSON.parse(data.message));
	
	// 채팅창 표시하기 위해 showChatRoom() 함수 호출
	showChatRoom(data);
}
// ============================================================================
// [ 채팅방 목록 영역에 1개 채팅방 정보 추가 ]
function appendChatRoomToRoomList(room) {
	console.log("appendChatRoomToRoomList - 채팅방 목록 1개 추가");
	console.log(room);
	
	// 클래스 선택자 "chatRoomList" 클래스 요소 중
	// 클래스 선택자에 룸 아이디에 해당하는 요소가 없을 경우
	// 채팅방 목록에 새 채팅방 목록 1개 추가
	if(!$(".chatRoomList").hasClass(room.room_id)) {
		// 채팅방 제목과 채팅방 상태가 전달되지 않았을 경우 기본값 설정
		let title = room.title;
		// -------------------------------------------------------
		// 채팅방 상태가 1이 아닐 경우 처리
		// -------------------------------------------------------
		// 새 채팅방 목록 1개의 div 태그 작성
		let divRoom = "<div class='chatRoomList " + room.room_id + "'>" 
						+ title 
						+ "<span class='messageStatus'></span></div>";
		// 채팅방 목록 영역에 div 태그 출력
		$("#chatRoomListArea").prepend(divRoom);
		
		// --------------------------------------------------
		console.log("unread_count : " + room.unread_count);
		console.log("채팅 목록 : " + $(".chatRoomList." + room.room_id).find(".messageStatus").html());
		
		// 읽지 않은 메세지 갯수가 0보다 클 경우 채팅방 제목 옆에 표시
		if(room.unread_count > 0) {
			$(".chatRoomList." + room.room_id).find(".messageStatus").html(room.unread_count);
		}	
		// --------------------------------------------------
		// 생성된 채팅방 목록 1개 div 태그에 더블클릭 이벤트 연결
		// => 주의! 2개 클래스를 묶어서 지정 시 $(".A.B") 형식으로 지정
		$(".chatRoomList." + room.room_id).on("dblclick", () => {
			// 채팅방 1개 표시하도록 showChatRoom() 함수 호출
			// => 파라미터 : 채팅방 1개 정보가 저장된 room
			// 단, id 선택자 "chatRoom" 이 존재하고 
			// 더블클릭 된 채팅방의 room_id 와 열려있는 채팅방의 room_id 가 다를 경우
			// 열려있는 채팅방 제거 후에 새 채팅방 표시
			// 만약, 채팅방이 열려있지 않을 경우에는 바로 채팅방 표시
			console.log("chatRoom 룸 아이디 : " + $("#room_id").val());
			if($("#chatRoom").length == 1 && $("#room_id").val() != room.room_id) {
				closeRoom();
				showChatRoom(room);
			} else if($("#chatRoom").length == 0) {
				showChatRoom(room);
			}
			
		});
	}
}
// ============================================================================
// [ 채팅방 영역에 1개의 채팅방 생성(표시) ]
function showChatRoom(room) {
	console.log("showChatRoom - 채팅화면 표시");
	console.log(room);
	
	// 현재 채팅창 크기를 채팅목록 + 채팅화면 영역만큼으로 변경
	window.resizeTo(750, 500);
	
	// 수신자가 세션 아이디와 동일할 경우
	// 방 생성에 사용될 receiver_id 값을 송신자 아이디로 변경하고, 아니면 그대로 사용
	let receiver_id = room.receiver_id == sId ? room.sender_id : room.receiver_id;
	
	console.log("새 채팅방 표시!");
	
	// 생성할 채팅방 div 태그를 문자열로 생성
	let divRoom = '<div id="chatRoom">'
					+ '	<div id="chatTitleArea">&lt;' + receiver_id + '&gt;</div>'
					+ '	<div id="chatMessageArea"></div>'
					+ '	<div id="commandArea">'
					+ '		<input type="hidden" id="room_id" value="' + room.room_id + '">'
					+ '		<input type="hidden" id="receiver_id" value="' + room.receiver_id + '">'
					+ '		<input type="text" id="chatMessage" onkeypress="checkEnterKey(event)">'
					+ '		<input type="button" id="btnSend" value="전송" onclick="sendInputMessage()">'
					+ '		<span class="fileArea">'
					+ '			<label for="file"><img src="' + baseUrl + '/resources/images/clip.png"></label>'
					+ '			<input type="file" id="file" onchange="sendFile()" accept="image/*">'
					+ '		</span">'
					+ '		<br>'
					+ '		<input type="button" id="btnCloseRoom" value="닫기" onclick="closeRoom()">'
					+ '		<input type="button" id="btnQuitChat" value="대화나가기" onclick="quitChat()">'
					+ '	</div>'
					+ '</div>';
	
	console.log("출력할 채팅방 div 태그 : " + divRoom);
	
	// id 선택자 "chatRoomArea" 영역에 채팅방 div 태그 출력
	$("#chatRoomArea").html(divRoom);
	// 채팅방 화면에 보이기
	$("#chatRoomArea").show(); // 위의 객체에 메서드 연결하여 사용도 가능
	
	// 채팅목록에 해당 채팅방의 수신메세지 표시(*) 제거
	$(".chatRoomList." + room.room_id).find(".messageStatus").empty();
	// ------------------------------------------------------------------
	// 기존 채팅 내역을 불러오기 위한 요청 전송(송신자 아이디와 룸 아이디 포함하여 전송)
	sendMessage(TYPE_REQUEST_CHAT_LIST, sId, "", room.room_id, "");
		
}

// ============================================================================
// [ 채팅창에 메세지 출력 ]
function appendMessage(type, sender_id, receiver_id, message, send_time) {
	// ------------------ 채팅 메세지 날짜 형식 변환하기 -----------------
	// send_time 값이 비어있을 경우(undefined or "") 현재 시스템 날짜 설정하고
	// 아니면, 전송받은 날짜를 자바스크립트 Date 객체로 변환
	let date;
	if(!send_time) { // 날짜가 전송되지 않았을 경우
		date = new Date(); // 새 Date 객체 생성(현재 시스템의 날짜 및 시각 정보 생성)
	} else {
		// 새 Date 객체 생성(전달받은 날짜 및 시각 정보를 기준으로 생성)
		date = new Date(send_time);
	}
	
	// 현재 시스템 날짜 및 시각 정보를 기준으로 Date 객체(now) 생성
	let now = new Date();
	// ------------------------------
	// 기본적으로 시:분은 무조건 표시되므로 먼저 전송 시각에 저장
	send_time = date.getHours() + ":" + date.getMinutes();
	
	// 메세지 전송일자가 올해가 아니거나 올해의 오늘이 아닐 경우 날짜 추가
	// 메세지 전송일자의 연도가 올해가 아닐 경우 전송 연도도 추가
	if(date.getFullYear() != now.getFullYear() || (date.getFullYear() == now.getFullYear() && (date.getMonth() != now.getMonth() || date.getDate() != now.getDate()))) {
		send_time = (date.getMonth() + 1) + "-" + date.getDate() + " " + send_time; // 날짜 추가
		
		if(date.getFullYear() != now.getFullYear()) { // 올해가 아닐 경우
			send_time = date.getFullYear() + "-" + send_time; // 연도 추가
		}
	}
	// -------------------------------------------------------------------
	// 메세지 타입에 따라 정렬 위치 다르게 표시하기 위한 div 태그 생성
	let div_message = "";
	
	// 메세지 타입 판별(TALK 가 아닌것 vs TALK 인 것(송신자가 or 수신자가 자신인 경우))
	if(type != TYPE_TALK && type != TYPE_FILE) { // 시스템 메세지(채팅메세지 또는 파일메세지 아님)
		// 가운데 정렬을 통해 메세지만 표시
		div_message = "<div class='message message_align_center'><span class='chat_text'>" + message + "</span></div>";
	} else { // 채팅메세지 또는 파일메세지
		let span_time = "<span class='send_time'>" + send_time + "</span>"; // 메세지 전송 시각
		let span_message = ""; // 메세지
		
		// 채팅메세지와 파일메세지를 각각 다르게 설정
		if(type == TYPE_TALK) { // 채팅메세지
			span_message = "<span class='chat_text'>" + message + "</span>";
		} else { // 파일메세지
			// 채팅창에 표시할 썸네일 이미지에 대한 img 태그 생성
			// => 이미지에 하이퍼링크 연결(새 창에서 원본 이미지 표시)
			// => 하이퍼링크 경로는 업로드 된 이미지(썸네일 아님)를 링크로 설정
			let hrefUrl = baseUrl + "/resources/upload/chat/" + message.split(":")[0]; // 원본이미지
			let imgUrl = baseUrl + "/resources/upload/chat/" + message.split(":")[1]; // 썸네일이미지
			span_message = "<span class='chat_img'><a href='" + hrefUrl + "' target='_blank'><img class='img' src='" + imgUrl + "'></a></span>";
		}
		
		if(sender_id == sId) { // 자신의 메세지일 때(자신이 송신자일 경우)
			div_message = "<div class='message message_align_right'>" + span_time + span_message + "</div>"
		} else { // 상대방의 메세지일 때(자신이 수신자일 경우)
			div_message = "<div class='message message_align_left'><div class='sender_id'>" + sender_id + "</div>" + span_message + span_time + "</div>"
		}
		
	}
	
	// 채팅방 메세지 표시 영역("#chatMessageArea")에 메세지 추가
	$("#chatMessageArea").append(div_message);
	
	// 채팅 메세지 출력창 스크롤바를 항상 맨 밑으로 유지
	// 메세지 표시 영역의 스크롤바 크기(높이)를 구한 후
	// 채팅 메세지 표시 영역의 스크롤바 위치(scrollTop)값으로 지정
	$("#chatMessageArea").scrollTop($("#chatMessageArea")[0].scrollHeight);
}

// ============================================================================
// [ 부모창의 sendMessage() 함수를 호출하여 메세지 전송을 요청하는 함수 정의 ]
function sendMessage(type, sender_id, receiver_id, room_id, message, idx) {
	// 부모창의 자바스크립트 함수 chat2_top.js - sendMessage() 호출
	opener.sendMessage(type, sender_id, receiver_id, room_id, message, idx);
}
// ============================================================================
// 채팅창 키보드 입력 엔터키 판별
function checkEnterKey(event) {
	// KeyboardEvent 객체의 keyCode 값 활용하여 누른 키의 정보(= 아스키코드값) 판별
	// => 참고) 엔터키의 아스키코드값 : 13
	let keyCode = event.keyCode;
	
	if(keyCode == 13) {
		sendInputMessage();
	}
}

// 입력된 채팅 메세지 판별 후 전송 및 출력 작업 수행하는 sendInputMessage() 함수 정의
function sendInputMessage() {
	// 채팅메세지 입력창 내용 가져오기
	let message = $("#chatMessage").val();
	console.log("입력한 메세지 : " + message);
	
	// 메세지 미입력 시 경고창 출력 및 함수 실행 종료 또는 그냥 함수 실행 종료
	if(message == "") {
		return;
	}
	
	// 채팅창 영역의 room_id 와 receiver_id 정보 가져와서 변수에 저장
	let room_id = $("#commandArea > #room_id").val();	
	let receiver_id = $("#commandArea > #receiver_id").val();	
	
	// sendMessage() 함수 호출하여 채팅 메세지 전달(TYPE_TALK 로 전송)
	// => sendMessage(type, sender_id, receiver_id, room_id, message)
	sendMessage(TYPE_TALK, sId, receiver_id, room_id, message);
	
	// 자신의 채팅창에도 메세지 출력을 위해 appendMessage() 함수 호출
	// => 자신의 메세지는 우측 정렬을 위해 "right" 값을 두번째 파라미터로 전달
//	appendMessage(message, ALIGN_RIGHT);
	
	// 메세지 전송 요청 후 입력창 초기화 및 입력창 커서 요청
	$("#chatMessage").val("");	
	$("#chatMessage").focus();	
}
// ============================================================================
// [ 채팅창 크기 처음 상태로 조정 ]
function resetWindowSize() {
	window.resizeTo(400, 500);
}
// ============================================================================
// 채팅창에서 닫기 버튼 클릭 시 채팅방 닫기(대화 종료가 아닌 화면 상에서 채팅방 제거)
function closeRoom() {
	$("#chatRoom").remove(); // 채팅방 제거
	$("#chatRoomArea").hide(); // 채팅방 영역 숨기기
	resetWindowSize(); // 채팅창 크기 원래 사이즈로 리셋
}
// ============================================================================
// 채팅방 열려있는지 확인
function isOpenedChatRoom() {
	if($("#chatRoom").length == 1) { // 채팅방 열려있을 경우
		return true;
	}
	
	return false;
}

// 열려있는 채팅방의 룸아이디 리턴
function getOpenedChatRoomId() {
	// 채팅방이 열려있을 때 해당 채팅방 room_id 값 리턴
	if(isOpenedChatRoom()) {
		return $("#chatRoom").find("#room_id").val();
	}
}

// =========================================================
// =========================================================
// =========================================================
// [[[[[[[[[[[[[[[[ 파일 전송 ]]]]]]]]]]]]]]]]
function sendFile() {
	// 파일 선택창에서 선택한 파일 가져오기
	let file = $("#file")[0].files[0];
	console.log(file);
	
	// ========= 임시 =========
	// 파일 미리보기를 위해 FileReader 객체 생성
//	let reader = new FileReader();
//	// FileReader 를 통해 대상 파일을 읽어올 때 핸들링
//	reader.onload = (e) => { // 읽어들인 파일이 포함된 이벤트 객체가 함수 파라미터(e)로 전달됨
//		let div_message = "<div class='message message_align_right'><span class='send_time'></span><span class='chat_img'><img src='" + e.target.result + "'></span></div>";
//		$("#chatMessageArea").append(div_message);
//	};
//	
//	// Blob 이나 File 형식의 데이터 읽어오기 위해
//	// FileReader 객체의 readAsDataURL() 메서드 호출
//	// => 파라미터 : File 객체
//	reader.readAsDataURL(file);
	// ========================
	// ========= 임시 =========
	// 웹소켓을 사용하여 파일을 서버로 전송
	// => readAsDataURL() 메서드 대신 readAsArrayBuffer() 메서드로 파일 읽어오기 필요
	// => 또한, 파일 전송시에도 웹소켓 객체의 send() 메서드 호출하는 형태는 동일함
	//    단, send() 메서드 파라미터로 읽어온 파일(e.target.result) 전달
//	let reader = new FileReader();
//	// FileReader 를 통해 대상 파일을 읽어올 때 핸들링
//	reader.onload = (e) => { // 읽어들인 파일이 포함된 이벤트 객체가 함수 파라미터(e)로 전달됨
//		ws.send(e.target.result);
//		// => 전송 완료 시 스프링 웹소켓 핸들러의 handleBinaryMessage() 메서드 호출됨
//		console.log("파일이 서버로 전송되었습니다 : " + file.name);
//	};
//	
//	reader.readAsArrayBuffer(file);
	// ========================
	// [ AJAX 로 파일 전송 처리 ]
	// 주의! form 태그를 사용하여 <input type="file"> 태그가 감싸져 있지 않을 경우
	// 반드시 자바스크립트의 FormData 객체를 통해 전송할 파일을 담아 AJAX 의 data 속성으로 전송
	let formData = new FormData(); // 기본 객체 생성
	// FormData 객체의 append() 메서드 호출하여 키, 값 형태로 전송할 파일 전달
	formData.append("file", file); // 위에서 가져온 파일 정보 전달
	
	// AJAX 로 파일 전송 코드 작성 시 주의사항
	// 1) type 속성을 "post" 로 지정
	// 2) data 속성에 전달할 데이터는 FormData 객체를 사용(form 태그 미사용 시)
	// 3) processData 속성을 false 값으로 지정하여 전송 데이터를 쿼리 스트링으로 변환되는 것을 차단
	// 4) contentType 속성을 false 값으로 지정하여 content-type 헤더를 multipart/form-data 로 설정
	$.ajax({
		type : "POST",
		url : "ChatFileUpload", // 웹소켓 핸들러 대신 서블릿 클래스(= 컨트롤러 = ChatController)로 처리
		data : formData,
		dataType : "json",
		processData : false, // 파일 전송을 위해 전송 데이터 -> 쿼리 스트링 형식으로의 변환 차단
		contentType : false // 파일 전송을 위해 content-type 헤더를 multipart/form-data 로 설정
	}).done(function(response) {
		console.log("파일 업로드 성공 후 AJAX 응답 결과");
		console.log(response);
		
		if(response.result == "fail") { // 파일 처리를 수행하지 못한 경우(이미지 파일 아님 등)
			alert(response.message); // 경고창 출력
			return;
		} 
		
		console.log("fileName : " + response.fileName + ", thumbnailFileName : " + response.thumbnailFileName);
		if(response.fileName != "" && response.thumbnailFileName != "") {
			// sendMessage() 함수 호출하여 서버 파일 업로드 완료 신호를 웹소켓을 통해 전송
			// => 이 때, 메세지는 "업로드파일명:썸네일파일명" 형태로 전송 
			sendMessage(TYPE_FILE_UPLOAD_COMPLETE, sId, getReceiverId(), getRoomId(), response.fileName + ":" + response.thumbnailFileName);
		}
	}).fail(function() {
		alert("파일 전송 오류 발생!\n다시 시도해 주세요!");
	});
	
}

function getReceiverId() {
	return $("#receiver_id").val();
}

function getRoomId() {
	return $("#room_id").val();
}








