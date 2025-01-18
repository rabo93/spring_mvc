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
	
	/* ----------- 댓글 영역 ----------- */
	#replyArea {
		width: 500px;
		height: 100%;
		margin: auto;
		margin-top: 20px;
		margin-bottom: 20px;
	}
	
	#replyTextarea { /* 댓글 입력창 */
		width: 400px;
		height: 50px;
		resize: none; 
		vertical-align: middle;
	}
	
	#replySubmit { /* 댓글 작성 버튼 */
		width: 85px;
		height: 55px;
		vertical-align: middle;
	}
	
	#replyListArea { /* 댓글 목록 영역 */
		margin-top: 20px;
		font-size: 12px;
	}
	
	#replyListArea table, #replyListArea tr, #replyListArea td {
		border: none;
	}
	
	.replyContent {
		width: 300px;
		text-align: left;
	}
	
	.replyContent img {
		width: 10px;
		height: 10px;
	}
	
	.replyWriter {
		width: 80px;
	}
	
	.replyRegDate {
		width: 100px;
	}
	
	/* ------ 대댓글 ------ */
	#reReplyTextarea { /* 대댓글 입력창 */
		width: 350px;
		height: 20px;
		resize: none; 
		vertical-align: middle;
	}
	
	#reReplySubmit { /* 대댓글 작성 버튼 */
		width: 65px;
		height: 25px;
		vertical-align: middle;
	}
	
	.deleted {
		color: gray;
	}
</style>
</head>
<body>
	<header>
		<%-- inc/top.jsp 페이지 삽입(jsp:include 액션태그 사용 시 / 경로는 webapp 가리킴) --%>
		<jsp:include page="/WEB-INF/views/inc/top.jsp"></jsp:include>
	</header>
	<!-- 게시판 등록 -->
	<article id="articleForm">
		<h1>글 상세내용 보기</h1>
		<section id="basicInfoArea">
			<table>
				<tr>
					<th class="td_title">제목</th>
					<td colspan="3">${board.board_subject}</td>
				</tr>
				<tr>
					<th class="td_title">작성자</th>
					<td>${board.board_name}</td>
					<th class="td_title">작성일시</th>
					<td><fmt:formatDate value="${board.board_date}" pattern="yy-MM-dd HH:mm" /></td>
				</tr>
				<tr>
					<th class="td_title">작성자IP</th>
					<td>${board.board_writer_ip}</td>
					<th class="td_title">조회수</th>
					<td>${board.board_readcount}</td>
				</tr>
				<tr>
					<th class="td_title">첨부파일</th>
					<td colspan="3" id="board_file">
<!-- 						<div> -->
							<%--
							파일 다운로드 기능을 추가하려면 하이퍼링크에 download 속성 설정 추가 시 다운로드 가능
							=> 만약, 버튼으로 파일 다운로드를 수행하려면 버튼을 하이퍼링크로 감싸기
							=> download 속성에 속성값 지정 시 지정된 이름으로 다운로드 파일명 변경 가능
							--%>
<%-- 							<a href="${pageContext.request.contextPath}/resources/upload/${board.board_file1}" download>${board.board_file1}</a> --%>
<%-- 							${board.board_file1} --%>
<%-- 							<a href="${pageContext.request.contextPath}/resources/upload/${board.board_file1}" download}"> --%>
<!-- 								<input type="button" value="다운로드"> -->
<!-- 							</a> -->
							<%-- ============================================================== --%>
							<%-- [ JSTL - functions 라이브러리 함수 활용하여 원본 파일명 추출 ] --%>
							<%-- 1) split() 함수 활용하여 "-" 구분자로 분리 후 두번째 인덱스값 사용 --%>
							<%--    단, _ 기호가 2개 이상일 경우 반복문을 통해 문자열 결합 추가 필요 --%>
<%-- 							split() : ${fn:split(board.board_file1, "_")[1]} --%>

							<%-- 2) substring() 함수 활용하여 시작인덱스부터 지정인덱스까지 추출 --%>
							<%-- 2-1) 파일명의 길이 알아내기 --%>
<%-- 							<c:set var="fileLength" value="${fn:length(board.board_file1)}" /> --%>
							<%-- 2-2) 구분자(_)의 인덱스 알아내기(복수개일 경우 첫번째 기호 탐색) --%>
<%-- 							<c:set var="delimIndex" value="${fn:indexOf(board.board_file1, '_')}" /> --%>
							<%-- 2-3) substring() 함수 활용하여 추출(파일명과 구분자 인덱스 활용) --%>
							<%--      => substring(원본문자열, 시작인덱스, 끝인덱스) --%>
