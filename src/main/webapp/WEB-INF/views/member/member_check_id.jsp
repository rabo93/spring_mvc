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
</head>
<body>
	<article>
		<h3>아이디 중복 검사</h3>
		<div id="checkIdForm">
			<form action="MemberCheckId" method="get">
				<input type="text" name="id" value="${param.id}" placeholder="검색할 아이디 입력" size="10">
				<input type="submit" value="검색">
			</form>
		</div>
		<div id="checkIdResultArea">
			<%-- 파라미터 isDuplicate 값이 true 이면 "사용 가능한 아이디" 표시하고 [사용] 버튼 표시 --%>
			<%-- 아니면, "사용 불가능한 아이디" 표시 --%>
			<c:choose>
				<c:when test="${param.isDuplicate eq false}">
					<span style="color:GREEN">사용 가능한 아이디</span>&nbsp;
					<input type="button" value="아이디 사용" onclick="useId('${param.id}')">
				</c:when>
				<c:otherwise>
					<span style="color:RED">사용 불가능한 아이디</span>
				</c:otherwise>				
			</c:choose>
		</div>
	</article>
	<script type="text/javascript">
		function useId(id) {
			// 사용할 아이디를 부모창의 아이디 입력 텍스트박스에 표시
			// 1) 기본 자바스크립트 객체 활용(opener 객체를 활용하여 부모창 지정 가능)
			opener.document.querySelector("#id").value = id;
			
			// 2) jQuery 활용
			// 2-1) 선택자 지정 시 선택자 뒤에 두번째 파라미터로 opener.document 전달
// 			$("#id", opener.document).val(id);
			// 2-2) $(opener.document).find() 메서드를 활용하여 선택자 탐색
// 			$(opener.document).find("#id").val(id);
			// ----------------------------------------------
			// 현재창(자식창) 닫기
			close(); // window 객체 생략
		}
	</script>
</body>
</html>












