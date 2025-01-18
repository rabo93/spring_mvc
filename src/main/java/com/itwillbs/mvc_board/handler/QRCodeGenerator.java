package com.itwillbs.mvc_board.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Component
public class QRCodeGenerator {

	public byte[] generateQRCode(String data, int width, int height) throws WriterException, IOException {
		// QR 코드 이미지를 생성하는 ZXing 라이브러리의 QRCodeWriter 인스턴스 생성
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		
		// QR 코드 생성에 사용되는 옵션 값을 저장할 Map 객체 생성
		// => QR 코드에 저장할 항목에 대한 문자열을 UTF-8 로 인코딩하도록 지정
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		
		// 입력된 텍스트를 QR 코드 형태로 변환하여 비트매트릭스 생성
		// QRCodeWriter 객체의 encode() 메서드 호출
		// => 파라미터 : 인코딩 문자열, 생성할 코드의 형태, 가로크기, 세로크기, 설정옵션
		BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
		
		// 바이트 배열로 출력할 출력스트림 생성 및 비트매트릭스를 PNG 로 변환하여 배열로 출력
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
		
		return outputStream.toByteArray();
	}
	
}









