package com.luoying.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通账号登录请求
 *
 * @author 落樱的悔恨
 */
@Data
public class UserLoginRequest implements Serializable {
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userPassword;

    private static final long serialVersionUID = 1L;
}