<%-- 							substring() : ${fn:substring(board.board_file1, delimIndex + 1, fileLength)} --%>

							<%-- 3) substringAfter() 함수 활용하여 지정된 문자 다음부터 끝까지 추출 --%>
<%-- 							substringAfter() : ${fn:substringAfter(board.board_file1, '_')} --%>
<%-- 							${board.board_file1} --%>
<%-- 							<a href="${pageContext.request.contextPath}/resources/upload/${board.board_file1}" download="${fn:substringAfter(board.board_file1, '_')}"> --%>
<!-- 								<input type="button" value="다운로드"> -->
<!-- 							</a> -->
<!-- 						</div> -->
<%-- 						<div>${board.board_file2}</div> --%>
<%-- 						<div>${board.board_file3}</div> --%>
						<%-- ============================================================= --%>
						<%-- 컨트롤러에서 파일명을 추출하여 전달한 경우 --%>
						<%-- List 객체("fileList") 크기만큼 반복문을 통해 파일명 출력 --%>
<%-- 						<c:forEach var="i" begin="0" end="${fileList.size() - 1}"> --%>
<%-- 							${fileList[i]} : ${originalFileList[i]}<br> --%>
<%-- 						</c:forEach> --%>
						<%-- 향상된 for문(foreach) 사용 시 --%>
						<%-- 기본 반복 객체 지정 후 다른 객체는 varStatus 속성을 통해 index 값 사용 가능 --%>
						<c:forEach var="file" items="${fileList}" varStatus="status">
							<%-- 단, 파일명이 존재할 경우에만 출력 --%>
							<c:if test="${not empty file}">
								<div>
									${file}
									<a href="${pageContext.request.contextPath}/resources/upload/${file}" download="${originalFileList[status.index]}">
										<input type="button" value="다운로드">
									</a>
								</div>
							</c:if>
						</c:forEach>
					</td>
				</tr>
			</table>
		</section>
		<section id="articleContentArea">
			${board.board_content}
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
		<%-- ========================== [ 댓글 영역 ] ========================= --%>
		<section id="replyArea">
			<%-- 댓글 작성 폼 영역 --%>
			<form action="BoardTinyReplyWrite" method="post">
				<%-- 댓글 작성 시 입력받지 않은 글번호, 페이지번호 함께 전달 --%>
				<input type="hidden" name="board_num" value="${param.board_num}">
				<input type="hidden" name="pageNum" value="${param.pageNum}">
				
				<%-- 세션 아이디가 없을 경우 댓글 작성 차단 --%>
				<%-- textarea 와 submit 버튼 비활성화(disabled), textarea 에 placeholder 설정 --%>
				<c:choose>
					<c:when test="${empty sessionScope.sId}"> <%-- 미 로그인 시 --%>
						<textarea id="replyTextarea" name="reply_content" disabled placeholder="로그인 한 사용자만 작성 가능합니다."></textarea>
						<input type="submit" value="댓글쓰기" id="replySubmit" disabled>
					</c:when>
					<c:otherwise>
						<textarea id="replyTextarea" name="reply_content"></textarea>
						<input type="submit" value="댓글쓰기" id="replySubmit">
					</c:otherwise>
				</c:choose>
			</form>
			<%-- 댓글 목록 표시 영역 --%>
			<div id="replyListArea">
				<table>
					<%-- 댓글 내용(reply_content), 작성자(reply_writer), 작성일시(reply_reg_date) 표시 --%>
					<%-- 반복문 활용 List 객체(tinyReplyBoardList) 로부터 Map 객체 꺼내서 출력 --%>
					<c:forEach var="tinyReplyBoard" items="${tinyReplyBoardList}" varStatus="status">
						<tr class="replyTr">
							<%-- 댓글의 삭제 상태(reply_delete_status)가 1일 경우 --%>
							<%-- 댓글을 표시하지 않고 "삭제된 댓글입니다." 표시 --%>
							<c:choose>
								<c:when test="${tinyReplyBoard.reply_delete_status eq 1}">
									<td class="replyContent deleted">
										<c:forEach var="i" begin="1" end="${tinyReplyBoard.reply_re_lev}">
											&nbsp;&nbsp;
										</c:forEach>
										삭제된 댓글입니다.
									</td>
									<td class="replyWriter"></td>
									<td class="replyRegDate"></td>
								</c:when>
								<c:otherwise>
									<td class="replyContent">
										<%-- 대댓글일 때 들여쓰기 처리 --%>
										<c:forEach var="i" begin="1" end="${tinyReplyBoard.reply_re_lev}">
											&nbsp;&nbsp;
										</c:forEach>
										${tinyReplyBoard.reply_content}
										<%-- 세션 아이디 존재 시 대댓글 작성 이미지(reply-icon.png) 표시 --%>
										<c:if test="${not empty sessionScope.sId}">
											<%-- 대댓글 작성 아이콘 클릭 시 자바스크립트 reReplyWriteForm() 함수 호출 --%>
											<%-- 파라미터 : 댓글번호(reply_num), 댓글 그룹번호, 들여쓰기레벨, 순서번호, 반복 인덱스값(varStatus 속성값) --%>
											<a href="javascript:reReplyWriteForm(${tinyReplyBoard.reply_num}, ${tinyReplyBoard.reply_re_ref}, ${tinyReplyBoard.reply_re_lev}, ${tinyReplyBoard.reply_re_seq}, ${status.index})"><img src="${pageContext.request.contextPath}/resources/images/reply-icon_25px.png" title="대댓글작성"></a>
											<%-- 또한, 세션 아이디가 댓글 작성자와 동일하거나 관리자("admin")일 경우 --%>
											<%-- 댓글 삭제 이미지(delete-icon.png) 표시 --%>
											<c:if test="${sessionScope.sId eq 'admin' or sessionScope.sId eq tinyReplyBoard.reply_writer}">
												<%-- 댓글 삭제 아이콘 클릭 시 자바스크립트 confirmReplyDelete() 함수 호출 --%>
												<%-- 파라미터 : 댓글번호, 반복 인덱스값 --%>
												<a href="javascript:confirmReplyDelete(${tinyReplyBoard.reply_num}, ${status.index})"><img src="${pageContext.request.contextPath}/resources/images/delete-icon_25px.png" title="댓글삭제"></a>
											</c:if>
										</c:if>
									</td>
									<td class="replyWriter">${tinyReplyBoard.reply_writer}</td>
									<td class="replyRegDate">
		<%-- 								${tinyReplyBoard.reply_reg_date} --%>
										<%-- 
										테이블 조회 결과를 Map 타입으로 관리 시 날짜 및 시각 데이터가
										LocalXXX 타입으로 관리됨(ex. LocalDate, LocalTime, LocalDateTime)
										=> 날짜 및 시각 정보가 yyyy-MM-ddTHH:mm:ss 형식으로 저장되어 있음
										=> 일반 Date 타입으로 사용하는 형태로 파싱해야만
										   <fmt:formatDate> 태그를 통해 포맷팅이 가능해진다!
										   (파싱없이 사용할 경우 예외 발생함 => javax.el.ELException: Cannot convert [2024-12-10T10:27:18] of type [class java.time.LocalDateTime] to [class java.util.Date])
										=> JSTL fmt 라이브러리 - <fmt:parseDate> 태그 활용하여 날짜 및 시각 파싱 후
										   <fmt:formatDate> 태그 활용하여 포맷팅 수행
										   1) var 속성 : 파싱 후 해당 결과를 다룰 객체명
										   2) value 속성 : 파싱할 대상 날짜 데이터(LocalXXX 객체)
										   3) pattern 속성 : 파싱할 날짜 데이터의 기존 형식 패턴
										                     => 날짜와 시각 사이의 구분자 T 까지 정확하게 명시
										                     => 주의! 구분자 T 는 단순 텍스트로 취급하기 위해
										                        작은따옴표('') 로 T 를 둘러싸서 표기해야함!
										                     => 주의! XX분 정각일 경우 전달되는 데이터에 초가 빠진 상태로 전달되므로
										                        파싱 패턴 지정 시 ss 제외시킬 것!
										   4) type 속성 : 파싱 대상 타입(time : 시각, date : 날짜, both : 둘 다)
										--%>	
										<fmt:parseDate var="parsedReplyRegDate"
														value="${tinyReplyBoard.reply_reg_date}"
														pattern="yyyy-MM-dd'T'HH:mm"
														type="both" />
		<%-- 								${parsedReplyRegDate} --%>
										<%-- 파싱 결과 : 2024-12-10T10:27:18 Tue Dec 10 10:27:18 KST 2024 --%>
										<%-- 파싱된 날짜 및 시각 Date 객체에 대한 포맷팅 수행 필요(yy-MM-dd HH:mm) --%>
										<fmt:formatDate value="${parsedReplyRegDate}" pattern="yy-MM-dd HH:mm" />
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
				</table>
			</div>
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
		// ============================================================================
		// [ 대댓글 처리 ]
		// 대댓글 작성 아이콘 클릭 처리를 위한 reReplyWriteForm() 함수 정의
