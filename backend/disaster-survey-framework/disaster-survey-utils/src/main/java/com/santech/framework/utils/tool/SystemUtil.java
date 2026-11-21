package com.santech.framework.utils.tool;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * 系统工具类
 */
@Slf4j
public class SystemUtil {

    /**
     * 执行命令
     * @param cmd
     * @return
     */
    public static String executeCmd(String cmd) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(cmd);
            InputStream in = process.getInputStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[8192];
            for (int n; (n = in.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
            in.close();
            process.destroy();
            return out.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
