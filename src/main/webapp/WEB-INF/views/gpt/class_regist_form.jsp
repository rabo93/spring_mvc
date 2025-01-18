<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MVC 게시판</title>
<!-- 외부 CSS 파일(css/default.css) 연결하기 -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	#writeForm {
		width: 500px;
		min-height: 550px;
		margin: auto;
		border: 1px solid gray
	}
	
	#writeForm table {
		margin: auto;
		width: 500px;
	}
	
	.write_td_left {
		width: 150px;
		text-align: center;
	}
	
	.write_td_right {
		width: 300px;
	}
	
	#board_name {
		background-color: #77777744;
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
	<!-- 게시판 등록 -->
	<article id="writeForm">
		<h1>게시판 글 등록</h1>
		<form action="ClassRegist" name="writeForm" method="post">
			<table>
				<tr>
					<td class="write_td_left"><label for="board_name">강사명</label></td>
					<td class="write_td_right">
						<input type="text" id="class_master" name="class_master" required />
					</td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_subject">클래스ID</label></td>
					<td class="write_td_right"><input type="text" id="class_id" name="class_id" required /></td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_subject">클래스명</label></td>
					<td class="write_td_right"><input type="text" id="class_subject" name="class_subject" required /></td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_content">상세설명</label></td>
					<td class="write_td_right">
						<textarea id="class_content" name="class_content" rows="15" cols="40" required></textarea>
					</td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_content">커리큘럼</label></td>
					<td class="write_td_right">
						<textarea id="class_curriculum" name="class_curriculum" rows="15" cols="40" required></textarea>
					</td>
				</tr>
				<tr>
					<td class="write_td_left"><label for="board_subject">해시태그(최대 10개)</label></td>
					<td class="write_td_right">
						<input type="text" id="hashtag" name="hashtag" required placeholder="#아이티윌,#자바,#스프링" pattern="^#([a-zA-Z0-9가-힣]{1,10})(,#([a-zA-Z0-9가-힣]{1,10})){0,9},?$" title="ex) #홍길동,#이순신,#강감찬,#전지현,#김태희" />
						<input type="button" value="자동생성" onclick="requestHashcode()">
					</td>
				</tr>
			</table>
			<section id="commandCell">
				<input type="submit" value="등록">&nbsp;&nbsp;
				<input type="reset" value="다시쓰기">&nbsp;&nbsp;
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
		function requestHashcode() {
			if($("#class_subject").val() == "") {
				alert("과정명 입력 필수!");
				$("#class_subject").focus();
				return;
			} else if($("#class_content").val() == "") {
				alert("과정 상세설명 입력 필수!");
				$("#class_content").focus();
				return;
			}
			
			// AJAX 활용하여 ClassRequestHashtag 서블릿 요청(POST)
			// => 파라미터 : 강의명, 강의 상세설명
			$.ajax({
				type : "POST",
				url : "ClassRequestHashtag",
				data : {
					class_subject : $("#class_subject").val(),
					class_content : $("#class_content").val()
				},
				dataType : "JSON"
			}).done(function(response) {
				console.log(response);
				console.log(JSON.stringify(response));
				
				let hashtags = response.choices[0].message.content;
				$("#hashtag").val(hashtags);
			}).fail(function() {
				alert("요청 실패!");
			});
		}
	</script>
</body>
</html>








