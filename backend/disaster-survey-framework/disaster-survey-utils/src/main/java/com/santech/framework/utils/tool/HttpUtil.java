package com.santech.framework.utils.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

    /**
     * 获取请求的返回结果
     * @param url
     * @return
     */
    public static String resultData(String url){
        String result = "";
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建Get请求
        HttpGet httpGet = new HttpGet(url);
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
//            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
//                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                result = EntityUtils.toString(responseEntity);
//                System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    

    /**
     * 获取请求的返回结果
     * @param request
     * @return
     */
    public static String resultData(HttpUriRequest request) throws Exception{
    	CloseableHttpClient httpClient = null;
		if (request.getURI().getPath().contains("https://")) {
			// 加载自定义的keystore
			SSLConnectionSocketFactory sslConnectionSocketFactory = null;
			File keystore = new File("/home/openssl/bserver/bserver.keystore");
			if (keystore.exists()) {
				SSLContext sslContext = SSLContexts.custom()
						.loadTrustMaterial(new File("/home/openssl/bserver/bserver.keystore"), "changeit".toCharArray())
						.build();
				// 默认的域名校验类为DefaultHostnameVerifier，比对服务器证书的AlternativeName和CN两个属性。
				// 如果服务器证书这两者不合法而我们又必须让其校验通过，则可以自己实现HostnameVerifier。
				sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
					public boolean verify(String s, SSLSession sslSession) {
						// 我们可以重写域名校验逻辑
						return true;
					}
				});
			}
			// 一个httpClient对象对于https仅会选用一个SSLConnectionSocketFactory
			// 至少在4.5.3和4.5.4中，如果给HttpClient对象设置ConnectionManager，我们必须在PoolingHttpClientConnectionManager的构造方法中传入Registry，
			// 并将https对应的工厂设置为我们自己的SSLConnectionSocketFactory对象，因为在DefaultHttpClientConnectionOperator.connect()中，逻辑是从这里找SSLConnectionSocketFactory的。
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
					RegistryBuilder.<ConnectionSocketFactory>create()
							.register("http", PlainConnectionSocketFactory.getSocketFactory())
							.register("https", sslConnectionSocketFactory).build());
			connectionManager.setMaxTotal(20);
			connectionManager.setDefaultMaxPerRoute(20);
			httpClient = HttpClients.custom()
					// 不在connectionManager中注册，仅在这里设置SSLConnectionSocketFactory是无效的，详见build()内部逻辑，在connectionManager不为null时，不会使用里的SSLConnectionSocketFactory
					.setSSLSocketFactory(sslConnectionSocketFactory).setConnectionManager(connectionManager).build();
		} else {
			httpClient = HttpClients.createDefault();
		}
		HttpResponse response =httpClient.execute(request);
        InputStream is = response.getEntity().getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
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


    /**
     * get请求
     * @param serviceUrl 请求url
     * @param parameters 请求参数
     * @return
     * @throws Exception 
     */
    public static String httpGet(String serviceUrl, String parameters) throws Exception {
        String result = "";
        //链接可能采用restful，需做判断
        if(!StringUtils.isEmpty(parameters)){
            if(serviceUrl.indexOf("{")>-1){
                String key = serviceUrl.substring(serviceUrl.indexOf("{")+1,serviceUrl.indexOf("}"));
                Map<String, String> map = JSONObject.parseObject(parameters, Map.class);
                serviceUrl = serviceUrl.substring(0,serviceUrl.indexOf("{")) + map.get(key);
            }else {
                Map<String, String> map = JSONObject.parseObject(parameters, Map.class);
                //参数拼接  用&
                String par = "";
                for(String key : map.keySet()){
                    try {
                        Object obj = map.get(key);
                        if(obj instanceof Integer){
                            par += key+"="+ Integer.valueOf(obj.toString())+"&";
                        }else{
                            par += key+"="+ URLEncoder.encode(map.get(key),"UTF-8")+"&";
                        }
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                }
                if(par.length()>1){
                    par = par.substring(0,par.length()-1);
                }
                serviceUrl = serviceUrl+"?"+par;
            }
        }

        try {
            HttpGet request = new HttpGet(serviceUrl);
//            request.addHeader("token", Constants.INTERNAL_CALL_TOKEN_VALUE);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).build();
            request.setConfig(requestConfig);
            result = resultData(request);
        } catch (Exception e) {
            log.error("get请求异常:"+e.toString());
            throw new Exception("get请求异常");
        }
        return result;
    }

    /**
     * get请求
     * @param serviceUrl 请求url
     * @param parameters 请求参数
     * @return
     */
	public static String httpGetCloudServer(String serviceUrl, String parameters, String token) {
		String result = "";
		// 链接可能采用restful，需做判断
		if (!StringUtils.isEmpty(parameters)) {
			if (serviceUrl.indexOf("{") > -1) {
				String key = serviceUrl.substring(serviceUrl.indexOf("{") + 1, serviceUrl.indexOf("}"));
				Map<String, Object> map = JSONObject.parseObject(parameters, Map.class);
				serviceUrl = serviceUrl.substring(0, serviceUrl.indexOf("{")) + map.get(key);
			} else {
				Map<String, String> map = JSONObject.parseObject(parameters, Map.class);
				// 参数拼接 用&
				String par = "";
				for (String key : map.keySet()) {
					try {
						Object obj = map.get(key);
						if (obj instanceof Integer) {
							par += key + "=" + Integer.valueOf(obj.toString()) + "&";
						} else {
							par += key + "=" + URLEncoder.encode(map.get(key), "UTF-8") + "&";
						}
					} catch (Exception e) {
						log.error(e.toString());
					}
				}
				if (par.length() > 1) {
					par = par.substring(0, par.length() - 1);
				}
				serviceUrl = serviceUrl + "?" + par;
			}
		}
		try {
			HttpGet request = new HttpGet(serviceUrl);
			if (StringUtil.isNotEmpty(token)) {
				request.addHeader("Authorization", "Bearer " + token);
			}
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).build();
			request.setConfig(requestConfig);
			result = resultData(request);
		} catch (Exception e) {
			log.error(e.toString());
		}
		return result;
	}
    
    /**
     * post请求
     * @param serviceUrl 请求url
     * @param parameters 请求参数
     * @return
     * @throws Exception 
     */
    public static String httpPost(String serviceUrl, String parameters) throws Exception {
        String result = "";
        try {
            HttpPost request = new HttpPost(serviceUrl);
            if(StringUtils.isNotEmpty(parameters)) {
            	 request.setEntity(new StringEntity(parameters, ContentType.APPLICATION_JSON));
            }
//          request.addHeader("token", Constants.INTERNAL_CALL_TOKEN_VALUE);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setSocketTimeout(150000).build();
            request.setConfig(requestConfig);
            result = resultData(request);
        } catch (Exception e) {
            log.error("post请求报错："+e.toString());
            throw new Exception("post请求异常");
        }
        return result;
    }

    /**
     * post请求
     * @param serviceUrl 请求url
     * @param parameters 请求参数
     * @return
     */
	public static String httpPostCloudServer(String serviceUrl, String parameters, String token) {
		String result = "";
		try {
			HttpPost request = new HttpPost(serviceUrl);
			if (StringUtils.isNotEmpty(parameters)) {
				request.setEntity(new StringEntity(parameters, ContentType.APPLICATION_JSON));
			}
			if (StringUtil.isNotEmpty(token)) {
				request.addHeader("Authorization", "Bearer " + token);
			}
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).build();
			request.setConfig(requestConfig);
			result = resultData(request);
		} catch (Exception e) {
			log.error(e.toString());
		}
		return result;
	}
    

    /**
     * 模拟请求前最好转码
     * @param params
     * @return
     */
    public static String newParams(String params){
        String newParams = "";
        if(StrUtil.isNotEmpty(params)){
            String[] paramArr = params.split("&");
            for(String ps : paramArr){
                String[] pArr = ps.split("=");
                if(pArr.length==1){
                    newParams += pArr[0] + "=&";
                }if(pArr.length==2){
                    try {
                        newParams += pArr[0] + "=" + URLEncoder.encode(pArr[1],"UTF-8") + "&";
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
            if(newParams.endsWith("&")){
                newParams = newParams.substring(0,newParams.length()-1);
            }
        }
        return  newParams;
    }


}
