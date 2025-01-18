<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
			<input type="button" value="글쓰기" onclick="location.href='BoardWrite'" />
		</section>
		<section id="listForm">
			<table>
				<tr id="tr_top">
					<td width="100px">번호</td>
					<td>제목</td>
					<td width="150px">작성자</td>
					<td width="150px">날짜</td>
					<td width="100px">조회수</td>
				</tr>
			</table>
		</section>
	</article>
</body>
</html>













