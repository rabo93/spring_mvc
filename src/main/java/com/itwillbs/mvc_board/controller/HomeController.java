package com.itwillbs.mvc_board.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.WriterException;
import com.itwillbs.mvc_board.handler.QRCodeGenerator;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		return "index";
	}
	
	// ==============================================
	@Autowired
	private QRCodeGenerator qrCodeGenerator;
	
	// QR 코드 생성 요청
	@ResponseBody // 바이너리데이터를 직접 응답하기 위해 @ResponseBody 지정
	@GetMapping("GenerateQRCode")
	public ResponseEntity<byte[]> generateQrCode(@RequestParam String data) {
		System.out.println("data : " + data);
		try {
			// QR코드 생성 메서드 호출
			// => 파라미터 : QR코드에 포함할(인코딩) 문자열, 가로크기, 세로크기
			// => 리턴타입 : 생성된 QR 코드 이미지를 PNG 형식으로 생성하여 byte 배열로 반환
			byte[] qrCodeImage = qrCodeGenerator.generateQRCode(data, 150, 150);
			
			// 응답 헤더 설정(컨텐츠 타입을 이미지(PNG)로 설정하기 위해)
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			
			// ResponseEntity 객체를 활용하여 응답 상태코드, 헤더, 바디 정보를 묶어 리턴
			return ResponseEntity.ok()
								.headers(headers)
								.body(qrCodeImage);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(null); // 응답데이터 생성하여 전달(500 에러)
		}
		
	}
	
}










