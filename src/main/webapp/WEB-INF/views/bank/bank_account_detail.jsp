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
		<h1>핀테크 계좌 상세 정보</h1>
		<h3>${account_holder_name} 고객님의 정보</h3>
		<table id="info_table">
			<tr>
				<th>사용자번호</th> <%-- 세션의 token 객체에 저장되어 있음 --%>
				<th>${token.user_seq_no}</th>
			</tr>
			<tr>
				<th>은행명</th>
				<td>${accountDetail.bank_name}</td>
			</tr>
			<tr>
				<th>계좌번호</th>
				<td>${account_num_masked}</td>
			</tr>
			<tr>
				<th>상품명</th>
				<td>${accountDetail.product_name}</td>
			</tr>
			<tr>
				<th>계좌잔액</th>
				<td>￦ ${accountDetail.balance_amt}</td>
			</tr>
			<tr>
				<th>출금가능금액</th>
				<td>￦ ${accountDetail.available_amt}</td>
			</tr>
			<tr>
				<th>핀테크이용번호</th>
				<td>${accountDetail.fintech_use_num}</td>
			</tr>
		</table>
		<div align="center">
			<input type="button" value="돌아가기" onclick="history.back()">
			<hr>
			<%-- 2.6. 계좌이체 서비스 - 2.6.1. 출금이체 API 서비스 요청 폼 --%>
			<%-- 거래 요청 고객(출금계좌 예금주) 정보(핀테크이용번호, 예금주명, 출금금액) 전달 --%>
			<form action="BankWithdraw" method="post">
				<%-- 출금 계좌가 복수개일 경우 구분을 위해 핀테크 이용번호도 출금 요청 시 전송 --%>
				<%-- 만약, 대표계좌 1개만 사용하여 입출금 구현 시 DB 에서 조회를 통해 핀테크 이용번호 조회 --%>
				<input type="hidden" name="withdraw_client_fintech_use_num" value="${accountDetail.fintech_use_num}">
				<%-- 예금주명도 핀테크 이용번호와 동일함 --%>
				<input type="hidden" name="withdraw_client_name" value="${account_holder_name}">
				<%-- 실제 거래금액은 상품 결정되면 해당 상품의 거래금액을 사용 --%>
				<%-- 현재는 임시로 거래금액 텍스트박스를 통해 입력(임의의 기본값 입력) --%>
				거래금액 <input type="text" name="tran_amt" value="5000"> 
				<input type="submit" value="출금이체">
			</form>
			<%-- 2.6. 계좌이체 서비스 - 2.6.2. 입금이체 API 서비스 요청 폼 --%>
			<%-- 거래 요청 고객(입금계좌 예금주) 정보(핀테크이용번호, 예금주명, 입금금액) 전달 --%>
			<form action="BankDeposit" method="post">
				<input type="hidden" name="deposit_client_fintech_use_num" value="${accountDetail.fintech_use_num}">
				<input type="hidden" name="deposit_client_name" value="${account_holder_name}">
				<%-- 실제 거래금액은 상품 결정되면 해당 상품의 거래금액을 사용 --%>
				<%-- 현재는 임시로 거래금액 텍스트박스를 통해 입력(임의의 기본값 입력) --%>
				거래금액 <input type="text" name="tran_amt" value="33000"> 
				<input type="submit" value="입금이체">
			</form>
			<hr>
			<%-- P2P 송금(이체) 요청(출금이체 + 입금이체) --%>
			<form action="BankTransfer" method="post">
				<h3>송금</h3>
				<input type="text" name="receiver_id" placeholder="상대방 아이디 입력"><br>
				거래금액 <input type="text" name="tran_amt" value="4888"> 
				<input type="submit" value="송금">
			</form>
		</div>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
</body>
</html>










