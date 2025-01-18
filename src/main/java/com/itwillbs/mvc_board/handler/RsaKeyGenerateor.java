package com.itwillbs.mvc_board.handler;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RsaKeyGenerateor {
	private static final String CIPHER_ALGORITHM = "RSA"; // 암호화 알고리즘명 저장
	
	// 암호화에 사용될 공개키/개인키를 생성하는 메서드
	public static Map<String, String> generateKey() {
		Map<String, String> rsaKey = new HashMap<String, String>(); // 한 쌍의 키를 저장할 Map 객체
		
		try {
			// 1. 공개키/개인키 한 쌍을 생성하는 KeyPairGenerator 객체 생성
			// => KeyPairGenerator 클래스의 static 메서드 getInstance() 호출(알고리즘 전달)
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(CIPHER_ALGORITHM);
			
			// 2. 생성된 KeyPairGenerator 객체의 initialize() 메서드 호출하여 키의 길이(정수) 전달
			// => 현재 사용하는 일반적인 키의 길이는 2048 바이트
			keyPairGenerator.initialize(2048);
			
			// 3. KeyPairGenerator 객체의 genKeyPair() 메서드를 호출하여 KeyPair 객체를 얻어온 후
			//    KeyPair 객체를 사용하여 PublicKey(공개키) 와 Private(개인키) 객체를 생성
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			PublicKey publicKey = keyPair.getPublic(); // 공개키
			PrivateKey privateKey = keyPair.getPrivate(); // 개인키
			
			// 4. java.util.Base64 인코딩을 사용하여 생성된 키를 문자열로 변환
			String strPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded()); 
			String strPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded()); 
			
			// 5. 생성된 키 문자열을 Map 객체에 저장
			rsaKey.put("publicKey", strPublicKey);
			rsaKey.put("privateKey", strPrivateKey);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return rsaKey;
	}

	// 개인키로 복호화를 수행하는 메서드
	public static String decrypt(String base64PrivateKey, String encryptedData) {
		String decryptedData = null; // 최종 복호화 결과를 저장할 변수
		
		try {
			// Base64 형식의 개인키를 byte[] 타입으로 변환(디코딩)
			byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);
			
			// 개인키를 관리하는 PKCS8EncodedKeySpec 객체 생성
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			// KeyFactory 객체를 사용하여 지정된 알고리즘에 맞는 개인키 객체 생성
			KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_ALGORITHM); // 복호화 알고리즘 지정
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			
			// 복호화를 위한 Cipher 객체 생성 및 복호화에 사용할 개인키 전달
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey); // 복호화 모드로 설정 및 개인키 전달
			
			// 클라이언트로부터 전송받은 Base64 형식 암호화 문자열을 복호화
			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
			// Cipher 객체의 doFinal() 메서드 호출하여 복호화 작업 수행 후 결과를 리턴받아 문자열로 변환
			decryptedData = new String(cipher.doFinal(encryptedBytes), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return decryptedData;
	}
}










