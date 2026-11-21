/*
Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/
本软件受适用的国家软件著作权法（包括国际条约）和开源协议 双重保护许可。

开源协议中文释意如下：
1.JeeLowCode开源版本无任何限制，在遵循本开源协议（Apache2.0）条款下，【允许商用】使用，不会造成侵权行为。
2.允许基于本平台软件开展业务系统开发。
3.在任何情况下，您不得使用本软件开发可能被认为与【本软件竞争】的软件。

最终解释权归：http://www.jeelowcode.com
*/
package com.santech.framework.constants;
import com.santech.framework.code.*;

/**
 * 框架 错误码枚举类
 * <p>
 * 低代码 系统，使用 6-001-000-000 段
 */
public interface FrameErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(0, "成功");

    // ========== 框架相关  6-001-001-000 ============
    ErrorCode FRAME_PARAM_ERROR = new ErrorCode(1_600_001_000, "系统开小差了");
    ErrorCode FRAME_NOT_ROLE_ERROR = new ErrorCode(1_600_011_000, "无权限访问");

    ErrorCode FRAM_SELF_ERROR = new ErrorCode(1_600_001_001, "自定义错误");
    ErrorCode FRAME_ISNOT_ADMINISTOR_ERROR = new ErrorCode(1_600_001_002, "该功能只允许超级管理员操作");


    ErrorCode FRAME_OP_ERROR = new ErrorCode(1_600_001_003, "操作失败");
    ErrorCode FRAME_ENHANCE_ERROR = new ErrorCode(1_600_001_004, "增强已锁");
    ErrorCode FRAME_LOGIN_VIEW_ERROR = new ErrorCode(1_600_001_005,"用户未登录");

    ErrorCode FRAME_TABLE_NAME_ILLEGAL= new ErrorCode(1_600_001_006,"表名称不符合");
    ErrorCode FRAME_DESFORM_CORE_ISEXIT_ERROR = new ErrorCode(1_600_001_07,"编码已存在");
    ErrorCode FRAME_PARAM_NULL_ERROR = new ErrorCode(1_600_001_08,"参数不允许为空");
    ErrorCode FRAME_MAIN_TABLE_NOT = new ErrorCode(1_600_001_09,"绑定的主表不存在");
    ErrorCode FRAME_IMPORT_EXCEL = new ErrorCode(1_600_001_10,"请使用正确的Excel表导入数据");
    ErrorCode FRAME_DESFORM_IS_OPEN = new ErrorCode(1_600_001_11,"该表单需要登录才可访问");

    ErrorCode FRAME_IMPORT_EXCEL_NOT_DATA = new ErrorCode(1_600_001_12,"数据不存在");
    ErrorCode FRAME_IMPORT_EXCEL_STATE_ERROR = new ErrorCode(1_600_001_13,"表状态不符合");
    ErrorCode FRAME_CORE_EXIT = new ErrorCode(1_600_001_14,"编号已存在");
}
