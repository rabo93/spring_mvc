package com.itwillbs.mvc_board.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 커스텀 어노테이션 작성을 위해 New - Annotation 메뉴를 클릭하여 어노테이션 생성
// => interface 키워드 앞에 @ 기호가 붙은 상태로 인터페이스 형태로 정의됨(인터페이스 생성 규칙 적용)
//    (상수(enum 포함) 및 추상메서드만 포함할 수 있다!)
@Retention(RetentionPolicy.RUNTIME) // LoginCheck 어노테이션이 컴파일 후 실행 과정에서도 유지됨(AOP 를 통해 감지할 수 있도록)
@Target(ElementType.METHOD)
public @interface LoginCheck {
	// 인터페이스 내부에 상수(enum 도 포함됨) 및 추상메서드만 포함 가능(지금은 불필요)
	// 연습) 사용자의 타입을 지정할 수 있는 enum 상수 정의
	public static enum MemberRole { 
		ADMIN, USER
	}
	
	// 연습) 추상메서드 정의
	public MemberRole memberRole();
	// => 추상메서드 리턴타입으로 지정한 타입에 대한 값은
	//    @LoginCheck 어노테이션 지정 시 @LoginCheck(memberRole = 값) 형태로 지정해야함 
	// => 따라서, 이 추상메서드가 정의될 경우 MemberRole 이라는 enum 도 함께 정의해야함
}













