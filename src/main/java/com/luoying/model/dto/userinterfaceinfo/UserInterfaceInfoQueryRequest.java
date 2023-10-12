package com.luoying.model.dto.userinterfaceinfo;

import com.luoying.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author 落樱的悔恨
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInterfaceInfoQueryRequest extends PageRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

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