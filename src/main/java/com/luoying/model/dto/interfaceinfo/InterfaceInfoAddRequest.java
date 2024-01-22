package com.luoying.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 接口创建请求
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 扣减积分数
     */
    private Long reduceScore;

    /**
     * 请求示例
     */
    private String requestExample;

    /**
     * 返回格式（JSON等）
     */
    private String returnFormat;

    /**
     * 接口请求参数
     */
    private List<RequestParamsField> requestParams;

    /**
     * 接口响应参数
     */
    private List<ResponseParamsField> responseParams;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}