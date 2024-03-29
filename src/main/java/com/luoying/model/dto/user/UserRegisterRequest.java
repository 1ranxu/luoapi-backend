package com.luoying.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通账号注册请求
 *
 * @author 落樱的悔恨
 */
@Data
public class UserRegisterRequest implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 二次密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 1L;
}
