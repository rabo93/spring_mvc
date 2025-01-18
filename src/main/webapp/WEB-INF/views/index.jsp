<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<!-- 외부 CSS 파일(resources/css/default.css) 연결하기 -->
<!-- 외부 자원 접근을 위한 경로 지정 시 컨텍스트루트부터 탐색하지 않고 서버 상의 루트부터 탐색함 -->
<!-- 프로젝트명이 포함되는 상대경로로 지정하는 것이 안전하다! -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	#qrCodeContainer {
		border: 1px solid black; 
		width: 200px;
		height: 200px;
		margin: 20px;
		padding: 10px;
		display: none;
	}
</style>
</head>
<body>
	<header>
		<!-- 기본 메뉴 표시 영역 - inc/top.jsp 페이지 삽입 -->
		<!-- 상대경로를 사용하여 top.jsp 페이지를 지정할 경우 -->
		<!-- 1) 현재 index.jsp 파일 경로(views) 기준으로 inc/top.jsp 지정 -->
<%-- 		<jsp:include page="inc/top.jsp"></jsp:include> --%>
		<!-- 2) 루트(/) 기준으로 inc/top.jsp 지정 -->
		<!-- 스프링 뷰페이지 기준 루트는 webapp 디렉토리이므로 나머지 경로 기술해야함 .-->
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<article>
		<h1>MVC 게시판</h1>
		<h3><a href="BoardWrite">글쓰기</a></h3>
		<h3><a href="BoardList">글목록</a></h3>
		<h3><a href="BoardList2">글목록2</a></h3>
		<hr>
		<h3><a href="ChatGPTMain">ChatGPT 메인</a></h3>
		<hr>
		<%-- top.jsp 에서 로딩할 chat2.js 와의 중복 피하기 위해 임시로 사용 중지 --%>
<!-- 		<h3><a href="ChatMain">통합 채팅방 입장</a></h3> ChatController - chat/chat_main.jsp -->
		<h3><a href="ChatMain2">1:1 채팅방 입장</a></h3>
		<hr>
		<div align="center">
			<input type="text" id="qrData" placeholder="QR코드 데이터">
			<input type="button" id="generateQr" value="QR코드 생성"><br>
			<input type="button" id="generateQr2" value="QR코드 생성(서버)"><br>
			<div id="qrCodeContainer"><%-- QR 코드 표시 영역 --%></div>
		</div>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
	<script src="${pageContext.request.contextPath}/resources/js/jquery-3.7.1.js"></script>
	<%-- 클라이언트에서 QR 코드 생성이 가능한 qrcode.js 라이브러리 추가 --%>
	<script src="${pageContext.request.contextPath}/resources/js/qrcode.js"></script>
	<script type="text/javascript">
		$(function() {
			$("#generateQr").click(() => {
				$("#qrCodeContainer").empty(); 
				
				let data = $("#qrData").val();
				
				// qrcode.js 라이브러리를 활용한 QRCode 생성
				// => 객체 생성 시 첫번째 파라미터로 QR 코르를 출력할 위치 요소 전달(jQuery 객체 X)
				//    두번째 파라미터로 생성할 QR 코드 내용 지정
// 				new QRCode(document.querySelector("#qrCodeContainer"), data);
				
				// 두번째 방법
				// 두번째 파라미터 부분에 다양한 옵션을 지정하기 위해 객체 사용 가능
				new QRCode(document.querySelector("#qrCodeContainer"), {
					text: data, // QR 코드 내용
					width: 150, // 가로크기
					height: 150, // 세로크기
					colorDark: "#0000FF", // QR코드 어두운부분 색상
					colorLight: "#FFFFFF", // QR코드 밝은부분 색상
				});
				$("#qrCodeContainer").show();
			});
			// ======================================
			$("#generateQr2").click(() => {
				let data = $("#qrData").val();
				
				// AJAX 요청
				$.ajax({
					url: "GenerateQRCode", // HomeController 에서 매핑
					type: "GET",
					data: {
						data
					},
					xhrFields: { // 서버로부터 응답되는 원시데이터를 처리하기 위한 설정
						responseType: "blob" // 바이너리데이터를 수신하기 위한 응답 데이터타입 지정
						// => 이미지 파일등을 직접 수신하여 처리할 때 사용
					}
				}).done((response) => {
					// => 응답데이터는 xhrFields 속성의 responseType: "blob" 으로 인해 바이너리데이터가 전송되므로
					//    이를 URL 형태로 변환하여 직접 이미지로 출력하기
					const url = URL.createObjectURL(response);
					console.log(url);
					
					// QR 코드 표시 영역에 <img> 태그를 활용하여 출력(출력 대상은 변환된 url 값)
					$("#qrCodeContainer").html(`<img src="\${url}">`); // 백틱(``) 사용 시 자바스크립트 변수는 \${변수명} 으로 지정
					$("#qrCodeContainer").show();
				}).fail(() => {
					alert("QR 코드 생성 오류!");
				});
			});
		});
	</script>
	
</body>
</html>










