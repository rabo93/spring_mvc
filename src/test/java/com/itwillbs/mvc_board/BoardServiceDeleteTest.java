package com.itwillbs.mvc_board;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.itwillbs.mvc_board.mapper.BoardMapper;

// JUnit 테스트를 수행할 클래스 정의
// @RunWith 어노테이션을 사용하여 테스트에 사용할 스프링 빈을 자동 주입하는 역할의 클래스 파일 지정
// => spring-test 라이브러리 필수! (pom.xml 에서 등록)
// => @RunWith(SpringJUnit4ClassRunner.class) 형식으로 지정(JUnit 버전에 따라 클래스가 달라질 수 있음)
@RunWith(SpringJUnit4ClassRunner.class) // .class 필수!!!!
// @ContextConfiguration 어노테이션을 사용하여 테스트에 필요한 자원(XML 파일 등)을 수동으로 지정
// => 기본적으로 root-context.xml 파일은 포함시키고, 나머지는 필요에 따라 추가
// => 기본 문법 : @ContextConfiguration(locations = {"파일명1", "파일명2", ..., "파일명n"})
//@ContextConfiguration(locations = {"root-context.xml"}) // 현재 클래스와 같은 경로에서 파일 탐색(복사 필요)
//@ContextConfiguration(locations = {"classpath:/root-context.xml"}) // src/test/resources 내에서 탐색(복사 필요)
@ContextConfiguration(locations = {"file:src/main/resources/config/root-context.xml"}) // 실제 원본 파일 위치 탐색(복사 불필요)
public class BoardServiceDeleteTest {
	@Autowired
	private BoardMapper mapper;

	@Test
	public void testRemoveBoard() {
		mapper.deleteBoard(37);
	}

}









