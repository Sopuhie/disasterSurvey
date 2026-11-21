package com.santech.core.framework.entity.enums.biz;

/**
 * 评估子项枚举（按灾种分组）
 */
public enum AssessmentSubTypeEnum {

	/** 风暴潮：受灾信息标准化整合 */
	STORM_INFO_INTEGRATION("STORM_INFO_INTEGRATION", "风暴潮信息整合", DisasterTypeEnum.STORM_SURGE),
	/** 风暴潮：经济损失评估 */
	STORM_ECONOMIC_LOSS("STORM_ECONOMIC_LOSS", "风暴潮经济损失", DisasterTypeEnum.STORM_SURGE),
	/** 风暴潮：综合评估 */
	STORM_COMPREHENSIVE("STORM_COMPREHENSIVE", "风暴潮综合评估", DisasterTypeEnum.STORM_SURGE),

	/** 海风：SAR破坏性分析 */
	WIND_DAMAGE_ANALYSIS("WIND_DAMAGE_ANALYSIS", "海风破坏性分析", DisasterTypeEnum.SEA_WIND),
	/** 海风：行为分析 */
	WIND_BEHAVIOR_ANALYSIS("WIND_BEHAVIOR_ANALYSIS", "海风行为分析", DisasterTypeEnum.SEA_WIND),
	/** 海风：综合评估 */
	WIND_COMPREHENSIVE("WIND_COMPREHENSIVE", "海风综合评估", DisasterTypeEnum.SEA_WIND),

	/** 海浪：SAR破坏性分析 */
	WAVE_DAMAGE_ANALYSIS("WAVE_DAMAGE_ANALYSIS", "海浪破坏性分析", DisasterTypeEnum.SEA_WAVE),
	/** 海浪：行为分析 */
	WAVE_BEHAVIOR_ANALYSIS("WAVE_BEHAVIOR_ANALYSIS", "海浪行为分析", DisasterTypeEnum.SEA_WAVE),
	/** 海浪：综合评估 */
	WAVE_COMPREHENSIVE("WAVE_COMPREHENSIVE", "海浪综合评估", DisasterTypeEnum.SEA_WAVE);

	private final String code;
	private final String label;
	private final DisasterTypeEnum disasterType;

	AssessmentSubTypeEnum(String code, String label, DisasterTypeEnum disasterType) {
		this.code = code;
		this.label = label;
		this.disasterType = disasterType;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public DisasterTypeEnum getDisasterType() {
		return disasterType;
	}

	public static AssessmentSubTypeEnum fromCode(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		for (AssessmentSubTypeEnum e : values()) {
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
