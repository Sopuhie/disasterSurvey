package com.santech.module.biz.survey.service;

import java.util.Map;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;

/**
 * 灾情调查叠置计算：受灾识别、人口/乡镇/承灾体叠置。
 * 从 StepComputeService 拆出的调查计算能力。
 */
public interface SurveyOverlayComputeService {

	/** Step2/识别：受灾范围县域台账推算 */
	ExecuteResult<Map<String, Object>> computeIdentify(StepComputeRequestDto dto);

	/** Step3：单目标叠置（人口/乡镇/单类承灾体） */
	ExecuteResult<Map<String, Object>> computeOverlay(StepComputeRequestDto dto);

	/**
	 * Step3：一次性执行人口 + 乡镇 + 四类承灾体叠置，返回结构化结果。
	 */
	ExecuteResult<Map<String, Object>> computeFullOverlay(StepComputeRequestDto dto);
}
