<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<script>
	function logout() {
		// confirm() 함수 활용하여 "로그아웃하시겠습니까?" 질문을 표시하고
		// 확인 버튼 클릭 시(true) "MemberLogout" 페이지로 이동 처리
		if(confirm("로그아웃하시겠습니까?")) {
			location.href = "MemberLogout";
		}
	}
</script>
<div id="top_menu">
	<h4>
		<a href="./">HOME</a>
		<c:choose>
			<c:when test="${empty sessionScope.sId}"> <%-- 로그인 상태 아닐 경우(sId 속성값 없음) --%>
				| <a href="MemberLogin">로그인</a>
				| <a href="MemberJoin">회원가입</a>
			</c:when>
			<c:otherwise> <%-- 로그인 상태일 경우 --%>
				| <a href="MemberInfo">${sessionScope.sId}</a> 님
				| <a href="BankMain">계좌관리</a>
				| <a href="javascript:void(0)" onclick="logout()">로그아웃</a>
				<c:if test="${sId eq 'admin'}">
					| <a href="AdminMain">관리자페이지</a>
				</c:if>
				<%-- 로그인 상태일 경우에만 채팅 메뉴 표시 --%>
				| <a href="javascript:void(0)" onclick="openChatWindow()">채팅</a><span id="messageStatus"></span><br>
				<%-- 채팅창 자바스크립트 파일에서 세션 아이디 접근을 위해 hidden 속성 추가 --%>
				<input type="hidden" id="sId" value="${sessionScope.sId}">
				<%-- 로그인 상태일 경우에만 채팅 관련 스크립트 로딩 --%>
				<script src="${pageContext.request.contextPath}/resources/js/jquery-3.7.1.js"></script>
				<script src="${pageContext.request.contextPath}/resources/js/chat2_top.js"></script>
			</c:otherwise>
		</c:choose>
	</h4>
</div>
<hr>