// 		<a href="javascript:reReplyWriteForm(${tinyReplyBoard.reply_num}, ${tinyReplyBoard.reply_re_ref}, ${tinyReplyBoard.reply_re_lev}, ${tinyReplyBoard.reply_re_seq}, ${status.index})">
		function reReplyWriteForm(reply_num, reply_re_ref, reply_re_lev, reply_re_seq, index) {
			console.log(reply_num + ", " + reply_re_ref + ", " + reply_re_lev + ", " + reply_re_seq + ", " + index);	
			
			// 기존 대댓글 입력폼 존재할 경우 대비하여 대댓글 입력폼 요소(#reReplyTr) 제거
			$("#reReplyTr").remove();
			
			// 클릭된 대상 댓글 아래쪽에 대댓글 입력폼 표시
			// => 댓글 요소 tr 태그(.replyTr) 지정 후 jQuery after() 메서드 호출하여 
			//    해당 요소 바깥쪽 뒷부분에 입력폼이 포함된 tr 태그 추가
			// => 
			// => form 태그 내에는 작성된 대댓글 내용(reply_content) 외에도
			//    원본글 번호, 원본 댓글 번호, 댓글그룹번호, 댓글들여쓰기레벨, 댓글순서번호도 전달
			$(".replyTr").eq(index).after(
				'<tr id="reReplyTr">'
				+ '	<td colspan="3">'
				+ '		<form action="BoardTinyReReplyWrite" method="post" id="reReplyForm">'
				+ '			<input type="hidden" name="board_num" value="${board.board_num}">'
				+ '			<input type="hidden" name="reply_num" value="' + reply_num + '">'
				+ '			<input type="hidden" name="reply_re_ref" value="' + reply_re_ref + '">'
				+ '			<input type="hidden" name="reply_re_lev" value="' + reply_re_lev + '">'
				+ '			<input type="hidden" name="reply_re_seq" value="' + reply_re_seq + '">'
				+ '			<textarea id="reReplyTextarea" name="reply_content"></textarea>'
				+ '			<input type="button" value="댓글쓰기" id="reReplySubmit" onclick="reReplyWrite()">'
				+ '		</form>'
				+ '	</td>'
				+ '</tr>'
			);
		}
		
		// 대댓글 작성 시 버튼 클릭 처리 => AJAX 활용하여 대댓글 등록 요청
		function reReplyWrite() {
			if($("#reReplyTextarea").val() == "") {
				alert("댓글 내용 입력 필수!");
				$("#reReplyTextarea").focus();
				return;
			}
			
			// AJAX 활용하여 대댓글 등록 요청(POST) - BoardTinyReReplyWrite
			// => 전송할 데이터는 폼 대상으로 serialize() 메서드 활용
			// => 응답 형식은 JSON 형식 설정
			$.ajax({
				type : "POST",
				url : $("#reReplyForm").attr("action"),
				data : $("#reReplyForm").serialize(),
				dataType : "JSON"
			}).done(function(response) {
				console.log(JSON.stringify(response));
				
				if(response.invalidSession) { // 세션 아이디가 없을 경우
					alert("잘못된 접근입니다!");
				}
				
				location.reload(true); // 새로고침 시 서버에서 데이터를 다시 불러옴
				// => 만약, true 값 생략 시 캐시에서 페이지를 리로드하므로 이전 내용만 보일 수 있음
			}).fail(function() {
				alert("댓글 작성 실패!");
			});
			
		}
		
		// -----------------------------------------------
		// 대댓글 삭제 버튼 처리
		function confirmReplyDelete(reply_num, index) {
// 			console.log(reply_num + ", " + index);
			
			if(confirm("해당 댓글을 삭제하시겠습니까?")) {
				// AJAX 활용하여 "BoardTinyReplyDelete" 서블릿 요청(파라미터 : 댓글번호) - GET
				// => 응답 데이터 형식은 JSON 타입
				$.ajax({
					type : "GET",
					url : "BoardTinyReplyDelete",
					data : {
						reply_num : reply_num
					},
					dataType : "JSON"
				}).done(function(response) {
					console.log(JSON.stringify(response));
					
					if(response.invalidSession) { // 세션 아이디가 없을 경우
						alert("잘못된 접근입니다!");
					}
					
// 					location.reload(true); // 새로고침 시 서버에서 데이터를 다시 불러옴
					// => 만약, true 값 생략 시 캐시에서 페이지를 리로드하므로 이전 내용만 보일 수 있음
				}).fail(function() {
					alert("댓글 작성 실패!");
				});
			}
		}
	</script>
</body>
</html>





















