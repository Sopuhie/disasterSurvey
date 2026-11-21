package com.santech.module.biz.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.santech.core.framework.dao.ExposureDao;
import com.santech.core.framework.entity.model.QxRegionSimpleModel;
import com.santech.core.framework.entity.model.exposure.BaseCultureAreaModel;
import com.santech.core.framework.entity.model.exposure.BaseDikeModel;
import com.santech.core.framework.entity.model.exposure.BaseFisheryModel;
import com.santech.core.framework.entity.model.exposure.BaseTouristAreaModel;
import com.santech.core.framework.service.RegionService;
import com.santech.framework.global.ExecuteResult;
import com.santech.framework.utils.tool.CommonUtils;
import com.santech.module.biz.entity.vo.ExposureTreeVo;
import com.santech.module.biz.service.ExposureDataService;

/**
 * @Title: ExposureDataServiceImpl.java 
 * @Description：承灾体数据查询统计业务层 ，实现ExposureDataService接口
 * @Version： V1.0
 * @Modify：   
 */
@Service
public class ExposureDataServiceImpl implements ExposureDataService {
    private static final  Logger logger = LoggerFactory.getLogger(ExposureDataServiceImpl.class);

    @Resource
    private ExposureDao exposureDao;
    
    @Resource
    private RegionService regionService;
    
