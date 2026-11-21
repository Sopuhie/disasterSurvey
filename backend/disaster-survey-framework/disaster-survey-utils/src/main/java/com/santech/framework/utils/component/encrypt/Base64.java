package com.santech.framework.utils.component.encrypt;

import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class Base64 {
    private static final String UTF_8 = "UTF-8";
    private static final Encoder encoder = java.util.Base64.getEncoder();
    private static final Decoder decoder = java.util.Base64.getDecoder();

    public static String encode(String ming, String charset) {
        try {
            return encoder.encodeToString(ming.getBytes(charset));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(String ming) {
        try {
            return encode(ming, UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encode(byte[] ming) {
        try {
            return encoder.encode(ming);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeToString(byte[] ming) {
        try {
            return encoder.encodeToString(ming);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decode(String mi, String charset) {
        try {
            return new String(decoder.decode(mi), charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decode(String mi) {
        try {
            return decode(mi, UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decode(byte[] mi) {
        try {
            return decoder.decode(mi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decodeToString(byte[] mi, String charset) {
        try {
            return new String(decoder.decode(mi),charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}