<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
</head>
<body>
	<header>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<article>
		<h1>인증메일 재발송</h1>
		<div id="loginForm">
			<form action="ReSendAuthMail" method="post">
				<input type="text" name="id" placeholder="아이디" required><br>
				<input type="text" name="email" placeholder="E-Mail" required><br>
				<input type="submit" value="재발송">
			</form>	
		</div>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
</body>
</html>