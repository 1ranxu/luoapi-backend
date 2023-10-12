package com.luoying.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {

    /**
     * 调用者Id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceInfoId;

    /**
     * 已调用次数
     */
    private Long invokedNum;

    /**
     * 剩余调用次数
     */
    private Long leftNum;

    /**
     * 用户状态 0-限制 1-正常
     */
    private Integer status;
}