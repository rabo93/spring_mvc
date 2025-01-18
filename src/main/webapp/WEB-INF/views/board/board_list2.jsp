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
	
	.board_subject {
		height: 50px;
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
			<form action="BoardList" method="get">
				<select name="searchType" id="searchType">
					<option value="subject" <c:if test="${param.searchType eq 'subject'}">selected</c:if>>제목</option>
					<option value="content" <c:if test="${param.searchType eq 'content'}">selected</c:if>>내용</option>
					<option value="subject_content" <c:if test="${param.searchType eq 'subject_content'}">selected</c:if>>제목&내용</option>
					<option value="name" <c:if test="${param.searchType eq 'name'}">selected</c:if>>작성자</option>
				</select>
				<input type="text" name="searchKeyword" id="searchKeyword" value="${param.searchKeyword}">
				<input type="submit" value="검색" />
				<input type="button" value="글쓰기" onclick="location.href='BoardWrite'" />
			</form>
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
				<c:set var="pageNum" value="1" />
				<c:if test="${not empty param.pageNum}">
					<c:set var="pageNum" value="${param.pageNum}" />
				</c:if>
				<%-- =================================================================== --%>
				<c:choose>
					<c:when test="${empty boardList}">
						<tr><td colspan="5">게시물이 존재하지 않습니다.</td></tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="board" items="#{boardList}">
							<tr>
								<td class="board_num">${board.board_num}</td>
								<td class="board_subject">
									<c:if test="${board.board_re_lev > 0}">
										<c:forEach begin="1" end="${board.board_re_lev}">
											&nbsp;&nbsp;
										</c:forEach>
										<img src="${pageContext.request.contextPath}/resources/images/re.gif">
									</c:if>
									${board.board_subject}
								</td>
								<td>${board.board_name}</td>
								<td>
									<fmt:formatDate value="${board.board_date}" pattern="yy-MM-dd HH:mm" />
								</td>
								<td>${board.board_readcount}</td>
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
			// 클릭 대상 요소의 부모 요소 탐색 => parent() 메서드 활용
			let parent = $(event.target).parent();
// 			console.log(parent.html()); // 부모요소 내의 HTML 태그 모두 출력됨
			
			// 부모 요소의 내부 요소 중에서 클래스 선택자 "board_num" 탐색 = find() 메서드 활용
			let board_num = $(parent).find(".board_num").text();
			console.log("board_num = " + board_num);
			
			// "BoardDetail" 서블릿 주소 요청 => 파라미터 : 글번호, 페이지번호
			// => 글번호는 위에서 탐색한 번호 사용
			// => 페이지번호는 c:set 태그로 설정한 변수값 그대로 사용
			location.href = "BoardDetail?board_num=" + board_num + "&pageNum=" + ${pageNum};
		});
		// =================================================================================
		// AJAX + JSON 을 활용한 간단한 게시물 목록 무한 스크롤 구현
		// 페이지 번호값 미리 변수에 저장(스크롤 목록 페이지 번호) = 기본값 2
		// => 첫 페이지는 목록 출력 시 미리 가지고 와서 출력했기 때문
		let pageNum = "2";
		
		// 검색타입 & 검색어 가져와서 변수에 저장
		let searchType = $("#searchType").val();
		let searchKeyword = $("#searchKeyword").val();
