package com.santech.core.framework.entity.enums.biz;

/**
 * 调查基线状态枚举
 */
public enum BaselineStatusEnum {

	/** 草稿/调查中（可修改、重算） */
	DRAFT("DRAFT", "草稿"),
	/** 已核查确认（只读锁定，可作为下游灾害评估的正式输入） */
	CONFIRMED("CONFIRMED", "已确认"),
	/** 已归档 */
	ARCHIVED("ARCHIVED", "已归档");

	private final String code;
	private final String label;

	BaselineStatusEnum(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static BaselineStatusEnum fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (BaselineStatusEnum e : values()) {
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
