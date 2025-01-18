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
		<h1>핀테크 계좌 출금 이체 결과</h1>
		<table id="info_table">
			<tr>
				<th>사용자번호</th> <%-- 세션의 token 객체에 저장되어 있음 --%>
				<th>${token.user_seq_no}</th>
			</tr>
			<tr>
				<th>핀테크이용번호</th>
				<td>${withdrawResult.fintech_use_num}</td>
			</tr>
			<tr>
				<th>상대방 계좌번호</th>
				<%-- 핀테크 이용번호로 출금했으므로 임의의 계좌번호가 출력됨 --%>
				<td>${withdrawResult.dps_account_num_masked}</td>
			</tr>
			<tr>
				<th>출금금액</th>
				<td>￦ ${withdrawResult.tran_amt}</td>
			</tr>
			<tr>
				<th>출금일시</th>
				<td>${withdrawResult.api_tran_dtm}</td>
			</tr>
		</table>
		<div align="center">
			<input type="button" value="돌아가기" onclick="history.back()">
		</div>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
</body>
</html>










