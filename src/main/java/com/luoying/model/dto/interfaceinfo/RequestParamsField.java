package com.luoying.model.dto.interfaceinfo;

import lombok.Data;

/**
 * 请求参数字段
 */
@Data
public class RequestParamsField {
    private String fieldName;
    private String type;
    private String desc;
    private String required;
}