package com.santech.core.framework.entity.enums.biz;

/**
 * 承灾体类型枚举（前后端统一编码）
 */
public enum ExposureTypeEnum {

	SEAWALL("SEAWALL", "海堤/海岸防护工程", "Dike", "seawall"),
	FISHING_PORT("FISHING_PORT", "渔港", "Fishery", "fishing"),
	MARICULTURE("MARICULTURE", "海水养殖区", "CultureArea", "mariculture"),
	TOURIST_AREA("TOURIST_AREA", "滨海旅游区", "TouristArea", "tourist");

	private final String code;
	private final String label;
	/** 后端实体类型：Dike / Fishery / CultureArea / TouristArea */
	private final String entityType;
	/** 历史前端别名：seawall / fishing / mariculture / tourist */
	private final String legacyAlias;

	ExposureTypeEnum(String code, String label, String entityType, String legacyAlias) {
		this.code = code;
		this.label = label;
		this.entityType = entityType;
		this.legacyAlias = legacyAlias;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getLegacyAlias() {
		return legacyAlias;
	}

	public static ExposureTypeEnum fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (ExposureTypeEnum e : values()) {
			if (e.code.equalsIgnoreCase(code)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 兼容标准编码、实体类型、历史前端别名。
	 */
	public static ExposureTypeEnum fromAny(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return null;
		}
		String v = raw.trim();
		ExposureTypeEnum byCode = fromCode(v);
		if (byCode != null) {
			return byCode;
		}
		for (ExposureTypeEnum e : values()) {
			if (e.entityType.equalsIgnoreCase(v) || e.legacyAlias.equalsIgnoreCase(v)) {
				return e;
			}
		}
		// 额外历史别名
		if ("dike".equalsIgnoreCase(v) || "seawalls".equalsIgnoreCase(v)) {
			return SEAWALL;
		}
		if ("fishery".equalsIgnoreCase(v) || "fishing_port".equalsIgnoreCase(v) || "fishingport".equalsIgnoreCase(v)) {
			return FISHING_PORT;
		}
		if ("culturearea".equalsIgnoreCase(v) || "culture_area".equalsIgnoreCase(v)) {
			return MARICULTURE;
		}
		if ("touristarea".equalsIgnoreCase(v) || "tourist_area".equalsIgnoreCase(v)) {
			return TOURIST_AREA;
		}
		return null;
	}

	public static boolean isValid(String code) {
		return fromAny(code) != null;
	}
}
