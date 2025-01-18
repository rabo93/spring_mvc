package com.itwillbs.mvc_board.vo;

import java.sql.Timestamp;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/*
[ spring_mvc_board3.board 테이블 정의 ]
글번호(board_num) - 정수, PK
작성자(board_name) - 문자열(16), NN
제목(board_subject) - 문자열(100), NN
내용(board_content) - 문자열(2000), NN

파일명(board_file) - 문자열(200), NN => 다중 파일 업로드 시 사용할 컬럼
파일명(board_file1) - 문자열(200), NN => 단일 파일 업로드 시 사용할 컬럼
파일명(board_file2) - 문자열(200), NN => 단일 파일 업로드 시 사용할 컬럼
파일명(board_file3) - 문자열(200), NN => 단일 파일 업로드 시 사용할 컬럼

참조글번호(board_re_ref) - 정수, NN
들여쓰기레벨(board_re_lev) - 정수, NN
순서번호(board_re_seq) - 정수, NN
조회수(board_readcount) - 정수, NN
작성일시(board_date) - 날짜 및 시각(DATETIME), NN
작성자 IP주소(board_writer_ip) - 문자열(20), NN
--------------------------------------------------
CREATE TABLE board (
	board_num INT PRIMARY KEY,
 	board_name VARCHAR(16) NOT NULL,
 	board_subject VARCHAR(100) NOT NULL,
 	board_content VARCHAR(2000) NOT NULL,
 	board_file VARCHAR(200) NOT NULL,
 	board_file1 VARCHAR(200) NOT NULL,
 	board_file2 VARCHAR(200) NOT NULL,
 	board_file3 VARCHAR(200) NOT NULL,
 	board_re_ref INT NOT NULL,
 	board_re_lev INT NOT NULL,
 	board_re_seq INT NOT NULL,
 	board_readcount INT NOT NULL,
 	board_date DATETIME NOT NULL,
 	board_writer_ip VARCHAR(20) NOT NULL
);
*/
@Data
public class BoardVO {
	private int board_num;
	private String board_name;
	private String board_subject;
	private String board_content;
	
	// --------------------------------------------------------------------------------
	// 파일 업로드 시 실제 파일과 파일명을 별도로 분리하여 관리
	// 1) String 타입 멤버변수는 실제 파일이 아닌 파일명을 저장하는 용도로 사용
	private String board_file;
	private String board_file1;
	private String board_file2;
	private String board_file3;
	
	// 2) MultipartFile 타입 변수를 실제 업로드되는 파일을 관리하는 용도로 사용
	private MultipartFile[] file; // multiple 속성을 통한 다중 파일 업로드 시 사용
	private MultipartFile file1; // 단일 파일 업로드 시 사용
	private MultipartFile file2; // 단일 파일 업로드 시 사용
	private MultipartFile file3; // 단일 파일 업로드 시 사용
	// --------------------------------------------------------------------------------
	
	private int board_re_ref;
	private int board_re_lev;
	private int board_re_seq;
	private int board_readcount;
	private Timestamp board_date; // java.sql.Timestamp(java.sql.Date 도 사용 가능)
	private String board_writer_ip;
}














