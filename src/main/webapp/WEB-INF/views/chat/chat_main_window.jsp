<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	/* 채팅방 목록 표시 영역 */
	#chatRoomListArea {
		width: 300px;
		height: 300px;
		border: 1px solid black;
		display: inline-block;
		overflow: auto;
	}
	
	.chatRoomList {
		height: 20px;
		font-size: 12px;
		margin: 5px;
		border-bottom: 1px solid gray;
	}
	
	.chatRoomList:hover {
		background-color: pink;
	}
	
	
	/* 채팅방 표시 영역 */
	#chatRoomArea {
		width: 350px;
		height: 300px;
		border: 1px solid black;
		display: inline-block;
		overflow: auto;
	}
	
	/* 1개 채팅방 */
	#chatRoom {
		display: inline-block;
		margin: 10px;
	}
	
	#chatTitleArea {
		text-align: center;
	}
	
	#chatMessageArea {
		width: 300px;
		height: 200px;
		border: 1px solid gray;
		overflow: auto;
	}
	
	#chatMessage {
		width: 200px;
	}
	
	#btnSend {
		width: 50px;
	}
	
	#commandArea {
		width: 310px;
		position: relative;
	}
	
	.message {
		font-size: 13px;
		margin: 10px;
	}
	
	.message.message_align_center {
		text-align: center;
	}
	
	.message.message_align_left {
		text-align: left;
	}
	
	.message.message_align_right {
		text-align: right;
	}
	
	.message.message_align_right .chat_text {
		background-color: yellow;
	}
	
	.message.message_align_left .chat_text {
		background-color: lightpink;
	}
	
	.message.message_align_center .chat_text {
		background-color: skyblue;
	}
	
	.sender_id, .send_time {
		font-size: 10px;
		margin: 0px 5px 0px 5px;
	}
	
	.messageStatus {
		margin-left: 10px;
		padding: 0px 5px 0px 5px;
		color: white;
		background-color: red;
	}
	
	/* ------- 채팅방 파일 선택 영역 --------- */
	/* 실제 파일 선택 요소 숨기기 */
	.fileArea input {
		position: absolute;
		width: 0;
		height: 0;
		padding: 0;
		border: 0;
		overflow: hidden;
	}	
	
	/* 파일 선택 요소 대신 이미지 표시 */
	.fileArea label {
		display: inline-block;
		vertical-align: middle;
		cursor: pointer;
	}
	
	.fileArea img {
		width: 20px;
		height: 20px;
	}
</style>
</head>
<body>
	<header>
<%-- 		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include> --%>
	</header>
	<article>
		<h3>채팅 - ${sessionScope.sId}</h3>
		<hr>
		<%-- 채팅방 목록 표시 영역 --%>
		<div id="chatRoomListArea">
			<%-- 각각의 채팅방 목록이 표시될 위치 --%>
		</div>
		<%-- 채팅중인 채팅방 표시 영역 --%>
		<div id="chatRoomArea">
		
		</div>
	</article>
	<footer>
<%-- 		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include> --%>
	</footer>
	<script src="${pageContext.request.contextPath}/resources/js/jquery-3.7.1.js"></script>
	<script src="${pageContext.request.contextPath}/resources/js/chat2_main.js"></script>
</body>
</html>










