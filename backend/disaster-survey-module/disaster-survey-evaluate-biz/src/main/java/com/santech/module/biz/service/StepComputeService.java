package com.santech.module.biz.service;

import java.util.Map;

import com.santech.framework.global.ExecuteResult;
import com.santech.module.biz.entity.dto.StepComputeRequestDto;

/**
 * 步骤 UI 服务端计算：识别 / 叠置 / 经济损失
 */
public interface StepComputeService {

	ExecuteResult<Map<String, Object>> computeIdentify(StepComputeRequestDto dto);

	ExecuteResult<Map<String, Object>> computeOverlay(StepComputeRequestDto dto);

	ExecuteResult<Map<String, Object>> computeLoss(StepComputeRequestDto dto);
}
