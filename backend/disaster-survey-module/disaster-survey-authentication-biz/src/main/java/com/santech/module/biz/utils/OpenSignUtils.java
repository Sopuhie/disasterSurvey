package com.santech.module.biz.utils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
/**
 * 反馈系统嵌入签名工具（与反馈平台 hsfj-fkxt 的 OpenSignUtils 算法一致）
 * <p>
 * 签名规则：HMAC-SHA256(sysCode + userId + timestamp, 密钥)，hex 小写编码；
 * timestamp 有效期 5 分钟（防重放）
 *
 * @author hsfj
 * @version 2026-09-10
 */
public class OpenSignUtils {
	private OpenSignUtils() {
    }

    /**
     * 生成签名
     *
     * @param secret    分配密钥
     * @param sysCode   业务系统标识
     * @param userId    用户ID
     * @param timestamp 签名时间戳（毫秒）
     * @return hex 小写签名
     */
    public static String sign(String secret, String sysCode, String userId, long timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal((sysCode + userId + timestamp).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256签名计算失败", e);
        }
    }

}
