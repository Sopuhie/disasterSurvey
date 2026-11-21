package com.santech.core.framework.entity.enums.biz;

/**
 * 异步计算任务状态枚举
 */
public enum ComputeTaskStatusEnum {

	CREATED("CREATED", "已创建"),
	RUNNING("RUNNING", "计算中"),
	SUCCEEDED("SUCCEEDED", "已完成"),
	FAILED("FAILED", "失败");

	private final String code;
	private final String label;

	ComputeTaskStatusEnum(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static ComputeTaskStatusEnum fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (ComputeTaskStatusEnum e : values()) {
			if (e.code.equalsIgnoreCase(code)) {
				return e;
			}
		}
		return null;
	}

	public static boolean isValid(String code) {
		return fromCode(code) != null;
	}
}
