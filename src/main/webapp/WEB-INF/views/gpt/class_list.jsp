<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MVC 게시판</title>
<!-- 외부 CSS 파일(css/default.css) 연결하기 -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	#listForm {
		width: 1024px;
		max-height: 610px;
		margin: auto;
	}
	
	#listForm > table {
		margin: auto;
		width: 1024px;
	}
	
	#tr_top {
		background: orange;
		text-align: center;
	}
	
	table td {
		text-align: center;
	}
	
	/* 제목 열 좌측 정렬 및 여백 설정 */
	#listForm .board_subject {
		text-align: left;
		padding-left: 20px;
	}
	
	#listForm .board_subject:hover {
		background-color: #FFD8D8;
	}
	
	#pageList {
		margin: auto;
		width: 1024px;
		text-align: center;
	}
	
	#emptyArea {
		margin: auto;
		width: 1024px;
		text-align: center;
	}
	
	#commandArea {
		margin: auto;
		width: 1024px;
		text-align: right;
	}
	
	/* 하이퍼링크 밑줄 제거 */
	a {
		text-decoration: none;
	}
</style>
<script src="${pageContext.request.contextPath}/resources/js/jquery-3.7.1.js"></script>
</head>
<body>
	<header>
		<%-- inc/top.jsp 페이지 삽입(jsp:include 액션태그 사용 시 / 경로는 webapp 가리킴) --%>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<article>
		<!-- 게시판 리스트 -->
		<h1>게시판 글 목록</h1>
		<section id="commandArea">
			<%-- ====================== [ 게시물 검색 기능 ] ===================== --%>
			<%-- 검색을 위한 폼 생성 --%>
			<form action="ClassList" method="get">
				<%-- 검색타입 목록(셀렉트박스), 검색어(텍스트박스) 추가 --%>
				<%-- 파라미터에 해당 항목이 있을 경우 해당 내용 표시 --%>
				<select name="searchType">
					<option value="subject" <c:if test="${param.searchType eq 'subject'}">selected</c:if>>제목</option>
					<option value="content" <c:if test="${param.searchType eq 'content'}">selected</c:if>>내용</option>
					<option value="subject_content" <c:if test="${param.searchType eq 'subject_content'}">selected</c:if>>제목&내용</option>
					<option value="name" <c:if test="${param.searchType eq 'name'}">selected</c:if>>작성자</option>
				</select>
				<input type="text" name="searchKeyword" value="${param.searchKeyword}">
				<input type="submit" value="검색" />
				<input type="button" value="글쓰기" onclick="location.href='BoardWrite'" />
			</form>
		</section>
		<section id="listForm">
			<table>
				<tr id="tr_top">
					<td>강좌명</td>
					<td width="150px">강사명</td>
				</tr>
				<c:choose>
					<c:when test="${empty classList}">
						<%-- 게시물 목록이 하나도 존재하지 않을 경우 --%>
						<tr><td colspan="2">게시물이 존재하지 않습니다.</td></tr>
					</c:when>
					<c:otherwise>
						<%-- 게시물 목록이 하나라도 존재할 경우 --%>
						<c:forEach var="classInfo" items="#{classList}">
							<tr>
								<td class="board_subject" id="class_${classInfo.class_id}">${classInfo.class_subject}</td>
								<td>${classInfo.class_master}</td>
							</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</table>
		</section>
	</article>
	<script type="text/javascript">
		// 게시물 제목열 클릭 이벤트 핸들링
		$(".board_subject").on("click", function(event) {
// 			console.log(event.target);
			// 클릭 대상 요소의 id 선택자 탐색 => attr() 메서드 활용
			let class_id = $(event.target).attr("id").substring(6); // 6번 인덱스부터 추출
			console.log(class_id);
			
			// "ClassInfo" 서블릿 주소 요청 => 파라미터 : 글번호, 페이지번호
			// => 글번호는 위에서 탐색한 번호 사용
			// => 페이지번호는 c:set 태그로 설정한 변수값 그대로 사용
			location.href = "ClassInfo?class_id=" + class_id;
		});
	</script>
</body>
</html>













