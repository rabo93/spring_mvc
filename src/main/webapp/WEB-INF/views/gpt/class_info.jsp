<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MVC 게시판</title>
<!-- 외부 CSS 파일(css/default.css) 연결하기 -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	#articleForm {
		width: 500px;
		height: 100%;
		margin: auto;
	}
	
	#articleForm table {
		border: 1px solid black;
		margin: auto;
		width: 500px;
	}
	
	th, td {
 		border: 1px solid black;
	}
	
	th {
		width: 70px;
	}
	
	td {
		text-align: center;
	}
	
	#basicInfoArea {
		height: auto;
		text-align: center;
	}
	
	#board_file {
		height: auto;
		font-size: 12px;
	}
	
	#board_file div {
		margin: 3px;
	}
	
	#articleContentArea {
		background: orange;
		margin-top: 10px;
		min-height: 200px;
		text-align: center;
		overflow: auto;
		white-space: pre-line;
	}
	
	#commandCell {
		text-align: center;
		margin-top: 10px;
		padding: 10px;
		border-top: 1px solid gray;
	}
	
	#hashtag {
		text-align: center;
		font-size: 10px;
		color: skyblue;
	}
</style>
</head>
<body>
	<header>
		<%-- inc/top.jsp 페이지 삽입(jsp:include 액션태그 사용 시 / 경로는 webapp 가리킴) --%>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<article id="articleForm">
		<h1>강의 상세내용 보기</h1>
		<section id="basicInfoArea">
			<table>
				<tr>
					<th class="td_title">강의명</th>
					<td colspan="3">
						${classInfo.class_subject}<br>
						<span id="hashtag">${classInfo.hashtag}</span>
					</td>
				</tr>
				<tr>
					<th class="td_title">강사명</th>
					<td colspan="3">${classInfo.class_master}</td>
				</tr>
				<tr>
					<th class="td_title">강의 상세내용</th>
					<td colspan="3" id="board_content">
						${classInfo.class_content}
					</td>
				</tr>
				<tr>
					<th class="td_title">강의 커리큘럼</th>
					<td colspan="3" id="board_content">
						${classInfo.class_curriculum}
					</td>
				</tr>
			</table>
		</section>
		<section id="commandCell">
			<%-- 답글, 수정, 삭제 버튼 모두 로그인 한 사용자에게만 표시 --%>
			<c:if test="${not empty sessionScope.sId}">
				<input type="button" value="답글작성" onclick="location.href='BoardReply?board_num=${board.board_num}&pageNum=${param.pageNum}'">&nbsp;&nbsp;
				<%-- 수정, 삭제 버튼은 작성자가 세션 아이디와 동일할 경우에만 표시 --%>
				<%-- 단, 관리자("admin") 의 경우 다른 작성자의 게시물에도 수정, 삭제 버튼 표시 --%>
				<c:if test="${sessionScope.sId eq board.board_name or sessionScope.sId eq 'admin'}">
					<input type="button" value="수정" onclick="requestModify()">&nbsp;&nbsp;
					<input type="button" value="삭제" onclick="confirmDelete()">&nbsp;&nbsp;
				</c:if>
			</c:if>
			
			<%-- 목록 버튼은 항상 표시하고, 이전 페이지로 돌아가기로 처리 --%>
			<input type="button" value="목록" onclick="location.href='BoardList?pageNum=${param.pageNum}'">
		</section>
	</article>
	<footer>
		<!-- 회사 소개 영역(inc/bottom.jsp) 페이지 삽입 -->
		<jsp:include page="/WEB-INF/views/inc/bottom.jsp"></jsp:include>
	</footer>
	<script src="${pageContext.request.contextPath}/resources/js/jquery-3.7.1.js"></script>
	<script type="text/javascript">
		function getQueryParams() {
			let params = "";
			
			// URL 에서 파라미터 탐색하여 파라미터가 존재하면 URL 뒤에 파라미터 결합
			let searchParams = new URLSearchParams(location.search);
			for(let param of searchParams) {
				params += param[0] + "=" + param[1] + "&";
			}
			
			// 마지막 파라미터 뒤에 붙은 & 기호 제거
			if(params.lastIndexOf("&") == params.length - 1) { // & 기호가 배열의 끝에 있을 경우
				// & 기호 앞까지 추출하여 url 변수에 저장(덮어쓰기)
				params = params.substring(0, params.length - 1);
			}
			
			// 파라미터 결합된 문자열 리턴
			return params;
		}
	
		function confirmDelete() {
			if(confirm("삭제하시겠습니까?")) {
				location.href = "BoardDelete?" + getQueryParams(); // 페이지 요청
			}
		}
		
		function requestModify() {
			location.href = "BoardModify?" + getQueryParams(); // 페이지 요청
		}
	</script>
</body>
</html>








