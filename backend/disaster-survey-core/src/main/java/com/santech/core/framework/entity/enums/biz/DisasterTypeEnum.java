package com.santech.core.framework.entity.enums.biz;

/**
 * 灾害类型枚举（前后端统一编码）
 */
public enum DisasterTypeEnum {

	STORM_SURGE("STORM_SURGE", "风暴潮"),
	SEA_WIND("SEA_WIND", "海风"),
	SEA_WAVE("SEA_WAVE", "海浪");

	private final String code;
	private final String label;

	DisasterTypeEnum(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static DisasterTypeEnum fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (DisasterTypeEnum e : values()) {
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
