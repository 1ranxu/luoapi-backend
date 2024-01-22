package com.luoying.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 接口调用请求
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {
    /**
     * 接口id
     */
    private Long id;

    /**
     * 请求参数
     */
    private List<Field> requestParams;

    @Data
    public static class Field {
        private String fieldName;
        private String value;
    }

    private static final long serialVersionUID = 1L;
}