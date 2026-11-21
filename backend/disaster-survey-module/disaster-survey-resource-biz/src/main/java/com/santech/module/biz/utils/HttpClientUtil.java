package com.santech.module.biz.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.santech.framework.global.Constants;
import com.santech.framework.utils.tool.StreamUtil;

public class HttpClientUtil {

    /**
     * 获取请求的返回结果
     * @param request
     * @return
     */
    public static String resultData(HttpUriRequest request) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpResponse response =httpClient.execute(request);

        InputStream is = response.getEntity().getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(is,Constants.UTF8));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }

        return buffer.toString();
    }

    /**
     * 获取请求的返回结果(byte[])
     * @param request
     * @return
     */
    public static byte[] resultDataByte(HttpUriRequest request) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpResponse response =httpClient.execute(request);
        InputStream is = response.getEntity().getContent();

        return StreamUtil.toByteArray(is);
    }



}
