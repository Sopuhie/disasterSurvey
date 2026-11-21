package com.santech.module.biz.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.santech.core.framework.entity.enums.LogSystem;
import com.santech.core.framework.entity.model.QxUserModel;
import com.santech.framework.global.Constants;
import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.service.UserService;
import com.santech.module.biz.utils.OpenSignUtils;

import io.swagger.annotations.Api;
/**
 * 反馈窗口签名Controller（本系统作为嵌入方，向 iframe 反馈窗口提供身份签名）
 * <p>
 * 前端登录后调用本接口初始化 Feedback.init，并在每次打开反馈窗口时刷新签名；
 * 提交/查询等接口由反馈平台（hsfj-fkxt）开放接口提供，密钥配置 feedback.open.secret 与平台一致。
 *
 * @author hsfj
 * @version 2026-09-10
 */
@RestController
@RequestMapping("/web-disaster-authentication/web/disaster/authentication/feedback/open")
@Api(tags = "Auth-反馈平台相关接口")
public class FeedbackSignController {
	private static final Logger logger = LoggerFactory.getLogger(FeedbackSignController.class);
	@Resource
    private UserService userService;
	/**
     * 验签密钥（与反馈平台一致）
     */
    @Value("${feedback.open.secret:}")
    private String secret;

    /**
     * 业务系统标识（反馈平台字典 tb_b_fk_gd_ywxt 中登记）
     */
    @Value("${feedback.open.sys-code:{{SYS_CODE}}}")
    private String sysCode;

    /**
     * 业务系统显示名称
     */
    @Value("${feedback.open.sys-name:{{SYS_NAME}}}")
    private String sysName;

    /**
     * 反馈平台后端开放接口地址（含协议和端口，如 https://fk.xxx.com，不带尾斜杠）。
     * iframe 内调用的开放接口由它派生，平台迁移后端域名时宿主只改这一行配置。
     */
    @Value("${feedback.open.server-url:{{SERVER_URL}}}")
    private String serverUrl;

    /**
     * 反馈平台前端部署上的嵌入件目录地址（含协议端口和尾斜杠）。
     * 挂件js与iframe页面所在目录，与 server-url 解耦——与 aigent 接入同构，前端独立部署时只改这行配置。
     */
    @Value("${feedback.open.front-url:{{FRONT_URL}}}")
    private String frontUrl;

    /**
     * 获取反馈窗口初始化信息与身份签名
     *
     * @return sysCode/sysName/frontUrl/baseUrl/apiBase/user/sign/timestamp
     */
    @LogSystem(target = "获取反馈窗口身份签名", type = Constants.LOG_TYPE_FEEDBACK)
    @GetMapping("sign")
    public ExecuteResult<Map<String, Object>> sign(HttpServletRequest request) {
    	String userId =(String) request.getAttribute(Constants.HEADER_USERID); 
    	try {
        	ExecuteResult<QxUserModel> userResult = userService.queryUserInfoById(userId);
        	QxUserModel user = userResult.getData();
        	if("F".equalsIgnoreCase(userResult.getResultCode())||user==null||user.getUserId()==null) {
        		return ExecuteResult.error("未获取到用户信息", null);
        	}
            long timestamp = System.currentTimeMillis();
            String sign = OpenSignUtils.sign(secret, sysCode, userId, timestamp);
            Map<String, Object> result = new HashMap<>();
            result.put("sysCode", sysCode);
            result.put("sysName", sysName);
            // frontUrl/baseUrl 由 front-url（前端部署嵌入件目录）派生，apiBase 为平台后端开放接口地址（与 aigent 接入同构）
            result.put("frontUrl", frontUrl + "feedback-widget.js");
            result.put("baseUrl", frontUrl);
            result.put("apiBase", serverUrl);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("name", user.getUsername());
            userInfo.put("phone", user.getTel());
            result.put("user", userInfo);
            result.put("sign", sign);
            result.put("timestamp", timestamp);
            return ExecuteResult.success("获取反馈窗口身份签名成功", result);
		} catch (Exception e) {
			logger.error("获取反馈窗口身份签名异常,用户id【{}】", userId);
			return ExecuteResult.error("获取反馈窗口身份签名异常", null);
		}
    	
        
    }

}