    @Override
   	public ExecuteResult<List<ExposureTreeVo>> queryExposureTreeInfos(String exposureType) {
           try {
        	 //请求参数
               Map<String,Object> params = new HashMap<String,Object>();
               //拼接查询sql
               Map<String,Object> dbParams = CommonUtils.getWhereSearchSql(params,"SORTINDEX asc","");
               //获取目标行政区树信息，返回省级、市级、县级三级行政区
               ExecuteResult<List<QxRegionSimpleModel>> regionTreeResult = regionService.showRegionTree("省级,市级,县级");
               if("F".equalsIgnoreCase(regionTreeResult.getResultCode())) {
            	   logger.error("行政区信息获取失败，详情【{}】",regionTreeResult.getResultMsg());
            	   return ExecuteResult.error("查询失败",null);
               }
               List<ExposureTreeVo> exposureTree = null;
               switch (exposureType) {
	   			case "Dike":
	   				//根据拼接sql,调用dao层获取数据
	                List<BaseDikeModel> dikeDatas = exposureDao.queryDangerObjects(BaseDikeModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
	                //生成堤防树
	                exposureTree = getDikeTree(regionTreeResult.getData(),dikeDatas,new ArrayList<ExposureTreeVo>());
	   				break;
	   			case "Fishery":
	   				//根据拼接sql,调用dao层获取数据
	                List<BaseFisheryModel> fisheryDatas = exposureDao.queryDangerObjects(BaseFisheryModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
	                //生成渔港树
	                exposureTree = getFisheryTree(regionTreeResult.getData(),fisheryDatas,new ArrayList<ExposureTreeVo>());
	   				break;
	   			case "CultureArea":
	   				//根据拼接sql,调用dao层获取数据
	   	            List<BaseCultureAreaModel> cultureAreaDatas = exposureDao.queryDangerObjects(BaseCultureAreaModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
	   	            //生成海水养殖区树
	   	            exposureTree = getCultureAreaTree(regionTreeResult.getData(),cultureAreaDatas,new ArrayList<ExposureTreeVo>());
	   				break;
	   			case "TouristArea":
	   				//根据拼接sql,调用dao层获取数据
	   	            List<BaseTouristAreaModel> touristAreaDatas = exposureDao.queryDangerObjects(BaseTouristAreaModel.class, (String) dbParams.get("sql"),(Map<String, Object>) dbParams.get("paramsMap"));
	   	            //生成滨海旅游区树
	   	            exposureTree = getTouristAreaTree(regionTreeResult.getData(),touristAreaDatas,new ArrayList<ExposureTreeVo>());
	   				break;
	   			default:
	   				return ExecuteResult.error("查询失败-不能识别的承灾体类型",null);
	   			}
               return ExecuteResult.success("查询成功", exposureTree);
           } catch (Exception e) {
               logger.error("查询承灾体树结构信息异常，异常原因【{}】,异常详情：", e.getMessage(),e);
               return ExecuteResult.error("查询失败",null);
           }
   	}
    
    //根据目标行政区信息组装堤防目录树
    private List<ExposureTreeVo> getDikeTree(List<QxRegionSimpleModel> regionTree,List<BaseDikeModel> dikes,List<ExposureTreeVo> trees) {
		if(trees==null) {
			trees = new ArrayList<ExposureTreeVo>();
		}
		if(regionTree!=null&&regionTree.size()>0) {
			for(QxRegionSimpleModel region:regionTree) {
				String code = region.getCode();
				ExposureTreeVo tree = new ExposureTreeVo();
   			  	tree.setNodeId(code);
   			  	tree.setNodeName(region.getName());
   			  	tree.setNodeType("REGION");
   			  	tree.setNodeCode(code);
   			  	tree.setPid(region.getParentCode());
   			  	tree.setSortIndex(region.getSortIndex());
   			  	tree.setRemark(region.getRemark());
   			  	List<ExposureTreeVo> childrenTree = getDikeTree(region.getChildren(),dikes,new ArrayList<ExposureTreeVo>());
   			  	if(dikes!=null&&dikes.size()>0) {
	   			  	List<ExposureTreeVo> vos = null;
					for(BaseDikeModel dike:dikes) {
						if(code.equals(dike.getRegionCode())) {
							 if(vos==null) {
								vos = new ArrayList<ExposureTreeVo>();
							 }
							 ExposureTreeVo tree1 = new ExposureTreeVo();
			     			  tree1.setNodeId(dike.getId());
			     			  tree1.setNodeName(dike.getName());
			     			  tree1.setNodeType("EXPOSURE");
			     			  tree1.setNodeCode(dike.getCode());
			     			  tree1.setPid(code);
			     			  tree1.setSortIndex(dike.getSortIndex());
			     			  tree1.setRemark(dike.getRemark());
			     			  vos.add(tree1);
		     		   }
		     	    }
					if(vos!=null&&vos.size()>0) {
						//有对应堤防
						childrenTree.addAll(vos);
					}
   			  	}
				tree.setChildren(childrenTree);
   			  	trees.add(tree);
			}
        }
		return trees;
	}
    
    //根据目标行政区信息组装渔港目录树
    private List<ExposureTreeVo> getFisheryTree(List<QxRegionSimpleModel> regionTree,List<BaseFisheryModel> fisherys,List<ExposureTreeVo> trees) {
		if(trees==null) {
			trees = new ArrayList<ExposureTreeVo>();
		}
		if(regionTree!=null&&regionTree.size()>0) {
			for(QxRegionSimpleModel region:regionTree) {
				String code = region.getCode();
				ExposureTreeVo tree = new ExposureTreeVo();
   			  	tree.setNodeId(code);
   			  	tree.setNodeName(region.getName());
   			  	tree.setNodeType("REGION");
   			  	tree.setNodeCode(code);
   			  	tree.setPid(region.getParentCode());
   			  	tree.setSortIndex(region.getSortIndex());
   			  	tree.setRemark(region.getRemark());
   			  	List<ExposureTreeVo> childrenTree = getFisheryTree(region.getChildren(),fisherys,new ArrayList<ExposureTreeVo>());
   			  	if(fisherys!=null&&fisherys.size()>0) {
	   			  	List<ExposureTreeVo> vos = null;
					for(BaseFisheryModel fishery:fisherys) {
						if(code.equals(fishery.getRegionCode())) {
							 if(vos==null) {
								vos = new ArrayList<ExposureTreeVo>();
							 }
							 ExposureTreeVo tree1 = new ExposureTreeVo();
			     			  tree1.setNodeId(fishery.getId());
			     			  tree1.setNodeName(fishery.getName());
			     			  tree1.setNodeType("FISHERY");
			     			  tree1.setNodeCode(fishery.getCode());
			     			  tree1.setPid(code);
			     			  tree1.setSortIndex(fishery.getSortIndex());
			     			  tree1.setRemark(fishery.getRemark());
			     			  vos.add(tree1);
		     		   }
		     	    }
					if(vos!=null&&vos.size()>0) {
						//有对应渔港
						childrenTree.addAll(vos);
					}
   			  	}
				tree.setChildren(childrenTree);
   			  	trees.add(tree);
			}
        }
		return trees;
	}
    
  //根据目标行政区信息组装海水养殖区目录树
    private List<ExposureTreeVo> getCultureAreaTree(List<QxRegionSimpleModel> regionTree,List<BaseCultureAreaModel> cultureAreas,List<ExposureTreeVo> trees) {
		if(trees==null) {
			trees = new ArrayList<ExposureTreeVo>();
		}
		if(regionTree!=null&&regionTree.size()>0) {
			for(QxRegionSimpleModel region:regionTree) {
				String code = region.getCode();
				ExposureTreeVo tree = new ExposureTreeVo();
   			  	tree.setNodeId(code);
   			  	tree.setNodeName(region.getName());
   			  	tree.setNodeType("REGION");
   			  	tree.setNodeCode(code);
   			  	tree.setPid(region.getParentCode());
   			  	tree.setSortIndex(region.getSortIndex());
   			  	tree.setRemark(region.getRemark());
   			  	List<ExposureTreeVo> childrenTree = getCultureAreaTree(region.getChildren(),cultureAreas,new ArrayList<ExposureTreeVo>());
   			  	if(cultureAreas!=null&&cultureAreas.size()>0) {
	   			  	List<ExposureTreeVo> vos = null;
					for(BaseCultureAreaModel cultureArea:cultureAreas) {
						if(code.equals(cultureArea.getRegionCode())) {
							 if(vos==null) {
								vos = new ArrayList<ExposureTreeVo>();
							 }
							 ExposureTreeVo tree1 = new ExposureTreeVo();
			     			  tree1.setNodeId(cultureArea.getId());
			     			  tree1.setNodeName(cultureArea.getName());
			     			  tree1.setNodeType("CultureArea");
			     			  tree1.setNodeCode(cultureArea.getCode());
			     			  tree1.setPid(code);
			     			  tree1.setSortIndex(cultureArea.getSortIndex());
			     			  tree1.setRemark(cultureArea.getRemark());
			     			  vos.add(tree1);
		     		   }
		     	    }
					if(vos!=null&&vos.size()>0) {
						//有对应海水养殖区
						childrenTree.addAll(vos);
					}
   			  	}
				tree.setChildren(childrenTree);
   			  	trees.add(tree);
			}
        }
		return trees;
	} 
    
  //根据目标行政区信息组装滨海旅游区目录树
    private List<ExposureTreeVo> getTouristAreaTree(List<QxRegionSimpleModel> regionTree,List<BaseTouristAreaModel> touristAreas,List<ExposureTreeVo> trees) {
		if(trees==null) {
			trees = new ArrayList<ExposureTreeVo>();
		}
		if(regionTree!=null&&regionTree.size()>0) {
			for(QxRegionSimpleModel region:regionTree) {
				String code = region.getCode();
				ExposureTreeVo tree = new ExposureTreeVo();
   			  	tree.setNodeId(code);
   			  	tree.setNodeName(region.getName());
   			  	tree.setNodeType("REGION");
   			  	tree.setNodeCode(code);
   			  	tree.setPid(region.getParentCode());
   			  	tree.setSortIndex(region.getSortIndex());
   			  	tree.setRemark(region.getRemark());
   			  	List<ExposureTreeVo> childrenTree = getTouristAreaTree(region.getChildren(),touristAreas,new ArrayList<ExposureTreeVo>());
   			  	if(touristAreas!=null&&touristAreas.size()>0) {
	   			  	List<ExposureTreeVo> vos = null;
					for(BaseTouristAreaModel touristArea:touristAreas) {
						if(code.equals(touristArea.getRegionCode())) {
							 if(vos==null) {
								vos = new ArrayList<ExposureTreeVo>();
							 }
							 ExposureTreeVo tree1 = new ExposureTreeVo();
			     			  tree1.setNodeId(touristArea.getId());
			     			  tree1.setNodeName(touristArea.getName());
			     			  tree1.setNodeType("TouristArea");
			     			  tree1.setNodeCode(touristArea.getCode());
			     			  tree1.setPid(code);
			     			  tree1.setSortIndex(touristArea.getSortIndex());
			     			  tree1.setRemark(touristArea.getRemark());
			     			  vos.add(tree1);
		     		   }
		     	    }
					if(vos!=null&&vos.size()>0) {
						//有对应滨海旅游区
						childrenTree.addAll(vos);
					}
   			  	}
				tree.setChildren(childrenTree);
   			  	trees.add(tree);
			}
        }
		return trees;
	}

	@Override
	public ExecuteResult<List<Map<String, Object>>> queryExposureList(String exposureType, String keyword) {
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			Map<String, Object> dbParams = CommonUtils.getWhereSearchSql(params, "SORTINDEX asc", "");
			String sql = (String) dbParams.get("sql");
			@SuppressWarnings("unchecked")
			Map<String, Object> paramsMap = (Map<String, Object>) dbParams.get("paramsMap");
			List<Map<String, Object>> result = new ArrayList<>();
			String kw = keyword == null ? "" : keyword.trim();
			switch (exposureType) {
			case "Dike":
			case "seawall":
				for (BaseDikeModel item : exposureDao.queryDangerObjects(BaseDikeModel.class, sql, paramsMap)) {
					if (!matchKeyword(kw, item.getName(), item.getCode(), item.getRegionName())) {
						continue;
					}
					Map<String, Object> row = new HashMap<>();
					row.put("id", item.getId());
					row.put("name", item.getName());
					row.put("dangerName", item.getName());
					row.put("hazardCode", item.getCode());
					row.put("division", item.getRegionName());
					row.put("length", item.getLength());
					row.put("lengthKm", item.getLength());
					row.put("Dam_elevat", item.getHtd());
					row.put("Return_per", item.getGrade());
					row.put("HDXL", item.getType());
					row.put("time", item.getBuildTime());
					row.put("remark", item.getRemark());
					result.add(row);
				}
				break;
			case "Fishery":
			case "fishing":
				for (BaseFisheryModel item : exposureDao.queryDangerObjects(BaseFisheryModel.class, sql, paramsMap)) {
					if (!matchKeyword(kw, item.getName(), item.getCode(), item.getAddress())) {
						continue;
					}
					Map<String, Object> row = new HashMap<>();
					row.put("id", item.getId());
					row.put("name", item.getName());
					row.put("hazardCode", item.getCode());
					row.put("division", item.getAddress());
					row.put("type", item.getGrade());
					row.put("method", item.getBreakWater());
					row.put("capacity", item.getCapacity());
					row.put("lon", parseDouble(item.getLongitude()));
					row.put("lat", parseDouble(item.getLatitude()));
					row.put("remark", item.getRemark());
					result.add(row);
				}
				break;
			case "CultureArea":
			case "mariculture":
				for (BaseCultureAreaModel item : exposureDao.queryDangerObjects(BaseCultureAreaModel.class, sql, paramsMap)) {
					if (!matchKeyword(kw, item.getName(), item.getCode())) {
						continue;
					}
					Map<String, Object> row = new HashMap<>();
					row.put("id", item.getId());
					row.put("name", item.getName());
					row.put("hazardCode", item.getCode());
					row.put("method", item.getType());
					row.put("area", item.getArea());
					row.put("areaKm2", item.getArea() == null ? null : item.getArea().doubleValue() * 0.01);
					row.put("lon", parseDouble(item.getLongitude()));
					row.put("lat", parseDouble(item.getLatitude()));
					row.put("remark", item.getRemark());
					result.add(row);
				}
				break;
			case "TouristArea":
			case "tourist":
				for (BaseTouristAreaModel item : exposureDao.queryDangerObjects(BaseTouristAreaModel.class, sql, paramsMap)) {
					if (!matchKeyword(kw, item.getName(), item.getCode(), item.getAddress())) {
						continue;
					}
					Map<String, Object> row = new HashMap<>();
					row.put("id", item.getId());
					row.put("name", item.getName());
					row.put("hazardCode", item.getCode());
					row.put("division", item.getAddress());
					row.put("manager", item.getCompany());
					row.put("area", item.getSeaArea());
					row.put("areaKm2", item.getSeaArea());
					row.put("remark", item.getRemark());
					result.add(row);
				}
				break;
			default:
				return ExecuteResult.error("不能识别的承灾体类型", new ArrayList<>());
			}
			return ExecuteResult.success("查询成功", result);
		} catch (Exception e) {
			logger.error("查询承灾体列表异常", e);
			return ExecuteResult.error("查询失败", new ArrayList<>());
		}
	}

	private boolean matchKeyword(String kw, String... fields) {
		if (kw == null || kw.isEmpty()) {
			return true;
		}
		for (String f : fields) {
			if (f != null && f.contains(kw)) {
				return true;
			}
		}
		return false;
	}

	private Double parseDouble(String text) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (Exception e) {
			return null;
		}
	}

}
