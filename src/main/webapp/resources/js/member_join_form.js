// 아이디, 비밀번호 입력값 체크 결과(true = 적합/false = 부적합)를 저장할 변수 선언
// => 여러 함수에서 접근해야하므로 전역변수로 선언
let checkIdResult = false;
let checkPasswdResult = false;
let checkPasswd2Result = false;

//function checkId() {
//	window.open("MemberCheckIdForm", "id_check", "width=400, height=400");
//}

function checkId() {
	let id = $("#id").val();
//	console.log(id + ", " + id.length);
	
	// 아이디 : 영문자, 숫자만 첫글자에 올 수 있고,
	//          두번째 글자부터는 영문자, 숫자, 특수문자 _ 만 사용 가능(전체 5 ~ 16글자)
	let regex = /^[A-Za-z0-9][A-Za-z0-9_]{4,15}$/;
	if(regex.exec(id)) {
		console.log("AJAX 요청 시작");
		// AJAX 활용하여 "MemberCheckId" 서블릿 주소로 아이디 중복 검사 요청(GET)
		// => 파라미터 : 입력받은 아이디
		$.ajax({
			type : "GET",
			url : "MemberCheckId",
			data : {
				id : id
			},
			success : function(result) {
//				console.log(result);

				// 아이디 중복 검사 판별 결과(true/false => 단, 문자열)를 통해
				// 사용 가능/불가능 여부 출력
				if(result.trim() == "false") { // 중복이 아닐 경우
					$("#checkIdResult").text("사용 가능");
					$("#checkIdResult").css("color", "green");
					
					checkIdResult = true;
				} else { // 중복일 경우
					$("#checkIdResult").text("사용 불가능");
					$("#checkIdResult").css("color", "red");
					
					checkIdResult = false;
				}
				
			},
			error : function() {
				alert("일시적인 시스템 장애로\n아이디 중복 검사 불가!");
			}
		});
		
	} else {
		$("#checkIdResult").text("5 ~ 16글자의 영문자, 숫자, _ 만 사용 가능");
		$("#checkIdResult").css("color", "RED");
		
		// 체크 결과 저장을 위해 checkIdResult 변수에 false 저장
		checkIdResult = false;
	}
}
// -----------------------------------------------------------------
$(function() {
	$("#passwd").keyup(function() {
		let passwd = $("#passwd").val();
		
		if(passwd.length >= 8 && passwd.length <= 16) { // 적합
// 			document.querySelector("#checkPasswdResult").innerText = "사용 가능한 패스워드";
// 			document.querySelector("#checkPasswdResult").style.color = "BLUE";
			$("#checkPasswdResult").text("사용 가능한 패스워드");
			$("#checkPasswdResult").css("color", "BLUE");
			
			// 체크 결과 저장을 위해 checkPasswdResult 변수에 true 저장
			checkPasswdResult = true;
		} else { // 부적합
// 			document.querySelector("#checkPasswdResult").innerText = "사용 불가능한 패스워드";
// 			document.querySelector("#checkPasswdResult").style.color = "RED";
			$("#checkPasswdResult").text("사용 불가능한 패스워드");
			$("#checkPasswdResult").css("color", "RED");
			
			// 체크 결과 저장을 위해 checkPasswdResult 변수에 false 저장
			checkPasswdResult = false;
			
		}
			
		// 비밀번호가 변경되면 비밀번호확인 작업을 다시 수행해야한다!
		// 단, 비밀번호확인을 수행하는 함수가 익명함수일 때 호출이 불가능하므로
		// 익명함수 호출 대신 비밀번호확인 항목에 대한 keyup 이벤트를 강제로 발생시킬 수 있다!
		// => 이벤트 트리거(trigger)를 활용하여 특정 요소에 이벤트 발생을 강제로 제어 가능
		$("#passwd2").trigger("keyup");
		
	});
	// ----------------------------------------------
	$("#passwd2").keyup(function() {
		let passwd = $("#passwd").val();
		let passwd2 = $("#passwd2").val();
		
		if(passwd == passwd2) { // 적합
			$("#checkPasswd2Result").text("비밀번호 일치");
			$("#checkPasswd2Result").css("color", "BLUE");
			
			// 체크 결과 저장을 위해 checkPasswdResult 변수에 true 저장
			checkPasswdResult = true;
		} else { // 부적합
			$("#checkPasswd2Result").text("비밀번호 불일치");
			$("#checkPasswd2Result").css("color", "RED");
			
			// 체크 결과 저장을 위해 checkPasswdResult 변수에 false 저장
			checkPasswd2Result = false;
		}
	});
	// --------------------------------------------------------------
	// 이메일 도메인 선택 시 텍스트박스에 입력
	$("#emailDomain").change(function() {
		$("#email2").val($("#emailDomain").val());
	});
	// --------------------------------------------------------------
	$("#check_all").click(function() {
		// 전체선택을 제외한 나머지 체크박스에 대한 반복 수행
		$("input[name=hobby]").each(function(index, item) {
			// 전체선택 체크박스 체크상태값을 각 체크박스 체크상태값으로 설정
			$(item).prop("checked", $("#check_all").prop("checked"));
		});
		
	});
	// --------------------------------------------------------------
	// 이미지 파일 업로드 시 해당 이미지 미리보기 표시
	// => change 이벤트 익명함수 파라미터에 이벤트 객체를 전달받도록 변수 event 선언
	$("#profile_img").change(function(event) {
		// 업로드 되는 이미지 파일 정보 가져오기
		let file = event.target.files[0];
		
		// 자바스크립트의 FileReader 객체 생성(파일 읽기용)
		let reader = new FileReader();
		
		// FileReader 객체 로딩 완료 시 핸들링 - 익명함수 파라미터로 event 객체 받아오기 
		reader.onload = function(event2) {
			// 파일 로딩 완료되면 img 태그에 업로드 파일 이미지 미리보기로 표시(src 속성값 변경)
			// => 익명함수 파라미터로 전달받은 event2 변수에 해당 업로드 파일 정보가 전달되므로
			//    해당 객체의 target.result 속성으로 파일에 접근 가능 
			console.log("파일 : " + event2.target.result);
			$("#preview_profile").attr("src", event2.target.result);
		}
		
		// FileReader 객체를 사용하여 전달된 파일 정보를 통해 파일 읽어오기
		reader.readAsDataURL(file);
	});
	// --------------------------------------------------------------
	// 가입 버튼의 click 이벤트 핸들링
//	$("#btnSubmit").click(function() {
//		// 회원가입 폼의 다른 데이터들 입력값 체크는 생략
//		// 이메일만 체크
//		let email1 = $("#email1").val();
//		let email2 = $("#email2").val();
//		console.log(email1 + ", " + email2);
//		
//		if(email1 == "") {
//			alert("이메일 입력 필수!");
//			$("#email1").focus();
//			return;
//		} else if(email2 == "") {
//			alert("이메일 입력 필수!");
//			$("#email2").focus();
//			return;
//		}
//		
//		// email1 과 email2 입력 완료 시
//		// 폼 내에 <input type="hidden"> 태그를 활용하여 email 파라미터 설정
//		// => 이 때, 해당 value 속성값은 email1 과 email2 를 결합한 값을 사용(email1 + "@" + email2)
//		// => form 태그 내부에 <input> 태그 추가 => append() 또는 prepend() 메서드 활용
//		$("form").prepend("<input type='hidden' name='email' value='" + email1 + "@" + email2 + "'>");
//		
//		$("form").submit(); // submit 동작 수행
//		
//	});
	$("form").on("submit", function() {
		// 회원가입 폼의 다른 데이터들 입력값 체크는 생략
		// 이메일만 체크
		let email1 = $("#email1").val();
		let email2 = $("#email2").val();
		console.log(email1 + ", " + email2);
		
		if(email1 == "") {
			alert("이메일 입력 필수!");
			$("#email1").focus();
			return false; // submit 동작 취소
		} else if(email2 == "") {
			alert("이메일 입력 필수!");
			$("#email2").focus();
			return false; // submit 동작 취소
		}
		
		// email1 과 email2 입력 완료 시
		// 폼 내에 <input type="hidden"> 태그를 활용하여 email 파라미터 설정
		// => 이 때, 해당 value 속성값은 email1 과 email2 를 결합한 값을 사용(email1 + "@" + email2)
		// => form 태그 내부에 <input> 태그 추가 => append() 또는 prepend() 메서드 활용
		// => 먼저 기존에 이메일 hidden 태그가 존재할 경우 제거 수행
		$("input[name=email]").remove();
		$("form").prepend("<input type='hidden' name='email' value='" + email1 + "@" + email2 + "'>");
		
		// true 값 리턴하거나 생략 시 submit 동작 수행됨
	});
	
}); // document ready 이벤트 끝














