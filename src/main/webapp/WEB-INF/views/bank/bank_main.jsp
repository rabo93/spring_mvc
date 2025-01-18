<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<!-- 외부 CSS 파일(resources/css/default.css) 연결하기 -->
<!-- 외부 자원 접근을 위한 경로 지정 시 컨텍스트루트부터 탐색하지 않고 서버 상의 루트부터 탐색함 -->
<!-- 프로젝트명이 포함되는 상대경로로 지정하는 것이 안전하다! -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
</head>
<body>
	<header>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<article>
		<h1>계좌관리 메인페이지</h1>
		<h3>
			<%-- 엑세스토큰 존재 여부에 따라 다른 링크 표시 --%>
			<%-- 세션 객체의 "token" 속성이 비어있을 경우 계좌 미인증 회원으로 계좌연결 버튼 표시 --%>
			<%-- 아니면, 계좌관리 기능에 대한 버튼 표시 --%>
			<c:choose>
				<c:when test="${empty sessionScope.token}">
					계좌 미인증 회원입니다.<br>
					계좌 인증을 먼저 수행한 후 서비스 이용이 가능합니다!<br>
<!-- 					<form action="https://testapi.openbanking.or.kr/oauth/2.0/authorize" method="get"> -->
<!-- 						<input type="hidden" name="response_type" value="code"> -->
<!-- 						<input type="hidden" name="client_id" value="4066d795-aa6e-4720-9383-931d1f60d1a9"> -->
<!-- 						<input type="hidden" name="redirect_uri" value="http://localhost:8081/mvc_board/callback"> -->
<!-- 						<input type="hidden" name="scope" value="login inquiry transfer"> -->
<!-- 						<input type="hidden" name="state" value="12345678901234567890123456789012"> -->
<!-- 						<input type="hidden" name="auth_type" value="0"> -->
<!-- 						<input type="submit" value="계좌연결2"> -->
<!-- 					</form> -->
					<%-- form 태그로 인증 수행 시 중간 오류 발생하면 뒤로 가기 작업을 별도로 수행해야하므로 --%>
					<%-- 새 창을 통해 인증 수행 화면을 표시 --%>
					<input type="button" value="계좌연결" onclick="linkAccount()">
				</c:when>
				<c:otherwise>
					<input type="button" value="핀테크 사용자정보 조회" onclick="location.href='BankUserInfo'">
					<input type="button" value="핀테크 계좌목록 조회" onclick="location.href='BankAccountList'">
				</c:otherwise>
			</c:choose>
		</h3>
		<%-- 
		임시) 인증코드를 전달받은 후 엑세스토큰 발급 요청(세션에 저장된 인증코드 사용)
		-------------------------------------------------------------------------------
		단, 요청 후 응답 시 콜백 주소로 콜백 기능이 수행되지 않는다! (API 상의 문제)
		따라서, form 태그를 활용한 엑세스토큰 요청을 수행할 경우 응답 데이터가 JSON 형식으로 전달되는데
		이 응답데이터를 브라우저 화면에서 처리할 방법이 없다! (콜백 기능이 동작하지 않기 때문)
		=> 결론> 자바 코드 상에서 REST API 요청 형태로 엑세스토큰 요청 및 처리를 수행해야한다!
		--%>
<%-- 		<c:if test="${not empty sessionScope.code}"> --%>
<!-- 			<hr> -->
<!-- 			<form action="https://testapi.openbanking.or.kr/oauth/2.0/token" method="post"> -->
<%-- 				<input type="hidden" name="code" value="${sessionScope.code}"> --%>
<!-- 				<input type="hidden" name="client_id" value="4066d795-aa6e-4720-9383-931d1f60d1a9"> -->
<!-- 				<input type="hidden" name="client_secret" value="36b4a668-94ba-426d-a291-771405e498e4"> -->
<!-- 				<input type="hidden" name="redirect_uri" value="http://localhost:8081/mvc_board/callback"> -->
<!-- 				<input type="hidden" name="grant_type" value="authorization_code"> -->
<!-- 				<input type="submit" value="엑세스토큰 발급 요청"> -->
<!-- 			</form> -->
<%-- 		</c:if> --%>
			<hr>
			<%-- 관리자일 경우 센터인증 이용기관 토큰발급을 위한 버튼 생성 --%>
			<c:if test="${sessionScope.sId eq 'admin'}">
				<h3>
					관리자용 엑세스 토큰(oob) 발급용<br>
					<input type="button" value="센터인증 이용기관 토큰발급 요청" 
							onclick="location.href='AdminBankRequestToken'">
				</h3>
			</c:if>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
	<script type="text/javascript">
		function linkAccount() {
			// 새 창으로 사용자 인증 요청 수행
			// => 빈 창을 먼저 띄운 후 해당 창에 사용자 인증 페이지 요청
			let authWindow = window.open("about:blank", "authWindow", "width=500,height=700");
			authWindow.location = "https://testapi.openbanking.or.kr/oauth/2.0/authorize?"
									+ "response_type=code" 
									+ "&client_id=4066d795-aa6e-4720-9383-931d1f60d1a9" 
									+ "&redirect_uri=http://localhost:8081/mvc_board/callback" 
									+ "&scope=login inquiry transfer" 
									+ "&state=12345678901234567890123456789012" 
									+ "&auth_type=0"; 
		}
	</script>
</body>
</html>










