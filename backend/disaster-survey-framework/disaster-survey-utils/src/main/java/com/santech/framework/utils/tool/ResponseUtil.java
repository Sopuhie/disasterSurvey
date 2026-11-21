package com.santech.framework.utils.tool;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Response工具类
 */
@Slf4j
public class ResponseUtil {

    public static void print(int responseStatus,HttpServletResponse response, Object message){
        try {
            response.setStatus(responseStatus);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(message));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
