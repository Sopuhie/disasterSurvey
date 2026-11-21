package com.santech.framework.global;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import santech.common.dbcomponent.entity.dbcomponents.PageInfo;

/**
 * 执行结果类。<br/>
 * 服务层对外传输的主对象。
 * 
 * @param <T>
 */
@Data
@ApiModel(description= "接口响应数据")
public class ExecuteResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "目标数据")
    private T data;//返回的数据
    
    @ApiModelProperty(value = "响应状态码，S成功，F失败")
    private String resultCode;//相应码，S成功，F失败
    
    @ApiModelProperty(value = "响应描述信息")
    private String resultMsg;//相应信息
    
    @ApiModelProperty(value = "分页信息")
    private PageInfo pageInfo;//分页信息
    
    public ExecuteResult(String resultCode, String resultMsg){
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }
    public ExecuteResult(String resultCode, String resultMsg, T data){
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
    }
    public ExecuteResult(String resultCode, String resultMsg, T data,PageInfo pageInfo){
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
        this.pageInfo = pageInfo;
    }
    public ExecuteResult() {
        // TODO Auto-generated constructor stub
    }

    public static ExecuteResult<String> success(){
        return new ExecuteResult<String>("S","成功");
    }
    public static ExecuteResult<String> success(String msg){
        return new ExecuteResult<String>("S",msg);
    }
    public static <T> ExecuteResult<T> success(String msg,T data){
        return new ExecuteResult<T>("S",msg,data);
    }
    public static <T> ExecuteResult<T> success(String msg,T data,PageInfo pageInfo){
        return new ExecuteResult<T>("S",msg,data,pageInfo);
    }

    public static <T> ExecuteResult<T> error(String code,String msg,T data){
        return new ExecuteResult<T>(code,msg,data);
    }
    public static <T> ExecuteResult<T> error(String msg,T data){
        return new ExecuteResult<T>("F",msg,data);
    }
}
