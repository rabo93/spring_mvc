<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<script src="${pageContext.request.contextPath}/resources/js/jquery-3.7.1.js"></script>
<%-- 클라이언트에서 서버측으로 전송할 데이터 암호화에 사용할 라이브러리 추가 --%>
<%-- 다양한 기능이 제공되는 rsa.js 라이브러리도 있지만, 간편한 암호화에는 jsencrypt.js 라이브러리 많이 사용 --%>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsencrypt/3.3.2/jsencrypt.min.js"></script>
</head>
<body>
	<header>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<article>
		<h1>로그인</h1>
		<div id="loginForm">
			<form action="MemberLogin" method="post">
				<%-- EL 을 통해 쿠키 접근 문법(쿠키값 가져오기) : ${cookie.쿠키명.value} --%>
<%-- 				<h3>userId 쿠키값 : ${cookie.userId.value}</h3> --%>
				<%-- "userId" 라는 쿠키가 존재할 경우 해당 값을 아이디 입력란에 표시 --%>
<%-- 				<input type="text" name="id" value="${cookie.userId.value}"  placeholder="아이디" required><br> --%>
<!-- 				<input type="password" name="passwd" placeholder="패스워드" required><br> -->
				<%-- 아이디, 패스워드 암호화를 위해 name 속성값 제거(입력값은 암호화 후 별도로 전송) --%>
				<input type="text" id="id" value="${cookie.userId.value}"  placeholder="아이디" required><br>
				<input type="password" id="passwd" placeholder="패스워드" required><br>
				<%-- checkbox 생성 시 value 속성 지정하지 않으면, 체크 시 "on", 미 체크 시 null --%>
				<%-- 쿠키에 저장된 "rememberId" 속성의 쿠키값이 있을 경우 아이디기억하기 체크박스 체크 --%>
				<input type="checkbox" name="rememberId" <c:if test="${not empty cookie.rememberId}">checked</c:if>>아이디 기억하기<br>
				<input type="submit" value="로그인">
				<input type="button" value="인증메일재발송" onclick="location.href='ReSendAuthMail'">
				
				<%-- 아이디/패스워드를 각각 암호화하여 폼으로 전송할 경우 암호문을 저장할 요소 생성 --%>
				<input type="hidden" id="encryptedId" name="id">
				<input type="hidden" id="encryptedPasswd" name="passwd">
			</form>	
		</div>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
	<script type="text/javascript">
		$(function() {
			const publicKey = '${publicKey}'; // 공개키 저장
			
			$("form").submit((e) => { // form 태그 submit 이벤트 핸들링
// 				e.preventDefault();
				// 입력받은 아이디 및 패스워드 변수에 저장
				const id = $("#id").val();
				const passwd = $("#passwd").val();
				// ------------------------------------------
				// JSEncrypt 객체 생성
				const jsEncrypt = new JSEncrypt();
				// JSencrypt 객체에 공개키 전달
				jsEncrypt.setPublicKey(publicKey);
				
				// JSEncrypt 객체의 encrypt() 메서드 호출하여 데이터 암호화 두 가지 방법
				// 각각 id, passwd 를 따로 암호화하여 폼으로 전송
				let encryptedId = jsEncrypt.encrypt(id); 
				let encryptedPasswd = jsEncrypt.encrypt(passwd); 
				$("#encryptedId").val(encryptedId);
				$("#encryptedPasswd").val(encryptedPasswd);
				// -------------------------------------------
				// 암호화할 데이터(id, passwd)를 JSON 형식 문자열로 변환하여 폼에 추가
				let encryptedData = jsEncrypt.encrypt(JSON.stringify({id, passwd}));
				$("form").prepend("<input type='hidden' name='encryptedData' value='" + encryptedData + "'>");
			});
		});
	</script>
</body>
</html>








