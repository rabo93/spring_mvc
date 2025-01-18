<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<script type="text/javascript">
		// JSP 내장객체 request 객체의 "msg" 속성값을 자바스크립트 alert() 함수로 출력
		alert("${msg}"); // \${msg} 부분은 서버측에서 실행된 후 결과값으로 치환되어 전송됨
		
		// request 객체의 "targetURL" 속성값이 비어있을 경우 이전 페이지로 돌아가기
		// 아니면, "targetURL" 속성에 지정된 페이지로 이동 처리
// 		if("${targetURL}" == "") {
// 			history.back();
// 		} else {
// 			location.href = "${targetURL}";
// 		}
		// ------------------------------------
		// 추가) "isClose" 속성값이 "true" 일 경우(또는 비어있지 않을 경우) 현재 창 닫기
		// => 자바의 true/false 는 자바스크립트에서 의미가 없으며
		//    true 일 경우에만 값을 전달하고 false 일 경우에는 속성 자체를 전달하지 않기 때문에
		//    문자열 존재 여부만 판별해도 됨(위의 target 도 동일하며 !"${isClose}" 는 없을 경우 판별)
		if("${isClose}") { // 창 닫기를 수행해야할 경우
			if("${targetURL}") { // 이동할 주소가 있을 경우
				// 부모창의 주소 변경
				window.opener.location.href = "${targetURL}";
			} else { // 이동할 주소가 없을 경우
				// 부모창 새로고침
				window.opener.location.reload();
			}
		
			// 현재창(= 자식창 닫기)
			window.close();
		} 
		
		if("${targetURL}" == "") {
			history.back();
		} else {
			location.href = "${targetURL}";
		}
		
	</script>
</body>
</html>













