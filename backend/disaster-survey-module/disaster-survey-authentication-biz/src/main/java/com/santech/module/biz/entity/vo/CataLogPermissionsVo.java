package  com.santech.module.biz.entity.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description= "角色资源目录权限信息")
public class CataLogPermissionsVo {

	@ApiModelProperty(value = "资源目录ID")
    private String id;

	@ApiModelProperty(value = "上级id")
    private String parentId;

	@ApiModelProperty(value = "资源目录名称")
    private String name;
	
	@ApiModelProperty(value = "专题别名")
    private String alias;

	@ApiModelProperty(value = "节点类型，folder目录， theme专题，system系统分类目录")
    private String type;

	@ApiModelProperty(value = "专题ID")
    private String themeId;
	
	@ApiModelProperty(value = "排序")
    private Integer sortIndex;

	@ApiModelProperty(value = "子目录")
	private List<CataLogPermissionsVo> childrens;

}
