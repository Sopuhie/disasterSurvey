package com.santech.module.biz.entity.vo;

import lombok.Data;
import lombok.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: MapServiceQueryPojo.java 
 * @Description：数据服务查询参数统一模板类 
 * @Version： V1.0
 * @Modify：   
 */
@Data
public class MapServiceQueryPojo {
    public MapServiceQueryPojo() {}
    public MapServiceQueryPojo(String url, String where, String geojson) {
        this.url = url;
        if(where != null && where.length() > 0) {
            this.where = where;
        }
        this.geometry = geojson;
    }
    @NonNull
    private String url;
    @NonNull
    private String where="0 < 1";

    private String outFields = "*";

    private boolean returnidsonly = true;
    
    private boolean returnGeometry = true;

    private String geometryType = "esriGeometryPolygon";

    private String geometry;
    
    /**EPSG的 数字*/
    private String inSR;
    
    /**EPSG的 数字*/
    private String outSR;

    private String spatialRel="esriSpatialRelContains";

    private String f = "json";

    @Override
    public String toString() {
        if(inSR != null) {
            inSR = inSR.toUpperCase().replace("EPSG:", "");
        }
        if(outSR != null) {
            outSR = outSR.toUpperCase().replace("EPSG:", "");
        }

        List<String> params = new ArrayList<>();
        params.add("f=json");
        params.add("where=" + where);
        params.add("outFields=" + outFields);
        params.add("returnidsonly=" + returnidsonly);
        params.add("returnGeometry=" + returnGeometry);
        params.add("geometryType=" + geometryType);
        params.add("inSR=" + inSR);
        params.add("outSR=" + outSR);
        return url + "?" + String.join("&", params);
    }

    public MultiValueMap<String, Object> toMap(){
        if(inSR != null) {
            inSR = inSR.toUpperCase().replace("EPSG:", "");
        }
        if(outSR != null) {
            outSR = outSR.toUpperCase().replace("EPSG:", "");
        }

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("outFields",outFields);
        map.add("returnidsonly",String.valueOf(returnidsonly));
        map.add("returnGeometry",String.valueOf(returnGeometry));
        map.add("geometryType",geometryType);
        map.add("geometry",geometry);
        map.add("inSR", inSR);
        map.add("outSR", outSR);
        map.add("spatialRel",spatialRel);
        map.add("f",f);
        map.add("where",where);
        return map;
    }
}
