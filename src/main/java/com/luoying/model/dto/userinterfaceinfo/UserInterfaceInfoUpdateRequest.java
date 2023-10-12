package com.luoying.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

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