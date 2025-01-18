package com.itwillbs.mvc_board.handler;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

// 특정 길의의 난수 생성에 사용할 GenerateRandomCode 클래스 정의
public class GenerateRandomCode {
	// 난수 생성하여 문자열로 리턴할 getRandomCode() 메서드 정의
	// => 파라미터 : 난수 길이(정수)   리턴타입 : String
	// => 인스턴스 생성 없이 즉시 호출 가능하도록 static 메서드로 정의
	public static String getRandomCode(int length) {
		// [ 난수 생성 방법 ]
		// 1. java.util.Random 클래스 활용(또는 java.lang.Math 클래스의 random() 메서드 활용)
		// 5자리 정수(0 ~ 99999)로 된 난수 생성
//		Random r = new Random();
//		int rNum = r.nextInt(100000); // 0 ~ 99999 까지의 난수 생성
		
//		int rNum = r.nextInt(10 ^ length); // 10의 length 승 까지의 난수 생성
		// => ex. length = 5 일 때 10 의 5승 = 100000
		// => 주의! 10 ^ length 수식 형태로 지정 시 ^ 연산자가 비트 단위 논리연산자 XOR 로 취급되어
		//    2진수 10 과 2진수 length 값의 비트 단위 XOR 연산 결과값이 적용되므로 다른 값으로 연산됨
		// => 결론! x ^ y 연산을 수행하기 위해서는 Math.pow() 메서드를 활용하는 것이 정확하다!
		//    (기본문법 : double Math.pow(double, double))
//		System.out.println(Math.pow(10, length)); //  값 확인
//		int rNum = r.nextInt((int)Math.pow(10, length));
		// ---------------------------------------------------------------------------------------
		// 2. Random 클래스의 보안 취약점을 해결하기 위해 제공되는 SecureRandom 클래스 활용
		// => 사용법은 Random 클래스와 동일
//		SecureRandom sr = new SecureRandom();
//		int rNum = sr.nextInt((int)Math.pow(10, length));
		// ---------------------------------------------------------------------------------------
		// 생성된 난수가 5자리 미만일 경우 모자라는 자릿수는 비워둔 채로 실제 숫자만 표기함
		// => 이유 : 정수 앞에 0 채울 경우 맨 앞자리가 0인 정수는 8진수로 취급되기 때문
		// => 따라서, 문자열로 변환하여 문자열 포맷을 앞자리 빈자리에 0 채우도록 변경하여 리턴
//		return "" + String.format("%05d", rNum);
		// =======================================================================================
		// 3. 난수 생성에 관한 다양한 기능을 제공하는 라이브러리 활용
		// RandomStringUtils(또는 RandomUtils) 클래스 활용(commons-lang3 라이브러리 추가 필요)
		// => randomXXX() 메서드 활용하여 원하는 형태의 난수 생성
//		return RandomStringUtils.randomNumeric(length); // 지정된 길이의 정수형 난수 생성(앞자리 0 포함)
		// => 단, 정수형 난수는 secure() 등의 다른 메서드를 사용하도록 권장됨(randomNumeric() 은 deprecated 처리됨)
		
		return RandomStringUtils.randomAlphanumeric(length); // 지정된 길이의 알파벳&정수 조합을 문자열로 생성
	}
}












