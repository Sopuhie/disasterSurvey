package com.santech.framework.utils.component.encrypt;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Util {


    private SHA1Util() {
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha1(byte[] data) {
        return getDigest().digest(data);
    }

    public static byte[] sha1(String data) {
        return sha1(data.getBytes());
    }

    public static String sha1Hex(byte[] data) {
        return HexUtil.toHexString(sha1(data));
    }

    public static String sha1Hex(String data) {
        return HexUtil.toHexString(sha1(data));
    }

    public static byte[] hmacSha1(byte[] data, String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hmacSha1(String data, String key) {
        return hmacSha1(data.getBytes(), key);
    }

    public static String hmacSha1Hex(byte[] data, String key) {
        return HexUtil.toHexString(hmacSha1(data, key));
    }

    public static String hmacSha1Hex(String data, String key) {
        return hmacSha1Hex(data.getBytes(), key);
    }
}
