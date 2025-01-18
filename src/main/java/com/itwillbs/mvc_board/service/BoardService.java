package com.itwillbs.mvc_board.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwillbs.mvc_board.mapper.BoardMapper;
import com.itwillbs.mvc_board.vo.BoardVO;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class BoardService {
	@Autowired
	private BoardMapper mapper;

	// 게시물 등록 요청을 위한 registBoard()
	public int registBoard(BoardVO board) {
		// BoardMapper - insertBoard() 
		return mapper.insertBoard(board);
	}

	// 게시물 목록 조회 요청을 위한 getBoardList()
	public List<BoardVO> getBoardList(String searchType, String searchKeyword, int startRow, int listLimit) {
		// BoardMapper - selectBoardList()
		return mapper.selectBoardList(searchType, searchKeyword, startRow, listLimit);
	}
	

	// 전체 게시물 목록 갯수 조회 요청을 위한 getBoardListCount()
	public int getBoardListCount(String searchType, String searchKeyword) {
		// BoardMapper - selectBoardListCount()
		return mapper.selectBoardListCount(searchType, searchKeyword);
	}

	// 게시물 상세정보 조회 요청을 위한 getBoard()
//	public BoardVO getBoard(int board_num) {
//		// BoardMapper - selectBoard()
//		return mapper.selectBoard(board_num);
//	}

	// 게시물 상세정보 조회 요청을 위한 getBoard() - 조회수 증가 작업 추가
	public BoardVO getBoard(int board_num, boolean isIncreaseReadcount) {
		// BoardMapper - selectBoard() 메서드 호출하여 게시물 상세정보 조회 요청
		BoardVO board = mapper.selectBoard(board_num);
		
		// 조회 결과가 존재하고 조회수 증가를 수행해야할 경우
		// BoardMapper - updateReadcount() 메서드 호출하여 해당 게시물의 조회수 증가
		// => 단, 조회수가 증가된 게시물의 변경된 조회수 값을 BoardVO 객체에 반영하기 위해
		//    조회가 완료된 BoardVO 객체를 파라미터로 다시 전달(글번호도 포함되어 있음)
		if(board != null && isIncreaseReadcount) {
			mapper.updateReadcount(board);
		}
		
		// 조회 결과 리턴
		return board;
	}

	// 게시물 삭제 요청을 수행하는 removeBoard()
	public int removeBoard(int board_num) {
		// BoardMapper - deleteBoard()
		return mapper.deleteBoard(board_num);
	}

	// 게시물 수정 요청을 수행하는 modifyBoard()
	public int modifyBoard(BoardVO board) {
		return mapper.updateBoard(board);
	}

	// 답글 등록 요청
	// => 답글들의 순서번호 조정 작업과 답글 등록 작업 두 가지의 DB 데이터 조작을 차례대로 수행하는데
	//    이 때, 두 작업을 하나의 트랙잭션으로 처리하기 위해 @Transactional 어노테이션 적용 필수!
	//    (개발자가 별도로 commit 또는 rollback 작업을 지시하지 않아도 자동으로 처리됨)
	//    (주의! root-context.xml 과 servlet-context.xml 파일에 트랜잭션 설정 필수!)
	@Transactional
	public int registReplyBoard(BoardVO board) {
//		log.info(">>>>>>>>>> 답글 등록 작업 시작!");
		// 기존 답글들의 순서번호 조정을 위해 updateBoardReSeq() 메서드 호출
		// => 파라미터 : BoardVO 객체   리턴타입 : void
		mapper.updateBoardReSeq(board);
		
		// 답글 등록 작업 위해 insertReplyBoard() 메서드 호출
		// => 파라미터 : BoardVO 객체   리턴타입 : int
		return mapper.insertReplyBoard(board);
	}

	// 게시물 파일 삭제 요청
	public int removeBoardFile(Map<String, String> map) {
		return mapper.deleteBoardFile(map);
	}

	// =======================================================
	// 댓글 등록 요청
	public int registTinyReplyBoard(Map<String, String> map) {
		return mapper.insertTinyReplyBoard(map);
	}

	// 댓글 목록 조회 요청
	public List<Map<String, Object>> getTinyReplyBoardList(int board_num) {
		return mapper.selectTinyReplyBoardList(board_num);
	}

	// 대댓글 등록 요청
	@Transactional
	public int registTinyReReplyBoard(Map<String, String> map) {
		// 기존 댓글들의 순서번호 조정을 위해 updateTinyReplyBoardReSeq() 메서드 호출
		// => 파라미터 : Map 객체   리턴타입 : void
		mapper.updateTinyReplyBoardReSeq(map);
		// => 댓글 목록 순서가 등록된 순서(오름차순)이므로 최신 댓글이 아래쪽으로 위치함
		//    따라서, 기본적으로는 댓글간의 순서 조정은 불필요함
		// => 그러나, 대댓글에 대한 n차 댓글 작성 시 항상 맨 마지막에 위치하게 되므로
		//    결국 n차 대댓글에 대한 순서번호 조정이 필요함
		
		// 대댓글 등록 작업 위해 insertTinyReReplyBoard() 메서드 호출
		// => 파라미터 : Map 객체   리턴타입 : int
		return mapper.insertTinyReReplyBoard(map);
	}

	// 댓글 삭제 작업 전 댓글 작성자 조회 요청
	public String getTinyReplyWriter(Map<String, String> map) {
		return mapper.selectTinyReplyWriter(map);
	}

	// 댓글 삭제 요청
	public int removeTinyReplyBoard(Map<String, String> map) {
		return mapper.deleteTinyReplyBoard(map);
	}
}