// 		alert(searchType + ", " + searchKeyword);

		// 현재 스크롤바 위치값(마지막 위치값)을 저장할 변수 선언(기본값 0)
		let lastScroll = 0;
		
		// 스크롤바에 대한 이벤트가 중복으로 발생하지 않도록 하기 위한 boolean 타입 변수 선언
		let isContinue = true; // 기본값 true(페이지 로딩, 만약 false 이면 페이지 로딩 X)
		// --------------------------------------------------------------
		// 무한스크롤 기능 구현을 위한 window 객체의 scroll 이벤트 핸들링
		$(window).scroll(function() {
// 			console.log("window scroll event");
			
			// 1. 현재 스크롤바 높이(현재 위치) 알아내기
			let currentScroll = $(this).scrollTop();
			// 2. 현재 문서 전체 높이 알아내기
			let documentHeight = $(document).height();
			// 3. 현재 창의 전체 높이 알아내기
			let windowHeight = $(window).height();
			
// 			console.log("currentScroll : " + currentScroll + ", documentHeight : " + documentHeight + ", windowHeight : " + windowHeight);
// 			console.log("windowHeight + currentScroll : " + (windowHeight + currentScroll));
			
			// 스크롤바가 아래쪽으로 이동할 경우에만 목록 로딩 작업 수행
			if(currentScroll > lastScroll) {
				// 스크롤바 위치값 + 현재 창 높이 + x 값이 문서 전체 높이 이상일 경우
				// 다음 페이지 게시물 목록 로딩하여 화면에 추가
				// => 이 때, x 값은 스크롤바가 바닥에서 얼마만큼 떨어져 있을 때 동작할지 결정
				//    x 가 0 이면 바닥에 닿았을 때이지만 일반적으로 1 정도의 갭이 발생하므로
				//    가급적 1보다 큰 값 사용
				if((currentScroll + windowHeight + 10) > documentHeight && isContinue) {
					load_list();
					
					// 다음 스크롤은 더 이상 페이지 로딩 작업에 영향을 주지 못하도록 하기 위해
					// isContinue 변수값을 false 로 변경
					isContinue = false;
				}
			}
			
			// 현재 스크롤바 위치값을 마지막 위치값(스크롤 이벤트 기준값)으로 저장(현재 스크롤바 위치의 최신화)
			lastScroll = currentScroll;
			
		});
		
		// AJAX + JSON 으로 게시물 목록 조회를 수행할 load_list() 함수 정의
		function load_list() {
			// BoardListJson 서블릿을 AJAX 로 요청(GET)
			$.ajax({
				type : "GET",
				url : "BoardListJson",
				data : {
					pageNum : pageNum,
					searchType : searchType,
					searchKeyword : searchKeyword
				},
				dataType : "json" // 요청 응답 성공 시 리턴받을 데이터 타입을 JSON 형식으로 지정
			}).done(function(data) { // 응답 정보(JSON 데이터)가 data 변수에 저장됨
// 				console.log(JSON.stringify(data));
				// PageInfo 객체와 BoardList 객체 추출
// 				let pageInfo = data.pageInfo;
// 				let boardList = data.boardList;
				
				// boardList 객체 for문을 통해 반복
				for(let board of data.boardList) {
// 					$("#listForm > table").append("<tr><td colspan='5'>" + JSON.stringify(board) + "</td></tr>");
					// -----------------------
					// board 객체의 board_re_lev 값이 0보다 크면 해당 값만큼 공백 2개 추가 후
					// 공백 뒤에 답글 아이콘 이미지(re.gif) 추가하여 답글 앞에 표시될 문자열 생성
					let reMark = "";
					if(board.board_re_lev > 0) {
						for(let i = 0; i < board.board_re_lev; i++) {
							reMark += "&nbsp;&nbsp;";
						}
						
						reMark += "<img src=\"${pageContext.request.contextPath}/resources/images/re.gif\">";
					}
					
					// 테이블에 표시할 JSON 데이터 출력문 생성(1개 게시물 생성 후 반복)
					let item = "<tr>"
								+ "<td class='board_num'>" + board.board_num + "</td>"
								+ "<td class='board_subject'>"
									+ reMark
									+ board.board_subject 
								+ "</td>"
								+ "<td>" + board.board_name + "</td>"
								+ "<td>" + getDateTime(board.board_date) + "</td>"
								+ "<td>" + board.board_readcount + "</td>"
								+ "</tr>";
					
					// 생성된 출력문을 테이블 마지막 요소로 추가
					$("#listForm > table").append(item);
					
				} // for문 끝
				
				// 현재 페이지번호(pageNum)가 전체 페이지 번호(maxPage)보다 작을 경우
				// 목록 출력 후 다음 페이지 로딩을 위한 스크롤 이벤트가 동작 재개할 수 있도록
				// isContinue 변수값을 true 로 변경하고 현재 페이지번호값 1 증가시키기
				console.log("현재 페이지번호 : " + pageNum + ", 끝 페이지 번호 : " + data.pageInfo.maxPage);
				if(pageNum < data.pageInfo.maxPage) {
					isContinue = true;
					
					// 현재 페이지번호 1 증가시키기
					pageNum++;
// 					console.log("페이지번호 : " + pageNum);
				}
			}).fail(function() {
				alert("요청 실패!");
			});
		}
		
		function getDateTime(date) {
			// 2024-10-29 15:10:07.0 => 24-10-29 15:10
			return date.substring(2, 16);
		}
	</script>
</body>
</html>













