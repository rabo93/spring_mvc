package com.itwillbs.mvc_board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
	
	// 기본 에러 페이지 포워딩
	@GetMapping("error")
	public String error() {
		return "error/error";
	}
	
}
