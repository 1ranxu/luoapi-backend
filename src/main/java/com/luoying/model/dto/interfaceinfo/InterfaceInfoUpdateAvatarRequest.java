package com.luoying.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口头像url更新请求
 */
@Data
public class InterfaceInfoUpdateAvatarRequest implements Serializable {
    /**
     * 接口id
     */
    private long id;
    /**
     * 接口头像
     */
    private String avatarUrl;

    private static final long serialVersionUID = 1L;
}
