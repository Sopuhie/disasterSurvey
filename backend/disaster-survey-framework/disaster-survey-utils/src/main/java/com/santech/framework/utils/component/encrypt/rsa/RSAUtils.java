package com.santech.framework.utils.component.encrypt.rsa;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
/**
 * @version V1.0
 * @Description: RSA加解密工具类，实现公钥加密私钥解密和私钥解密公钥解密
 */
public class RSAUtils {
	
    /**
     * 公钥解密
     *
     * @param publicKeyText
     * @param text
     * @return
     * @throws Exception
     */
    public static String decryptByPublicKey(String publicKeyText, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));
        return new String(result);
    }

    /**
     * 私钥加密
     *
     * @param privateKeyText
     * @param text
     * @return
     * @throws Exception
     */
    public static String encryptByPrivateKey(String privateKeyText, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(text.getBytes());
        return Base64.encodeBase64String(result);
    }

    /**
     * 私钥解密
     *
     * @param privateKeyText
     * @param text
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String privateKeyText, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));
        return new String(result);
    }

    /**
     * 公钥加密
     *
     * @param publicKeyText
     * @param text
     * @return
     */
    public static String encryptByPublicKey(String publicKeyText, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(text.getBytes());
        return Base64.encodeBase64String(result);
    }

    /**
     * 构建RSA密钥对
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static RSAKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        String publicKeyString = Base64.encodeBase64String(rsaPublicKey.getEncoded());
        String privateKeyString = Base64.encodeBase64String(rsaPrivateKey.getEncoded());
        RSAKeyPair rsaKeyPair = new RSAKeyPair(publicKeyString, privateKeyString);
        return rsaKeyPair;
    }
    /**
     * 公钥加密私钥解密
     */
    private static void test1(RSAKeyPair keyPair, String source) throws Exception {
        System.out.println("***************** 公钥加密私钥解密开始 *****************");
        String text1 = encryptByPublicKey(keyPair.getPublicKey(), source);
        String text2 = decryptByPrivateKey(keyPair.getPrivateKey(), text1);
        System.out.println("加密前：" + source);
        System.out.println("加密后：" + text1);
        System.out.println("解密后：" + text2);
        if (source.equals(text2)) {
            System.out.println("解密字符串和原始字符串一致，解密成功");
        } else {
            System.out.println("解密字符串和原始字符串不一致，解密失败");
        }
        System.out.println("***************** 公钥加密私钥解密结束 *****************");
    }

    /**
     * 私钥加密公钥解密
     *
     * @throws Exception
     */
    private static void test2(RSAKeyPair keyPair, String source) throws Exception {
        System.out.println("***************** 私钥加密公钥解密开始 *****************");
        String text1 = encryptByPrivateKey(keyPair.getPrivateKey(), source);
        String text2 = decryptByPublicKey(keyPair.getPublicKey(), text1);
        System.out.println("加密前：" + source);
        System.out.println("加密后：" + text1);
        System.out.println("解密后：" + text2);
        if (source.equals(text2)) {
            System.out.println("解密字符串和原始字符串一致，解密成功");
        } else {
            System.out.println("解密字符串和原始字符串不一致，解密失败");
        }
        System.out.println("***************** 私钥加密公钥解密结束 *****************");
    }
    
    /*public static void main(String[] args) {
    	
    	try {
    		String source = "加密前";
			RSAKeyPair keyPair = RSAUtils.generateKeyPair();
			System.out.println("公钥=="+keyPair.getPublicKey());
			System.out.println("私钥=="+keyPair.getPrivateKey());
			RSAUtils.test1(keyPair, source);
    		
    		
    		String resouce = "Eo4Wfflw4gEhtJt3zOJjKb7M7SJaybIWuVnI3jlCjXzHed59mbBrFYhFzeadx8iQcPbkfh5Dxb9jtcatiqBXTuUeO5HaIOvQ8Ci2PhZ1R15CeTihYAkuVnG5IMDyzdW5YAv3Df4ciLfw0JuSRS8G6sLlE6dBgyyRu1xlkZKB/EE=";
    		String privateKsy = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMQLS8D2yYka3AHyZjmZ4USeaSp+qSEGfU8odXYVwDwYKdqORX9HsuPqWetXCCLIqIVP8kFVRqADL2yFgHTh9RMD6geWA6P3opd//O2TDgD3GBNv7gVajQzb7OdgQ3wn/n9wmow+2UHp+Mlo0nVecshgIk/Cu7TctU3hnsiOWm/jAgMBAAECgYAINhTpMy0D0h0u87E2bmySDDSJdB6vZG8xNpec55JSsgxmvBjLGPy0MSetBMsxccglNVEHTYQUxa1h1UGpI6G5JEk8ZKKF5btWL6i5q/fpp1ZBazRdLcW0YPOSWC5H2EcoHbwDd0iLwnuqkc9+aouRl4YWfaDRQ2Qz2pK3h1d/IQJBAPqjn6cw0Y/XpNbb1TqGbRHbrgZErkWacwL1fvpqe1Q6iC7Gt7eOFH5xUkxUzaDSPvZLbzEPIjMl1mqAZI1TzpECQQDIPLygHpfQaMCUhueZYmbrreq7eJ9YkodSHc2zqXAgzGpxqHIEipcsuaJu1XxHGC1bUhKtKD8U3BqZSsXsCjkzAkBUt8nXMWEkRbeqCFRim91yE9cWt1BQpowJjAGHk0ZsifxOE0019ghnVNaEhoWavnYZuhWzd3IP59I53XwfaRyBAkEArycp3GpGZABtjL6g8jP4UWo1bT2rLUAnbLYzTFgHhDkrVzaAXeaZLH9JTegiqNHGQx58xcjxvR53ieW1Ua7zrwJAV456oCFWCV5OMgGlk8mvvMq773DluVgyJPLTEewMJjHy5D1FyFs3wzwdNuiK0hJmJ5dPsFcgu7qax89OjTY6lQ==";
    		String text1 = decryptByPrivateKey(privateKsy, resouce);
            System.out.println("解密后：" + text1);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}*/
}
