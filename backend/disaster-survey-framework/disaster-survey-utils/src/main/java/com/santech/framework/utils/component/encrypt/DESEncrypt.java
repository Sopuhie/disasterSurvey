package com.santech.framework.utils.component.encrypt;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESEncrypt {

    public static String getEncString(String strMing, String key, String iv, String charset) {
        byte[] byteMi = null;
        byte[] byteMing = null;
        String strMi = "";
        try {
            byteMing = strMing.getBytes(charset);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes(charset));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv2);
            byteMi = cipher.doFinal(byteMing);
            strMi = new String(Base64.encode(byteMi), charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            byteMi = null;
        }
        return strMi;
    }

    public static String getDesString(String strMi, String key, String iv, String charset) {
        byte[] byteMing = null;
        byte[] byteMi = null;
        String strMing = "";
        try {
            byteMi = Base64.decode(strMi.getBytes(charset));
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes(charset));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv2);
            byteMing = cipher.doFinal(byteMi);
            strMing = new String(byteMing, charset);
            return strMing;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getEnc(byte[] data, String key, String iv, String charset) {
        byte[] byteMi = null;
        byte[] byteMing = null;
        try {
            byteMing = data;
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes(charset));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv2);
            byteMi = cipher.doFinal(byteMing);
            byteMi = Base64.encode(byteMi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteMi;
    }

    public static byte[] getDes(byte[] data, String key, String iv, String charset) {
        byte[] byteMing = null;
        byte[] byteMi = null;
        try {
            byteMi = Base64.decode(data);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes(charset));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv2);
            byteMing = cipher.doFinal(byteMi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteMing;
    }
}
