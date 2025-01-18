package com.itwillbs.mvc_board.handler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnailator;

// 파일 처리를 담당하는 클래스
@Component
public class FileHandler {
	// 전달된 가상 경로에 대한 실제 경로 리턴하는 메서드
	public String getRealPath(HttpSession session, String virtualPath) {
		return session.getServletContext().getRealPath(virtualPath);
	}
	
	// 전달된 실제 경로 상의 서브디렉토리를 생성하는 메서드
	public String createDirectories(String realPath) {
		String subDir = ""; // 서브 디렉토리명을 저장할 변수 선언
		
		LocalDate today = LocalDate.now(); // 현재 시스템의 날짜 정보 생성
		String datePattern = "yyyy/MM/dd"; // 날짜 포맷 변경에 사용될 패턴 문자열 생성
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datePattern);
		subDir = today.format(dtf); // LocalDate - DateTimeFormatter
		
		realPath += "/" + subDir;
//				
		try {
			Path path = Paths.get(realPath); // 파라미터로 실제 업로드 경로 전달
			Files.createDirectories(path); // IOException 예외 처리 필요(임시로 현재 클래스에서 처리)
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return subDir;
	}

	// 전달된 원본 파일명에 대한 중복 방지 처리
	public String processDuplicateFileName(String originalFilename) {
		// 파일명 중복 처리하여 변수에 저장
		// => 시스템시각정보(long 타입 값) + UUID 앞 8자리 + 파일확장자
		//    (원본 파일명은 사용하지 않고 자동으로 생성된 파일명 사용)
		// => UUID 전체 문자열 그대로 사용할 경우 시스템 시각 정보 등 다른 문자열 불필요
		String uuid = UUID.randomUUID().toString().substring(0, 8); // UUID 8자리 추출
		// 원본 파일명에서 파일 확장자 추출(. 기호 제외하고 추출)
//		String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		// 원본 파일명에서 파일 확장자 추출(. 기호 포함하여 추출)
		String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
		
		return System.currentTimeMillis() + "_" + uuid + fileExtension;
//		return UUID.randomUUID().toString() + fileExtension; // UUID 만 사용 시
	}

	// 임시 경로에 저장된 파일을 실제 업로드 처리하는 메서드
	public String completeUpload(MultipartFile file, String realPath, String fileName) {
		String uploadFileName = "";
		
		// FileHandler - createDirectories() 메서드 호출하여 서브디렉토리 생성
		// => 파라미터 : 실제 업로드 경로   리턴타입 : String(생성된 서브디렉토리)
		String subDir = createDirectories(realPath);
		
		try {
			// MultipartFile 객체의 transferTo() 메서드 호출하여 실제 경로로 임시 파일 이동 처리
			file.transferTo(new File(realPath, subDir + "/" + fileName));
			// 예외가 발생하지 않았을 경우 uploadFileName 변수에 서브디렉토리를 포함한 파일명 저장
			uploadFileName = subDir + "/" + fileName;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return uploadFileName;
	}

	// 썸네일 이미지 생성하는 메서드
	public String createThumbnailImage(String realPath, String uploadFileName) {
		String thumbnailFileName = "";
		
		// 원본 파일명에서 파일명 앞에 "thumb_" 문자열 추가
		// => String 클래스 대신 StringBuffer 클래스 사용할 경우 문자열 편집이 편함 
		// => String -> StringBuffer 로 변환하여 작업 후 다시 String 타입으로 변환
		StringBuffer sb = new StringBuffer(uploadFileName);
		sb.insert(sb.lastIndexOf("/") + 1, "thumb_");
//		System.out.println(sb);
		
		String fileName = sb.toString();
		
		// 1. File + FileInputStream 객체 조합으로 원본 파일 읽어오기
		File file = new File(realPath, uploadFileName);
		
		// 2. File + FileOutputStream 객체 조합으로 출력 파일 생성
		File thumbnailFile = new File(realPath, fileName);
		
		// try ~ resources 블록 형태로 스트림 객체를 try() 문 내에서 작성 시 자동 close() 호출됨
		try(FileInputStream fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(thumbnailFile);) {
			// File 객체를 java.awt.image.BufferedImage 객체로 변환하여 이미지 사이즈 알아내기
			// => 주의! 이클립스 최근 버전들은 java.awt.* 패키지가 정상적으로 접근되지 않을 수 있음
			// => Window - Preferences - Java - Appearance - Type Filters 항목에서 java.awt 항목 체크 해제 필요
			BufferedImage bufferedImage = ImageIO.read(fis);
			int width = bufferedImage.getWidth();
			int height = bufferedImage.getHeight();
			System.out.println("Width : " + width + "px , Height : " + height + "px");
			
			// 이미지 사이즈 가로, 세로 중 한쪽이라도 150px 보다 클 경우
			// 썸네일 이미지 크기를 150, 150 으로 설정하고
			// 아니면 기본 이미지 사이즈 가로, 세로 크기로 설정하여 썸네일 이미지 생성
			if(width > 150 || height > 150) {
				System.out.println("가로 또는 세로 사이즈가 150px 보다 큼!");
				width = 150;
				height = 150;
			}
			
			// 3. 1번과 2번 파일을 대상으로 Thumbnailator.createThumbnail() 메서드를 호출하여 썸네일 생성
			Thumbnailator.createThumbnail(file,  thumbnailFile, width, height);
			// => 무조건 지정된 크기로 조정되는것이 아니라 비율에 맞게 자동으로 처리됨
			//    ex) 450 * 300 일 때 150 * 150 이 아닌 150 * 100 으로 변환됨 
			
			// 4. 스트림 자원 반환
			// => try ~ resources 블럭 처리로 인해 자동 close() 수행됨
			
			// 5. 썸네일 파일명을 변수에 저장
			thumbnailFileName = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return thumbnailFileName;
	}
}














