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
	#modifyForm {
		width: 500px;
		height: 100%;
		margin: auto;
	}
	
	#modifyForm table {
		border: 1px solid black;
		margin: auto;
		width: 450px;
	}
	
	.write_td_left {
		width: 100px;
		text-align: center;
	}
	
	.board_file {
		font-size: 12px;
		margin: 3px;
		vertical-align: middle;
	}
	
	.img_btn {
		width: 15px;
		height: 15px;
		vertical-align: middle;
	}
	
	#commandCell {
		text-align: center;
		margin-top: 10px;
		padding: 10px;
		border-top: 1px solid gray;
	}
</style>
</head>
<body>
	<header>
		<%-- inc/top.jsp 페이지 삽입(jsp:include 액션태그 사용 시 / 경로는 webapp 가리킴) --%>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<!-- 게시판 답글 작성 -->
	<article id="modifyForm">
		<h1>답글 작성</h1>
		<form action="BoardReply" method="post" enctype="multipart/form-data">
			<%-- 입력받지 않은 글번호, 페이지번호 파라미터를 hidden 속성으로 전달 --%>
			<input type="hidden" name="board_num" value="${param.board_num}">
			<input type="hidden" name="pageNum" value="${param.pageNum}">
			<%-- 답글 작성에 필요한 원본 글에 대한 추가 정보(참조글번호, 들여쓰기레벨, 순서번호)도 전달 --%>
			<input type="hidden" name="board_re_ref" value="${board.board_re_ref}">
			<input type="hidden" name="board_re_lev" value="${board.board_re_lev}">
			<input type="hidden" name="board_re_seq" value="${board.board_re_seq}">
			
			<table>
				<tr>
					<td class="write_td_left"><label for="board_name">작성자</label></td>
					<td class="write_td_right">
						<%-- 글쓴이(작성자)는 세션 아이디값을 그대로 사용하므로 그냥 출력(읽기 전용) --%>
						<input type="text" id="board_name" name="board_name" value="${sessionScope.sId}" readonly required />
					</td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_subject">제목</label></td>
					<td class="write_td_right">
						<input type="text" id="board_subject" name="board_subject" value="Re:${board.board_subject}" required />
					</td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_content">내용</label></td>
					<td class="write_td_right">
						<textarea id="board_content" name="board_content" rows="15" cols="40" required>${board.board_content}</textarea>
					</td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_file">첨부파일</label></td>
					<td class="write_td_right">
						<input type="file" name="file1">
						<input type="file" name="file2">
						<input type="file" name="file3">
					</td>
				</tr>
			</table>
			<section id="commandCell">
				<input type="submit" value="답글등록">
				<input type="reset" value="초기화">
				<input type="button" value="취소" onclick="history.back()">
			</section>
		</form>
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
		
		function deleteFile(board_num, file, index) {
			console.log(board_num + ", " + file + ", " + index);
		}
	</script>
</body>
</html>








