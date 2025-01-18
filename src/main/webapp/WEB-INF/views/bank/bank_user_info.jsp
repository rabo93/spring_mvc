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
		<h1>핀테크 사용자 정보</h1>
		<h3>${bankUserInfo.user_name} 고객님의 정보</h3>
		<table id="info_table">
			<tr>
				<th>사용자번호</th>
				<th colspan="3">${bankUserInfo.user_seq_no}</th>
				<th>전체 계좌수</th>
				<th>${bankUserInfo.res_cnt}</th>
			</tr>
			<tr>
				<th>계좌명</th>
				<th>계좌번호</th>
				<th>은행명(은행코드)</th>
				<th>예금주명</th>
				<th>핀테크이용번호</th>
				<th></th>
			</tr>
			<%-- 계좌 정보(목록) 출력을 위해 bankUserInfo 객체의 res_list 리스트 객체 반복 --%>
			<c:forEach var="account" items="${bankUserInfo.res_list}">
				<%-- 1개 계좌 정보를 테이블 1개 행에 출력(반복) --%>
				<tr>
					<td>${account.account_alias}</td>
					<td>${account.account_num_masked}</td> <%-- 마스킹 된 계좌번호 --%>
					<td>${account.bank_name}(${account.bank_code_std})</td>
					<td>${account.account_holder_name}</td>
					<td>${account.fintech_use_num}</td>
					<td>
						<%--
						2.3. 계좌조회 서비스(사용자) - 2.3.1. 잔액조회 API 서비스 요청을 위한
						데이터 전송 폼 생성(각 계좌 당 1개 폼 생성)
						=> 요청 URL : BankAccountDetail(POST)
						=> 파라미터 : 핀테크이용번호, 예금주명, 계좌번호(마스킹) - hidden
						--%>
						<form action="BankAccountDetail" method="POST">
							<input type="hidden" name="fintech_use_num" value="${account.fintech_use_num}">
							<input type="hidden" name="account_holder_name" value="${account.account_holder_name}">
							<input type="hidden" name="account_num_masked" value="${account.account_num_masked}">
							<input type="submit" value="상세정보">
						</form>
						<br>
						<%-- 사용자가 선택한 대표계좌 정보를 DB 에 저장 요청 --%>
						<form action="BankRegistRepresentAccount" method="POST">
							<input type="hidden" name="fintech_use_num" value="${account.fintech_use_num}">
							<input type="hidden" name="account_holder_name" value="${account.account_holder_name}">
							<input type="hidden" name="account_num_masked" value="${account.account_num_masked}">
							<input type="submit" value="대표계좌로설정">
						</form>
					</td>
				</tr>
			</c:forEach>
		</table>
		<input type="button" value="돌아가기" onclick="history.back()">
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
</body>
</html>










